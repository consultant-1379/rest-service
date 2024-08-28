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
package com.ericsson.bos.dr.rest.service;

import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.METHOD_NAME_REQUIRED_IN_RUNREQUEST;
import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.RESOURCE_CONFIGURATION_IO_READ_ERROR;
import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.RESOURCE_CONFIGURATION_NOT_FOUND;
import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.RESOURCE_METHOD_NOT_FOUND;
import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.RESOURCE_NOT_FOUND;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.multipart.MultipartFile;

import com.ericsson.bos.dr.rest.jpa.ResourceConfigurationRepository;
import com.ericsson.bos.dr.rest.jpa.model.ResourceConfigurationEntity;
import com.ericsson.bos.dr.rest.model.mappers.ResourceConfigurationEntityMapper;
import com.ericsson.bos.dr.rest.model.mappers.ResourceConfigurationMapper;
import com.ericsson.bos.dr.rest.model.resources.ResourceMethodDefinition;
import com.ericsson.bos.dr.rest.service.exceptions.ErrorCode;
import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException;
import com.ericsson.bos.dr.rest.service.utils.JSONSchema;
import com.ericsson.bos.dr.rest.service.utils.YAML;
import com.ericsson.bos.dr.rest.web.v1.api.model.ResourceConfigurationDto;
import com.ericsson.bos.dr.rest.web.v1.api.model.ResourceDto;
import com.ericsson.bos.dr.rest.web.v1.api.model.ResourceMethodPropertyDto;
import com.networknt.schema.ValidationMessage;

/**
 * Manage Resource Configurations.
 */
@Service
public class ResourceConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceConfigurationService.class);

    @Autowired
    private ResourceConfigurationRepository resourceConfigurationRepository;

    /**
     * Create a new resource configuration
     * @param file resource configuration yml file
     * @return ResourceConfigurationDto
     */
    public ResourceConfigurationDto createResourceConfiguration(final MultipartFile file) {
        final byte[] contents = getFileContentAsByteArray(file);
        LOGGER.info("Starting upload resource configuration file: {}", file.getOriginalFilename());
        validateResourceConfiguration(contents);
        final var resourceConfigurationDto = YAML.read(contents, ResourceConfigurationDto.class);

        resourceConfigurationRepository.findByName(resourceConfigurationDto.getName()).ifPresent(
            rce -> { throw new RestServiceException(ErrorCode.RESOURCE_CONFIGURATION_ALREADY_EXISTS, resourceConfigurationDto.getName()); });
        final var resourceConfigurationEntity = saveResourceConfiguration(
                resourceConfigurationDto.getName(), resourceConfigurationDto.getDescription(), contents);
        return new ResourceConfigurationMapper().apply(resourceConfigurationEntity);
    }

    /**
     * Delete a resource configuration identified by its name.
     * @param resourceConfigurationName resource configuration name
     */
    public void deleteResourceConfiguration(final String resourceConfigurationName) {
        LOGGER.info("Starting delete resource configuration: {}", resourceConfigurationName);
        resourceConfigurationRepository.deleteById(findResourceConfigurationByName(resourceConfigurationName).getId());
    }

    /**
     * Get a resource configuration identified by its name.
     * @param resourceConfigurationName resource configuration name
     * @return ResourceConfigurationDto
     */
    @Transactional(readOnly = true)
    public ResourceConfigurationDto getResourceConfiguration(final String resourceConfigurationName) {
        LOGGER.info("Starting get resource configuration: {}", resourceConfigurationName);
        return new ResourceConfigurationMapper().apply(findResourceConfigurationByName(resourceConfigurationName));
    }

    /**
     * get all resource configurations.
     * @return ResourceConfigurationDto list
     */
    @Transactional(readOnly = true)
    public List<ResourceConfigurationDto> getResourceConfigurations() {
        LOGGER.info("Starting get all resource configurations");
        final Iterable<ResourceConfigurationEntity> resourceConfigurationEntities = resourceConfigurationRepository.findAll();
        return StreamSupport.stream(resourceConfigurationEntities.spliterator(), false)
            .map(rse -> new ResourceConfigurationMapper().apply(rse))
            .toList();
    }

    /**
     * Download the original contents of a resource configuration identified by its name, as a yaml file.
     * @param name resource configuration name
     * @return Resource
     */
    @Transactional(readOnly = true)
    public Resource downloadResourceConfiguration(final String name) {
        LOGGER.info("Starting download resource configuration: {}", name);
        final var resourceConfigurationEntity = findResourceConfigurationByName(name);
        final byte[] contents = resourceConfigurationEntity.getContents();
        return new ByteArrayResource(contents);
    }

    /**
     * Replace an existing resource configuration. It deletes the existing resource configuration and replaces it with the new resource configuration.
     * @param name resource configuration name
     * @param file resource configuration yml file
     * @return ResourceConfigurationDto
     */
    @Transactional
    public ResourceConfigurationDto replaceResourceConfiguration(final String name, final MultipartFile file) {
        final byte[] contents = getFileContentAsByteArray(file);
        LOGGER.info("Starting replace resource configuration {} with file: {}", name, file.getOriginalFilename());
        validateResourceConfiguration(contents);
        final var resourceConfigurationDto = YAML.read(contents, ResourceConfigurationDto.class);

        final var existingResourceConfigurationEntity = findResourceConfigurationByName(name);
        resourceConfigurationRepository.delete(existingResourceConfigurationEntity);
        resourceConfigurationRepository.flush();
        final var updatedResourceConfigurationEntity = saveResourceConfiguration(
                resourceConfigurationDto.getName(), resourceConfigurationDto.getDescription(), contents);
        return new ResourceConfigurationMapper().apply(updatedResourceConfigurationEntity);
    }

    /**
     * Get the definition of the specified resource method.
     * @param resourceConfigurationName resource configuration name
     * @param resource resource name
     * @param method resource method name
     * @return ResourceMethodDefinition
     */
    public ResourceMethodDefinition getResourceMethodDefinition(final String resourceConfigurationName, final String resource, final String method) {
        final var resourceConfigurationEntity = findResourceConfigurationByName(resourceConfigurationName);
        final var resourceDto = getResourceDto(resourceConfigurationName, resource, resourceConfigurationEntity);
        if (StringUtils.isBlank(method) && resourceDto.getMethods().size() != 1) {
            throw new RestServiceException(METHOD_NAME_REQUIRED_IN_RUNREQUEST, resource);
        }
        final String targetMethodName = Optional.ofNullable(method).orElseGet(() -> {
            final String defaultName = resourceDto.getMethods().keySet().iterator().next();
            LOGGER.info("No method name specified, defaulting to {}", defaultName);
            return defaultName;
        });
        final Map<String, ResourceMethodPropertyDto> resourceMethods = new LinkedCaseInsensitiveMap<>();
        resourceMethods.putAll(resourceDto.getMethods());
        final ResourceMethodPropertyDto resourceMethod = resourceMethods.get(targetMethodName);
        if (resourceMethod == null) {
            throw new RestServiceException(RESOURCE_METHOD_NOT_FOUND, method, resource);
        }

        final ResourceConfigurationDto resourceConfigurationDto = new ResourceConfigurationMapper().apply(resourceConfigurationEntity);
        return new ResourceMethodDefinition(targetMethodName, resourceMethod, resourceDto, resourceConfigurationDto);
    }

    private ResourceConfigurationEntity findResourceConfigurationByName(final String resourceConfigurationName) {
        return resourceConfigurationRepository.findByName(resourceConfigurationName)
            .orElseThrow(() -> new RestServiceException(RESOURCE_CONFIGURATION_NOT_FOUND, resourceConfigurationName));
    }

    private ResourceConfigurationEntity saveResourceConfiguration(final String name, final String description, final byte[] contents) {
        final var resourceConfigurationDto = YAML.read(contents, ResourceConfigurationDto.class);
        final var resourceConfigurationEntity =
            new ResourceConfigurationEntityMapper(name, description, contents).apply(resourceConfigurationDto);
        return resourceConfigurationRepository.save(resourceConfigurationEntity);
    }

    private void validateResourceConfiguration(final byte[] contents) {
        final Set<ValidationMessage> result = JSONSchema.validate("/schemas/resourceconfig_schema.json", contents);
        if (!result.isEmpty()) {
            throw new RestServiceException(ErrorCode.SCHEMA_ERROR, result.toString());
        }
    }

    private byte[] getFileContentAsByteArray(final MultipartFile file) {
        try {
            return file.getBytes();
        } catch (final IOException ex) {
            LOGGER.error("Error processing resource configuration yaml file: " + file.getOriginalFilename(), ex);
            throw new RestServiceException(RESOURCE_CONFIGURATION_IO_READ_ERROR, file.getOriginalFilename());
        }
    }

    private ResourceDto getResourceDto(final String resourceConfigurationName, final String resource,
                                       final ResourceConfigurationEntity resourceConfiguration) {
        LOGGER.info("Getting resource: {}", resource);
        final var resourceDto = resourceConfiguration.getConfig().getResources().get(resource);
        if (resourceDto == null) {
            throw new RestServiceException(RESOURCE_NOT_FOUND, resource, resourceConfigurationName);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Resource dto: {}", resourceDto);
        }
        return resourceDto;
    }
}
