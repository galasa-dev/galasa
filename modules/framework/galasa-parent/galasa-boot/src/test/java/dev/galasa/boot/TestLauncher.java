/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot;

import static org.assertj.core.api.Assertions.*;

import dev.galasa.boot.mocks.MockEnvironment;

import org.junit.Test;
import java.util.Properties;

public class TestLauncher {

	@Test
    public void testLauncher() throws Exception{
        Launcher l = new Launcher();

        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("GALASA_HOME","/Users/hobbit/galasa_home_dir/");

        String home = l.getGalasaHome(mockEnv);

        assertThat(home).isEqualTo("/Users/hobbit/galasa_home_dir/");

    }

    @Test
    public void checkEnvironmentVariablesAreHandled() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();
        Properties bootstrap = new Properties();

        bootstrap.setProperty("framework.config.store","/this/should/be/ignored.properties");
        me.setenv("GALASA_CONFIG_STORE","/Users/hobbit/galasa_home/cps.properties");

        bootstrap.setProperty("framework.dynamicstatus.store","/this/should/be/ignored.properties");
        me.setenv("GALASA_DYNAMICSTATUS_STORE","/Users/hobbit/galasa_home/dss.properties");

        bootstrap.setProperty("framework.resultarchive.store","/this/should/be/ignored/ras");
        me.setenv("GALASA_RESULTARCHIVE_STORE","/Users/hobbit/galasa_home/ras");

        bootstrap.setProperty("framework.credentials.store","/this/should/be/ignored.properties");
        me.setenv("GALASA_CREDENTIALS_STORE","/Users/hobbit/galasa_home/creds.properties");

        bootstrap.setProperty("framework.auth.store","/this/should/be/ignored");
        me.setenv("GALASA_AUTH_STORE","https://my-auth-store-server:1234");

        l.setStoresFromEnvironmentVariables(me,bootstrap);

        assertThat(bootstrap.getProperty("framework.config.store")).isEqualTo("/Users/hobbit/galasa_home/cps.properties");
        assertThat(bootstrap.getProperty("framework.dynamicstatus.store")).isEqualTo("/Users/hobbit/galasa_home/dss.properties");
        assertThat(bootstrap.getProperty("framework.resultarchive.store")).isEqualTo("/Users/hobbit/galasa_home/ras");
        assertThat(bootstrap.getProperty("framework.credentials.store")).isEqualTo("/Users/hobbit/galasa_home/creds.properties");
        assertThat(bootstrap.getProperty("framework.auth.store")).isEqualTo("https://my-auth-store-server:1234");
    }

    @Test
    public void checkNullBootstrap() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();
        Properties bootstrap = new Properties();

        me.setenv("GALASA_CONFIG_STORE","/Users/hobbit/galasa_home/cps.properties");

        me.setenv("GALASA_DYNAMICSTATUS_STORE","/Users/hobbit/galasa_home/dss.properties");

        me.setenv("GALASA_RESULTARCHIVE_STORE","/Users/hobbit/galasa_home/ras");

        me.setenv("GALASA_CREDENTIALS_STORE","/Users/hobbit/galasa_home/creds.properties");

        me.setenv("GALASA_AUTH_STORE","https://my-auth-store-server:1234");

        l.setStoresFromEnvironmentVariables(me,bootstrap);

        assertThat(bootstrap.getProperty("framework.config.store")).isEqualTo("/Users/hobbit/galasa_home/cps.properties");
        assertThat(bootstrap.getProperty("framework.dynamicstatus.store")).isEqualTo("/Users/hobbit/galasa_home/dss.properties");
        assertThat(bootstrap.getProperty("framework.resultarchive.store")).isEqualTo("/Users/hobbit/galasa_home/ras");
        assertThat(bootstrap.getProperty("framework.credentials.store")).isEqualTo("/Users/hobbit/galasa_home/creds.properties");
        assertThat(bootstrap.getProperty("framework.auth.store")).isEqualTo("https://my-auth-store-server:1234");
    }

    @Test
    public void checkBootstrapIsDefault() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();
        Properties bootstrap = new Properties();

        bootstrap.setProperty("framework.config.store","/this/should/be/ignored.properties");

        bootstrap.setProperty("framework.dynamicstatus.store","/this/should/be/ignored.properties");

        bootstrap.setProperty("framework.resultarchive.store","/this/should/be/ignored/ras");

        bootstrap.setProperty("framework.credentials.store","/this/should/be/ignored.properties");

        bootstrap.setProperty("framework.auth.store","https://my-auth-store-server:1234");

        l.setStoresFromEnvironmentVariables(me,bootstrap);

        assertThat(bootstrap.getProperty("framework.config.store")).isEqualTo("/this/should/be/ignored.properties");
        assertThat(bootstrap.getProperty("framework.dynamicstatus.store")).isEqualTo("/this/should/be/ignored.properties");
        assertThat(bootstrap.getProperty("framework.resultarchive.store")).isEqualTo("/this/should/be/ignored/ras");
        assertThat(bootstrap.getProperty("framework.credentials.store")).isEqualTo("/this/should/be/ignored.properties");
        assertThat(bootstrap.getProperty("framework.auth.store")).isEqualTo("https://my-auth-store-server:1234");
    }

    @Test
    public void checkEnvironmentVariablesAreTrimmed() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();
        Properties bootstrap = new Properties();

        bootstrap.setProperty("framework.config.store","/this/should/be/ignored.properties");
        me.setenv("GALASA_CONFIG_STORE","/Users/hobbit/galasa_home/cps.properties  ");

        bootstrap.setProperty("framework.dynamicstatus.store","/this/should/be/ignored.properties");
        me.setenv("GALASA_DYNAMICSTATUS_STORE","  /Users/hobbit/galasa_home/dss.properties");

        bootstrap.setProperty("framework.resultarchive.store","/this/should/be/ignored/ras");
        me.setenv("GALASA_RESULTARCHIVE_STORE"," /Users/hobbit/galasa_home/ras  ");

        bootstrap.setProperty("framework.credentials.store","/this/should/be/ignored.properties");
        me.setenv("GALASA_CREDENTIALS_STORE","/Users/hobbit/galasa_home/creds.properties");

        bootstrap.setProperty("framework.auth.store","/this/should/be/ignored");
        me.setenv("GALASA_AUTH_STORE","     https://my-auth-store-server:1234  ");

        l.setStoresFromEnvironmentVariables(me,bootstrap);

        assertThat(bootstrap.getProperty("framework.config.store")).isEqualTo("/Users/hobbit/galasa_home/cps.properties");
        assertThat(bootstrap.getProperty("framework.dynamicstatus.store")).isEqualTo("/Users/hobbit/galasa_home/dss.properties");
        assertThat(bootstrap.getProperty("framework.resultarchive.store")).isEqualTo("/Users/hobbit/galasa_home/ras");
        assertThat(bootstrap.getProperty("framework.credentials.store")).isEqualTo("/Users/hobbit/galasa_home/creds.properties");
        assertThat(bootstrap.getProperty("framework.auth.store")).isEqualTo("https://my-auth-store-server:1234");
    }

    @Test
    public void testJava8Fails() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();

        me.setProperty("java.version","8");

        try{
            l.validateJavaLevel(me);
        }catch(LauncherException le){
            return;
        }
        fail("LauncherException should have been thrown");
    }

    @Test
    public void testJava17Passes() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();

        me.setProperty("java.version","17");

        try{
            l.validateJavaLevel(me);
        }catch(LauncherException le){
            fail("LauncherException thrown");
        }
    }

    @Test
    public void testJava16Fails() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();

        me.setProperty("java.version","16");

        try{
            l.validateJavaLevel(me);
        }catch(LauncherException le){
            return;
        }
        fail("LauncherException should have been thrown");
    }

    @Test
    public void testCurrentJavaWorks() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();

        me.setProperty("java.version",System.getProperty("java.version"));

        try{
            l.validateJavaLevel(me);
        }catch(LauncherException le){
            fail("LauncherException thrown");
        }
    }

    @Test
    public void testWhenNoJavaFails() {
        Launcher l  = new Launcher();
        MockEnvironment me = new MockEnvironment();
        try{
            l.validateJavaLevel(me);
        }catch(LauncherException le){
            return;
        }
        fail("LauncherException should have been thrown");
    }

    @Test
    public void testLogCommandLineArgumentsWithJWTReturnsMask() throws LauncherException, InterruptedException{
        // Given...
        String jwtValue = "myJWT15h3r3";
        Launcher launcher = new Launcher();
        String[] arguments = new String[]{"--bootstrap","file://home/.galasa/bootstrap.properties","-DGALASA_JWT="+jwtValue, "--obr","file://path/to/my/obr"};
        
        // When...
        String output = launcher.logCommandLineArguments(arguments);

        // Then...
        String expectedOutput = "Supplied command line arguments: --bootstrap file://home/.galasa/bootstrap.properties -DGALASA_JWT=******* --obr file://path/to/my/obr ";
        assertThat(output).contains("GALASA_JWT");
        assertThat(output).doesNotContain(jwtValue);
        assertThat(output).isEqualTo(expectedOutput);
    }

    @Test
    public void testLogCommandLineArgumentsWithoutJWTReturnsvalues() throws LauncherException, InterruptedException{
        // Given...
        Launcher launcher = new Launcher();
        String[] arguments = new String[]{"--bootstrap","file://home/.galasa/bootstrap.properties","--test", "please-test.java", "--obr","file://path/to/my/obr"};
        
        // When...
        String output = launcher.logCommandLineArguments(arguments);

        // Then...
        String expectedOutput = "Supplied command line arguments: --bootstrap file://home/.galasa/bootstrap.properties --test please-test.java --obr file://path/to/my/obr ";
        assertThat(output).isEqualTo(expectedOutput);
    }

    @Test
    public void testLogCommandLineArgumentsWithBadJWTFlagReturnsvalues() throws LauncherException, InterruptedException{
        // Given...
        Launcher launcher = new Launcher();
        String[] arguments = new String[]{"--bootstrap","file://home/.galasa/bootstrap.properties","--jwt", "im4d3am15tak3", "--obr","file://path/to/my/obr"};
        
        // When...
        String output = launcher.logCommandLineArguments(arguments);

        // Then...
        String expectedOutput = "Supplied command line arguments: --bootstrap file://home/.galasa/bootstrap.properties --jwt im4d3am15tak3 --obr file://path/to/my/obr ";
        assertThat(output).isEqualTo(expectedOutput);
    }
}
