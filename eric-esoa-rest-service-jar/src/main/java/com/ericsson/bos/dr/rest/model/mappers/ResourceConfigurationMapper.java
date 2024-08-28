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
 * Map <code>ResourceConfigurationEntity</code> to <code>ResourceConfigurationDto</code>
 */
public class ResourceConfigurationMapper implements Mapper<ResourceConfigurationEntity, ResourceConfigurationDto> {

    @Override
    public ResourceConfigurationDto apply(final ResourceConfigurationEntity resourceConfigurationEntity) {
        final var resourceConfigurationDto = resourceConfigurationEntity.getConfig();
        resourceConfigurationDto.setId(String.valueOf(resourceConfigurationEntity.getId()));
        resourceConfigurationDto.setName(resourceConfigurationEntity.getName());
        resourceConfigurationDto.setDescription(resourceConfigurationEntity.getDescription());
        resourceConfigurationDto.setCreatedAt(resourceConfigurationEntity.getCreationDate().toInstant().toString());
        resourceConfigurationDto.setModifiedAt(resourceConfigurationEntity.getModifiedDate().toInstant().toString());
        return resourceConfigurationDto;
    }
}
