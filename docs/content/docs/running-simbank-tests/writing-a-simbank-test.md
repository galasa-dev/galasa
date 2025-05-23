---
title: "Writing your own SimBank test"
---
Now you have run through the tests provided as part of Galasa SimBank, you can have a go at writing your own test. This requires you to use a 3270 terminal emulator to connect with SimBank and perform a menu-driven transfer between two accounts. Why not attempt to follow the instructions without looking at the code? You can use the provided test examples as a reference and for inspiration. 

To write and run simple Galasa tests, you need a modest knowledge of Java. Even if your application under test is written in a completely different language, you need to understand enough Java to be able to create all of your required tests. The <a href="https://github.com/galasa-dev/managers/tree/main/galasa-managers-parent" target="_blank"> Manager installation verification tests (IVTs)</a> that are stored in GitHub provide useful reference material and can be used to aid understanding of how to  structure a test.

Don't forget that whenever you create a test, or modify an existing one, you need to run a clean install in Maven to ensure that the correct version of the test is submitted to the test runner.

## Create a new Galasa test class


1. Launch SimBank using the instructions in the [CLI online](../running-simbank-tests/simbank-cli) or [CLI offline](../running-simbank-tests/simbank-cli-offline) documentation. 
1. Create a new test class. If you are using the CLI, manually create the test class file, for example in VS Code. Complete the next dialog as follows and then click *Finish*. Note that in the example a new package is created that is called `dev.galasa.simbanks.tests`. ![New Java Class](./create-new-class.png)
1. Annotate the new class with the `@Test` annotation.


<details>
<summary>Stage 1 - code so far</summary>

```java
package dev.galasa.simbanks.tests;

import dev.galasa.Test;

@Test
public class BasicTransferTest {

}
```
</details>

## Provision the necessary resources and write a *not null* test.

4. Just inside your new test class, declare the required Managers and add a *not null* test. You will need declarations for `@ZosImage`, `@Zos3270Terminal`, `@ArtifactManager`, `@HttpClient`, `@CoreManager`, and `@Logger`. Make sure that `@ZosImage` and `@Zos3270Terminal` are assigned the imageTag `simbank`.
1. Add a `testNotNull()` method that asserts that your `@Zos3270Terminal`, `@ArtifactManager` and `@HttpClient` have instantiated correctly.

<details>
<summary>Stage 2 - code so far</summary>

```java
package dev.galasa.simbanks.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.artifact.ArtifactManager;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.http.HttpClient;
import dev.galasa.http.IHttpClient;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zos3270.ITerminal;
import dev.galasa.zos3270.Zos3270Terminal;

@Test
public class BasicTransferTest {
	@ZosImage(imageTag="SIMBANK")
    public IZosImage image;

    @Zos3270Terminal(imageTag="SIMBANK")
    public ITerminal terminal;

    @ArtifactManager
    public IArtifactManager artifacts;

    @HttpClient
    public IHttpClient client;
    
    @CoreManager
    public ICoreManager coreManager;
    
    @Logger
    public Log logger;
    

    @Test
    public void testNotNull() {
        //Check all objects loaded
        assertThat(terminal).isNotNull();
        assertThat(artifacts).isNotNull();
        assertThat(client).isNotNull();
    }
}

```

</details>

You can run the *Not Null* test by creating a new run configuration as in the other examples (don't forget to ensure that SimBank is running first). 

## Create the main test method and open the main bank menu
6. Inside your test class, create a public method called `transferCredit()`. Annotate it with `@Test`.
1. Log on to SimBank using the `terminal` object, which is an instance of `ITerminal`. Use the credentials IBMUSER and SYS1.
1. Use `assertThat()` to confirm that the session manager has a bank session available.
1. Open the banking application.
1. Use `assertThat()` to confirm that the bank menu is showing.

Refer to the provided tests to see how to use the various `terminal` methods such as `waitForKeyboard`, `positionCursorToFieldContaining`, `enter`, `tab`, `clear`, `pf1` and `type`.

<details>
<summary>Stage 3 - the main `transferCredit()` method (in progress)</summary>

```java
@Test
public void transferCredit() throws TimeoutException, KeyboardLockedException, NetworkException, FieldNotFoundException, TextNotFoundException, InterruptedException {
	//Logon through the session manager
	terminal.waitForKeyboard()
	.positionCursorToFieldContaining("Userid").tab().type("IBMUSER")
	.positionCursorToFieldContaining("Password").tab().type("SYS1")
	.enter().waitForKeyboard();
	
	//Assert that the session manager has a bank session available
	assertThat(terminal.retrieveScreen()).containsOnlyOnce("SIMPLATFORM MAIN MENU");
	assertThat(terminal.retrieveScreen()).containsOnlyOnce("BANKTEST");
	
	//Open banking application
	terminal.pf1().waitForKeyboard()
	.clear().waitForKeyboard()
	.tab().type("bank").enter().waitForKeyboard();
	
	//Assert that the bank menu is showing
	assertThat(terminal.retrieveScreen()).containsOnlyOnce("Options     Description        PFKey ");
	assertThat(terminal.retrieveScreen()).containsOnlyOnce("BROWSE      Browse Accounts    PF1");
	assertThat(terminal.retrieveScreen()).containsOnlyOnce("TRANSF      Transfer Money     PF4");
    }
```
</details>

## Retrieve the initial account balances and declare an amount to transfer
11. Create a private `getBalance(<string>)` method by copying it from the `BasicAccountCreditTest.java` example.
1. Retrieve the initial balances of accounts `123456789` and `987654321` and write them to the log. Use the `getBalance(<string>)` method you just created.
1. Declare a `final BigDecimal transferAmount` variable to store an amount to transfer by instantiating a new `BigDecimal` object. 

<details>
<summary>Stage 4 - the full `BasicTransferTest` code so far</summary>

```java
@Test
public class BasicTransferTest {
	@ZosImage(imageTag="SIMBANK")
    public IZosImage image;

    @Zos3270Terminal(imageTag="SIMBANK")
    public ITerminal terminal;

    @ArtifactManager
    public IArtifactManager artifacts;

    @HttpClient
    public IHttpClient client;
    
    @CoreManager
    public ICoreManager coreManager;
    
    @Logger
    public Log logger;
    

    @Test
    public void testNotNull() {
        //Check all objects loaded
        assertThat(terminal).isNotNull();
        assertThat(artifacts).isNotNull();
        assertThat(client).isNotNull();
    }
    

    @Test
    public void transferCredit() throws TimeoutException, KeyboardLockedException, NetworkException, FieldNotFoundException, TextNotFoundException, InterruptedException {
    	//Logon through the session manager
    	terminal.waitForKeyboard()
        .positionCursorToFieldContaining("Userid").tab().type("IBMUSER")
        .positionCursorToFieldContaining("Password").tab().type("SYS1")
        .enter().waitForKeyboard();
    	
    	//Assert that the session manager has a bank session available
        assertThat(terminal.retrieveScreen()).containsOnlyOnce("SIMPLATFORM MAIN MENU");
    	assertThat(terminal.retrieveScreen()).containsOnlyOnce("BANKTEST");
    	
        //Open banking application
        terminal.pf1().waitForKeyboard()
        .clear().waitForKeyboard()
        .tab().type("bank").enter().waitForKeyboard();
    	
        //Assert that the bank menu is showing
        assertThat(terminal.retrieveScreen()).containsOnlyOnce("Options     Description        PFKey ");
        assertThat(terminal.retrieveScreen()).containsOnlyOnce("BROWSE      Browse Accounts    PF1");
        assertThat(terminal.retrieveScreen()).containsOnlyOnce("TRANSF      Transfer Money     PF4");
        
        //Retrieve initial account balances
        BigDecimal account123456789InitialBalance = getBalance("123456789");
        logger.info("Pre-test balance for account 123456789 is " + account123456789InitialBalance.toString());
        BigDecimal account987654321InitialBalance = getBalance("987654321");
        logger.info("Pre-test balance for account 987654321 is " + account987654321InitialBalance.toString());
        
        //Declare the amount to be transferred
        final BigDecimal transferAmount = new BigDecimal(10.0);
    }
    
    private BigDecimal getBalance(String accountNum) throws DatastreamException, TimeoutException, KeyboardLockedException, NetworkException, FieldNotFoundException, TextNotFoundException, InterruptedException {
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
    
}
```
</details>

## Automate Galasa to make the transfer and check that everything is correct.
14. Choose the TRANSFER MONEY option (PF4) and check that we are on the right screen - it contains the string SIMBANK TRANSFER MENU.
1. Enter the transfer amount details using the various methods of your `terminal` instance. Note that you you have to convert the `transferAmount` to a `String` before you can use `terminal.type` to enter it into the screen's fields.
1. Check that a `Transfer Successful` message appears.
1. Go back to the main bank menu (PF3).
1. Retrieve the final balances and use assertions to check that the account balances have been debited and credited correctly.

<details>
<summary>The completed test code</summary>

```java
package dev.galasa.simbanks.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.artifact.ArtifactManager;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.http.HttpClient;
import dev.galasa.http.IHttpClient;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.ITerminal;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.Zos3270Terminal;
import dev.galasa.zos3270.spi.DatastreamException;
import dev.galasa.zos3270.spi.NetworkException;

@Test
public class BasicTransferTest {
	@ZosImage(imageTag="SIMBANK")
    public IZosImage image;

    @Zos3270Terminal(imageTag="SIMBANK")
    public ITerminal terminal;

    @ArtifactManager
    public IArtifactManager artifacts;

    @HttpClient
    public IHttpClient client;
    
    @CoreManager
    public ICoreManager coreManager;
    
    @Logger
    public Log logger;
    

    @Test
    public void testNotNull() {
        //Check all objects loaded
        assertThat(terminal).isNotNull();
        assertThat(artifacts).isNotNull();
        assertThat(client).isNotNull();
    }
    
    @Test
    public void transferCredit() throws TimeoutException, KeyboardLockedException, NetworkException, FieldNotFoundException, TextNotFoundException, InterruptedException {
    	//Logon through the session manager
    	terminal.waitForKeyboard()
        .positionCursorToFieldContaining("Userid").tab().type("IBMUSER")
        .positionCursorToFieldContaining("Password").tab().type("SYS1")
        .enter().waitForKeyboard();
    	
    	//Assert that the session manager has a bank session available
        assertThat(terminal.retrieveScreen()).containsOnlyOnce("SIMPLATFORM MAIN MENU");
    	assertThat(terminal.retrieveScreen()).containsOnlyOnce("BANKTEST");
    	
        //Open banking application
        terminal.pf1().waitForKeyboard()
        .clear().waitForKeyboard()
        .tab().type("bank").enter().waitForKeyboard();
    	
        //Assert that the bank menu is showing
        assertThat(terminal.retrieveScreen()).containsOnlyOnce("Options     Description        PFKey ");
        assertThat(terminal.retrieveScreen()).containsOnlyOnce("BROWSE      Browse Accounts    PF1");
        assertThat(terminal.retrieveScreen()).containsOnlyOnce("TRANSF      Transfer Money     PF4");
        
        //Retrieve initial account balances
        BigDecimal account123456789InitialBalance = getBalance("123456789");
        logger.info("Pre-test balance for account 123456789 is " + account123456789InitialBalance.toString());
        BigDecimal account987654321InitialBalance = getBalance("987654321");
        logger.info("Pre-test balance for account 987654321 is " + account987654321InitialBalance.toString());
        
        //Declare the amount to be transferred
        final BigDecimal transferAmount = new BigDecimal(10.0);
        
        //Choose the Transfer Money option
        terminal.pf4().waitForKeyboard();
        
        //Assert that we are on the right screen
        assertThat(terminal.retrieveScreen()).containsOnlyOnce("SIMBANK TRANSFER MENU");
        
        //Enter the transfer amount details
        //Open account menu and enter account numbers and transfer amount
        terminal.positionCursorToFieldContaining("Transfer from Account Number").tab()
                .type("123456789").enter().waitForKeyboard();
        terminal.positionCursorToFieldContaining("Transfer to Account Number").tab()
        		.type("987654321").enter().waitForKeyboard();
        terminal.positionCursorToFieldContaining("Transfer Amount").tab()
				.type(transferAmount.toString()).enter().waitForKeyboard();
        
        //Assert that the transfer was successful
        assertThat(terminal.retrieveScreen()).containsOnlyOnce("Transfer Successful");
        
        //Back to main menu - important we do this to prepare for getBalance().
        terminal.pf3().waitForKeyboard();
        
        //Retrieve final account balances
        BigDecimal account123456789FinalBalance = getBalance("123456789");
        logger.info("Final balance for account 123456789 is " + account123456789FinalBalance.toString());
        BigDecimal account987654321FinalBalance = getBalance("987654321");
        logger.info("Final balance for account 987654321 is " + account987654321FinalBalance.toString());
        
        //Assert that the final balances differ by exactly the transferred amount
        assertThat(account123456789FinalBalance).isEqualTo(account123456789InitialBalance.subtract(transferAmount));
        assertThat(account987654321FinalBalance).isEqualTo(account987654321InitialBalance.add(transferAmount));
    }
    
    private BigDecimal getBalance(String accountNum) throws DatastreamException, TimeoutException, KeyboardLockedException, NetworkException, FieldNotFoundException, TextNotFoundException, InterruptedException {
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
    
}

```
</details>

If you haven't yet done so, you can run the final test by first ensuring that SimBank is already running and then creating and running a new run configuration as you did with the other examples. You can also view the expected and actual values.
</details>

Now create your *own* test to run against SimBank, or copy and modify one of the tests provided, to get more experience in writing Galasa tests. You could also try rewriting *this* test to use a provisioning Manager (e.g. IAccount) just as the `ProvisionedAccoutCreditTests.java` example improved the design of `BasicAccountCreditTests.java`.

