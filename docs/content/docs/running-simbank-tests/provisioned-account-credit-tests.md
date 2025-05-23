---
title: "ProvisionedAccountCreditTests"
---
To run this test, follow the same steps as for `SimBankIVT.java` but using the test class name `ProvisionedAccountCreditTests` instead of `SimBankIVT`. The SimBank Provisioned Account Credit test is available in the <a href="https://github.com/galasa-dev/simplatform/blob/main/galasa-simbank-tests/dev.galasa.simbank.tests/src/main/java/dev/galasa/simbank/tests/ProvisionedAccountCreditTests.java" target="_blank"> Galasa simplatform repository</a> in GitHub. 

This test performs the same function as `BasicAccountCreditTest.java` but has an improved design which includes the use of a provisioned account object, which, once declared as:
```java
@Account(existing=false, accountType=AccountType.HighValue)
public IAccount account;
```
is used to retrieve the account balance:
```java
BigDecimal userBalance = account.getBalance();
```
Note how the `getBalance()` method has been moved into the provisioned account object, decluttering the main test class.
This declaration:
```java
@Logger
public Log logger;
```
provides access to the log, to which an initial message is written:
```java
Logger.info("Pre-test balance is " + userBalance.toString());
```
Once again, the amount to be credited is then set and a HashMap prepared with the parameters for the subequent web services call:
```java
BigDecimal amount = BigDecimal.valueOf(500.50);
logger.info("Will credit account with " + amount.toString());
HashMap<String,Object> parameters = new HashMap<String,Object>();
parameters.put("ACCOUNT_NUMBER", account.getAccountNumber());
parameters.put("AMOUNT", amount.toString());
```
This time, the account number is obtained by calling `account.getAccountNumber()` rather than by hardcoding the value. 

The sample request is loaded with the prepared parameters, and a note made to the log:
```java
//Load sample request with the given parameters
IBundleResources resources = artifacts.getBundleResources(this.getClass());
InputStream is = resources.retrieveSkeletonFile("/resources/skeletons/testSkel.skel", parameters);
String textContent = resources.streamAsString(is);

logger.info("Credit actioned");
```
The XML request is stored in the test results archive:
```java
//Store the xml request in the test results archive
storeOutput("webservice", "request.txt", textContent);
```
This uses a private method called `storeOutput`:
```java
private void storeOutput(String folder, String file, String content) throws IOException {
	//Store the xml request in the test results archive
	Path requestPath = artifactRoot.resolve(folder).resolve(file);
	Files.write(requestPath, 
			content.getBytes(), 
			new SetContentType(ResultArchiveStoreContentType.TEXT), 
			StandardOpenOption.CREATE);
}
```
Then the actual web request is invoked, and the response stored in the test results archive:
```java
//Invoke the web request
client.setURI(new URI(bank.getFullAddress()));
String response = (String) client.postTextAsXML(bank.getUpdateAddress(), textContent, false);

//Store the response in the test results archive
storeOutput("webservice", "response.txt", response);
```
To complete the test, the final balance is obtained and logged, an assertion checks that the correct amount has been credited and a final note is made to the log:
```java
//Obtain the final balance
BigDecimal newUserBalance = account.getBalance();
logger.info("Post-test balance is " + newUserBalance.toString());

//Assert that the correct amount has been credited to the account
assertThat(newUserBalance).isEqualTo(userBalance.add(amount));

logger.info("Balances matched");
```