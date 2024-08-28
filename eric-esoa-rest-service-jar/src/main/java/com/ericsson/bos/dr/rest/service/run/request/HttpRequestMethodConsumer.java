/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.bos.dr.rest.service.run.request;

import com.ericsson.bos.dr.rest.service.http.HttpRequest;
import com.ericsson.bos.dr.rest.service.run.RunExecutionContext;
import com.ericsson.bos.dr.rest.service.substitution.SubstitutionEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Set the method in the HTTP request.
 */
@Component
public class HttpRequestMethodConsumer implements HttpRequestConsumer {

    @Autowired
    private SubstitutionEngine substitutionEngine;

    @Override
    public void apply(HttpRequest httpRequest, RunExecutionContext runExecutionContext) {
        httpRequest.setMethod(runExecutionContext.getResourceMethod().getMethodName());
    }
}