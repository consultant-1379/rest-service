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

import java.util.Optional;

import com.ericsson.bos.dr.rest.service.http.HttpRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

/**
 * Http Response for run request.
 */
public class HttpRunResponse {

    private final ResponseEntity<byte[]> originalResponse;
    private final HttpRequest httpRequest;

    private Integer statusCode;
    private HttpHeaders httpHeaders;
    private String transformedBody;

    /**
     * Http Response.
     * @param originalResponse original http response
     * @param httpRequest http request
     */
    public HttpRunResponse(final ResponseEntity<byte[]> originalResponse, final HttpRequest httpRequest) {
        this.originalResponse = originalResponse;
        this.httpRequest = httpRequest;
    }

    public int getOriginalStatusCode() {
        return originalResponse.getStatusCode().value();
    }

    public int getStatusCode() {
        return Optional.ofNullable(statusCode).orElseGet(() -> originalResponse.getStatusCode().value());
    }

    public void setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Get the http response headers.
     * Returns the original response headers if no other headers have been set.
     * @return <code>HttpHeaders</code>
     */
    public HttpHeaders getHttpHeaders() {
        return Optional.ofNullable(httpHeaders).orElseGet(this::getOriginalHttpHeaders);
    }

    /**
     * Get the original http response headers.
     * @return <code>HttpHeaders</code>
     */
    public HttpHeaders getOriginalHttpHeaders() {
        final HttpHeaders reducedHeaders = HttpHeaders.writableHttpHeaders(originalResponse.getHeaders());
        reducedHeaders.remove(HttpHeaders.CONTENT_LENGTH);
        reducedHeaders.remove(HttpHeaders.TRANSFER_ENCODING);
        return reducedHeaders;
    }

    public void setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public String getOriginalBody() {
        return  Optional.ofNullable(originalResponse.getBody()).map(String::new).orElse("");
    }

    public void setTransformedBody(String body) {
        this.transformedBody = body;
    }

    public HttpRequest getRequest() {
        return httpRequest;
    }

    /**
     * Return the <code>HttpResponse</code> as a <code>ResponseEntity</code>.
     * @return ResponseEntity
     */
    public ResponseEntity<Object> asResponseEntity() {
        return ResponseEntity.status(getStatusCode())
                .headers(getHttpHeaders())
                .body(transformedBody != null ? transformedBody : originalResponse.getBody());
    }
}