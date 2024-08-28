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
package com.ericsson.bos.dr.rest.service.connectivity;

import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.CONNECTED_SYSTEM_NOT_FOUND;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.bos.dr.rest.service.http.HttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException;
import com.ericsson.bos.dr.rest.service.http.HttpExecutor;
import com.ericsson.bos.dr.rest.service.utils.JSON;

/**
 * Retrieves the connected system from subsystem manager
 */
@Component
public class ConnectivityRetriever {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectivityRetriever.class);

    @Value("${service.connected-system.url}")
    private String subsystemUrl;

    @Value("${service.connected-system.subsystems-path}")
    private String subsystemsPath;

    @Autowired
    @Qualifier("internal_service")
    private HttpExecutor httpExecutor;

    /**
     * Return <Code>Subsystem</Code>
     *
     * @param subsystemName Name of the connected system in subsystem manager
     * @return Subsystem
     */
    @Cacheable(value = "subsystem_cache", key = "#subsystemName")
    public Subsystem getSubsystem(String subsystemName) {
        final Subsystem subsystem = fetchSubsystem(subsystemName);
        if (StringUtils.isNotEmpty(subsystem.getConnection().getAuthSubsystemName())) {
            subsystem.setAuthSubsystem(fetchSubsystem(subsystem.getConnection().getAuthSubsystemName()));
        }
        return subsystem;
    }

    private Subsystem fetchSubsystem(final String subsystemName) {
        LOGGER.info("Getting subsystem: {}", subsystemName);
        final var httpRequest = createHttpRequest(subsystemName);
        final var responseEntity = httpExecutor.execute(httpRequest);

        final List<Subsystem> subsystems = JSON.readList(new String(responseEntity.getBody(), StandardCharsets.UTF_8), Subsystem.class);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Subsystems: {}", subsystems);
        }
        if (subsystems.isEmpty()){
            throw new RestServiceException(CONNECTED_SYSTEM_NOT_FOUND, subsystemName);
        }
        final Subsystem subsystem = subsystems.iterator().next();
        if (CollectionUtils.isEmpty(subsystems.get(0).getConnectionProperties())) {
            throw new IllegalStateException(String.format("Subsystem %s does not contain a connection property", subsystemName));
        }
        return subsystem;
    }

    private HttpRequest createHttpRequest(final String subsystemName) {
        final String queryParams = "?name=" + subsystemName;
        final var url = StringUtils.join(subsystemUrl, subsystemsPath, queryParams);

        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("content-type", List.of("application/json"));

        final var httpRequest = new HttpRequest();
        httpRequest.setUrl(url);
        httpRequest.setMethod("GET");
        httpRequest.setBody(null);
        httpRequest.setHeaders(headers);
        httpRequest.setConnectTimeoutSeconds(10);
        httpRequest.setReadTimeoutSeconds(60);
        httpRequest.setWriteTimeoutSeconds(60);
        httpRequest.setSslVerify(false);
        httpRequest.setEncodeUrl(true);
        return httpRequest;
    }
}