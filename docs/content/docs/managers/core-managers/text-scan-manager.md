---
title: "Text Scan Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/textscan/package-summary.html){target="_blank"}.


## Overview

The Text Scan Manager provides utility text scanning routines for tests and Managers to use. It is designed for searching and validating text content in logs, batch job outputs, files, and other text sources. The Manager provides both stateless text scanning (`ITextScanner`) and stateful log scanning (`ILogScanner`) capabilities.

The Text Scan Manager is a core Manager that can be used by any Galasa test to search for patterns, validate expected content, and detect failure conditions in text output.


## Annotations

The following annotations are available with the Text Scan Manager.


### Text Scanner

| Annotation: | Text Scanner |
| --------------------------------------- | :------------------------------------- |
| Name: | `@TextScanner` |
| Description: | The `@TextScanner` annotation requests the Text Scan Manager to provide a text scanner instance. The text scanner is stateless and can be reused across different text sources. |
| Syntax: | <pre lang="java">@TextScanner<br>public ITextScanner textScanner;<br></pre> |
| Notes: | The `ITextScanner` interface provides methods to scan strings, input streams, and scannable objects for regex patterns or literal text. It does not maintain state between scans.<br><br> See [TextScanner](https://galasa.dev/docs/reference/javadoc/dev/galasa/textscan/TextScanner.html){target="_blank"} and [ITextScanner](https://galasa.dev/docs/reference/javadoc/dev/galasa/textscan/ITextScanner.html){target="_blank"} to find out more. |


### Log Scanner

| Annotation: | Log Scanner |
| --------------------------------------- | :------------------------------------- |
| Name: | `@LogScanner` |
| Description: | The `@LogScanner` annotation requests the Text Scan Manager to provide a log scanner instance. The log scanner maintains state and remembers where it last scanned, making it ideal for monitoring growing logs. |
| Syntax: | <pre lang="java">@LogScanner<br>public ILogScanner logScanner;<br></pre> |
| Notes: | The `ILogScanner` interface extends `ITextScanner` and adds the ability to remember scan positions, making it suitable for continuously monitoring log files that grow over time.<br><br> See [LogScanner](https://galasa.dev/docs/reference/javadoc/dev/galasa/textscan/LogScanner.html){target="_blank"} and [ILogScanner](https://galasa.dev/docs/reference/javadoc/dev/galasa/textscan/ILogScanner.html){target="_blank"} to find out more. |


## Code Snippets

### Scan text for a pattern

```java
@TextScanner
public ITextScanner textScanner;

@Test
public void testScanForPattern() throws Exception {
    String logContent = getLogContent();
    
    // Search for a pattern, fail if "ERROR" is found, expect at least 1 match
    Pattern searchPattern = Pattern.compile("SUCCESS.*completed");
    Pattern failPattern = Pattern.compile("ERROR");
    
    textScanner.scan(logContent, searchPattern, failPattern, 1);
    
    logger.info("Found expected success message");
}
```

### Scan for literal text

```java
@TextScanner
public ITextScanner textScanner;

@Test
public void testScanForLiteral() throws Exception {
    String output = getCommandOutput();
    
    // Search for exact text
    textScanner.scan(output, "Transaction completed successfully", "FAILED", 1);
}
```

### Scan an input stream

```java
@TextScanner
public ITextScanner textScanner;

@Test
public void testScanInputStream() throws Exception {
    InputStream logStream = getLogStream();
    
    // Scan input stream line by line (prevents heap overflow)
    Pattern pattern = Pattern.compile("Job.*completed");
    
    textScanner.scan(logStream, pattern, null, 1);
}
```

### Get the matched text

```java
@TextScanner
public ITextScanner textScanner;

@Test
public void testGetMatch() throws Exception {
    String logContent = getLogContent();
    
    // Get the first occurrence of the pattern
    Pattern pattern = Pattern.compile("Transaction ID: (\\d+)");
    String match = textScanner.scanForMatch(logContent, pattern, null, 1);
    
    logger.info("Found transaction: " + match);
}
```

### Scan with multiple expected occurrences

```java
@TextScanner
public ITextScanner textScanner;

@Test
public void testMultipleOccurrences() throws Exception {
    String logContent = getLogContent();
    
    // Expect at least 5 occurrences of the pattern
    Pattern pattern = Pattern.compile("Record processed");
    
    textScanner.scan(logContent, pattern, null, 5);
    
    logger.info("Found at least 5 processed records");
}
```

### Use log scanner for growing logs

```java
@LogScanner
public ILogScanner logScanner;

@Test
public void testMonitorLog() throws Exception {
    ITextScannable log = getGrowingLog();
    
    // First scan - establishes checkpoint
    logScanner.scan(log, "Application started", null, 1);
    
    // Perform some action that generates log entries
    performAction();
    
    // Second scan - only scans new content since last scan
    logScanner.scan(log, "Action completed", "ERROR", 1);
    
    logger.info("Action completed successfully");
}
```

### Scan a scannable object

```java
@TextScanner
public ITextScanner textScanner;

@ZosBatchJob
public IZosBatchJob batchJob;

@Test
public void testScanBatchJob() throws Exception {
    batchJob.submitJob();
    batchJob.waitForJob();
    
    // Scan the job output (IZosBatchJobOutput implements ITextScannable)
    IZosBatchJobOutput output = batchJob.getOutput();
    
    textScanner.scan(output, "JOB.*ENDED", "ABEND", 1);
}
```

### Handle scan exceptions

```java
@TextScanner
public ITextScanner textScanner;

@Test
public void testHandleExceptions() throws Exception {
    String logContent = getLogContent();
    
    try {
        textScanner.scan(logContent, "SUCCESS", "ERROR", 1);
    } catch (FailTextFoundException e) {
        logger.error("Found failure text: " + e.getMessage());
        throw e;
    } catch (MissingTextException e) {
        logger.error("Expected text not found: " + e.getMessage());
        throw e;
    } catch (IncorrectOccurrencesException e) {
        logger.error("Incorrect number of occurrences: " + e.getMessage());
        throw e;
    }
}
```

### Scan with no failure pattern

```java
@TextScanner
public ITextScanner textScanner;

@Test
public void testScanWithoutFailPattern() throws Exception {
    String output = getOutput();
    
    // Only search for success pattern, no failure check
    textScanner.scan(output, "COMPLETED", null, 1);
}
```

### Extract specific match occurrence

```java
@TextScanner
public ITextScanner textScanner;

@Test
public void testExtractOccurrence() throws Exception {
    String logContent = getLogContent();
    
    // Get the 3rd occurrence of the pattern
    Pattern pattern = Pattern.compile("User: (\\w+)");
    String thirdUser = textScanner.scanForMatch(logContent, pattern, null, 3);
    
    logger.info("Third user found: " + thirdUser);
}
```

### Fluent scanning

```java
@TextScanner
public ITextScanner textScanner;

@Test
public void testFluentScanning() throws Exception {
    String log1 = getLog1();
    String log2 = getLog2();
    
    // Chain multiple scans
    textScanner
        .scan(log1, "Started", null, 1)
        .scan(log2, "Completed", "Failed", 1);
    
    logger.info("Both logs validated successfully");
}
```


## Configuration Properties

The Text Scan Manager does not have any CPS properties to configure.


## Best Practices

1. **Use literal scans for exact matches** - When searching for exact text, use the literal string methods rather than escaping regex special characters.

2. **Set appropriate occurrence counts** - Use `1` for "at least one", or specify the exact number you expect.

3. **Use failure patterns** - Always specify a failure pattern when possible to catch errors early.

4. **Use ILogScanner for growing logs** - When monitoring logs that grow over time, use `ILogScanner` to avoid re-scanning the same content.

5. **Scan input streams for large files** - For very large files, use the `InputStream` scan methods to avoid loading the entire file into memory.

6. **Handle exceptions appropriately** - Catch specific exceptions to provide meaningful error messages in your tests.

7. **Use fluent interface** - Chain multiple scans together for cleaner code.