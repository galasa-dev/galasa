---
name: galasa-ecosystem
description: Explains the Galasa Ecosystem for running tests at scale, including stores, services, and key concepts.
---

## What is the Galasa Ecosystem?

The Galasa Ecosystem is a cloud native application that enables you to run automated testing away from your workstation, outside of a local JVM. It provides the full power of Galasa for enterprise-scale testing.

## Why Use the Ecosystem?

### Limitations of Local-Only Testing

Running tests only locally has several limitations:
- Configuration settings, test results and artifacts are stored locally and cannot be easily shared
- Tests cannot run headlessly - workstation must remain active
- Scaling is limited by workstation resources
- No monitoring and management features (test streams, test catalog, dashboards)

### Benefits of the Ecosystem

**Sharing tests across an enterprise**
- Scale horizontally to run large numbers of tests in parallel
- More testing completes in shorter timeframes
- Data is locked whilst in use, preventing cross-contamination
- Key differentiator from other test frameworks

**Re-usability**
- One person configures properties for use across all test runs
- Configurations maintained in single location (CPS)
- Single source of truth for test configurations
- Test results, logs, and artifacts stored centrally
- Easy sharing across teams

**Testing as a service**
- Run regression tests, application tests, system verification tests on demand
- Integrate with DevOps pipelines
- Run in dedicated cloud environment
- Workload separated from CI pipeline
- Prevents resource diversion from other pipeline jobs

**Automated test runs**
- Test catalog stores related tests in shared location
- Tests automatically selected for any given change set
- Uses latest version of test cases
- No need for local test material

## Ecosystem Architecture

The Ecosystem is made up of:
- Collection of microservices for orchestrating runtimes
- Monitoring services for tests and resources
- Resource cleanup services
- Centralized store for run configurations
- Single location for all test results and artifacts
- REST endpoint callable from any IDE or pipeline

## Key Components

### Galasa Stores

**Configuration Property Store (CPS)**
- Defines object properties, topologies, system configurations
- Instructs how Galasa tests run
- Contains properties for endpoints, ports, timeouts
- All tests use same CPS configuration (unless overridden at submission)
- Enables tests to run against multiple environments without code changes
- **Security note**: Hard drive encryption recommended as IP addresses and ports are stored

**Dynamic Status Store (DSS)**
- Provides status information about ecosystem and running tests
- Used by resource manager and engine controller
- Ensures CPS limits are not exceeded
- Property values change dynamically as tests run
- Shows resources currently being used, shared, or locked
- Prevents throttling by limiting workloads
- Shared by every framework instance in automation

**Result Archive Store (RAS)**
- Single database storing all test elements
- Contains test results, run logs, test artifacts
- Helps diagnose failures
- Gathers information about tests
- Enables entire teams to view results in one place

**Credentials Store (CREDs)**
- Securely provides credentials for test automation
- Stores passwords, usernames, personal access tokens
- Supports KeyStore credentials (PKCS12, JKS format) for SSL/TLS certificates
- Hosted in etcd server
- Validated during credential creation

**etcd Server**
- Highly available key-value pair store
- Hosts CPS, DSS, and CREDs
- Maintains single, consistent source of truth
- Tracks ecosystem status at any given point in time

**CouchDB**
- Runs inside Docker container or Kubernetes pod
- Contains the Result Archive Store (RAS)

### Galasa Services

**Engine Controller**
- Enables tests to run at scale
- Instantiates individual test engines
- Creates Docker containers or Kubernetes pods for test runs
- Allocates test engine if required resources are available
- Puts tests in waiting state if resources unavailable

**Resource Management**
- Monitors running tests and in-use resources
- Performs cleanup if test becomes stale or is ended manually
- Returns resources to pool for other tests
- Can de-provision entire environments

**Metrics Server**
- Indicates ecosystem health
- Provides metrics on successful test runs
- Tracks throughput and performance

**API Server**
- Central point for controlling Galasa Ecosystem
- Endpoint for IDEs and pipelines
- Used for submitting tests and retrieving results
- Hosts the bootstrap server

**Bootstrap Server**
- Part of the API server
- Stores initial configuration for instantiating Galasa framework
- IDE must point to bootstrap configured for ecosystem

**Galasa Web UI**
- Currently under construction (planned for future release)
- Dashboard overview of current and historical health
- Run, schedule, or reschedule tests
- Analyze output from failed test runs
- Manage configuration for maximum throughput, resilience, flexibility

**Dex**
- Authenticates users interacting with Galasa Ecosystem

### Code Deployment

**Maven Repositories and OBRs**
- Tests require compiled artifacts hosted in Maven repository
- Artifacts must be bundled as OSGI bundles
- Galasa provides Maven plug-in to create bundles

**Nexus**
- Enables deployment of Maven artifacts to ecosystem
- Can host Docker images
- Alternative internal artifact repositories can be used

## Key Concepts

### Universal Concepts (Local and Remote)

**Test Case**
- Piece of test logic that can be compiled or translated
- Runnable by Galasa framework
- Can be Java code or Gherkin test feature

**Test Run (Run)**
- Execution of a test case
- Started at specific point in time
- Executes logic steps
- Either still running or finished with status and result (Passed/Failed)

### Ecosystem-Only Concepts

**User**
- Authorized person manipulating the Galasa system

**Role Based Access Control (RBAC)**
- Mechanism to allow/limit user actions
- Some users can perform any task
- Other users have limited capabilities
- Helps isolate system capabilities for safety and security

**Test Streams**
- Organize and manage test execution
- Can be created and updated via REST API or CLI
- Enable automated test selection and scheduling