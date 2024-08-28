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
package com.ericsson.bos.dr.rest.service.run.response;

import com.ericsson.bos.dr.rest.service.run.RunExecutionContext;

/**
 * Http response consumer.
 */
public interface HttpResponseConsumer {

    /**
     * Apply functions to the http response.
     * @param httpRunResponse http response for run request.
     * @param runExecutionContext run execution context.
     */
    void apply(HttpRunResponse httpRunResponse, RunExecutionContext runExecutionContext);
}
