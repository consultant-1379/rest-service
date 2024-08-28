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
package com.ericsson.bos.dr.rest.tests.integration.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

@TestConfiguration
class WebClientConfiguration {

    @Bean
    WebClientCustomizer webClientCustomizer(WebClientConfiguration.WebClientRequestsRecorder webClientRequestsRecorder) {
        return (wcb) -> wcb.filter(webClientRequestsRecorder);
    }

    /**
     * <code>ExchangeFilterFunction</code> to record WebClient requests during test.
     */
    @Component
    public static class WebClientRequestsRecorder implements ExchangeFilterFunction {
        private List<String> requestUrls = new ArrayList<>();

        public long getRequestCount(String url) {
            return requestUrls.stream().filter(s -> s.equals(url)).count();
        }

        public void clear() {
            requestUrls.clear();
        }

        @Override
        public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
            requestUrls.add(request.url().toString());
            return next.exchange(request);
        }
    }
}
