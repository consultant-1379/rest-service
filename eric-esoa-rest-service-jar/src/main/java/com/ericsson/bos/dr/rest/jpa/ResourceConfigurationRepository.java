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
package com.ericsson.bos.dr.rest.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ericsson.bos.dr.rest.jpa.model.ResourceConfigurationEntity;

/**
 * Resource Configuration Repository
 */
public interface ResourceConfigurationRepository extends JpaRepository<ResourceConfigurationEntity, Long> {

    /**
     * Find Resource Configuration by name.
     * @param name Resource Configuration name
     * @return ResourceConfigurationEntity optional
     */
    Optional<ResourceConfigurationEntity> findByName(String name);

}
