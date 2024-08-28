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
package com.ericsson.bos.dr.rest.service.auth;

import static com.ericsson.bos.dr.rest.service.connectivity.Subsystem.AUTH_TYPE_NOAUTH;

import org.springframework.stereotype.Component;

import com.ericsson.bos.dr.rest.service.connectivity.Subsystem;

/**
 * No-auth handler
 */
@Component
public class NoAuthHandler extends AbstractAuthHandler {

    @Override
    public String getAuthKey(final Subsystem subsystem) {
        return null;
    }

    @Override
    public String authType() {
        return AUTH_TYPE_NOAUTH;
    }

    @Override
    public String getAuthToken(final Subsystem subsystem, final String authKey) {
        return null;
    }

    @Override
    protected TokenData generateTokenData(final Subsystem subsystem) {
        return null;
    }
}
