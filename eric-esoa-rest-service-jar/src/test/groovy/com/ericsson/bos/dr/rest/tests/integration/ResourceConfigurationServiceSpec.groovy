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
package com.ericsson.bos.dr.rest.tests.integration

import com.ericsson.bos.dr.rest.service.exceptions.ErrorCode
import com.ericsson.bos.dr.rest.web.v1.api.model.ResourceConfigurationDto
import org.springframework.test.web.servlet.ResultActions

import static com.ericsson.bos.dr.rest.tests.integration.asserts.JsonAssertComparators.RC_COMPARATOR
import static com.ericsson.bos.dr.rest.tests.integration.asserts.JsonAssertComparators.RC_LIST_COMPARATOR
import static com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils.readClasspathResourceAsString
import static com.ericsson.bos.dr.rest.tests.integration.utils.JsonUtils.toJsonString
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ResourceConfigurationServiceSpec extends BaseSpec {

    def "Upload resource configuration is successful"() {
        setup: "Setup"
        String resourceConfigurationName = "CTS"
        String expectedResponse = readClasspathResourceAsString("/resource-configurations/cts/responses/expected_cts_response.json")

        when: "Upload resource configuration"
        ResourceConfigurationDto resourceConfigurationDto = resourceConfigurationTestSteps
                .uploadResourceConfiguration("/resource-configurations/cts/cts.yml", resourceConfigurationName)

        then: "Response is as expected"
        assertEquals(expectedResponse, toJsonString(resourceConfigurationDto), RC_COMPARATOR)

        and: "Resource configuration is persisted"
        ResourceConfigurationDto uploadedResourceConfigurationDto = resourceConfigurationTestSteps.getResourceConfiguration(resourceConfigurationName)
        uploadedResourceConfigurationDto.name == resourceConfigurationName
    }

    def "Upload resource configuration returns 409 when name has already been used"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "CTS"
        resourceConfigurationTestSteps.uploadResourceConfiguration("/resource-configurations/cts/cts.yml", resourceConfigurationName)

        when: "Upload resource configuration with same name"
        ResultActions result =
                resourceConfigurationTestSteps.uploadResourceConfigurationResult("/resource-configurations/cts/cts.yml", resourceConfigurationName)

        then: "Response is conflict"
        result.andExpect(status().is(409))
                .andExpect(jsonPath("\$.errorCode").value(ErrorCode.RESOURCE_CONFIGURATION_ALREADY_EXISTS.errorCode))
    }

    def "Upload resource configuration returns 500 when file is badly formatted"() {
        when: "Upload invalid resource configuration"
        ResultActions result = resourceConfigurationTestSteps.uploadResourceConfigurationResult(
                "/resource-configurations/cts/invalid/invalid_resource_configuration_yaml.yml", "invalid_resource_configuration")

        then: "Response is internal server error"
        result.andExpect(status().is(500))
                .andExpect(jsonPath("\$.errorCode").value(ErrorCode.GENERAL_ERROR.errorCode))
    }

    def "Upload a resource configuration returns 400 when error validating the schema"() {

        when: "Upload resource configuration"
        ResultActions result = resourceConfigurationTestSteps.uploadResourceConfigurationResult(
                "/invalid_schemas/${resourceConfig}", "resourceConfig-invalid")

        then: "Response is bad parameter"
        result.andExpect(status().is(400))
                .andExpect(jsonPath("\$.errorCode").value(ErrorCode.SCHEMA_ERROR.errorCode))

        where:
        resourceConfig | _
        "invalid_schema_1.yml" | _
        "invalid_schema_2.yml" | _
        "invalid_schema_3.yml" | _
        "invalid_schema_4.yml" | _
        "invalid_schema_5.yml" | _
        "invalid_schema_6.yml" | _
        "invalid_schema_7.yml" | _
        "invalid_schema_8.yml" | _
        "invalid_schema_9.yml" | _
        "invalid_schema_10.yml" | _
        "invalid_schema_11.yml" | _
        "invalid_schema_12.yml" | _
        "invalid_schema_13.yml" | _
        "invalid_schema_14.yml" | _
    }

    def "Get resource configuration is successful"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "CTS"
        String expectedResponse = readClasspathResourceAsString("/resource-configurations/cts/responses/expected_cts_response.json")
        ResourceConfigurationDto uploadedResourceConfigurationDto = resourceConfigurationTestSteps
                .uploadResourceConfiguration("/resource-configurations/cts/cts.yml", resourceConfigurationName)

        when: "Get resource configuration"
        ResourceConfigurationDto resourceConfigurationDto =
                resourceConfigurationTestSteps.getResourceConfiguration(uploadedResourceConfigurationDto.name)

        then: "Response is as expected"
        assertEquals(expectedResponse, toJsonString(resourceConfigurationDto), RC_COMPARATOR)
    }

    def "Get resource configuration returns 404 when it does not exist"() {
        when: "Get resource configuration which does not exist"
        ResultActions result = resourceConfigurationTestSteps.getResourceConfigurationResult("not_existing")

        then: "Response is not found"
        result.andExpect(status().is(404))
                .andExpect(jsonPath("\$.errorCode").value(ErrorCode.RESOURCE_CONFIGURATION_NOT_FOUND.errorCode))
    }

    def "Get resource configurations is successful"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "CTS"
        String expectedListResponse = readClasspathResourceAsString("/resource-configurations/cts/responses/expected_cts_list_response.json")
        resourceConfigurationTestSteps.uploadResourceConfiguration("/resource-configurations/cts/cts.yml", resourceConfigurationName)

        when: "Get resource configurations"
        List<ResourceConfigurationDto> resourceConfigurationDtos = resourceConfigurationTestSteps.getResourceConfigurations()

        then: "Response is as expected"
        resourceConfigurationDtos.size() == 1
        assertEquals(expectedListResponse, toJsonString(resourceConfigurationDtos), RC_LIST_COMPARATOR)
    }

    def "Get resource configurations returns empty list when none exist"() {
        setup: "No resource configurations"

        when: "Get resource configurations"
        List<ResourceConfigurationDto> resourceConfigurationDtos = resourceConfigurationTestSteps.getResourceConfigurations()

        then: "Response is empty"
        resourceConfigurationDtos.isEmpty()
    }

    def "Download resource configuration is successful"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "CTS"
        String originalYamlFile = readClasspathResourceAsString("/resource-configurations/cts/cts.yml")
        ResourceConfigurationDto uploadedResourceConfigurationDto = resourceConfigurationTestSteps
                .uploadResourceConfiguration("/resource-configurations/cts/cts.yml", resourceConfigurationName)

        when: "Download resource configuration"
        String downloadedYamlFile = resourceConfigurationTestSteps.downloadResourceConfiguration(resourceConfigurationName)

        then: "Downloaded file contents are same as contents of original file that was uploaded"
        downloadedYamlFile == originalYamlFile
    }

    def "Download resource configuration returns 404 when it does not exist"() {
        when: "Download resource configuration which does not exist"
        ResultActions result = resourceConfigurationTestSteps.downloadResourceConfigurationResult("not_existing")

        then: "Response is not found"
        result.andExpect(status().is(404))
                .andExpect(jsonPath("\$.errorCode").value(ErrorCode.RESOURCE_CONFIGURATION_NOT_FOUND.errorCode))
    }

    def "Replace resource configuration is successful"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "CTS"
        ResourceConfigurationDto originalResourceConfigurationDto = resourceConfigurationTestSteps
                .uploadResourceConfiguration("/resource-configurations/cts/cts.yml", resourceConfigurationName)

        when: "Replace resource configuration"
        ResourceConfigurationDto replacedResourceConfigurationDto = resourceConfigurationTestSteps
                .replaceResourceConfiguration("/resource-configurations/cts/cts_replaced.yml", resourceConfigurationName)

        then: "number of resource configurations is 1, as the original resource configuration was deleted"
        resourceConfigurationTestSteps.getResourceConfigurations().size() == 1

        and: "Existing resource configuration is the replaced one and not the original one"
        replacedResourceConfigurationDto.description == "replaced_test_resource_configuration"
        replacedResourceConfigurationDto.description != originalResourceConfigurationDto.description
        replacedResourceConfigurationDto.resources.size() == 1
        replacedResourceConfigurationDto.resources.size() != originalResourceConfigurationDto.resources.size()
    }

    def "Replace resource configuration returns 404 when it does not exist"() {
        when: "Replace resource configuration which does not exist"
        String resourceConfigurationName = "CTS"
        ResultActions result = resourceConfigurationTestSteps
                .replaceResourceConfigurationResult("/resource-configurations/cts/cts_replaced.yml", resourceConfigurationName)

        then: "Response is not found"
        result.andExpect(status().is(404))
                .andExpect(jsonPath("\$.errorCode").value(ErrorCode.RESOURCE_CONFIGURATION_NOT_FOUND.errorCode))
    }

    def "Replace resource configuration returns 500 when file is badly formatted"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "CTS"
        resourceConfigurationTestSteps.uploadResourceConfiguration("/resource-configurations/cts/cts.yml", resourceConfigurationName)

        when: "Replace resource configuration using badly formatted file"
        ResultActions result = resourceConfigurationTestSteps.replaceResourceConfigurationResult(
                "/resource-configurations/cts/invalid/invalid_resource_configuration_yaml.yml", resourceConfigurationName)

        then: "Response is internal server error"
        result.andExpect(status().is(500))
                .andExpect(jsonPath("\$.errorCode").value(ErrorCode.GENERAL_ERROR.errorCode))
    }

    def "Delete resource configuration is successful"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "CTS"
        resourceConfigurationTestSteps.uploadResourceConfiguration("/resource-configurations/cts/cts.yml", resourceConfigurationName)

        when: "Delete resource configuration"
        resourceConfigurationTestSteps.deleteResourceConfiguration(resourceConfigurationName)

        then: "Resource configuration is deleted"
        resourceConfigurationTestSteps.getResourceConfigurationResult(resourceConfigurationName)
                .andExpect(status().is(404))
    }

    def "Delete resource configuration returns 404 when it does not exist"() {
        when: "Delete resource configuration which does not exist"
        ResultActions result = resourceConfigurationTestSteps.deleteResourceConfigurationResult("not_existing")

        then: "Response is not found"
        result.andExpect(status().is(404))
                .andExpect(jsonPath("\$.errorCode").value(ErrorCode.RESOURCE_CONFIGURATION_NOT_FOUND.errorCode))
    }
}
