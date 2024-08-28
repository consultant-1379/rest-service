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
package com.ericsson.bos.dr.rest.model.mappers;

import com.ericsson.bos.dr.rest.jpa.model.ResourceConfigurationEntity;
import com.ericsson.bos.dr.rest.web.v1.api.model.ResourceConfigurationDto;

/**
 * Map <code>ResourceConfigurationDto</code> to <code>ResourceConfigurationEntity</code>
 */
public class ResourceConfigurationEntityMapper implements Mapper<ResourceConfigurationDto, ResourceConfigurationEntity> {

    private final String name;
    private final String description;
    private final byte[] contents;

    /**
     * Constructor
     * @param name name
     * @param description description
     * @param contents raw file contents
     */
    public ResourceConfigurationEntityMapper(final String name, final String description, final byte[] contents) {
        this.name = name;
        this.description = description;
        this.contents = contents;
    }

    @Override
    public ResourceConfigurationEntity apply(final ResourceConfigurationDto resourceConfigurationDto) {
        final var resourceConfigurationEntity = new ResourceConfigurationEntity();
        resourceConfigurationEntity.setName(name);
        resourceConfigurationEntity.setDescription(description);
        resourceConfigurationEntity.setContents(contents);
        resourceConfigurationEntity.setConfig(resourceConfigurationDto);
        return resourceConfigurationEntity;
    }
}
