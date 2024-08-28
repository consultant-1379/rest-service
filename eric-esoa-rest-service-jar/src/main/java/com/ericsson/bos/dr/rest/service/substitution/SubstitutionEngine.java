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
package com.ericsson.bos.dr.rest.service.substitution;

import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ericsson.bos.dr.rest.service.exceptions.ErrorCode;
import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.RenderResult;

import jakarta.annotation.PostConstruct;

/**
 * Substitution Engine.
 */
@Component
public class SubstitutionEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubstitutionEngine.class);

    private final Jinjava jinjava = new Jinjava();

    /**
     *Register Jinja functions.
     */
    @PostConstruct
    public void registerJinjaFunction() {
        Stream.of(new ReplaceAtSymbolFunction(),
                new JqFunction(),
                new CurrentTimeStampFunction(),
                new CurrentTimeMillisFunction(),
                new GroovyFunction())
                .forEach(jinjava.getGlobalContext()::registerFunction);
    }

    /**
     * Render the Jinja template using the provided substitution context.
     * @param template Jinja template
     * @param substitutionContext substitution context
     * @return rendered template
     */
    public String render(final String template, final Map<String, Object> substitutionContext) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Jinja Template is:{}, substitutionContext is:{}", template, substitutionContext);
        }

        final JinjavaConfig config = JinjavaConfig.newBuilder().withFailOnUnknownTokens(true).build();
        final RenderResult result = jinjava.renderForResult(template, substitutionContext, config);

        if (result.hasErrors()) {
            throw new RestServiceException(ErrorCode.SUBSTITUTION_FAILED, result.getErrors().toString());
        }

        return result.getOutput();
    }
}