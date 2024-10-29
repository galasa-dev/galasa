
# Gherkin syntax

```
1a. <feature> ::= FEATURE_START <scenarioPartList> END_OF_FILE
2b. <scenarioPartList> ::= null
2c.                     | <scenarioPart> <scenarioPartList>
3a. <scenarioPart> ::= <scenarioOutline>
3b.                 | <scenario>
4a. <scenario> ::= SCENARIO_START <stepList>
5a. <scenarioOutline> ::= SCENARIO_OUTLINE_START <stepList> EXAMPLES_START <dataTable>
6a. <stepList> ::= null
6b.             | STEP <stepList>
7a. <dataTable> ::= <dataHeaderLine> <dataValuesLineList>
8a. <dataTableHeader> ::= DATA_LINE
9a. <dataTableValuesLineList> ::= null
9b.                       | DATA_LINE <dataTableValuesLineList>
```


