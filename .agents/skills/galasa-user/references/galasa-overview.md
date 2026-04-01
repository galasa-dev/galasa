---
name: galasa-overview
description: Provides an overview of what Galasa is, why it is needed, and its key features.
---

## What is Galasa?

Galasa is an open source **deep integration testing framework** for testing complex, interconnected enterprise systems. It specializes in:
- **Integration testing**: Multiple systems working together
- **End-to-end testing**: Complete business workflows
- **Mainframe testing**: z/OS, CICS, and other mainframe technologies
- **Automated testing**: No manual intervention required

Galasa is an [Open Mainframe Project](https://openmainframeproject.org/projects/galasa/) that enables deep integration testing across platforms and technologies within DevOps pipelines.

## Why Galasa is Different

Galasa differs from other test tools by:
- Supporting repeatable, reliable, agile testing at scale
- Enabling deep integration testing across platforms without stubbing or mocking
- Providing enterprise-level test management and reporting
- Being fully open-source with no vendor lock-in

## Key Features

### Consistent Testing for All Technologies

**Write once, run anywhere:**
- Write tests as JUnit-style Java classes
- Run locally using a CLI tool or in automation
- No code changes needed between environments

**Deep z/OS integration:**
- Verify data by interrogating CICS applications directly
- Check z/OS resources (queues, files, etc.) without stubs or mocking
- Native support for 3270 terminals, batch jobs, TSO commands

### Focus on Testing, Not Integration Problems

**Multi-technology integration:**
- One test case can interact with 3270, Selenium, JMeter, batch jobs, and more
- Framework handles integration complexity
- Tests focus on validation, not infrastructure

**DevOps integration:**
- Integrates easily into DevOps strategies
- Works alongside other test tools
- REST API for pipeline integration

### Fast Test Data Provisioning

**Test data management:**
- Integrate with test data strategy
- Provision new or existing test data records
- Lock test data for parallel test execution
- Repeat tests under identical conditions

**Automatic cleanup:**
- Provisioned environments automatically deprovisioned
- Clean test system state after completion
- Ready for next tests immediately

### Centralized Results and Artifacts

**Single location storage:**
- All test results in uniform style
- Easy extraction of big picture information
- Single place to search all test output
- Quick identification of failure causes

**Local debugging:**
- Run tests locally to assist debugging
- Access to all test artifacts
- Complete test history available

### Test Planning and Recording

**Test catalog:**
- Define areas under test
- Automate arduous manual tests
- Record what tests have run
- Plan what tests are left to do

**Test organization:**
- Categorize tests by function, type, priority
- Easy test selection and scheduling
- Comprehensive test coverage tracking

### Scalability and Extensibility

**Open source benefits:**
- Extend to support additional tooling
- No vendor lock-in
- No initial cost
- Community-driven development

**Enterprise throughput:**
- Scale horizontally in cloud environment
- Run thousands of tests in parallel
- Handle enterprise-level workloads
- Dedicated test execution environment

## Hybrid Cloud Testing

Galasa simplifies testing in hybrid cloud environments where applications span multiple platforms:

**Platform support:**
- z/OS mainframe systems
- Cloud native platforms
- Distributed systems
- Mixed technology stacks

**Technology support:**
- 3270 terminal emulation
- JCL batch jobs
- Selenium Web Driver
- REST APIs
- Message queues
- Databases

**End-to-end testing:**
- Test complete business workflows
- Validate across platform boundaries
- No stubbing or mocking required
- Real integration validation

## Use Galasa to:

- **Simplify test construction**: Build new integration tests easily and incorporate into regression suites
- **Validate against updates**: Quickly test applications against new middleware or OS releases
- **Deliver with confidence**: Deploy new functions fast with comprehensive testing
- **Verify system functionality**: Easily verify systems post-maintenance
- **Scale testing**: Run thousands of tests in parallel for faster feedback
- **Share test assets**: Centralize test code, data, and results across teams
- **Automate testing**: Run tests headlessly in CI/CD pipelines
- **Reduce technical debt**: Use proven Manager code instead of custom implementations

## Getting Started

To start using Galasa:
1. Install the Galasa CLI tool (galasactl)
2. Initialize your local environment
3. Create a Galasa project
4. Write and run tests
5. Optionally, set up a Galasa Ecosystem for enterprise-scale testing

For detailed instructions, see the other skill references in this skill.