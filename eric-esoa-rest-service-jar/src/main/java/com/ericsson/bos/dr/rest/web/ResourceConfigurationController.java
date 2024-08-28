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
package com.ericsson.bos.dr.rest.web;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ericsson.bos.dr.rest.service.ResourceConfigurationService;
import com.ericsson.bos.dr.rest.web.v1.api.ResourceConfigurationsApi;
import com.ericsson.bos.dr.rest.web.v1.api.model.ResourceConfigurationDto;

/**
 * Resource Configuration Controller.
 */
@RestController
public class ResourceConfigurationController implements ResourceConfigurationsApi {

    @Autowired
    private ResourceConfigurationService resourceConfigurationService;

    @Override
    public ResponseEntity<ResourceConfigurationDto> uploadResourceConfiguration(final MultipartFile file) {
        return new ResponseEntity<>(resourceConfigurationService.createResourceConfiguration(file), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteResourceConfiguration(final String resourceConfigurationName) {
        resourceConfigurationService.deleteResourceConfiguration(resourceConfigurationName);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Resource> downloadResourceConfiguration(final String resourceConfigurationName) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition
                        .attachment()
                        .filename(resourceConfigurationName + ".yaml", StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(resourceConfigurationService.downloadResourceConfiguration(resourceConfigurationName));
    }

    @Override
    public ResponseEntity<ResourceConfigurationDto> getResourceConfiguration(final String resourceConfigurationName) {
        return ResponseEntity.ok(resourceConfigurationService.getResourceConfiguration(resourceConfigurationName));
    }

    @Override
    public ResponseEntity<List<ResourceConfigurationDto>> getResourceConfigurations() {
        return ResponseEntity.ok(resourceConfigurationService.getResourceConfigurations());
    }

    @Override
    public ResponseEntity<ResourceConfigurationDto> updateResourceConfiguration(final String resourceConfigurationName,
                                                                                final MultipartFile file) {
        return new ResponseEntity<>(resourceConfigurationService.replaceResourceConfiguration(resourceConfigurationName, file),
            HttpStatus.CREATED);
    }
}
