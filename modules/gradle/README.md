# Gradle
Gradle plugins for Galasa 

## OBR Plugin
The OBR plugin allows you to build OSGi Bundle Repositories using Gradle. If you would like to build OBRs using Maven, please refer to the [Maven OBR plugin](https://github.com/galasa-dev/galasa/blob/main/modules/maven/README.md). 

### Usage 
To use the Gradle OBR plugin in a Gradle test project:

1. In each of your testcase projects: Add the these lines to include the `galasa.tests` plugin in your build.gradle file:
    ```groovy
    plugins {
        ...
        id 'java' 
        id 'maven-publish'
        id 'dev.galasa.tests' version '0.40.0'
        ...
    }
    ```
    - The `java` plugin builds the testcase code.
    - The `maven-publish` causes the built artifacts to be pushed to a maven repository
    - The `dev.galasa.tests` causes a test catalog to be built, for inclusion in an overall test catalog, which can be published to an ecosystem.

2. Create a `build.gradle` file in your project's OBR directory with the following contents:

    ```groovy
    plugins {
        ...
        id 'dev.galasa.obr' version '0.40.0'
        id 'dev.galasa.testcatalog' version '0.40.0'
        ...
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = 'https://development.galasa.dev/main/maven-repo/obr'
        }
    }

    // Here, all OSGi Bundles to be included in the OBR must be listed using the 'bundle' configuration
    dependencies {
        bundle project(':com.example.tests.manager')
        bundle project(':com.example.tests.mytests')
    }
    ```
    This applies the OBR plugin to the OBR subproject and specifies the bundles to be included in the OBR that will be built. It also defines the repositories that Gradle will search within to resolve dependencies and applies the `java` plugin to enable Java compilation, testing, and build features.


    Declare the obr file as an artifact, and add it to the list of artifacts which get published for the OBR project.
    ```
    def obrFile = file('build/galasa.obr')
    artifacts {
        archives obrFile
    }

    // Tell gradle to publish the built OBR as a maven artifact on the 
    // local maven repository.
    publishing {
        publications {
            maven(MavenPublication) {
                artifact obrFile
            }
        }
    }
    ```

3. The following step is deprecated in v0.33.0 and will be removed in future releases:
To publish the test catalog to a live Galasa ecosystem, you will need the following lines in your OBR project:
    ```
    deployTestCatalog {
        if ( System.getProperty("GALASA_BOOTSTRAP") != null) {
            bootstrap = System.getProperty("GALASA_BOOTSTRAP")
        } else {
            bootstrap = "none"
        }

        if (System.getProperty("GALASA_STREAM")!= null) {
            stream = System.getProperty("GALASA_STREAM")
        } else {
            stream = "none"
        }

        if(System.getProperty("GALASA_TOKEN") != null) {
            token = System.getProperty("GALASA_TOKEN");
        } else {
            token = "none"
        }
    }
    ```
    
    These lines set the three properties `bootstrap`, `stream` and `token` based on properties 
    passed on the build invokation `GALASA_BOOTSTRAP` `GALASA_STREAM` and `GALASA_TOKEN`.
    
    It's probably better to have the build accept these values from outside, as GALASA_TOKEN will
    probably be supplied from a secrets store.

4. Create a `settings.gradle` file in your project's root directory with the following contents:
   
    ```groovy
    pluginManagement {
        repositories {
            mavenLocal()
            mavenCentral()
            maven {
                url = "https://development.galasa.dev/prod/maven-repo/obr"
            }
            gradlePluginPortal()
        }
    }

    include 'com.example.tests.obr' // This must match the name of your OBR subproject.
    ```

    This defines the repositories that Gradle will search to find requested plugins. It also includes the OBR subproject in Gradle builds.

    If you would like to give your OBR subproject a different name, you can create a `settings.gradle` file in your OBR directory containing the following line:
    
    ```groovy
    rootProject.name='obrProjectName'
    ```
    
    If you do this, ensure your `include` statement in your root project's `settings.gradle` file matches the name given to your OBR subproject.

5. Building artifacts

    To verify that the OBR was successfully built, A `gradle build` directory will appear in your OBR directory and within it, a `galasa.obr` file should be present.

6. Publishing artifacts to the local maven repository
    If you run `gradle clean build publishToMavenLocal` then the artifacts built will be published to the local maven folders on the build machine.

7. Deploying the test catalog to the Galasa ecosystem

    Old method. Deprecated in v0.33.0:
    ```
    gradle deploytestcat \
        -DGALASA_BOOTSTRAP=$GALASA_BOOTSTRAP \
        -DGALASA_STREAM=$GALASA_STREAM \
        -DGALASA_TOKEN=$GALASA_TOKEN
    ```
    This will pick up values for the bootstrap, stream name and galasa access token from your environmnent variables.

    New method:
    Use the `galasactl properties set` command to set the `location` field of your test stream to refer directly to the URL of the test catalog where it is available from your maven repository.
        

## To Build the plugin locally
Use the `.build-locally.sh` script to invoke a build.

See the notes at the top of the script for a list of environment variables which can be over-ridden to control build behaviour.