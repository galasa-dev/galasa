---
name: troubleshooting-galasa
description: How to troubleshoot common issues with Galasa
---

## Troubleshooting Common Issues

### Build Failures
- Ensure all manager dependencies are correctly specified in `build.gradle` or `pom.xml`
- Check that Java version is compatible (Java 17 or later recommended)

### Test Execution Failures
- Verify CPS properties are correctly configured in `~/.galasa/cps.properties`
- Ensure credentials are properly set in `~/.galasa/credentials.properties`
- Check that the OBR coordinates match your project's group ID and version
- Review `run.log` in the RAS directory for detailed error messages

### Manager Import Errors
- Run the build command after adding new manager dependencies
- Verify the manager artifact ID matches the documentation
- For z/OS 3270 manager, ensure both z/OS and z/OS 3270 managers are added

## Quick Reference Examples

### Common Manager Combinations
- **z/OS Testing**: z/OS Manager + z/OS 3270 Manager
- **CICS Testing**: CICS Region Manager + z/OS Manager + z/OS 3270 Terminal Manager
- **Web Testing**: HTTP Manager + Selenium Manager
