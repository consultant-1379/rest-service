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

/**
 * Http request consumer.
 */
public interface HttpRequestConsumer {

    /**
     * Apply function to the <code>HttpRequest</code>.
     * @param httpRequest http request
     * @param runExecutionContext run request execution context
     */
    void apply(HttpRequest httpRequest, RunExecutionContext runExecutionContext);
}