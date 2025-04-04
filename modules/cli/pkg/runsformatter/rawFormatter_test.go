/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package runsformatter

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func createFormattableTestForRaw(runId string,
	name string,
	status string,
	result string,
	bundle string,
	testName string,
	requestor string,
	queuedTimeUTC string,
	startTimeUTC string,
	endTimeUTC string,
	apiServerUrl string,
	isLost bool,
	group string,
) FormattableTest {
	formattableTest := FormattableTest{
		RunId:         runId,
		Name:          name,
		TestName:      testName,
		Status:        status,
		Result:        result,
		StartTimeUTC:  startTimeUTC,
		EndTimeUTC:    endTimeUTC,
		QueuedTimeUTC: queuedTimeUTC,
		Requestor:     requestor,
		Bundle:        bundle,
		ApiServerUrl:  apiServerUrl,
		Group:         group,
		Lost:          isLost,
	}
	return formattableTest
}

func TestRawFormatterNoDataReturnsNothing(t *testing.T) {

	formatter := NewRawFormatter()
	// No data to format...
	formattableTest := make([]FormattableTest, 0)

	// When...
	actualFormattedOutput, err := formatter.FormatRuns(formattableTest)

	assert.Nil(t, err)
	expectedFormattedOutput := ""
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestRawFormatterReturnsExpectedFormat(t *testing.T) {
	formatter := NewRawFormatter()

	formattableTest := make([]FormattableTest, 0)
	formattableTest1 := createFormattableTestForRaw("cbd-123", "U456", "Finished", "Passed", "dev.galasa", "dev.galasa.Zos3270LocalJava11Ubuntu", "galasa", "2023-05-04T10:55:29.545323Z", "2023-05-05T06:00:14.496953Z", "2023-05-05T06:00:15.654565Z", "https://127.0.0.1", false, "none")
	formattableTest = append(formattableTest, formattableTest1)

	// When...
	actualFormattedOutput, err := formatter.FormatRuns(formattableTest)

	assert.Nil(t, err)
	expectedFormattedOutput := "U456|Finished|Passed|2023-05-04T10:55:29.545323Z|2023-05-05T06:00:14.496953Z|2023-05-05T06:00:15.654565Z|1157|dev.galasa.Zos3270LocalJava11Ubuntu|galasa|dev.galasa|none|https://127.0.0.1/ras/runs/cbd-123/runlog\n"

	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestRawFormatterWithMultipleFormattableTestsSeparatesWithNewLine(t *testing.T) {
	formatter := NewRawFormatter()

	formattableTest := make([]FormattableTest, 0)
	formattableTest1 := createFormattableTestForRaw("cbd-123", "U123", "Finished", "Passed", "dev.galasa", "dev.galasa.Zos3270LocalJava11Ubuntu", "galasa", "2023-05-04T10:55:29.545323Z", "2023-05-05T06:00:14.496953Z", "2023-05-05T06:00:15.654565Z", "https://127.0.0.1", false, "none")
	formattableTest2 := createFormattableTestForRaw("cbd-456", "U456", "Finished", "Passed", "dev.galasa", "dev.galasa.Zos3270LocalJava11Ubuntu", "galasa", "2023-05-04T10:55:29.545323Z", "2023-05-05T06:00:14.496953Z", "2023-05-05T06:00:15.654565Z", "https://127.0.0.1", false, "none")
	formattableTest3 := createFormattableTestForRaw("cbd-789", "U789", "Finished", "Passed", "dev.galasa", "dev.galasa.Zos3270LocalJava11Ubuntu", "galasa", "2023-05-04T10:55:29.545323Z", "2023-05-05T06:00:14.496953Z", "2023-05-05T06:00:15.654565Z", "https://127.0.0.1", false, "none")
	formattableTest = append(formattableTest, formattableTest1, formattableTest2, formattableTest3)

	// When...
	actualFormattedOutput, err := formatter.FormatRuns(formattableTest)

	assert.Nil(t, err)
	expectedFormattedOutput :=
		"U123|Finished|Passed|2023-05-04T10:55:29.545323Z|2023-05-05T06:00:14.496953Z|2023-05-05T06:00:15.654565Z|1157|dev.galasa.Zos3270LocalJava11Ubuntu|galasa|dev.galasa|none|https://127.0.0.1/ras/runs/cbd-123/runlog\n" +
			"U456|Finished|Passed|2023-05-04T10:55:29.545323Z|2023-05-05T06:00:14.496953Z|2023-05-05T06:00:15.654565Z|1157|dev.galasa.Zos3270LocalJava11Ubuntu|galasa|dev.galasa|none|https://127.0.0.1/ras/runs/cbd-456/runlog\n" +
			"U789|Finished|Passed|2023-05-04T10:55:29.545323Z|2023-05-05T06:00:14.496953Z|2023-05-05T06:00:15.654565Z|1157|dev.galasa.Zos3270LocalJava11Ubuntu|galasa|dev.galasa|none|https://127.0.0.1/ras/runs/cbd-789/runlog\n"
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestRawFormatterNoRunEndtimeReturnsBlankEndtimeFieldAndNoDuration(t *testing.T) {
	formatter := NewRawFormatter()

	formattableTest := make([]FormattableTest, 0)
	formattableTest1 := createFormattableTestForRaw("cbd-123", "U456", "Finished", "Passed", "dev.galasa", "dev.galasa.Zos3270LocalJava11Ubuntu", "galasa", "2023-05-04T10:55:29.545323Z", "2023-05-05T06:00:14.496953Z", "", "https://127.0.0.1", false, "none")
	formattableTest = append(formattableTest, formattableTest1)

	// When...
	actualFormattedOutput, err := formatter.FormatRuns(formattableTest)

	assert.Nil(t, err)
	expectedFormattedOutput := "U456|Finished|Passed|2023-05-04T10:55:29.545323Z|2023-05-05T06:00:14.496953Z|||dev.galasa.Zos3270LocalJava11Ubuntu|galasa|dev.galasa|none|https://127.0.0.1/ras/runs/cbd-123/runlog\n"

	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestRawFormatterOnlyLostRunReturnsEmpty(t *testing.T) {
	formatter := NewRawFormatter()

	formattableTest := make([]FormattableTest, 0)
	formattableTest1 := createFormattableTestForRaw("cbd-123", "U456", "Finished", "Passed", "dev.galasa", "dev.galasa.Zos3270LocalJava11Ubuntu", "galasa", "2023-05-04T10:55:29.545323Z", "2023-05-05T06:00:14.496953Z", "", "https://127.0.0.1", true, "none")
	formattableTest2 := createFormattableTestForRaw("cbd-456", "U456", "Finished", "Passed", "dev.galasa", "dev.galasa.Zos3270LocalJava11Ubuntu", "galasa", "2023-05-04T10:55:29.545323Z", "2023-05-05T06:00:14.496953Z", "2023-05-05T06:00:15.654565Z", "https://127.0.0.1", true, "none")
	formattableTest = append(formattableTest, formattableTest1, formattableTest2)

	// When...
	actualFormattedOutput, err := formatter.FormatRuns(formattableTest)

	assert.Nil(t, err)
	expectedFormattedOutput := ""

	assert.Equal(t, len(formattableTest), 2)
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}

func TestRawFormatterMultipleRunsPrintsOnlyFinishedRuns(t *testing.T) {
	formatter := NewRawFormatter()

	formattableTest := make([]FormattableTest, 0)
	formattableTest1 := createFormattableTestForRaw("cbd-123", "U456", "Finished", "Passed", "dev.galasa", "dev.galasa.Zos3270LocalJava11Ubuntu", "galasa", "2023-05-04T10:55:29.545323Z", "2023-05-05T06:00:14.496953Z", "", "https://127.0.0.1", true, "none")
	formattableTest2 := createFormattableTestForRaw("cbd-123", "U456", "Finished", "Passed", "dev.galasa", "dev.galasa.Zos3270LocalJava11Ubuntu", "galasa", "2023-05-04T10:55:29.545323Z", "2023-05-05T06:00:14.496953Z", "", "https://127.0.0.1", false, "none")
	formattableTest3 := createFormattableTestForRaw("cbd-456", "U456", "Finished", "Passed", "dev.galasa", "dev.galasa.Zos3270LocalJava11Ubuntu", "galasa", "2023-05-04T10:55:29.545323Z", "2023-05-05T06:00:14.496953Z", "2023-05-05T06:00:15.654565Z", "https://127.0.0.1", true, "none")
	formattableTest4 := createFormattableTestForRaw("cbd-456", "U456", "Finished", "Passed", "dev.galasa", "dev.galasa.Zos3270LocalJava11Ubuntu", "galasa", "2023-05-04T10:55:29.545323Z", "2023-05-05T06:00:14.496953Z", "2023-05-05T06:00:15.654565Z", "https://127.0.0.1", false, "none")
	formattableTest5 := createFormattableTestForRaw("cbd-789", "U789", "Finished", "Passed", "dev.galasa", "dev.galasa.Zos3270LocalJava11Ubuntu", "galasa", "2023-05-04T10:55:29.545323Z", "2023-05-05T06:00:14.496953Z", "2023-05-05T06:00:15.654565Z", "https://127.0.0.1", false, "none")
	formattableTest = append(formattableTest, formattableTest1, formattableTest2, formattableTest3, formattableTest4, formattableTest5)

	// When...
	actualFormattedOutput, err := formatter.FormatRuns(formattableTest)

	assert.Nil(t, err)
	expectedFormattedOutput := "U456|Finished|Passed|2023-05-04T10:55:29.545323Z|2023-05-05T06:00:14.496953Z|||dev.galasa.Zos3270LocalJava11Ubuntu|galasa|dev.galasa|none|https://127.0.0.1/ras/runs/cbd-123/runlog\n" +
		"U456|Finished|Passed|2023-05-04T10:55:29.545323Z|2023-05-05T06:00:14.496953Z|2023-05-05T06:00:15.654565Z|1157|dev.galasa.Zos3270LocalJava11Ubuntu|galasa|dev.galasa|none|https://127.0.0.1/ras/runs/cbd-456/runlog\n" +
		"U789|Finished|Passed|2023-05-04T10:55:29.545323Z|2023-05-05T06:00:14.496953Z|2023-05-05T06:00:15.654565Z|1157|dev.galasa.Zos3270LocalJava11Ubuntu|galasa|dev.galasa|none|https://127.0.0.1/ras/runs/cbd-789/runlog\n"

	assert.Equal(t, len(formattableTest), 5)
	assert.Equal(t, expectedFormattedOutput, actualFormattedOutput)
}
