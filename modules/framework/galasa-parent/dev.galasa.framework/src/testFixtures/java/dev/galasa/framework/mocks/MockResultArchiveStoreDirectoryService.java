/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.*;
import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasRunResultPage;
import dev.galasa.framework.spi.ras.RasSortField;
import dev.galasa.framework.spi.ras.RasTestClass;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class MockResultArchiveStoreDirectoryService implements IResultArchiveStoreDirectoryService {

    private List<IRunResult> runResults;
    private String nextCursor;

    public MockResultArchiveStoreDirectoryService(List<IRunResult> runsResults) {
        this.runResults = runsResults;
    }

	private void applySearchCriteria( IRasSearchCriteria searchCriteria) throws ResultArchiveStoreException{
		List<IRunResult> returnRuns = new ArrayList<IRunResult>() ;
		for (IRunResult run : this.runResults){
			Boolean compareInstant = searchCriteria.criteriaMatched(run.getTestStructure());
			if (compareInstant){
				returnRuns.add(run);
			}
		}
		
		this.runResults = returnRuns;
	}

	@Override
	public @NotNull List<IRunResult> getRuns(@NotNull IRasSearchCriteria... searchCriterias) throws ResultArchiveStoreException {
		for(IRasSearchCriteria searchCriteria : searchCriterias) {
			applySearchCriteria(searchCriteria);
		}
		return this.runResults;
	}

	@Override
	public @NotNull RasRunResultPage getRunsPage(int maxResults, RasSortField primarySort, String pageCursor, @NotNull IRasSearchCriteria... searchCriterias) throws ResultArchiveStoreException {
        return new RasRunResultPage(getRuns(searchCriterias), nextCursor);
	}

	@Override
	public @NotNull List<String> getRequestors() throws ResultArchiveStoreException {
		List<String> requestors = new ArrayList<>();
		for (IRunResult run : this.runResults){
			requestors.add(run.getTestStructure().getRequestor().toString());
		}
		return requestors;
	}

	@Override
	public @NotNull List<RasTestClass> getTests() throws ResultArchiveStoreException {
		HashMap<String,RasTestClass> tests = new HashMap<>();
        String key;
        for (IRunResult run : this.runResults){
			TestStructure testStructure = run.getTestStructure();
			key = testStructure.getBundle()+"/"+testStructure.getTestName();
			if (key.equals("ForceException/ForceException")){
				throw new ResultArchiveStoreException("ForceException result found in run");
			}
			if(!tests.containsKey(key)){
				tests.put(key,new RasTestClass(testStructure.getTestName(), testStructure.getBundle()));
			}
        }
        return new ArrayList<>(tests.values());
	}

	@Override
	public @NotNull List<String> getResultNames() throws ResultArchiveStoreException {
		List<String> resultNames = new ArrayList<>();
		for (IRunResult run : this.runResults){
				String result  = run.getTestStructure().getResult().toString();
				if (result.equals("ForceException")){
					throw new ResultArchiveStoreException("ForceException result found in run");
				}else if (!resultNames.contains(result)){
					resultNames.add(result);
				}
		}
		return resultNames;
	}

	@Override
	public IRunResult getRunById(@NotNull String runId) throws ResultArchiveStoreException {
		IRunResult resultToReturn = null;
		List <IRunResult> runResults = this.runResults;
		if (runResults != null) {

			if (runResults.isEmpty()) {
				// There is nothing the testcase wanted us to return.
			} else {

				for (int c =0; c < runResults.size(); c++){
					IRunResult match = runResults.get(c);
					if ( match.getRunId().equals(runId)){
						resultToReturn = match;
						break;
					}
				}

				if (resultToReturn==null) {
					throw new ResultArchiveStoreException("Run id not found in mock getRunById().");
				}
			}
		}
		return resultToReturn;
		
	}

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

	@Override
	public @NotNull String getName() {
		throw new UnsupportedOperationException("Unimplemented method 'getName'");
	}

	@Override
	public boolean isLocal() {
		throw new UnsupportedOperationException("Unimplemented method 'isLocal'");
	}

    @Override
    public List<IRunResult> getRunsByRunName(@NotNull String runName) throws ResultArchiveStoreException {
        List<IRunResult> matchingRuns = new ArrayList<>();
        for (IRunResult run : runResults) {
            if (run.getTestStructure().getRunName().equals(runName)) {
                matchingRuns.add(run);
            }
        }
        return matchingRuns;
    }

	@Override
	public List<IRunResult> getRunsByGroupName(@NotNull String groupName) throws ResultArchiveStoreException {
		List<IRunResult> matchingRuns = new ArrayList<>();
        for (IRunResult run : runResults) {
            if (run.getTestStructure().getGroup().equals(groupName)) {
                matchingRuns.add(run);
            }
        }
        return matchingRuns;
	}
}