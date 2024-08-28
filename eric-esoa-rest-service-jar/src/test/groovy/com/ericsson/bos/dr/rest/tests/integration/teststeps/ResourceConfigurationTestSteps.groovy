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
package com.ericsson.bos.dr.rest.tests.integration.teststeps

import com.ericsson.bos.dr.rest.web.v1.api.model.ResourceConfigurationDto
import com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils
import com.ericsson.bos.dr.rest.tests.integration.utils.JsonUtils

import java.nio.charset.StandardCharsets
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import java.util.concurrent.atomic.AtomicReference

@Component
class ResourceConfigurationTestSteps {

    private static final String RESOURCE_CONFIGURATION_URL = "/rest-service/v1/resource-configurations"

    @Autowired
    private MockMvc mockMvc

    ResourceConfigurationDto uploadResourceConfiguration(String filePath, String name, Map<String, String> replacements) {
        final AtomicReference<ResourceConfigurationDto> jsonResponse = new AtomicReference<>()
        uploadResourceConfigurationResult(filePath, name, replacements)
                .andExpect(status().isCreated())
                .andDo(result -> jsonResponse.set(JsonUtils.read(result.getResponse().getContentAsString(), ResourceConfigurationDto.class)))
        return jsonResponse.get()
    }

    ResultActions uploadResourceConfigurationResult(String filePath, String name, Map<String, String> replacements) {
        String originalFileName = name + ".yml"
        String fileAsString = new StringBuilder(IOUtils.readClasspathResourceAsString(filePath)).replace(replacements)
        byte[] fileAsBytes = fileAsString.getBytes(StandardCharsets.UTF_8)
        final MockMultipartFile file = new MockMultipartFile("file", originalFileName,
                MediaType.MULTIPART_FORM_DATA.toString(), fileAsBytes)
        mockMvc.perform(multipart(RESOURCE_CONFIGURATION_URL)
                .file(file))
    }

    ResourceConfigurationDto uploadResourceConfiguration(String filePath, String name) {
        final AtomicReference<ResourceConfigurationDto> jsonResponse = new AtomicReference<>()
        uploadResourceConfigurationResult(filePath, name)
                .andExpect(status().isCreated())
                .andDo(result -> jsonResponse.set(JsonUtils.read(result.getResponse()
                        .getContentAsString(), ResourceConfigurationDto.class)))
        return jsonResponse.get()
    }

    ResultActions uploadResourceConfigurationResult(String filePath, String name) {
        String originalFileName = name + ".yml"
        String fileAsString = new StringBuilder(IOUtils.readClasspathResourceAsString(filePath))
        byte[] fileAsBytes = IOUtils.readClasspathResourceAsBytes(filePath)
        final MockMultipartFile file = new MockMultipartFile("file", originalFileName,
                MediaType.MULTIPART_FORM_DATA.toString(), fileAsBytes)
        mockMvc.perform(multipart(RESOURCE_CONFIGURATION_URL)
                .file(file))
    }

    ResourceConfigurationDto getResourceConfiguration(String name) {
        final AtomicReference<ResourceConfigurationDto> jsonResponse = new AtomicReference<>()
        getResourceConfigurationResult(name)
                .andExpect(status().isOk())
                .andDo(result -> jsonResponse.set(JsonUtils.read(result.getResponse()
                        .getContentAsString(), ResourceConfigurationDto.class)))
        return jsonResponse.get()
    }

    ResultActions getResourceConfigurationResult(String name) {
        return mockMvc.perform(get("${RESOURCE_CONFIGURATION_URL}/${name}"))
    }

    List<ResourceConfigurationDto> getResourceConfigurations() {
        final AtomicReference<List<ResourceConfigurationDto>> jsonResponse = new AtomicReference<>()
        mockMvc.perform(get(RESOURCE_CONFIGURATION_URL))
                .andExpect(status().isOk())
                .andDo(result -> jsonResponse.set(JsonUtils.read(result.getResponse()
                        .getContentAsString(), List<ResourceConfigurationDto>.class)))
        return jsonResponse.get()
    }

    void deleteResourceConfiguration(String name) {
        deleteResourceConfigurationResult(name)
                .andExpect(status().isNoContent())
    }

    ResultActions deleteResourceConfigurationResult(String name) {
        return mockMvc.perform(delete("${RESOURCE_CONFIGURATION_URL}/${name}"))
    }

    String downloadResourceConfiguration(String name) {
        final AtomicReference<String> jsonResponse = new AtomicReference<>()
        downloadResourceConfigurationResult(name)
                .andExpect(status().isOk())
                .andDo(result -> jsonResponse.set(result.getResponse().getContentAsString()))
        return jsonResponse.get()
    }

    ResultActions downloadResourceConfigurationResult(String name) {
        return mockMvc.perform(get("${RESOURCE_CONFIGURATION_URL}/${name}/files"))
    }

    ResourceConfigurationDto replaceResourceConfiguration(String filePath, String name) {
        final AtomicReference<ResourceConfigurationDto> jsonResponse = new AtomicReference<>()
        replaceResourceConfigurationResult(filePath, name)
                .andExpect(status().isCreated())
                .andDo(result -> jsonResponse.set(JsonUtils.read(result.getResponse()
                        .getContentAsString(), ResourceConfigurationDto.class)))
        return jsonResponse.get()
    }

    ResultActions replaceResourceConfigurationResult(String filePath, String name) {
        String originalFileName = name + ".yml"
        byte[] fileAsBytes = IOUtils.readClasspathResourceAsBytes(filePath)
        final MockMultipartFile file = new MockMultipartFile("file", originalFileName,
                MediaType.MULTIPART_FORM_DATA.toString(), fileAsBytes)
        mockMvc.perform(multipart(HttpMethod.PUT, "${RESOURCE_CONFIGURATION_URL}/${name}")
                .file(file))
    }
}
