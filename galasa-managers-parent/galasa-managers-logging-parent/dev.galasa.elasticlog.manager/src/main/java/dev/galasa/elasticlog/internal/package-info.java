/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * ElasticLog Manager
 * 
 * @galasa.manager ElasticLog
 * 
 * @galasa.release.state ALPHA - This Manager is being actively developed. It is subject to change and has not been extensively tested.
 * It is available for Galasa administrators to utilise to experiment with Elasticsearch and Kibana dashboards
 * 
 * @galasa.description
 * 
 * This Manager exports test results to an elastic search endpoint, where the data can be visualized on a Kibana dashboard. 
 * Other Managers can contribute to the information that is exported to Elastic.
 * <br><br>
 * As an absolute minimum, the CPS properties <br>
 * <code>elasticlog.endpoint.address</code><br>and<br><code>elasticlog.endpoint.index</code><br>
 * must be provided. By default, this Manager only logs automated tests. To enable logging from locally run tests, <br>
 * <code>elasticlog.local.run.log</code> must be set to true.<br>
 * The bundle must also be loaded by the framework by using<br>
 * <code>framework.extra.bundles=dev.galasa.elasticlog.manager</code><br>
 * in bootstrap.properties.
 * <br><br>
 * This Manager provides two ElasticSearch indexes; one of all test data, and one of the latest run for each test case and each 
 * test environment.
 * 
 * @galasa.limitations
 * 
 * The Manager logs the following test information:<br>
 * <br>
 * - testCase<br>
 * - runId<br>
 * - startTimestamp<br>
 * - endTimestamp<br>
 * - requestor<br>
 * - result<br>
 * - testTooling<br>
 * - testType<br>
 * - testingEnvironment<br>
 * - productRelease<br>
 * - buildLevel<br>
 * - customBuild<br>
 * - testingAreas<br>
 * - tags<br>
 * <br>
 * If additional testing information is required, please raise a GitHub issue.<br><br>
 *
 * You can view the <a href="https://javadoc.galasa.dev/dev/galasa/elasticlog/manager/ivt/package-summary.html">Javadoc documentation for the Manager here</a>.
 * <br><br>
 * 
 */
package dev.galasa.elasticlog.internal;
