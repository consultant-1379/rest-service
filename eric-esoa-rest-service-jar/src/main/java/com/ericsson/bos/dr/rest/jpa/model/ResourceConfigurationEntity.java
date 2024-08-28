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
package com.ericsson.bos.dr.rest.jpa.model;

import java.util.Date;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.ericsson.bos.dr.rest.web.v1.api.model.ResourceConfigurationDto;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

/**
 * Resource Configuration Entity
 */
@Table(name= "resource_configuration")
@EntityListeners(AuditingEntityListener.class)
@Entity
public class ResourceConfigurationEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Version
    @Column(nullable = false)
    private Long version;

    @CreatedDate
    @Column(name = "creation_date")
    private Date creationDate;

    @LastModifiedDate
    @Column(name = "modified_date")
    private Date modifiedDate;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "contents")
    private byte[] contents;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "config", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private ResourceConfigurationDto config;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(final Long version) {
        this.version = version;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(final Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public byte[] getContents() {
        return contents;
    }

    public void setContents(final byte[] contents) {
        this.contents = contents;
    }

    public ResourceConfigurationDto getConfig() {
        return config;
    }

    public void setConfig(final ResourceConfigurationDto config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "ResourceConfigurationEntity{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", version=" + version +
               ", creationDate=" + creationDate +
               ", modifiedDate=" + modifiedDate +
               ", config=" + config +
               '}';
    }
}
