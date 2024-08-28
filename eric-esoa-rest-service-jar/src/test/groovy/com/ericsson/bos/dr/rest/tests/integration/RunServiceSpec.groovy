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

import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.CONNECTED_SYSTEM_NOT_FOUND
import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.CONTENT_TYPE_IN_HTTPRESPONSE_NOT_SUPPORTED
import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.METHOD_NAME_REQUIRED_IN_RUNREQUEST
import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.RESOURCE_CONFIGURATION_NOT_FOUND
import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.RESOURCE_NOT_FOUND
import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.RESOURCE_METHOD_NOT_FOUND
import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.GENERAL_ERROR
import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.SUBSTITUTION_FAILED
import static com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils.readClasspathResourceAsBytes
import static com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils.readClasspathResourceAsString
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.ericsson.bos.dr.rest.tests.integration.utils.WiremockUtil
import com.ericsson.bos.dr.rest.web.v1.api.model.ResourceConfigurationDto
import com.ericsson.bos.dr.rest.web.v1.api.model.RunRequestDto
import org.hamcrest.Matcher
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultMatcher
import spock.lang.Unroll

class RunServiceSpec extends BaseSpec {

    @Unroll
    def "Execute run succeeds, request and response handling as expected using runRequest and resource configuration "() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "EXTERNAL_SYSTEM"
        ResourceConfigurationDto dto = resourceConfigurationTestSteps
                .uploadResourceConfiguration("/run/resource-configuration/response_success_handling.yml", resourceConfigurationName)

        and: "create RunRequestDto"
        RunRequestDto runRequestDto = runRequest

        and: "stub subsystem manager request to return connected system"
        String subsystemName = "external-system"
        WiremockUtil.stubForGet("/subsystem-manager/v1/subsystems\\?name=${subsystemName}",
                "/run/responses/subsystem/subSystemResponse_BasicAuth.json")

        and: "stub external system request to get resource"
        Map expectedRequest = expected_request
        WiremockUtil.stubForGetorPost(resource_url, expectedRequest.get("method") as String,
                expectedRequest.get("request_header") as String, expectedRequest.get("request_body") as String,
                expectedRequest.get("response_status") as int, expectedRequest.get("response_header") as String, expectedRequest.get("response_body") as String)

        when: "execute run"
        ResultActions result = runServiceTestSteps
                .executeRunResult(subsystemName, resourceConfigurationName, resource, runRequestDto)

        then: "execute run successfully completes"
        Map expectedResponse = expected_response
        result.andExpect(status().is(expectedResponse.get("response_status")) as ResultMatcher)
                .andExpect(header().stringValues("content-type", expectedResponse.get("response_header") as Matcher<? super Iterable<String>>))
                .andExpect(content().string(expectedResponse.get("response_body") as String))

        where:
        [resource, runRequest, resource_url, expected_request, expected_response] <<
                [executeGet_if_responseFormatIsOriginal_then_externalSystemResponseWillNotBeTransformed(),
                 executeGet_if_responseFormatIsJson_then_externalSystemResponseWillBeTransformed(),
                 executePost_if_runRequestHasBody_then_externalSystemRequestWillUseBody(),
                 executePost_if_runRequestWithoutBody_then_externalSystemRequestWillUseTransformationInTemplate(),
                 executePost_if_runRequestHasBody_and_responseFormatIsJson_then_externalSystemRequestWillUseBody_and_externalSystemResponseWillBeTransformed(),
                 executeGet_if_runRequestWithoutMethod_then_externalSystemRequestWillDefaultToMethodInResourceIfResourceHasSingleMethod(),
                 executeGet_if_runRequestWithoutResponseFormat_then_externalSystemResponseDefaultsToJsonAndWillBeTransformed()]
    }

    def executeGet_if_responseFormatIsOriginal_then_externalSystemResponseWillNotBeTransformed() {
        return ["multi-method-resource", [method: "GET", responseFormat: "original", body: null, inputs: [id: "1", code: "202", name: "foo"]], "/path-to-multi-method-resource\\?id=1",
                [response_status: 200, response_header: "application/json", response_body: "{\"name\": \"myName\"}", method: "get", request_header: "application/json; charset=UTF-8", request_body: null],
                [response_status: 200, response_header: "application/json", response_body: "{\"name\": \"myName\"}"]]
    }

    def executeGet_if_responseFormatIsJson_then_externalSystemResponseWillBeTransformed() {
        return ["multi-method-resource", [method: "GET", responseFormat: "json", body: null, inputs: [id: "1", code: "202", name: "foo", filter: [[k1: "v1"], [k1: [k2_1: 1, k2_2: 2]]]]], "/path-to-multi-method-resource\\?id=1",
                [response_status: 200, response_header: "application/json", response_body: "{\"name\": \"myName\"}", method: "get", request_header: "application/json; charset=UTF-8", request_body: null],
                [response_status: 202, response_header: "application/json; charset=utf-8", response_body: "{\"transformationOutTemplate_Name\": \"myName\"}\n"]]
    }

    def executePost_if_runRequestHasBody_then_externalSystemRequestWillUseBody() {
        return ["multi-method-resource", [method: "POST", responseFormat: "original", body: "{\"prop1\": \"value1\"}", inputs: [id: "1", code: "202", name: "foo"]], "/path-to-multi-method-resource",
                [response_status: 200, response_header: "application/json", response_body: "{\"name\": \"myName\"}", method: "post", request_header: "application/json", request_body: "{\"prop1\": \"value1\"}"],
                [response_status: 200, response_header: "application/json", response_body: "{\"name\": \"myName\"}"]]
    }

    def executePost_if_runRequestWithoutBody_then_externalSystemRequestWillUseTransformationInTemplate() {
        return ["multi-method-resource", [method: "POST", responseFormat: "original", body: null, inputs: [id: "1", code: "202", name: "foo"]], "/path-to-multi-method-resource",
                [response_status: 200, response_header: "application/json", response_body: "{\"name\": \"myName\"}", method: "post", request_header: "application/json", request_body: "{\"transformationInTemplate_Name\": \"foo\"}\n"],
                [response_status: 200, response_header: "application/json", response_body: "{\"name\": \"myName\"}"]]
    }

    def executePost_if_runRequestHasBody_and_responseFormatIsJson_then_externalSystemRequestWillUseBody_and_externalSystemResponseWillBeTransformed() {
        return ["multi-method-resource",[method: "POST", responseFormat: "json", body: "{\"prop1\": \"value1\"}", inputs: [id: "1", code: "202", name: "foo"]], "/path-to-multi-method-resource",
                [response_status: 200, response_header: "application/json", response_body: "{\"name\": \"myName\"}", method: "post", request_header: "application/json", request_body: "{\"prop1\": \"value1\"}"],
                [response_status: 202, response_header: "application/json; charset=utf-8", response_body: "{\"transformationOutTemplate_Name\": \"myName\"}\n"]]
    }

    def executeGet_if_runRequestWithoutMethod_then_externalSystemRequestWillDefaultToMethodInResourceIfResourceHasSingleMethod() {
        return ["single-method-resource", [responseFormat: "json", body: null, inputs: [id: "1", code: "202", name: "foo"]], "/path-to-single-method-resource\\?id=1",
                [response_status: 200, response_header: "application/json", response_body: "{\"name\": \"myName\"}", method: "get", request_header: "application/json; charset=UTF-8", request_body: null],
                [response_status: 202, response_header: "application/json; charset=utf-8", response_body: "{\"transformationOutTemplate_Name\": \"myName\"}\n"]]
    }

    def executeGet_if_runRequestWithoutResponseFormat_then_externalSystemResponseDefaultsToJsonAndWillBeTransformed() {
        return ["multi-method-resource", [method: "GET", body: null, inputs: [id: "1", code: "202", name: "foo"]], "/path-to-multi-method-resource\\?id=1",
                [response_status: 200, response_header: "application/json", response_body: "{\"name\": \"myName\"}", method: "get", request_header: "application/json; charset=UTF-8", request_body: null],
                [response_status: 202, response_header: "application/json; charset=utf-8", response_body: "{\"transformationOutTemplate_Name\": \"myName\"}\n"]]
    }

    @Unroll
    def "Execute run fails, error handling as expected using globalErrorHandlers and method-local ErrorHandler"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "EXTERNAL_SYSTEM"
        ResourceConfigurationDto dto = resourceConfigurationTestSteps.uploadResourceConfiguration("/run/resource-configuration/response_error_handling.yml",
                resourceConfigurationName)

        and: "create RunRequestDto"
        Map inputs = [object : "1"]
        RunRequestDto runRequestDto = new RunRequestDto()
                .method("GET")
                .responseFormat("json")
                .body(null)
                .inputs(inputs)

        and: "stub subsystem manager request to return connected system"
        String subsystemName = "external-system"
        WiremockUtil.stubForGet("/subsystem-manager/v1/subsystems\\?name=${subsystemName}",
                "/run/responses/subsystem/subSystemResponse_BasicAuth.json")

        and: "stub external system request to get resource and return error"
        WiremockUtil.stubForGetAndResponseStatus("/path/to/resource1", originalStatus,"/run/responses/external-system/original_json.json")

        when: "execute run"
        ResultActions result = runServiceTestSteps
                .executeRunResult(subsystemName, resourceConfigurationName, "resource1", runRequestDto)

        then: "execute run fails, response status code and content are as expected"
        result.andExpect(status().is(expectedStatus))
                .andExpect(content().json(readClasspathResourceAsString("/run/responses/${expectedResponseContent}")))

        where:
        originalStatus | expectedResponseContent          | expectedStatus
        500            | "localErrorHandler.json"         | 404
        400            | "globalErrorHandler.json"        | 400
        //no matched errorCondition
        401            | "transformationOutTemplate.json" | 200
    }

    @Unroll
    def "Execute run fails, retrieving external system fails due to misconfiguration"() {
        setup: "Upload resource configuration"
        ResourceConfigurationDto dto = resourceConfigurationTestSteps.uploadResourceConfiguration("/run/resource-configuration/external_system.yml",
                "EXTERNAL_SYSTEM")

        and: "create RunRequestDto"
        RunRequestDto runRequestDto = runRequest

        and: "stub subsystem manager request to return connected system"
        String subsystemName = "external-system"
        WiremockUtil.stubForGet("/subsystem-manager/v1/subsystems\\?name=${subsystemName}",
                "/run/responses/subsystem/${subsystemResponse}")

        and: "stub external system request to get resource"
        WiremockUtil.stubForGet("/path/to/resource2/${runRequestDto.getInputs().get("object")}", "/run/responses/external-system/original_json.json")

        when: "execute run"
        ResultActions result = runServiceTestSteps
                .executeRunResult(subsystemName, resourceConfigName, resource, runRequestDto)

        then: "execute run fails, response status code and error code are as expected"
        result.andExpect(status().is(expStatus))
                .andExpect(jsonPath("\$.errorCode").value(expectedErrorCode))

        where:
        [subsystemResponse, resourceConfigName, resource, runRequest, expStatus, expectedErrorCode] <<
                [exception_if_connected_system_not_found(),
                 exception_if_resource_configuration_not_found(),
                 exception_if_resource_not_found(),
                 exception_if_invalid_rest_method_name(),
                 exception_if_substitution_failed_with_unknown_token_due_to_mismatching_input_name(),
                 exception_if_substitution_failed_with_unknown_token_due_to_missing_input(),
                 exception_if_resource_method_not_found_in_resource(),
                 exception_if_runRequestWithoutMethod_and_resourceHasMultipleMethods()]
    }

    def exception_if_connected_system_not_found() {
        return ["emptySubSystemResponse.json", "EXTERNAL_SYSTEM", "resource2", [method: "GET", responseFormat: "json", body: null, inputs: [object : "1"]], 404, CONNECTED_SYSTEM_NOT_FOUND.errorCode]
    }

    def exception_if_resource_configuration_not_found() {
        return ["subSystemResponse_BasicAuth.json", "not_exist", "resource2", [method: "GET", responseFormat: "json", body: null, inputs: [object : "1"]], 404, RESOURCE_CONFIGURATION_NOT_FOUND.errorCode]
    }

    def exception_if_resource_not_found() {
        return ["subSystemResponse_BasicAuth.json", "EXTERNAL_SYSTEM", "not_exist", [method: "GET", responseFormat: "json", body: null, inputs: [object : "1"]], 404, RESOURCE_NOT_FOUND.errorCode]
    }

    def exception_if_invalid_rest_method_name() {
        return ["subSystemResponse_BasicAuth.json", "EXTERNAL_SYSTEM", "resource2", [method: "nem", responseFormat: "json", body: null, inputs: [object : "1"]], 500, GENERAL_ERROR.errorCode]
    }

    def exception_if_substitution_failed_with_unknown_token_due_to_mismatching_input_name() {
        return ["subSystemResponse_BasicAuth.json", "EXTERNAL_SYSTEM", "resource2", [method: "GET", responseFormat: "json", body: null, inputs: [obj : "1"]], 500, SUBSTITUTION_FAILED.errorCode]
    }

    def exception_if_substitution_failed_with_unknown_token_due_to_missing_input() {
        return ["subSystemResponse_BasicAuth.json", "EXTERNAL_SYSTEM", "resource2", [method: "GET", responseFormat: "json", body: null, inputs: [:]], 500, SUBSTITUTION_FAILED.errorCode]
    }

    def exception_if_resource_method_not_found_in_resource() {
        return ["subSystemResponse_BasicAuth.json", "EXTERNAL_SYSTEM", "resource2", [method: "DELETE", responseFormat: "json", body: null, inputs: [object : "1"]], 404, RESOURCE_METHOD_NOT_FOUND.errorCode]
    }

    def exception_if_runRequestWithoutMethod_and_resourceHasMultipleMethods() {
        return ["subSystemResponse_BasicAuth.json", "EXTERNAL_SYSTEM", "resource1", [responseFormat: "json", body: null, inputs: [object : "1"]], 500, METHOD_NAME_REQUIRED_IN_RUNREQUEST.errorCode]
    }


    def "Execute run fails, xml response from external system not supported when responseFormat is json"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "EXTERNAL_SYSTEM"
        ResourceConfigurationDto dto = resourceConfigurationTestSteps.uploadResourceConfiguration("/run/resource-configuration/external_system.yml",
                resourceConfigurationName)

        and: "create RunRequestDto"
        Map inputs = [object : "1"]
        RunRequestDto runRequestDto = new RunRequestDto()
                .method("GET")
                .responseFormat("json")
                .body(null)
                .inputs(inputs)

        and: "stub subsystem manager request to return connected system"
        String subsystemName = "external-system"
        WiremockUtil.stubForGet("/subsystem-manager/v1/subsystems\\?name=${subsystemName}",
                "/run/responses/subsystem/subSystemResponse_BasicAuth.json")

        and: "stub external system request to get resource"
        WiremockUtil.stubForGetAndSingleResponseHeader("/path/to/resource2/${inputs.get("object")}", "application/xml",
                "/run/responses/external-system/original_xml.xml")

        when: "execute run"
        ResultActions result = runServiceTestSteps
                .executeRunResult(subsystemName, resourceConfigurationName, "resource2", runRequestDto)

        then: "execute run fails, response status code and error code are as expected"
        result.andExpect(status().is(500))
                .andExpect(jsonPath("\$.errorCode").value(CONTENT_TYPE_IN_HTTPRESPONSE_NOT_SUPPORTED.errorCode))
    }

    @Unroll
    def "Request headers are created as expected using inputs, globalRequestHeaders and inbound headers"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "EXTERNAL_SYSTEM"
        Map replacements = ["%GLOBAL_HEADER%" : globalHeader, "%INBOUND_HEADER%" : inboundHeader] as Map<String, String>
        ResourceConfigurationDto dto = resourceConfigurationTestSteps
                .uploadResourceConfiguration("/run/resource-configuration/request_headers.yml", resourceConfigurationName, replacements)

        and: "create RunRequestDto"
        Map inputs = [header : "application/ld+json"]
        RunRequestDto runRequestDto = new RunRequestDto()
                .method("GET")
                .responseFormat("original")
                .body(null)
                .inputs(inputs)

        and: "stub subsystem manager request to return connected system"
        String subsystemName = "external-system"
        WiremockUtil.stubForGet("/subsystem-manager/v1/subsystems\\?name=${subsystemName}",
                "/run/responses/subsystem/subSystemResponse_BasicAuth.json")

        and: "stub external system request to get resource"
        WiremockUtil.stubForGetAndRequestHeaders("/path/to/resource1/subresource", headerCount, headerName1, headerValue1, headerName2, headerValue2)

        when: "execute run"
        ResultActions result = runServiceTestSteps
                .executeRunResult(subsystemName, resourceConfigurationName, "resource1", runRequestDto)

        then: "execute run successfully completes"
        result.andExpect(status().is(200))

        where: ""
        globalHeader                          | inboundHeader                         | headerCount | headerName1    | headerValue1                   | headerName2 | headerValue2
        "~"                                   | "~"                                   | 0           | null           | null                           | null        | null
        "content-type: [application/json]"    | "~"                                   | 1           | "content-type" | "application/json"             | null        | null
        "~"                                   | "accept: [application/json]"          | 1           | "accept"       | "application/json"             | null        | null
        "content-type: [application/json]"    | "content-type: [application/ld+json]" | 1           | "content-type" | "application/ld+json"          | null        | null
        "content-type: [application/json]"    | "accept: [application/json]"          | 2           | "content-type" | "application/json"             | "accept"    | "application/json"
        "content-type: [application/json]"    | "content-type: ['{{auth_token}}']"    | 1           | "content-type" | "ZW8tdXNlcjphYmNkZUAxMjM0NQ==" | null        | null
        "content-type: ['{{auth_token}}']"    | "accept: [application/json]"          | 2           | "content-type" | "ZW8tdXNlcjphYmNkZUAxMjM0NQ==" | "accept"    | "application/json"
        "content-type: [application/json]"    | "content-type: ['{{inputs.header}}']" | 1           | "content-type" | "application/ld+json"          | null        | null
        "content-type: ['{{inputs.header}}']" | "accept: [application/json]"          | 2           | "content-type" | "application/ld+json"          | "accept"    | "application/json"
    }

    @Unroll
    def "Request url is created as expected using inputs, path and inbound queryParams"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "EXTERNAL_SYSTEM"
        Map replacements = ["%QUERY_PARAMS%" : queryParams] as Map<String, String>
        ResourceConfigurationDto dto = resourceConfigurationTestSteps
                .uploadResourceConfiguration("/run/resource-configuration/request_queryParams.yml", resourceConfigurationName, replacements)

        and: "create RunRequestDto"
        Map inputs = [queryValue : "foo"]
        RunRequestDto runRequestDto = new RunRequestDto()
                .method("GET")
                .responseFormat("original")
                .body(null)
                .inputs(inputs)

        and: "stub subsystem manager request to return connected system"
        String subsystemName = "external-system"
        WiremockUtil.stubForGet("/subsystem-manager/v1/subsystems\\?name=${subsystemName}",
                "/run/responses/subsystem/subSystemResponse_BasicAuth.json")

        and: "stub external system request to get resource"
        WiremockUtil.stubForGetAndQueryParams("/path/to/resource1/subresource", queryCount, queryParamName1, queryParamValue1, queryParamName2, queryParamValue2)

        when: "execute run"
        ResultActions result = runServiceTestSteps
                .executeRunResult(subsystemName, resourceConfigurationName, "resource1", runRequestDto)

        then: "execute run successfully completes"
        result.andExpect(status().is(200))

        where: ""
        queryParams                                          | queryCount | queryParamName1 | queryParamValue1                           | queryParamName2 | queryParamValue2
        "?name=subresource_one"                              | 1          | "name"          | "subresource_one"                          | null            | null
        "?\$filter=isVimAssigned=true 'and' projectName=xyz" | 1          | "\$filter"      | "isVimAssigned=true 'and' projectName=xyz" | null            | null
        "?name1=value1&name2=value2"                         | 2          | "name1"         | "value1"                                   | "name2"         | "value2"
        "?name1={{inputs.queryValue}}&name2=value2"          | 2          | "name1"         | "foo"                                      | "name2"         | "value2"
    }

    @Unroll
    def "Request content is created as expected using inputs, body and inbound transformationInTemplate"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "EXTERNAL_SYSTEM"
        Map replacements = ["%TRANSFORMATION_IN_TEMPLATE%" : transformationInTemplate] as Map<String, String>
        ResourceConfigurationDto dto = resourceConfigurationTestSteps
                .uploadResourceConfiguration("/run/resource-configuration/${resourceConfig}", resourceConfigurationName, replacements)

        and: "create RunRequestDto"
        Map inputs = inputsMap
        RunRequestDto runRequestDto = new RunRequestDto()
                .method("POST")
                .responseFormat(responseFormat)
                .body(body)
                .inputs(inputs)

        and: "stub subsystem manager request to return connected system"
        String subsystemName = "external-system"
        WiremockUtil.stubForGet("/subsystem-manager/v1/subsystems\\?name=${subsystemName}",
                "/run/responses/subsystem/subSystemResponse_BasicAuth.json")

        and: "stub external system request to post resource"
        WiremockUtil.stubForPostAndRequestBody("/path/to/resource1/subresource", expectedBody)

        when: "execute run"
        ResultActions result = runServiceTestSteps
                .executeRunResult(subsystemName, resourceConfigurationName, "resource1", runRequestDto)

        then: "execute run successfully completes"
        result.andExpect(status().is(200))

        where:
        resourceConfig          | responseFormat | inputsMap      | body                  | transformationInTemplate             | expectedBody
        'request_content.yml'   | "original"     | [name : "foo"] | null                  | '{"objectName" : "{{inputs.name}}"}' | '{"objectName" : "foo"}'
        'request_content.yml'   | "original"     | [name : "foo"] | '{"name1": "value1"}' | '{"objectName" : "{{inputs.name}}"}' | '{"name1": "value1"}'
        'request_content_2.yml' | "original"     | [name : "foo"] | null                  | '~'                                  | null
        'request_content.yml'   | "json"         | [name : "foo"] | null                  | '{"objectName" : "{{inputs.name}}"}' | '{"objectName" : "foo"}'
        'request_content.yml'   | "json"         | [name : "foo"] | '{"name1": "value1"}' | '{"objectName" : "{{inputs.name}}"}' | '{"name1": "value1"}'
        'request_content_2.yml' | "json"         | [name : "foo"] | null                  | '~'                                  | null
    }

    @Unroll
    def "Response status is created as expected using inputs and outbound code"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "EXTERNAL_SYSTEM"
        Map replacements = ["%CODE%" : code] as Map<String, String>
        ResourceConfigurationDto dto = resourceConfigurationTestSteps
                .uploadResourceConfiguration("/run/resource-configuration/response_status.yml", resourceConfigurationName, replacements)

        and: "create RunRequestDto"
        Map inputs = [responseCode : "202"]
        RunRequestDto runRequestDto = new RunRequestDto()
                .method("GET")
                .responseFormat(responseFormat)
                .body(null)
                .inputs(inputs)

        and: "stub subsystem manager request to return connected system"
        String subsystemName = "external-system"
        WiremockUtil.stubForGet("/subsystem-manager/v1/subsystems\\?name=${subsystemName}",
                "/run/responses/subsystem/subSystemResponse_BasicAuth.json")

        and: "stub external system request to get resource"
        WiremockUtil.stubForGetAndResponseStatus("/path/to/resource1/subresource", originalResponseStatus,
                "/run/responses/external-system/original_json.json")

        when: "execute run"
        ResultActions result = runServiceTestSteps
                .executeRunResult(subsystemName, resourceConfigurationName, "resource1", runRequestDto)

        then: "execute run successfully completes"
        result.andExpect(status().is(expectedResponseStatus))

        where: ""
        responseFormat | code                        | originalResponseStatus | expectedResponseStatus
        "original"     | "200"                       | 200                    | 200
        "original"     | "201"                       | 200                    | 200
        "original"     | ""                          | 200                    | 200
        "original"     | "~"                         | 200                    | 200
        "original"     | "'{{inputs.responseCode}}'" | 200                    | 200
        "json"         | "200"                       | 200                    | 200
        "json"         | "201"                       | 200                    | 201
        "json"         | "200"                       | 201                    | 200
        "json"         | ""                          | 200                    | 200
        "json"         | "'{{inputs.responseCode}}'" | 200                    | 202
    }

    @Unroll
    def "Response headers are created as expected using inputs, globalResponseHeaders and outbound headers"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "EXTERNAL_SYSTEM"
        Map replacements = ["%GLOBAL_HEADERS%": globalHeaders, "%OUTBOUND_HEADERS%": outboundHeaders] as Map<String, String>
        ResourceConfigurationDto dto = resourceConfigurationTestSteps
                .uploadResourceConfiguration("/run/resource-configuration/response_headers.yml", resourceConfigurationName, replacements)

        and: "create RunRequestDto"
        Map inputs = [header : "application/ld+json"]
        RunRequestDto runRequestDto = new RunRequestDto()
                .method("GET")
                .responseFormat(responseFormat)
                .body(null)
                .inputs(inputs)

        and: "stub subsystem manager request to return connected system"
        String subsystemName = "external-system"
        WiremockUtil.stubForGet("/subsystem-manager/v1/subsystems\\?name=${subsystemName}",
                "/run/responses/subsystem/subSystemResponse_BasicAuth.json")

        and: "stub external system request to get resource"
        WiremockUtil.stubForGetAndSingleResponseHeader("/path/to/resource1/subresource", originalResponseHeaders,
                "/run/responses/external-system/original_json.json")

        when: "execute run"
        ResultActions result = runServiceTestSteps
                .executeRunResult(subsystemName, resourceConfigurationName, "resource1", runRequestDto)

        then: "execute run successfully completes"
        result.andExpect(status().is(200))
                .andExpect(header().stringValues("content-type", expectedResponseHeader))
                .andExpect(header().doesNotExist(HttpHeaders.TRANSFER_ENCODING))

        where: ""
        responseFormat | originalResponseHeaders    | globalHeaders           | outboundHeaders                       | expectedResponseHeader
        "original"     | "application/json"         | '[application/json]'    | '[application/json]'                  | "application/json"
        "original"     | "application/json"         | '[application/xml]'     | '[application/json]'                  | "application/json"
        "original"     | "application/json"         | '[application/json]'    | '[application/xml]'                   | "application/json"
        "original"     | "application/xml"          | '[application/json]'    | '[application/json]'                  | "application/xml"
        "original"     | "application/json"         | '~'                     | '~'                                   | "application/json"
        "original"     | "application/json"         | '[application/xml]'     | '~'                                   | "application/json"
        "original"     | "application/json"         | '~'                     | '[application/xml]'                   | "application/json"
        "original"     | "application/xml"          | '~'                     | '~'                                   | "application/xml"
        "original"     | "text/html; charset=utf-8" | '~'                     | '[text/html; charset=utf-8]'          | "text/html; charset=utf-8"
        "original"     | "application/json"         | '["{{inputs.header}}"]' | '~'                                   | "application/json"
        "json"         | "application/json"         | '[application/json]'    | '[application/json]'                  | "application/json"
        "json"         | "application/json"         | '[application/xml]'     | '[application/json]'                  | "application/json"
        "json"         | "application/json"         | '[application/json]'    | '[application/xml]'                   | "application/xml"
//        "json"         | "application/xml"          | '[application/json]'    | '[application/json]'                  | "application/json" RS-10:ContentType 'xml' in HttpResponse not (yet) supported
        "json"         | "application/json"         | '[application/xml]'     | '~'                                   | "application/xml"
        "json"         | "application/json"         | '~'                     | '[application/xml]'                   | "application/xml"
        "json"         | "application/json"         | '~'                     | '[application/xml; application/json]' | "application/xml; application/json"
//        "json"         | "application/xml"          | '~'                     | '~'                                   | "application/xml" RS-10:ContentType 'xml' in HttpResponse not (yet) supported
        "json"         | "application/json"         | '["{{inputs.header}}"]' | '~'                                   | "application/ld+json"
        "json"         | "application/json"         | '~'                     | '["{{inputs.header}}"]'                 | "application/ld+json"
    }

    @Unroll
    def "Response content is created as expected using inputs and outbound transformationOutTemplate"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "EXTERNAL_SYSTEM"
        Map replacements = ["%TRANSFORMATION_OUT_TEMPLATE%": transformationOutTemplate] as Map<String, String>
        ResourceConfigurationDto dto = resourceConfigurationTestSteps
                .uploadResourceConfiguration("/run/resource-configuration/${resourceConfig}", resourceConfigurationName, replacements)

        and: "create RunRequestDto"
        RunRequestDto runRequestDto = new RunRequestDto()
                .method("GET")
                .responseFormat(responseFormat)
                .body(null)
                .inputs(null)

        and: "stub subsystem manager request to return connected system"
        String subsystemName = "external-system"
        WiremockUtil.stubForGet("/subsystem-manager/v1/subsystems\\?name=${subsystemName}",
                "/run/responses/subsystem/subSystemResponse_BasicAuth.json")

        and: "stub external system request to get resource"
        WiremockUtil.stubForGetAndSingleResponseHeader("/path/to/resource1/subresource", originalResponseHeaders,
                "/run/responses/external-system/${originalResponseContent}")

        when: "execute run"
        ResultActions result = runServiceTestSteps
                .executeRunResult(subsystemName, resourceConfigurationName, "resource1", runRequestDto)

        then: "execute run successfully completes"
        result.andExpect(status().is(200))
                .andExpect(content().string(readClasspathResourceAsString("/run/responses/${expectedResponseContent}")))

        where: ""
        responseFormat | resourceConfig           | transformationOutTemplate                   | originalResponseContent | originalResponseHeaders | expectedResponseContent
        "original"     | "response_content.yml"   | '{"objectName" : "{{response.body.name}}"}' | "original_json.json"    | "application/json"      | "external-system/original_json.json"
        "original"     | "response_content.yml"   | '{"objectName" : "{{response.body.name}}"}' | "original_xml.xml"      | "application/xml"       | "external-system/original_xml.xml"
        "original"     | "response_content.yml"   | '{"objectName" : "{{response.body.name}}"}' | "original_text.txt"     | "text/html"             | "external-system/original_text.txt"
        "json"         | "response_content.yml"   | '{"objectName" : "{{response.body.name}}"}' | "original_json.json"    | "application/json"      | "transformationOutTemplate.json"
        "json"         | "response_content_2.yml" | '~'                                         | "original_json.json"    | "application/json"      | "response.json"
//        "json"         | "response_content.yml"   | '{"objectName" : "{{response.body.name}}"}' | "original_text.txt"     | "text/html"             | "external-system/original_text.txt"   no supported yet for non-json in json response
//        "json"         | "response_content.yml"   | '{"objectName" : "{{response.body.name}}"}' | "original_xml.xml"      | "application/xml"       | "external-system/original_xml.xml"    RS-10:ContentType 'xml' in HttpResponse not (yet) supported.
    }

    def "Original octet-stream response with zip archive is successfully returned"() {
        setup: "Upload resource configuration"
        String resourceConfigurationName = "EXTERNAL_SYSTEM"
        Map replacements = ["%TRANSFORMATION_OUT_TEMPLATE%": "~"]
        ResourceConfigurationDto dto = resourceConfigurationTestSteps
                .uploadResourceConfiguration("/run/resource-configuration/response_content_2.yml", resourceConfigurationName, replacements)

        and: "create RunRequestDto to return original response"
        RunRequestDto runRequestDto = new RunRequestDto().method("GET").responseFormat("original")

        and: "stub subsystem manager request to return connected system"
        String subsystemName = "external-system"
        WiremockUtil.stubForGet("/subsystem-manager/v1/subsystems\\?name=${subsystemName}",
                "/run/responses/subsystem/subSystemResponse_BasicAuth.json")

        and: "stub external system request to return zip archive with content-type octet-stream"
        WiremockUtil.stubForGetAndSingleResponseHeader("/path/to/resource1/subresource", "application/octet-stream",
                "/run/responses/external-system/original.zip")

        when: "execute run"
        ResultActions result = runServiceTestSteps
                .executeRunResult(subsystemName, resourceConfigurationName, "resource1", runRequestDto)

        then: "execute run successfully completes and content is as expected"
        result.andExpect(status().is(200))
                .andExpect(content().bytes(readClasspathResourceAsBytes("/run/responses/external-system/original.zip")))
    }
}
