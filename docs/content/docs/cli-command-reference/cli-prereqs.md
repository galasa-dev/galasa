---
title: "Prerequisites"
---

Install the following software before installing Galasa.

## Java JDK (Required)

Install a Java 17, Java 21, or Java 25 JDK. Galasa tests and Managers are written in Java so a Java JDK is required.

_Note:_ Later versions of Java are not currently supported.

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

Maven version compatibility with Java 17, Java 21, and Java 25:

| Maven version | Java 17 support | Java 21 support | Java 25 support |
| :------------ | :-------------- | :-------------- | :-------------- |
| 3.8.1+        | Fully supported | Fully supported | Fully supported |
| 3.9.x         | Recommended     | Recommended     | Recommended     |

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

