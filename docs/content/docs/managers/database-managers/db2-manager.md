---
title: "DB2 Manager"
---

You can view the [Javadoc documentation for the Manager](../../reference/javadoc/dev/galasa/db2/package-summary.html){target="_blank"}.


## Overview

The DB2 Manager provides Galasa tests with the capability to connect to and interact with DB2 databases. This Manager enables tests to execute SQL statements, manage database schemas, and retrieve query results for validation.

The DB2 Manager provides two main components: DB2 instances for database connections and DB2 schemas for managing database schemas with default schema context.


## Annotations

The following annotations are available with the DB2 Manager.


### DB2 Instance

| Annotation: | DB2 Instance |
| --------------------------------------- | :------------------------------------- |
| Name: | `@Db2Instance` |
| Description: | The `@Db2Instance` annotation requests a connection to a DB2 database with a specified tag. This provides access to the database connection for executing SQL statements. |
| Attribute: `tag` |  The tag identifies the DB2 instance. If multiple DB2 instances are required, each must have a unique tag. Default is "PRIMARY". |
| Syntax: | <pre lang="java">@Db2Instance(tag = "PRIMARY")<br>public IDb2Instance db2;<br></pre> |
| Notes: | The `IDb2Instance` interface provides access to a standard `java.sql.Connection` object. This connection can be used to interact with the DB2 database using standard JDBC operations.<br><br> See [Db2Instance](https://galasa.dev/docs/reference/javadoc/dev/galasa/db2/Db2Instance.html){target="_blank"} and [IDb2Instance](https://galasa.dev/docs/reference/javadoc/dev/galasa/db2/IDb2Instance.html){target="_blank"} to find out more. |


### DB2 Schema

| Annotation: | DB2 Schema |
| --------------------------------------- | :------------------------------------- |
| Name: | `@Db2Schema` |
| Description: | The `@Db2Schema` annotation requests a schema to be created or accessed on a tagged DB2 instance. This provides a convenient interface for executing SQL statements within a specific schema context. |
| Attribute: `tag` |  The tag identifies the schema. Default is "PRIMARY". |
| Attribute: `db2Tag` |  The tag of the DB2 instance to connect to. Default is "PRIMARY". |
| Attribute: `archive` |  Whether to archive query results. Default is `false`. |
| Attribute: `resultSetType` |  The ResultSet type for queries. Default is `ResultSet.TYPE_SCROLL_INSENSITIVE`. |
| Attribute: `resultSetConcurrency` |  The ResultSet concurrency mode. Default is `ResultSet.CONCUR_READ_ONLY`. |
| Syntax: | <pre lang="java">@Db2Schema(tag = "PRIMARY", db2Tag = "PRIMARY", archive = true)<br>public IDb2Schema schema;<br></pre> |
| Notes: | The `IDb2Schema` interface provides methods to execute SQL statements and retrieve results. Statements that don't specify a schema will use this schema as the default.<br><br> See [Db2Schema](https://galasa.dev/docs/reference/javadoc/dev/galasa/db2/Db2Schema.html){target="_blank"} and [IDb2Schema](https://galasa.dev/docs/reference/javadoc/dev/galasa/db2/IDb2Schema.html){target="_blank"} to find out more. |


## Code Snippets

### Connect to a DB2 database

```java
@Db2Instance(tag = "PRIMARY")
public IDb2Instance db2;

@Test
public void testDb2Connection() throws Exception {
    // Get the standard JDBC connection
    Connection connection = db2.getConnection();
    
    // Get database name
    String dbName = db2.getDatabaseName();
    logger.info("Connected to database: " + dbName);
    
    // Use the connection for standard JDBC operations
    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT * FROM SYSIBM.SYSDUMMY1");
    
    while (rs.next()) {
        logger.info("Result: " + rs.getString(1));
    }
}
```

### Use a DB2 schema

```java
@Db2Schema(tag = "PRIMARY", db2Tag = "PRIMARY")
public IDb2Schema schema;

@Test
public void testSchemaOperations() throws Exception {
    // Get the schema name
    String schemaName = schema.getSchemaName();
    logger.info("Using schema: " + schemaName);
    
    // Execute a simple SQL statement
    IResultMap result = schema.executeSql("SELECT COUNT(*) AS CNT FROM MYTABLE");
    
    int count = result.getIntegerValue("CNT");
    logger.info("Table has " + count + " rows");
}
```

### Execute SQL with parameters

```java
@Db2Schema(tag = "PRIMARY", db2Tag = "PRIMARY")
public IDb2Schema schema;

@Test
public void testParameterizedQuery() throws Exception {
    // Execute SQL with parameters
    String sql = "SELECT * FROM EMPLOYEES WHERE DEPT = ? AND SALARY > ?";
    IResultMap result = schema.executeSql(sql, "IT", 50000);
    
    if (result != null) {
        String name = result.getStringValue("NAME");
        int salary = result.getIntegerValue("SALARY");
        logger.info("Found employee: " + name + " with salary: " + salary);
    }
}
```

### Execute SQL returning multiple rows

```java
@Db2Schema(tag = "PRIMARY", db2Tag = "PRIMARY")
public IDb2Schema schema;

@Test
public void testMultipleRows() throws Exception {
    // Execute SQL that returns multiple rows
    String sql = "SELECT NAME, SALARY FROM EMPLOYEES WHERE DEPT = ?";
    List<IResultMap> results = schema.executeSqlList(sql, "IT");
    
    logger.info("Found " + results.size() + " employees");
    
    for (IResultMap result : results) {
        String name = result.getStringValue("NAME");
        int salary = result.getIntegerValue("SALARY");
        logger.info("Employee: " + name + ", Salary: " + salary);
    }
}
```

### Execute SQL from a file

```java
@Db2Schema(tag = "PRIMARY", db2Tag = "PRIMARY")
public IDb2Schema schema;

@Test
public void testSqlFile() throws Exception {
    // Load SQL statements from a file
    InputStream sqlFile = getClass().getResourceAsStream("/sql/setup.sql");
    
    // Execute all statements in the file (line-separated)
    List<IResultMap> results = schema.executeSqlFile(sqlFile);
    
    logger.info("Executed " + results.size() + " SQL statements");
}
```

### Insert data

```java
@Db2Schema(tag = "PRIMARY", db2Tag = "PRIMARY")
public IDb2Schema schema;

@Test
public void testInsertData() throws Exception {
    // Insert a new record
    String sql = "INSERT INTO EMPLOYEES (ID, NAME, DEPT, SALARY) VALUES (?, ?, ?, ?)";
    schema.executeSql(sql, 1001, "John Doe", "IT", 75000);
    
    // Verify the insert
    IResultMap result = schema.executeSql(
        "SELECT NAME FROM EMPLOYEES WHERE ID = ?", 1001
    );
    
    String name = result.getStringValue("NAME");
    assertThat(name).isEqualTo("John Doe");
}
```

### Update data

```java
@Db2Schema(tag = "PRIMARY", db2Tag = "PRIMARY")
public IDb2Schema schema;

@Test
public void testUpdateData() throws Exception {
    // Update a record
    String sql = "UPDATE EMPLOYEES SET SALARY = ? WHERE ID = ?";
    schema.executeSql(sql, 80000, 1001);
    
    // Verify the update
    IResultMap result = schema.executeSql(
        "SELECT SALARY FROM EMPLOYEES WHERE ID = ?", 1001
    );
    
    int salary = result.getIntegerValue("SALARY");
    assertThat(salary).isEqualTo(80000);
}
```

### Delete data

```java
@Db2Schema(tag = "PRIMARY", db2Tag = "PRIMARY")
public IDb2Schema schema;

@Test
public void testDeleteData() throws Exception {
    // Delete a record
    String sql = "DELETE FROM EMPLOYEES WHERE ID = ?";
    schema.executeSql(sql, 1001);
    
    // Verify the delete
    List<IResultMap> results = schema.executeSqlList(
        "SELECT * FROM EMPLOYEES WHERE ID = ?", 1001
    );
    
    assertThat(results).isEmpty();
}
```

### Use multiple DB2 instances

```java
@Db2Instance(tag = "PRIMARY")
public IDb2Instance primaryDb;

@Db2Instance(tag = "SECONDARY")
public IDb2Instance secondaryDb;

@Db2Schema(tag = "PRIMARY_SCHEMA", db2Tag = "PRIMARY")
public IDb2Schema primarySchema;

@Db2Schema(tag = "SECONDARY_SCHEMA", db2Tag = "SECONDARY")
public IDb2Schema secondarySchema;

@Test
public void testMultipleDatabases() throws Exception {
    // Query from primary database
    IResultMap primaryResult = primarySchema.executeSql(
        "SELECT COUNT(*) AS CNT FROM ORDERS"
    );
    
    // Query from secondary database
    IResultMap secondaryResult = secondarySchema.executeSql(
        "SELECT COUNT(*) AS CNT FROM CUSTOMERS"
    );
    
    logger.info("Primary DB orders: " + primaryResult.getIntegerValue("CNT"));
    logger.info("Secondary DB customers: " + secondaryResult.getIntegerValue("CNT"));
}
```

### Archive query results

```java
@Db2Schema(tag = "PRIMARY", db2Tag = "PRIMARY", archive = true)
public IDb2Schema schema;

@Test
public void testWithArchiving() throws Exception {
    // When archive=true, query results are automatically stored
    // in the test results archive
    List<IResultMap> results = schema.executeSqlList(
        "SELECT * FROM EMPLOYEES WHERE DEPT = ?", "IT"
    );
    
    // Results are archived for later analysis
    logger.info("Query returned " + results.size() + " rows (archived)");
}
```

### Handle null values

```java
@Db2Schema(tag = "PRIMARY", db2Tag = "PRIMARY")
public IDb2Schema schema;

@Test
public void testNullValues() throws Exception {
    IResultMap result = schema.executeSql(
        "SELECT NAME, MANAGER_ID FROM EMPLOYEES WHERE ID = ?", 1001
    );
    
    String name = result.getStringValue("NAME");
    
    // Check if a value is null
    if (result.isNull("MANAGER_ID")) {
        logger.info(name + " has no manager");
    } else {
        int managerId = result.getIntegerValue("MANAGER_ID");
        logger.info(name + " reports to manager ID: " + managerId);
    }
}
```


## Methods

### IDb2Instance Methods

```java
Connection getConnection()
```

Returns the standard JDBC connection to the DB2 database. This can be used for any JDBC operations.

**Returns:** A `java.sql.Connection` object

---

```java
String getDatabaseName() throws Db2ManagerException
```

Returns the name of the database.

**Returns:** The database name

### IDb2Schema Methods

```java
IResultMap executeSql(String stmt, Object... params) throws Db2ManagerException
```

Executes a single SQL statement with optional parameters. Expects a single row result or no result.

**Parameters:**
- `stmt` - The SQL statement to execute
- `params` - Optional parameters for the SQL statement

**Returns:** An `IResultMap` containing the result row, or null if no results

---

```java
List<IResultMap> executeSqlList(String stmt, Object... params) throws Db2ManagerException
```

Executes a single SQL statement with optional parameters. Expects multiple rows to be returned.

**Parameters:**
- `stmt` - The SQL statement to execute
- `params` - Optional parameters for the SQL statement

**Returns:** A `List<IResultMap>` containing all result rows

---

```java
List<IResultMap> executeSqlFile(InputStream in) throws Db2ManagerException
```

Executes a file of SQL statements that are line-separated.

**Parameters:**
- `in` - An InputStream containing the SQL statements

**Returns:** A `List<IResultMap>` containing results from all statements

---

```java
String getSchemaName()
```

Returns the name of the schema.

**Returns:** The schema name


## Configuration Properties

The DB2 Manager requires configuration properties to connect to DB2 databases. These properties must be set in the Configuration Property Store (CPS).

### DB2 Instance Connection Properties

The following properties are typically required for each DB2 instance:

| Property: | DB2 JDBC URL |
| --------------------------------------- | :------------------------------------- |
| Name: | `db2.[tag].jdbc.url` |
| Description: | The JDBC URL for connecting to the DB2 database. |
| Required:  | Yes |
| Valid values: | A valid DB2 JDBC URL |
| Examples: | `db2.PRIMARY.jdbc.url=jdbc:db2://hostname:50000/DBNAME` |

| Property: | DB2 Username |
| --------------------------------------- | :------------------------------------- |
| Name: | `db2.[tag].credentials` |
| Description: | The credentials ID for accessing the DB2 database. The credentials should be stored in the Galasa Credentials Store. |
| Required:  | Yes |
| Valid values: | A valid credentials ID |
| Examples: | `db2.PRIMARY.credentials=...` |

| Property: | DB2 Driver Class |
| --------------------------------------- | :------------------------------------- |
| Name: | `db2.[tag].driver.class` |
| Description: | The JDBC driver class name for DB2. |
| Required:  | No |
| Default value: | `com.ibm.db2.jcc.DB2Driver` |
| Valid values: | A valid JDBC driver class name |
| Examples: | `db2.PRIMARY.driver.class=com.ibm.db2.jcc.DB2Driver` |


## Best Practices

1. **Use schemas for organized testing** - Use `@Db2Schema` instead of raw connections when possible for cleaner code and automatic schema context.

2. **Use parameterized queries** - Always use parameterized queries to prevent SQL injection and improve performance.

3. **Archive important results** - Set `archive=true` on schemas when you need to preserve query results for analysis.

4. **Clean up test data** - Always clean up any test data created during tests to avoid affecting other tests.

5. **Use transactions** - For tests that modify data, consider using transactions that can be rolled back.

6. **Handle null values** - Always check for null values in query results before accessing them.

7. **Use appropriate ResultSet types** - Choose the appropriate `resultSetType` and `resultSetConcurrency` for your use case.

8. **Close resources** - The DB2 Manager automatically closes connections, but if you create additional JDBC resources, ensure they are properly closed.