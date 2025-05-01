# How to annotate test runs with a tag cloud

## What are tags ?
A tag is a piece of metadata which can be attached to a test, or a test run when it is launched, such that when a test result is made available, 
the result also has those tags.

Each tag is a string, which is free-form, and can contain any characters.

Tags are organised into un-ordered sets, so order is unimportant, and duplicates are ignored.

Tags can be useful to categorise test results when charting, it can help define which areas of solution code were tested by a passing or failing test, 
and can help to group tests together.

You can select a group of tests to run based on many factors like test class name, test package name, bundle name. You can also select a group of tests
from a test portfolio file based on the tags associated with tests. So, for example, you could choose to launch all the tests which have a certain tag.

## How to inject tags into your test results

There are 2 ways of annotating a test run with tags:
1. Send a collection of tags to the Galasa Service when the test is launched
2. Add fixed tags to the test source code using an annotation

We will discuss each of these methods in separate sections.

### Inject tags when a test is submitted to the Galasa Service
When you launch a test using the REST inteface, or with the `galasactl` command-line tool, you can specify a collection of tags using the `--tags` flag.

So, consider the command-line to launch a single test:

```shell
>galasactl runs submit \
--class dev.galasa.ivts/dev.galasa.ivts.core.CoreManagerIVT \
--stream ivts \
--group myGroup \
--tags tag1,tag2,tag3
```

Alternatively, one or more tags can be specified using multiple occurrences of the same `--tags` option:

```shell
>galasactl runs submit \
--class dev.galasa.ivts/dev.galasa.ivts.core.CoreManagerIVT \
--stream ivts \
--group myGroup \
--tags tag1 \
--tags tag2,tag3
```

When that test passes, and you query the test back from the Galasa service, you can see the list of tags in the output of the test run.

```shell
>galasactl runs get --age 1d
submitted-time(UTC) name requestor  status   result test-name                           group          tags
2025-04-28 09:47:46 U69  techcobweb finished Passed dev.galasa.ivts.core.CoreManagerIVT mcobbett-14988 tag1,tag2,tag3

Total:3 Passed:3
```

### Inject tags into the test code using the annotation

To inject tags into your java source code, you must 
- import the `Tags` annotation.
  ```java
  import dev.galasa.Tags;
  ```
- use the `@Tags` annotation immediately above your test class definition. For example: 
  ```java
  @Tags({"tag1","tag2","tag3"})
  public class myTest {
    ...
  }
  ```

If you then run the test, you will see `tag1`,`tag2` and `tag3` are tags on that test run.

Only one such annotation is allowed per test class.

All sources of tags for a test run are combined when the test runs. Duplicates are ignored.



