---
name: galasa-architecture
description: Explains Galasa's architecture including the core framework, managers, and test runner components.
---

## Galasa Architecture Overview

Galasa decomposes into three major components:

1. **The core Galasa framework** - Orchestrates all component activities and coordinates with the test runner
2. **A collection of Managers** - Provide interfaces to interact with various technologies
3. **A test runner** - Executes your tests under the direction of the core framework

## The Core Framework

The Galasa framework:
- Orchestrates all component activities
- Coordinates with the test runner to execute tests
- Automatically recognizes test definitions
- Launches required Managers and the test runner
- Provisions and executes tests without explicit invocation

**Key benefit**: You never have to write code to *invoke* your tests - you just write the code that defines them as test classes and methods.

## Managers

Managers serve two main purposes:
1. **Reduce boilerplate code** - Simplify test code by abstracting common functionality
2. **Provide proven tool interaction code** - Use battle-tested code for interacting with systems

### Manager Characteristics

- Can be general-purpose (e.g., HTTPClientManager) or focused (e.g., Db2Manager)
- Can collaborate with each other to perform joint tasks
- Share information and delegate tasks to other Managers
- Coordination is handled by the Galasa framework

### Types of Managers

**Core Managers**
- Central, fundamental Managers with wide-ranging use
- Examples: zosFileManager, zosBatchManager, zosCommandManager
- Part of the core Galasa distribution

**Product Managers**
- Responsible for test interactions with specific products
- Examples: CICSTSManager, Db2Manager
- Some are part of core distribution, others may be custom-written

**Ancillary Managers**
- Orchestrate integration with useful software tools and components
- Examples: SeleniumManager, JMeterManager, DockerManager
- Teams often write custom Managers for their specific tools

**Application-Specific Managers**
- Custom Managers for your specific application under test
- Abstract application-specific boilerplate functionality
- Remove repetitive code from tests themselves

### Available Managers

Core managers provided by Galasa:
- **z/OS Managers**: z/OS Batch, z/OS Console, z/OS File, z/OS Program, z/OS TSO, z/OS UNIX, z/OS 3270 Terminal
- **CICS TS Managers**: CICS Terminal, CICS Region, CICS Resource, CICS CECI, CICS CEDA, CICS CEMT
- **HTTP Manager**: HTTP client functionality
- **Artifact Manager**: Test artifact management
- **Core Manager**: Core Galasa functionality
- **Docker Manager**: Docker container manipulation
- **Kubernetes Manager**: Kubernetes resource management
- **Selenium Manager**: Web UI testing
- **JMeter Manager**: Performance testing

For the complete list of managers, see: https://raw.githubusercontent.com/galasa-dev/galasa/refs/heads/main/docs/content/docs/managers/index.md

## The Test Runner

The test runner:
- Executes your tests under the direction of the core framework
- Handles test lifecycle management
- Manages test execution in local JVM or ecosystem environments
- Coordinates with Managers to provision resources

## Platform and Technology Integration

**Platform Integration**
- Built with knowledge of z/OS and cloud native platforms
- Enables end-to-end testing across different platforms
- No stubbing or mocking required

**Technology Integration**
- Fully extensible framework
- Managers enable interaction with any test technology
- Examples: JMeter, Selenium, JCL, 3270 screens

**Pipeline Integration**
- Provides REST endpoint for CI/CD pipelines
- Can be called from Jenkins, GitLab CI, or any pipeline tool
- Enables fast, reliable testing at scale
- Can run thousands of tests in parallel

**Enterprise Integration**
- Test catalog for understanding and managing tests
- Single repository for test results and artifacts
- Native Kibana and Grafana dashboard support
- Open-source with no vendor lock-in