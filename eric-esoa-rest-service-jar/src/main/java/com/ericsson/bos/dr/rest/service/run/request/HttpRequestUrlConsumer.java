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

import java.util.Map;

import com.ericsson.bos.dr.rest.service.http.HttpRequest;
import com.ericsson.bos.dr.rest.service.run.RunExecutionContext;
import com.ericsson.bos.dr.rest.service.substitution.SubstitutionEngine;
import com.ericsson.bos.dr.rest.web.v1.api.model.InboundPropertyDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Perform jinja substitution of the http request url path defined in the resource definition.
 * If the url path is not a jinja expression then it will be unchanged.
 */
@Component
public class HttpRequestUrlConsumer implements HttpRequestConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestUrlConsumer.class);

    @Autowired
    private SubstitutionEngine substitutionEngine;

    @Override
    public void apply(HttpRequest httpRequest, RunExecutionContext runExecutionContext) {
        final String url = StringUtils.join(runExecutionContext.getSubsystem().getUrl(),
                runExecutionContext.getResourceMethod().getPath(),
                runExecutionContext.getResourceMethod().getInbound().map(InboundPropertyDto::getQueryParams).orElse(""));
        LOGGER.debug("Request url: {}", url);
        final Map<String, Object> substitutionCtx = new HttpRequestSubstitutionContext(httpRequest, runExecutionContext).get();
        final String substitutedUrl = substitutionEngine.render(url, substitutionCtx);
        LOGGER.debug("Request url after substitution: {}", substitutedUrl);
        httpRequest.setUrl(substitutedUrl);
        runExecutionContext.getResourceMethod().getInbound().ifPresent(i -> httpRequest.setEncodeUrl(i.getEncodeUrl()));
    }
}