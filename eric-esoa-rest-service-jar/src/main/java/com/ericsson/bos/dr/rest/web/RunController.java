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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.ericsson.bos.dr.rest.service.RunService;
import com.ericsson.bos.dr.rest.web.v1.api.RunApi;
import com.ericsson.bos.dr.rest.web.v1.api.model.RunRequestDto;

/**
 * Run Controller.
 */
@RestController
public class RunController implements RunApi {

    @Autowired
    private RunService runService;

    @Override
    public ResponseEntity run(final String subsystemName, final String resourceConfigurationName, final String resource,
        final RunRequestDto runRequestDto) {
        return runService.run(subsystemName, resourceConfigurationName, resource, runRequestDto);
    }
}
