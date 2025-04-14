/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runs

// Dummy test run objects to use in unit testing.

const (
	// An active run that should be finished now.
	RUN_U123_FIRST_RUN = `{
		"runId": "xxx122xxx",
		"artifacts": [],
		"testStructure": {
			"runName": "U123",
			"bundle": "myBundleId",
			"testName": "myTestPackage.MyTestName",
			"testShortName": "MyTestName",
			"requestor": "unitTesting",
			"status" : "building",
			"queued" : "2023-05-10T06:00:00.000000Z",
			"startTime": "2023-05-10T06:00:10.000000Z",
			"methods": [{
				"className": "myTestPackage.MyTestName",
				"methodName": "myTestMethodName",
				"type": "test",
				"runLogStart":null,
				"runLogEnd":null,
				"befores":[], 
				"afters": []
			}]
		}
	}`
	// An active run
	RUN_U123_RE_RUN = `{
		"runId": "xxx123xxx",
		"artifacts": [],
		"testStructure": {
			"runName": "U123",
			"bundle": "myBundleId",
			"testName": "myTestPackage.MyTestName",
			"testShortName": "MyTestName",
			"requestor": "unitTesting",
			"status" : "building",
			"queued" : "2023-05-10T06:00:00.000000Z",
			"startTime": "2023-05-10T06:05:10.000000Z",
			"methods": [{
				"className": "myTestPackage.MyTestName",
				"methodName": "myTestMethodName",
				"type": "test",
				"runLogStart":null,
				"runLogEnd":null,
				"befores":[], 
				"afters": []
			}]
		}
	}`
	// Another active run
	RUN_U123_RE_RUN_2 = `{
		"runId": "xxx124xxx",
		"artifacts": [],
		"testStructure": {
			"runName": "U123",
			"bundle": "myBundleId",
			"testName": "myTestPackage.MyTestName",
			"testShortName": "MyTestName",
			"requestor": "unitTesting",
			"status" : "building",
			"queued" : "2023-05-10T06:00:00.000000Z",
			"startTime": "2023-05-10T10:10:10.000000Z",
			"methods": [{
				"className": "myTestPackage.MyTestName",
				"methodName": "myTestMethodName",
				"type": "test",
				"runLogStart":null,
				"runLogEnd":null,
				"befores":[], 
				"afters": []
			}]
		}
	}`
	// A finished run
	RUN_U120 = `{
		"runId": "xxx120xxx",
		"artifacts": [],
		"testStructure": {
			"runName": "U120",
			"bundle": "myBundleId",
			"testName": "myTestPackage.MyTestName",
			"testShortName": "MyTestName",
			"requestor": "unitTesting",
			"status" : "finished",
			"result": "Passed",
			"queued" : "2023-05-10T06:00:13.043037Z",
			"startTime": "2023-05-10T06:00:36.159003Z",
			"endTime": "2023-05-10T06:01:36.159003Z",
			"methods": [{
				"className": "myTestPackage.MyTestName",
				"methodName": "myTestMethodName",
				"type": "test",
				"status": "finished",
        		"result": "Passed",
				"startTime": "2023-05-10T06:00:36.159003Z",
				"endTime": "2023-05-10T06:01:36.159003Z",
				"runLogStart":0,
				"runLogEnd":0,
				"befores":[], 
				"afters": []
			}]
		}
	}`
	// Another finished run
	RUN_U121 = `{
		"runId": "xxx121xxx",
		"artifacts": [],
		"testStructure": {
			"runName": "U121",
			"bundle": "myBundleId",
			"testName": "myTestPackage.MyTestName",
			"testShortName": "MyTestName",
			"requestor": "unitTesting",
			"status" : "finished",
			"result": "Passed",
			"queued" : "2023-05-10T06:00:13.043037Z",
			"startTime": "2023-05-10T06:00:36.159003Z",
			"endTime": "2023-05-10T06:01:36.159003Z",
			"methods": [{
				"className": "myTestPackage.MyTestName",
				"methodName": "myTestMethodName",
				"type": "test",
				"status": "finished",
        		"result": "Passed",
				"startTime": "2023-05-10T06:00:36.159003Z",
				"endTime": "2023-05-10T06:01:36.159003Z",
				"runLogStart":0,
				"runLogEnd":0,
				"befores":[], 
				"afters": []
			}]
		}
	}`
)
