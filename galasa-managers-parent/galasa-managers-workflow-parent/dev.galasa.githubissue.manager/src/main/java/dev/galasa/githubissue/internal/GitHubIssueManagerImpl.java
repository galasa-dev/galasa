/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.githubissue.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsername;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ManagerException;
import dev.galasa.Test;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.githubissue.GitHubIssue;
import dev.galasa.githubissue.GitHubIssueManagerException;
import dev.galasa.githubissue.Issue;
import dev.galasa.githubissue.internal.properties.GitHubCredentials;
import dev.galasa.githubissue.internal.properties.GitHubIssueInstanceRepository;
import dev.galasa.githubissue.internal.properties.GitHubIssueInstanceUrl;
import dev.galasa.githubissue.internal.properties.GitHubIssuePropertiesSingleton;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;

@Component(service = { IManager.class })
public class GitHubIssueManagerImpl extends AbstractManager {
	
	private static final Log logger = LogFactory.getLog(GitHubIssueManagerImpl.class);
	
	private IFramework framework;
	
	protected static final String NAMESPACE = "githubissue";
	
	private boolean required;
	
	private GalasaTest galasaTest;
	
	private GitHubIssue classGitHubIssue;
	
	private boolean methodAnnotated;
	
	private IHttpManagerSpi  httpManager;
	private IHttpClient      httpClient;
	
	private ICredentialsService credService;
	
	private HashMap<GalasaMethod, HashMap<String, Object>> results = new HashMap<GalasaMethod, HashMap<String, Object>>();
	
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, galasaTest);
		
		// Check to see if GitHubIssue annotation is present in the test class or on any of the test methods
		// If it is, activate this manager
		
		if (galasaTest.isJava()) {
			Class<?> testClass = galasaTest.getJavaTestClass();
			
			this.classGitHubIssue = testClass.getAnnotation(GitHubIssue.class); // Set globally as might need later
			if (this.classGitHubIssue == null) { 
				GitHubIssue methodGitHubIssue = null;
				for (Method testMethod : testClass.getMethods()) {
					methodGitHubIssue = testMethod.getAnnotation(GitHubIssue.class);
					if (methodGitHubIssue != null) {
						methodAnnotated = true;
						break;
					}
				}
				// Annotation not present on the class or any methods
				if (!methodAnnotated) {
					return;
				}
			} 
		} else {
			return; // Only support Java
		}
		
		this.galasaTest = galasaTest;
		
		this.framework = framework;
		
		try {
			this.credService = framework.getCredentialsService();
		} catch (CredentialsException e) {
			logger.error("Could not access credential store from framework.", e);
			throw new GitHubIssueManagerException("Could not access credential store from framework.", e);
		}
		
		try {
            GitHubIssuePropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new GitHubIssueManagerException("Unable to request framework services", e);
        }	
		
		youAreRequired(allManagers, activeManagers, galasaTest);
	
	}
	
	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
		super.youAreRequired(allManagers, activeManagers, galasaTest);
		
		if (activeManagers.contains(this)) {
			return;
		}
		
		this.required = true;
		activeManagers.add(this);	
		
		this.httpManager = addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
        if (this.httpManager == null) {
            throw new GitHubIssueManagerException("Unable to locate the Http Manager, required for the GitHub Issue Manager");
        }
	}
	
	@Override
	public Result endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull Result currentResult,
			Throwable currentException) throws GitHubIssueManagerException {
		
		Result newResult = null;
		
		if (isTestMethod(galasaMethod)) {
			// If this test method is annotated with GitHubIssue, override it's result if needed
			GitHubIssue gitHubIssue = galasaMethod.getJavaExecutionMethod().getAnnotation(GitHubIssue.class);
			if (gitHubIssue != null) {
				newResult = overrideMethodResult(gitHubIssue, galasaMethod, currentResult, currentException);
			}
			
		}
		
		// Record method result for later
		HashMap<String, Object> result = new HashMap<String, Object>();
		if (newResult != null) {
			result.put("result", newResult);
		} else {
			result.put("result", currentResult);
		}
		result.put("throwable", currentException);
		results.put(galasaMethod, result);					
		
		return newResult;
	}
	
	private Result overrideMethodResult(@NotNull GitHubIssue gitHubIssue, @NotNull GalasaMethod method, @NotNull Result currentResult, Throwable currentException) throws GitHubIssueManagerException {

		if (!currentResult.isFailed()) {
			return null;
		}
		
		if (!isDefectCheckEnabled()) {
			return null;
		}
		
		logger.trace("This method is annotated with a GitHubIssue so checking if failure was due to a known defect");
		
		if (Integer.parseInt(gitHubIssue.issue()) <= 0) {
			return null;
		}
		
		if (!checkRegexException(gitHubIssue.regex(), currentException)) {
			logger.trace("The failing exception does not match the GitHub issue, so the failure is not due to a known defect");
			return null;
		}
		
		Issue issue = getGitHubIssue(gitHubIssue);
		if (issue == null) {
			return null;
		}
		
		if (issue.isClosed()) {
			logger.trace("Issue '" + issue.getIssue() + "'  is closed so does not override the result of this method");
			return null;
		}
		
		logger.info("Overriding method result from 'Failed' to 'Failed With Defects' due to open GitHub issue " + gitHubIssue.issue() + ": " + issue.getTitle());
		return Result.custom("Failed With Defects", false, true, true, false, false, false, false, "failed with defects");
		
	}
	
	@Override
	public Result endOfTestClass(@NotNull Result currentResult, Throwable currentException) throws GitHubIssueManagerException {

		Result newResult = null;
		
		// If this test class or any of it's methods are annotated with GitHubIssue, override it's result if needed
		if (this.classGitHubIssue != null || methodAnnotated) {
			newResult = overrideClassResult(this.galasaTest, currentResult, currentException);
		}
		
		return newResult;
		
	}
	
	private Result overrideClassResult(@NotNull GalasaTest klass, @NotNull Result currentResult, Throwable currentException) throws GitHubIssueManagerException {
		
		if (!currentResult.isFailed()) {
			return null;
		}

		Issue classLevelIssue = null;
		if (this.classGitHubIssue != null) {
			classLevelIssue = getGitHubIssue(this.classGitHubIssue);
		} 
		
		boolean failedNonDefectMethod = false;
		boolean failedDefectMethod    = false;
		boolean passedMethod          = false;
		
		int failedNonDefectMethodCount = 0;
		int failedDefectMethodCount = 0;
		int passedMethodCount = 0;
	
		logger.trace("Iterating through the results of all of the test methods to determine the overall class result");
		for (Map.Entry<GalasaMethod, HashMap<String,Object>> method : results.entrySet()) {
			
			HashMap<String, Object> entry = method.getValue();
			Result result = (Result) entry.get("result");
			Throwable throwable = (Throwable) entry.get("throwable");
			
			switch(result.getName()) {
			case "Failed":
				
				if (this.classGitHubIssue != null && this.classGitHubIssue.regex() != null && this.classGitHubIssue.regex().length > 0) {
					if (checkRegexException(this.classGitHubIssue.regex(), throwable)) {
						failedNonDefectMethod = true;
						failedNonDefectMethodCount++;
					}
				} else {
					failedNonDefectMethod = true;
					failedNonDefectMethodCount++;
				}	
				break;
			
			case "Failed With Defects": 
				failedDefectMethod = true;
				failedDefectMethodCount++;
				break;
				
			case "Passed":
				passedMethod = true;
				passedMethodCount++;
				break;
				
			default:
				break;
			}
			
			
		}
		
		logger.trace("@GitHubIssue is present at the class level: " + (classLevelIssue != null));
		logger.trace("This class contains: " + passedMethodCount + " Passed methods, " + failedNonDefectMethodCount + " Failed methods & " + failedDefectMethodCount + " Failed With Defects methods");
		
		if (failedDefectMethod && passedMethod && !failedNonDefectMethod) {
			
			// If test stopped at the first failed method, but this was the last method anyway, treat as Passed With Defects
			int methodsExecuted = results.size();
			int testMethods = 0;
			for (Method method : klass.getJavaTestClass().getMethods()) {
				if (hasTestAnnotation(method)) {
					testMethods++;
				}
			}
			if (methodsExecuted < testMethods) {
				logger.info("Overriding class result from 'Failed' to 'Failed With Defects' due to test stopping at 'Failed With Defect' method, so cannot determine what the remaining methods would have been");
				return Result.custom("Failed With Defects", false, true, true, false, false, false, false, "failed with defects");
			}
			
			logger.info("Overriding class result from 'Failed' to 'Passed With Defects' due to all methods passing other than 'Failed With Defects' methods");
			return Result.custom("Passed With Defects", true, false, true, false, false, false, false, "passed with defects");
		}
		
		if (classLevelIssue != null && !classLevelIssue.isClosed()) {
			if (failedDefectMethod || failedNonDefectMethod) {
				logger.info("Overriding class result from 'Failed' to 'Failed With Defects' due to failing methods and class level open GitHub issue " + this.classGitHubIssue.issue() + ": " + classLevelIssue.getTitle());
				return Result.custom("Failed With Defects", false, true, true, false, false, false, false, "failed with defects");
			}
		}
		
		if (failedNonDefectMethod || !failedDefectMethod) {
			logger.info("Not overriding the class result due to 'Failed' methods and no class level GitHub issue");
			return null;
		}
	
		logger.info("Overriding class result from 'Failed' to 'Failed With Defects' due to all methods being 'Failed With Defects'");
		return Result.custom("Failed With Defects", false, true, true, false, false, false, false, "failed with defects");
	}
	
	private boolean checkRegexException(String[] regexs, Throwable exception) {
		
		if (regexs == null || regexs.length == 0) {
			return true;
		}
		
		String exceptionStackTrace = convertException(exception);
		
		for (String r : regexs) {
			Pattern pattern = Pattern.compile(r);
			Matcher matcher = pattern.matcher(exceptionStackTrace);
			
			if (matcher.find()) {
				return true;
			}
		}
		
		return false;
		
	}
	
	private String convertException(Throwable exception) {
		
		if (exception == null) {
			return "";
		}
		
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exception.printStackTrace(pw);
			return sw.toString();
		} catch(Exception e) { 
			logger.error("Unable to create stacktrace of exception",e);
		}
		
		return null;
		
	}
	
	private Issue getGitHubIssue(GitHubIssue gitHubIssue) throws GitHubIssueManagerException {
		
		String fullUrl = getUrl(gitHubIssue);
		
		if (this.httpClient == null) {
			
			this.httpClient = this.httpManager.newHttpClient();
			
			String uriString = fullUrl.substring(0, fullUrl.indexOf("/repos"));
			URI uri = null;
			try {
				uri = new URI(uriString);
			} catch (URISyntaxException e) {
				throw new GitHubIssueManagerException("Badly formed URI", e);
			}
			this.httpClient.setURI(uri);
			
			setCreds(gitHubIssue);
        }
		
		logger.trace("Looking for GitHub Issue at: " + fullUrl);
		
		String url = fullUrl.substring(fullUrl.indexOf("/repos"));
		
		try {
			
			HttpClientResponse<JsonObject> response = this.httpClient.getJson(url);
			
			if (response.getStatusCode() == 200) {
            	JsonObject json = response.getContent();
            	Issue issue = getIssuePojo(json, gitHubIssue.issue());
            	logger.info("Issue '" + issue.getIssue() + "' is " + issue.getState() + " - " + issue.getUrl());
            	return issue;
        	} else {
        		throw new GitHubIssueManagerException("Unable to read GitHub issue '" + gitHubIssue.issue() + "' from url " + fullUrl + " - " + response.getStatusLine());
        	}
		} catch (HttpClientException e) {
        	throw new GitHubIssueManagerException("Unable to read GitHub issue '" + gitHubIssue.issue() + "' from url " + fullUrl, e);
		}
        
	}
	
	private void setCreds(GitHubIssue gitHubIssue) throws GitHubIssueManagerException {
		ICredentials creds = null;
		try {
			String credKey = GitHubCredentials.get(gitHubIssue);
			creds = credService.getCredentials(credKey);
		} catch (CredentialsException e) {
			throw new GitHubIssueManagerException("Unable to access the credentials service", e);
		}
		if (creds != null) {
			if (creds instanceof ICredentialsUsernamePassword) {
				String username = ((ICredentialsUsername) creds).getUsername();
				String password = ((ICredentialsUsernamePassword) creds).getPassword();
				this.httpClient = this.httpClient.setAuthorisation(username, password);
			}
		}
	}
	
	private String getGitHubInstance(GitHubIssue gitHubIssue) throws GitHubIssueManagerException {
		
		String gitHubInstanceUrl = GitHubIssueInstanceUrl.get(gitHubIssue).toString();
		gitHubInstanceUrl = gitHubInstanceUrl.substring(8);
		
		return gitHubInstanceUrl;
		
	}
	
	private String getRepo(GitHubIssue gitHubIssue) throws GitHubIssueManagerException {
		String repo = null;
		if (!gitHubIssue.repository().equals("")) {
			repo = gitHubIssue.repository();
		} else {
			repo = GitHubIssueInstanceRepository.get(gitHubIssue);
		}
		
		if (repo != null) { 
			return repo;
		} else {
			throw new GitHubIssueManagerException("GitHub repository not provided in annotation or CPS");
		}
	}
	
	private String getUrl(GitHubIssue gitHubIssue) throws GitHubIssueManagerException {
		
		String instanceUrl = getGitHubInstance(gitHubIssue);
		String repo = getRepo(gitHubIssue);
		String issueNumber = gitHubIssue.issue();
		
		String fullUrl = "https://api." + instanceUrl + "/repos/" + repo + "/issues/" + issueNumber;
		return fullUrl;
	}
	
	private Issue getIssuePojo(JsonObject json, String issueNumber) {
		
		String state = json.get("state").getAsString();
		String htmlUrl = json.get("html_url").getAsString();
		String title = json.get("title").getAsString();
		
		return new Issue(issueNumber, state, htmlUrl, title);
	}
	
	private boolean isTestMethod(GalasaMethod method) {
		if (!method.isJava()) {
			return false;
		}
		return hasTestAnnotation(method.getJavaExecutionMethod());
	}
	
	private boolean hasTestAnnotation(Method method) {
		for (Annotation a : method.getAnnotations()) {
			if (a instanceof Test) {
				return true;
			}
		}
		return false;		
	}
	
	private boolean isDefectCheckEnabled() {
		try {
			// TO DO - Check CPS property for enabled or disabled
			return true;
		} catch(Exception e) {
			logger.error("Unable to determine if defect check enabled", e);
			return false;
		}
	}
	
	
}