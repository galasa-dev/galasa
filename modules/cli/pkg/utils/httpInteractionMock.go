/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package utils

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/stretchr/testify/assert"
)

// The implementation of a HTTP interaction that allows unit tests to define
// interactions with the Galasa API server, with methods to validate requests
// and a lambda to write HTTP responses (which can be overridden as desired)
type HttpInteraction struct {
    ExpectedPath string
    ExpectedHttpMethod string

    // An override-able function to write a HTTP response for this interaction
    WriteHttpResponseFunc func(writer http.ResponseWriter, req *http.Request)

	ValidateRequestFunc func(t *testing.T, req *http.Request)
}

func NewHttpInteraction(expectedPath string, expectedHttpMethod string) HttpInteraction {
    httpInteraction := HttpInteraction{
        ExpectedPath: expectedPath,
        ExpectedHttpMethod: expectedHttpMethod,
    }

    // Set a basic implementation of the lambda to write a default response, which can be overridden by tests
    httpInteraction.WriteHttpResponseFunc = func(writer http.ResponseWriter, req *http.Request) {
        writer.WriteHeader(http.StatusOK)
    }

	httpInteraction.ValidateRequestFunc = func(t *testing.T, req *http.Request) {
		// Do nothing...
	}

    return httpInteraction
}

func (interaction *HttpInteraction) ValidateRequest(t *testing.T, req *http.Request) {
    assert.NotEmpty(t, req.Header.Get("ClientApiVersion"))
    assert.Equal(t, interaction.ExpectedHttpMethod, req.Method, "Actual HTTP request method did not match the expected method")
    assert.Equal(t, interaction.ExpectedPath, req.URL.Path, "Actual request path did not match the expected path")

	// Perform additional checks based on the possibly overridden function
	interaction.ValidateRequestFunc(t, req)
}

//-----------------------------------------------------------------------------
// Wrapper of a mock HTTP server that uses HTTP interactions to handle requests
//-----------------------------------------------------------------------------
type MockHttpServer struct {
    CurrentInteractionIndex int
    Server *httptest.Server
}

func NewMockHttpServer(t *testing.T, interactions []HttpInteraction) MockHttpServer {
    mockHttpServer := MockHttpServer{}
    mockHttpServer.CurrentInteractionIndex = 0

    mockHttpServer.Server = httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, req *http.Request) {

        currentInteractionIndex := &mockHttpServer.CurrentInteractionIndex
        if *currentInteractionIndex >= len(interactions) {
            assert.Fail(t, "Mock server received an unexpected request to '%s' when it should not have", req.URL.Path)
        } else {
            currentInteraction := interactions[*currentInteractionIndex]
            currentInteraction.ValidateRequest(t, req)
            currentInteraction.WriteHttpResponseFunc(writer, req)

            // The next request to the server should get the next interaction, so advance the index by one
            *currentInteractionIndex++
        }
    }))
    return mockHttpServer
}

func NewMockHttpServerWithUnorderedInteractions(t *testing.T, unorderedInteractions []HttpInteraction) MockHttpServer {
    mockHttpServer := MockHttpServer{}

    mockHttpServer.Server = httptest.NewServer(http.HandlerFunc(func(writer http.ResponseWriter, req *http.Request) {

        isInteractionFound := false
        for _, interaction := range unorderedInteractions {
            isInteractionFound = interaction.ExpectedPath == req.URL.Path
            if isInteractionFound {
                interaction.ValidateRequest(t, req)
                interaction.WriteHttpResponseFunc(writer, req)
                break
            }
        }

        if !isInteractionFound {
            assert.Fail(t, "Mock server received an unexpected request to " + req.URL.Path + " when it should not have")
        }
    }))
    return mockHttpServer
}
