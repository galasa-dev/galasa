/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.rest;

import org.apache.http.*;
import static org.assertj.core.api.Assertions.*;

import dev.galasa.extensions.mocks.*;


/**
 * An HTTP interaction in which we expect the caller of the REST call to pass a JWT as 
 * in the Authorization header
 * 
 * Subclasses will be granted the checking of the request from the client side. 
 */
public abstract class AuthenticatedHttpInteraction extends BaseHttpInteraction {

    private String expectedJwt ;

    public AuthenticatedHttpInteraction(String expectedUri, String expectedJwt) {
        super(expectedUri,null);
        this.expectedJwt = expectedJwt;
    }
    
    @Override
    public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
        super.validateRequest(host,request);
        Header[] headers = request.getHeaders("Authorization");
        assertThat(headers).as("There is no bearer token header being passed to the server side.").hasSize(1);
        assertThat(headers[0].getValue()).as("The bearer token is not being passed correctly to the REST API.").isEqualTo("Bearer "+expectedJwt);
    }
}
