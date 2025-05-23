---
path: "/docs/managers/z-os-file-zos-mf-manager"
title: "z/OS File zOS/MF Manager"
---

This Manager is at Beta level. You can view the <a href="https://javadoc.galasa.dev/overview-summary.html">Javadoc documentation for the Manager here</a>.<br>


# <a name="overview"></a>Overview
This Manager is an internal implementation of the z/OS File Manager using zOS/MF. The z/OS MF File Manager  is used in conjunction with the z/OS Manager. The z/OS Manager provides the interface for the z/OS file  function and pulls in the z/OS MF File Manager to provide the implementation of the interface. If your  test needs to instantiate a UNIX file, dataset, or VSAM data set, write and retrieve content from it,  or configure and manipulate it then you can call the z/OS Manager in your test code and the z/OS Manager  will call the z/OS MF File Manager to provide the implementation via the z/OS file function.  <p> The z/OS File zOS/MF Manager is enabled by setting the CPS property:<br> <code>zos.bundle.extra.file.manager=dev.galasa.zosfile.rseapi.manager</code><br> Galasa sets this property by default. <p>  See the <a href="/docs/managers/zos-manager">zOS Manager</a> for details of the z/OS File Annotations.





