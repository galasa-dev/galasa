/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * zOS Console Manager - oeconsol Internal Implementation
 * 
 * @galasa.manager zOS Console oeconsol
 * 
 * @galasa.release.state ALPHA - This Manager is being actively developed. It is subject to change and has not been extensively tested.
 * 
 * @galasa.description
 * 
* This Manager is the internal implementation of the z/OS Console Manager using <b>oeconsol</b>. The <b>oeconsol</b> z/OS Console Manager is used in conjunction 
* with the z/OS UNIX Command Manager. The z/OS Manager provides the interface for the z/OS console function and pulls in the <b>oeconsol</b> Console Manager 
* to provide the implementation of the interface. If your test needs to request a z/OS console instance, issue a console command or retrieve the console 
* command, you can call the z/OS Manager in your test code and the z/OS Manager will call the <b>oeconsol</b> Console Manager to provide the implementation 
* via the z/OS console function. Multiple z/OS console images can be requested by a test.
* <p>
* See the <a href="/docs/managers/zos-manager">zOS Manager</a> for details of the z/OS Console Annotations.
* <p>
* This implementation is less rich than the zOS/MF implementation due to the restricted functionallity of <b>oeconsol</b>:
* <p>
* <ul>
*   <li><b>oeconsol</b> does not directly support console name.
*     <br>Console name can be used to avoid clashes with other consoles that the user has open, e.g. in another Galsa test, in a TSO or SDSF session.
*         When supplying console name via <code>IZosConsole#issueCommand(String command, String consoleName)</code> the Manager attempts to obtain credentials
*         from the CPS, i.e. <code>secure.credentials.[consoleName].username</code>. The credentials are used to logon to z/OS UNIX and execute <b>oeconsol</b> with the supplied command.
*   </li>
*   <li><b>oeconsol</b> does not support retrieving delayed responses.
*     <br>A <code>ZosConsoleException</code> is will be thrown when the <code>IZosConsoleCommand#requestResponse()</code> method is called.
*   </li>
* </ul>   
* <p>
* See <a href="https://github.com/IBM/IBM-Z-zOS/tree/main/zOS-Tools-and-Toys/oeconsol">oeconsol</a> for documentation and download
 */
package dev.galasa.zosconsole.oeconsol.manager;
