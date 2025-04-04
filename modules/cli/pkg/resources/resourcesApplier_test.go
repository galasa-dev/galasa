/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package resources

import (
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/galasa-dev/cli/pkg/api"
	"github.com/galasa-dev/cli/pkg/files"
	"github.com/stretchr/testify/assert"
)

type MockServlet struct {
	server             *httptest.Server
	payloadToReturn    []byte
	httpStatusToReturn int
	expectedPayload    []byte
}

func NewMockServlet(t *testing.T, payloadToReturn []byte, httpStatusToReturn int, expectedPayload []byte) *MockServlet {

	mockServlet := new(MockServlet)

	mockServlet.payloadToReturn = payloadToReturn
	mockServlet.httpStatusToReturn = httpStatusToReturn
	mockServlet.expectedPayload = expectedPayload
	mockServlet.server = httptest.NewServer(http.HandlerFunc(func(resp http.ResponseWriter, req *http.Request) {
		mockServlet.handleRequest(t, resp, req)
	}))

	return mockServlet
}

func (mockServlet *MockServlet) getUrl() string {
	return mockServlet.server.URL
}

func (mockServlet *MockServlet) handleRequest(t *testing.T, resp http.ResponseWriter, req *http.Request) {
	if !strings.Contains(req.URL.Path, "/resources/") {
		t.Errorf("Expected to request '/resources/', got: %s", req.URL.Path)
	}

	if req.Header.Get("Accept") != "application/json" {
		t.Errorf("Expected Accept: application/json header, got: %s", req.Header.Get("Accept"))
	}

	var err error
	var reqBytePayloadSize int
	var reqPayload []byte = make([]byte, 1000)
	reqBytePayloadSize, err = req.Body.Read(reqPayload)

	assert.NotNil(t, err)

	// Check that the payload sent to the servlet is what we expect.
	assert.Equal(t, reqPayload[:reqBytePayloadSize], mockServlet.expectedPayload, "payload arriving at the mock servlet differs from what we expected")

	// Set up a sensible response
	resp.Header().Set("Content-Type", "application/json")

	var statusCode int = mockServlet.httpStatusToReturn
	resp.WriteHeader(statusCode)

	payload := mockServlet.payloadToReturn

	resp.Write(payload)
}

func TestCanApplySingleValidResource(t *testing.T) {
	// Given
	expectedJsonArrivingInServlet := `{
    "action": "apply",
    "data": [
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling",
                "namespace": "doughnuts"
            }
        }
    ]
}`

	mockServlet := NewMockServlet(t, nil, http.StatusOK, []byte(expectedJsonArrivingInServlet))
	mockservletUrl := mockServlet.getUrl()
    mockCommsClient := api.NewMockAPICommsClient(mockservletUrl)

	yamlToApply := validResourcesYamlFileContentSingleProperty

	action := "apply"

	fs := files.NewOverridableMockFileSystem()
	filePath := "/my/resources.yaml"
	fs.WriteTextFile(filePath, yamlToApply)

	// When
	err := ApplyResources(
		action,
		filePath,
		fs,
		mockCommsClient,
	)

	// Then
	assert.Nil(t, err)
}

func TestCanApplyValidMultipleResources(t *testing.T) {
	// Given
	expectedJsonArrivingInServlet := `{
    "action": "apply",
    "data": [
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling",
                "namespace": "doughnuts"
            }
        },
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard2"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling2",
                "namespace": "doughnuts2"
            }
        },
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard3"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling3",
                "namespace": "doughnuts3"
            }
        }
    ]
}`

	mockServlet := NewMockServlet(t, nil, http.StatusOK, []byte(expectedJsonArrivingInServlet))
	mockservletUrl := mockServlet.getUrl()
    mockCommsClient := api.NewMockAPICommsClient(mockservletUrl)

	yamlToApply := validResourcesYamlFileContentMultipleProperties

	action := "apply"

	fs := files.NewOverridableMockFileSystem()
	filePath := "/my/resources.yaml"
	fs.WriteTextFile(filePath, yamlToApply)

	// When
	err := ApplyResources(
		action,
		filePath,
		fs,
        mockCommsClient,
	)

	// Then
	assert.Nil(t, err)
}


func TestUnauthorizedResponseStatusFromServerShowsUnauthorizedError(t *testing.T) {
	// Given
	// We have a payload we are expecting...
	// Like this:
	expectedStringArrivingAtServlet := `{
        "action": "apply",
        "data": [
            {
                "apiVersion": "galasa-dev/v1alpha1",
                "data": {
                    "value": "custard"
                },
                "kind": "GalasaProperty",
                "metadata": {
                    "name": "filling",
                    "namespace": "doughnuts"
                }
            }
        ]
    }`

	expectedBytesArrivingAtServlet := []byte(expectedStringArrivingAtServlet)

	payloadToReturn := `{
        "error_code" : 2003,
        "error_message" : "Error: GAL2003 - Invalid yaml format"
    }`

	bytesToReturn := []byte(payloadToReturn)

	mockServlet := NewMockServlet(t, bytesToReturn, 401, expectedBytesArrivingAtServlet)
	mockservletUrl := mockServlet.getUrl()
    mockCommsClient := api.NewMockAPICommsClient(mockservletUrl)

	// When
	err := sendResourcesRequestToServer(expectedBytesArrivingAtServlet, mockCommsClient)

	// Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1119E")

}

func TestBadRequestResponseStatusFromServerShowsErrorsReturned(t *testing.T) {
	// Given
	// We have a payload we are expecting...
	// Like this:
	expectedStringArrivingAtServlet := `{
        "action": "apply",
        "data": [
            {
                "apiVersion": "galasa-dev/v1alpha1",
                "data": {
                    "value": "custard"
                },
                "kind": "GalasaProperty",
                "metadata": {
                    "name": "filling",
                    "namespace": "doughnuts"
                }
            }
        ]
    }`

	expectedBytesArrivingAtServlet := []byte(expectedStringArrivingAtServlet)

	payloadToReturn := `[
        {
            "error_code" : 2003,
            "error_message" : "Error: GAL2003 - Invalid yaml format"
        },
        {
            "error_code": 343,
            "error_message": "GAL343 - Unable to marshal into json"
        }
    ]`

	bytesToReturn := []byte(payloadToReturn)

	mockServlet := NewMockServlet(t, bytesToReturn, 400, expectedBytesArrivingAtServlet)
	mockservletUrl := mockServlet.getUrl()
    mockCommsClient := api.NewMockAPICommsClient(mockservletUrl)

	// When
	err := sendResourcesRequestToServer(expectedBytesArrivingAtServlet, mockCommsClient)

	// Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1113E")

}

func TestInternalServerErrorResponseStatusFromServerReturnsServerError(t *testing.T) {
	// Given

	// We have a payload we are expecting...
	// Like this:
	expectedStringArrivingAtServlet := `{
        "action": "apply",
        "data": [
            {
                "apiVersion": "galasa-dev/v1alpha1",
                "data": {
                    "value": "custard"
                },
                "kind": "GalasaProperty",
                "metadata": {
                    "name": "filling",
                    "namespace": "doughnuts"
                }
            }
        ]
    }`

	expectedBytesArrivingAtServlet := []byte(expectedStringArrivingAtServlet)

	payloadToReturn := `{
            "error_code" : 2003,
            "error_message" : "Error: GAL2003 - Invalid yaml format"
        }`

	bytesToReturn := []byte(payloadToReturn)

	mockServlet := NewMockServlet(t, bytesToReturn, 500, expectedBytesArrivingAtServlet)
	mockservletUrl := mockServlet.getUrl()
    mockCommsClient := api.NewMockAPICommsClient(mockservletUrl)

	// When
	err := sendResourcesRequestToServer(expectedBytesArrivingAtServlet, mockCommsClient)

	// Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1114E")
}

func TestResponseStatusCodeFromApiIsAnUnexpectedError(t *testing.T) {
	// Given

	// We have a payload we are expecting...
	// Like this:
	expectedStringArrivingAtServlet := `{
        "action": "apply",
        "data": [
            {
                "apiVersion": "galasa-dev/v1alpha1",
                "data": {
                    "value": "custard"
                },
                "kind": "GalasaProperty",
                "metadata": {
                    "name": "filling",
                    "namespace": "doughnuts"
                }
            }
        ]
    }`

	expectedBytesArrivingAtServlet := []byte(expectedStringArrivingAtServlet)

	payloadToReturn := `[
        {
            "error_code" : 2003,
            "error_message" : "Error: GAL2003 - Invalid yaml format"
        },
        {
            "error_code": 343,
            "error_message": "GAL343 - Unable to marshal into json"
        }
    ]`

	bytesToReturn := []byte(payloadToReturn)

	mockServlet := NewMockServlet(t, bytesToReturn, 203, expectedBytesArrivingAtServlet)
	mockservletUrl := mockServlet.getUrl()
    mockCommsClient := api.NewMockAPICommsClient(mockservletUrl)

	// When
	err := sendResourcesRequestToServer(expectedBytesArrivingAtServlet, mockCommsClient)

	// Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL1115E")
}

func TestCanDeleteSingleValidResource(t *testing.T) {
	// Given
	expectedJsonArrivingInServlet := `{
    "action": "delete",
    "data": [
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling",
                "namespace": "doughnuts"
            }
        }
    ]
}`

	mockServlet := NewMockServlet(t, nil, http.StatusOK, []byte(expectedJsonArrivingInServlet))
	mockservletUrl := mockServlet.getUrl()
    mockCommsClient := api.NewMockAPICommsClient(mockservletUrl)

	yamlToApply := validResourcesYamlFileContentSingleProperty

	action := "delete"

	fs := files.NewOverridableMockFileSystem()
	filePath := "/my/resources.yaml"
	fs.WriteTextFile(filePath, yamlToApply)

	// When
	err := ApplyResources(
		action,
		filePath,
		fs,
		mockCommsClient,
	)

	// Then
	assert.Nil(t, err)
}

func TestCanDeleteValidMultipleResources(t *testing.T) {
	// Given
	expectedJsonArrivingInServlet := `{
    "action": "delete",
    "data": [
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling",
                "namespace": "doughnuts"
            }
        },
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard2"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling2",
                "namespace": "doughnuts2"
            }
        },
        {
            "apiVersion": "galasa-dev/v1alpha1",
            "data": {
                "value": "custard3"
            },
            "kind": "GalasaProperty",
            "metadata": {
                "name": "filling3",
                "namespace": "doughnuts3"
            }
        }
    ]
}`

	mockServlet := NewMockServlet(t, nil, http.StatusOK, []byte(expectedJsonArrivingInServlet))
	mockservletUrl := mockServlet.getUrl()
    mockCommsClient := api.NewMockAPICommsClient(mockservletUrl)

	yamlToApply := validResourcesYamlFileContentMultipleProperties

	action := "delete"

	fs := files.NewOverridableMockFileSystem()
	filePath := "/my/resources.yaml"
	fs.WriteTextFile(filePath, yamlToApply)

	// When
	err := ApplyResources(
		action,
		filePath,
		fs,
		mockCommsClient,
	)

	// Then
	assert.Nil(t, err)
}

