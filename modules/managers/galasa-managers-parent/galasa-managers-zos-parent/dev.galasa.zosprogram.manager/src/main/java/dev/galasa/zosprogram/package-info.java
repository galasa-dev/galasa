/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * zOS Program Manager
 * 
 * @galasa.manager zOS Program
 * 
 * @galasa.release.state ALPHA - This Manager is being actively developed. It is subject to change and has not been extensively tested.
 * 
 * @galasa.description
 * 
 * This Manager allows Galasa tests to compile and link zOS Programs.
 * 
 * The <code>@ZosProgram</code> annotation defines the program to the Galasa test. Program attributes, for
 * example, program name, programming language and source location are specified by using the annotation 
 * elements.
 * The source for the program is stored as a resource, along with the test. The z/OS Program Manager processes 
 * each <code>@ZosProgram</code> annotation before any of the test methods are executed. The Manager 
 * retrieves the source from the test bundle, builds and submits the relevant compile and link JCL based on 
 * the programs attributes and CPS properties. The batch job is saved with the test run archive. The 
 * program can be executed in the test by retrieving the library containing the load module by using 
 * the <code>getLoadLibrary()</code> method.
 * 
 * The Simbank tutorial <a href="/docs/running-simbank-tests/batch-accounts-open-test">BatchAccountsOpenTest</a>BatchAccountsOpenTest 
 * contains an example of running a simulated z/OS program called SIMBANK by using the <code>EXEC PGM=SIMBANK</code> command.
 * 
 * You can view the <a href="https://javadoc.galasa.dev/dev/galasa/zosprogram/package-summary.html" target="_blank" >Javadoc documentation for the Manager here</a>. <br><br>
 * 
 */
package dev.galasa.zosprogram;