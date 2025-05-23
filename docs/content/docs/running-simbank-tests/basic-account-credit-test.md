---
title: "BasicAccountCreditTest"
---
This test updates a Galasa SimBank account using web services and examines the changes via some 3270 screens, illustrating how to use web services to interface with the SimBank application, and how to create and use Java methods that you do not wish to be recognized as test methods. The SimBank Basic Account Credit test is available in the <a href="https://github.com/galasa-dev/simplatform/blob/main/galasa-simbank-tests/dev.galasa.simbank.tests/src/main/java/dev/galasa/simbank/tests/BasicAccountCreditTest.java" target="_blank"> Galasa simplatform repository</a> in GitHub. 

To run this test, follow the same steps as for `SimBankIVT.java` but using the test class name `BasicAccountCreditTest` instead of `SimBankIVT`. 

The spine of this test resembles that of `SimBankIVT.java`, with a nearly-identical collection of imports and invoked Managers within the main test class - `BasicAccountCreditTest` in this case.

After registering the password as confidential text, the code uses the facilities of the 3270 terminal in much the same way as the previous example to retrieve an account balance:
```java
coreManager.registerConfidentialText("SYS1", "IBMUSER password");

//Initial actions to get into banking application
terminal.waitForKeyboard()
    .positionCursorToFieldContaining("Userid").tab().type("IBMUSER")
    .positionCursorToFieldContaining("Password").tab().type("SYS1")
    .enter().waitForKeyboard()

    //Open banking application
    .pf1().waitForKeyboard()
    .clear().waitForKeyboard()
    .tab().type("bank").enter().waitForKeyboard();

//Obtain the initial balance
BigDecimal userBalance = getBalance("123456789");
```
Note that the `userBalance` is obtained by calling the private method `getBalance` - it is not annotated with `@Test`, and so will (correctly) not be identified to Galasa as a test method. It's also a good habit to declare such methods as *private* if possible.

```java
private BigDecimal getBalance(String accountNum) throws DatastreamException, TimeoutException,
                        KeyboardLockedException, NetworkException, FieldNotFoundException,
                        TextNotFoundException, InterruptedException {
    BigDecimal amount = BigDecimal.ZERO;
    //Open account menu and enter account number
    terminal.pf1().waitForKeyboard()
            .positionCursorToFieldContaining("Account Number").tab()
            .type(accountNum).enter().waitForKeyboard();

    //Retrieve balance from screen
    amount = new BigDecimal(terminal.retrieveFieldTextAfterFieldWithString("Balance").trim());

    //Return to bank menu
    terminal.pf3().waitForKeyboard();
    return amount;
}
```

Next, the amount to be credited to the designated account is prepared by assigning it to the variable `amount`, and a HashMap is declared and loaded with the ACCOUNT_NUMBER and AMOUNT parameters.
```java
//Set the amount be credited and call web service
BigDecimal amount = BigDecimal.valueOf(500.50);
HashMap<String,Object> parameters = new HashMap<String,Object>();
parameters.put("ACCOUNT_NUMBER", "123456789");
parameters.put("AMOUNT", amount.toString());
```

A sample web services request is created by populating the `testSkel.skel` skeleton SOAP message with the prepared parameters, and the web services request invoked. The actual call is made by `client`, an instance of our HTTPClient Manager. You can review the skeleton by expanding `src/main/resources > resources > skeletons` and browsing the `testSkel.skel` file.
```java
//Load sample request with the given parameters
IBundleResources resources = artifacts.getBundleResources(this.getClass());
InputStream is = resources.retrieveSkeletonFile("/resources/skeletons/testSkel.skel", parameters);
String textContext = resources.streamAsString(is);

//Invoke the web request
client.setURI(new URI("http://" + image.getDefaultHostname() + ":2080"));
client.postTextAsXML("updateAccount", textContext, false);
```
Finally, the new balance is retrived, and an assertion checks that the new balance equates to the sum of the old balance plus the amount credited.
```java
//Obtain the final balance
BigDecimal newUserBalance = getBalance("123456789");

//Assert that the correct amount has been credited to the account
assertThat(newUserBalance).isEqualTo(userBalance.add(amount));
```
