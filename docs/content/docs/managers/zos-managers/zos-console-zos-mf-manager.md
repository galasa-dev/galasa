---
title: "zOS Console zOS MF Manager"
---

This Manager is at Beta level. You can view the <a href="https://javadoc.galasa.dev/overview-summary.html" target="_blank" rel="noopener noreferrer">Javadoc documentation for the Manager here</a>.<br>

# <a name="overview"></a>Overview
This Manager is the internal implementation of the z/OS Console Manager using zOS/MF. The z/OS MF Console Manager is used in conjunction with the z/OS Manager. The z/OS Manager provides the interface for the z/OS console function and pulls in the z/OS MF Console Manager to provide the implementation of the interface. If your test needs to request a z/OS console instance, issue a console command or retrieve the console command, you can call the z/OS Manager in your test code and the z/OS Manager will call the z/OS MF Console Manager to provide the implementation via the z/OS console function. Multiple z/OS console images can be requested by a test. <p> See the <a href="/docs/managers/zos-manager">zOS Manager</a> for details of the z/OS Console Annotations.





