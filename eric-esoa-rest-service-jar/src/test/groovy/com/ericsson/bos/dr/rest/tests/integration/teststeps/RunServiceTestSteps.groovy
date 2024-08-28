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

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.ericsson.bos.dr.rest.tests.integration.utils.JsonUtils
import com.ericsson.bos.dr.rest.web.v1.api.model.RunRequestDto
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions

@Component
class RunServiceTestSteps {

    private static final String RUN_URL = "/rest-service/v1/run"

    @Autowired
    private MockMvc mockMvc


    ResultActions executeRunResult(String subsystemName, String resourceConfigurationName, String resource,
        RunRequestDto runRequestDto) {
        String url = StringUtils.join(RUN_URL,"/",subsystemName,"/",resourceConfigurationName,"/",resource)

        return mockMvc.perform(post(url)
                .contentType(APPLICATION_JSON)
                .content(JsonUtils.toJsonString(runRequestDto)))
//                .andExpect(status().isOk())
    }
}
