# Galasa JMeter Manager

The Galasa JMeter Manager provides integration with Apache JMeter for performance testing within Galasa test automation.

## Features

- Execute JMeter test plans (.jmx files) from Galasa tests
- Support for Velocity template substitution for dynamic parameters
- Two execution modes: LOCAL (default) and DOCKER
- Automatic result archiving to RAS (Result Archive Store)
- Retrieve test results (JTL files, logs, console output)

## Quick Start

### Basic Usage

```java
@Test
public class MyPerformanceTest {
    
    @JMeterSession(jmxPath = "test-plan.jmx")
    public IJMeterSession jmeter;
    
    @Test
    public void testPerformance() throws Exception {
        // Load JMX with parameters
        Map<String, Object> params = new HashMap<>();
        params.put("HOST", "example.com");
        params.put("THREADS", "10");
        params.put("DURATION", "60");
        
        InputStream jmx = getClass().getResourceAsStream("/test-plan.jmx");
        jmeter.setChangedParametersJmxFile(jmx, params);
        
        // Execute test with 120 second (120000ms) timeout
        jmeter.startJmeter(120000);
        
        // Verify success
        assertTrue(jmeter.isTestSuccessful());
        
        // Get results
        String results = jmeter.getListenerFile("test-plan.jtl");
    }
}
```

## Execution Modes

### LOCAL Mode (Default - Recommended)

Runs JMeter using external binary installation. No Docker required.

**Configuration:**
```properties
jmeter.execution.mode=LOCAL
jmeter.binary.path=/opt/apache-jmeter-5.6.3/bin/jmeter  # Required - path to JMeter binary
```

**Advantages:**
- No Docker dependency
- Faster startup (~2-3 seconds)
- Simpler configuration
- Works in restricted environments

### DOCKER Mode

Runs JMeter in Docker containers using `galasadev/galasa-jmeter:latest` image.

**Configuration:**
```properties
jmeter.execution.mode=DOCKER
docker.default.engines=PRIMARY
docker.engine.PRIMARY.hostname=localhost
docker.engine.port=2375
```

Or when using a remote Docker engine:
```properties
jmeter.execution.mode=DOCKER
docker.default.engines=PRIMARY
docker.engine.PRIMARY.hostname=1.10.100.100
docker.engine.PRIMARY.port=2376
docker.engine.PRIMARY.max.slots=3
```

**Requirements:**
- Docker Manager must be available
- Docker Engine accessible
- JMeter Docker image available (access to Dockerhub)

**Advantages:**
- Isolated execution environment
- Consistent JMeter version

## Configuration Properties

### jmeter.execution.mode

Controls execution mode.

**Values:** `LOCAL` (default) | `DOCKER`

```properties
jmeter.execution.mode=LOCAL
```

### jmeter.binary.path

Path to JMeter binary (LOCAL mode only, required).

```properties
jmeter.binary.path=/opt/apache-jmeter-5.6.3/bin/jmeter
```

Must point to a valid JMeter binary such as `jmeter` (Unix/Linux/Mac) or `jmeter.bat` (Windows).

## Testing

### Run JMeterManagerIVT

```bash
galasactl runs submit local --log - \
--obr mvn:dev.galasa/dev.galasa.uber.obr/1.0.0/obr \
--class dev.galasa.jmeter.manager.ivt/dev.galasa.jmeter.manager.ivt.JMeterManagerIVT
```

## Troubleshooting

### LOCAL Mode

**Issue:** JMeter execution timeout
**Solution:** Increase timeout: `jmeter.startJmeter(180000)` (3 minutes = 180000ms)

**Issue:** Permission denied on JMeter script  
**Solution:** Manager automatically sets execute permissions on Unix/Linux/Mac

### DOCKER Mode

**Issue:** Docker Manager not available  
**Solution:** Ensure Docker Manager in dependencies and Docker running

**Issue:** Container image not found  
**Solution:** Pull image: `docker pull galasadev/galasa-jmeter:latest`

### Both Modes

**Issue:** Test fails but no clear error  
**Solution:** Check logs:
```java
String log = jmeter.getLogFile();
String console = jmeter.getConsoleOutput();
logger.info("JMeter log: " + log);
logger.info("Console: " + console);
```
