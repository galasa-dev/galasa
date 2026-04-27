---
title: "Prerequisites"
---

Install the following software before installing Galasa.

## Java JDK (Required)

Install Java 17 JDK. Galasa tests and Managers are written in Java so a Java JDK is required.

_Note:_ Java 21 and later versions are not currently supported.

After installation, set the `JAVA_HOME` environment variable to your JDK installation path. Verify by running:
=== "Linux or macOS"
    ```bash
    echo $JAVA_HOME
    ```

=== "Windows (PowerShell)"
    ```bash
    echo %JAVA_HOME%
    ```

## Maven or Gradle (Required)

Install either Maven or Gradle to build Galasa projects. Choose one:

### Maven

Maven version compatibility with Java 17:

| Maven version | Java 17 support |
| :------------ | :-------------- |
| 3.8.1+        | Fully supported |
| 3.9.x         | Recommended     |

Maven uses the Java version specified in your `JAVA_HOME` environment variable. Verify your Maven installation and Java version:

=== "Linux or macOS or Windows"
    ```bash
    mvn --version
    ```

The output shows both your Maven version and the Java version Maven is using.

### Gradle

Gradle version compatibility with Galasa:

| Gradle version | Compatible Galasa version |
| :------------- | :------------------------ |
| 6.8.x          | All                       |
| 6.9.x          | All                       |
| 7.x.x          | All                       |
| 8.x.x          | 0.36.0 or later           |

If upgrading to Gradle 8, see the [Upgrading](../upgrading/index.md) documentation for required changes.

Add Gradle to your PATH. Verify by running:
=== "Linux or macOS"
    ```bash
    echo $PATH
    ```
=== "Windows (PowerShell)"
    ```bash
    echo %PATH%
    ```

## 3270 Emulator (Optional)

A 3270 emulator is not required to run Galasa tests, but is useful for manually exploring the example application, [Galasa SimBank](../running-simbank-tests/simbank-cli.md), before running automated tests.

Common options include:

- IBM Personal Communications (PCOMM)
- IBM Host on Demand (supports Windows, Linux, and MacOS)

## Next steps

Install the Galasa CLI by following the [Installing the Galasa CLI](./installing-cli-tool.md) documentation.

