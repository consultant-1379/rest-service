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

import static com.ericsson.bos.dr.rest.service.connectivity.Subsystem.AUTH_PASSWORD;
import static com.ericsson.bos.dr.rest.service.connectivity.Subsystem.AUTH_TYPE_BASIC_AUTH;
import static com.ericsson.bos.dr.rest.service.connectivity.Subsystem.AUTH_USER_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.ericsson.bos.dr.rest.service.connectivity.Subsystem;
import com.ericsson.bos.dr.rest.service.exceptions.ErrorCode;
import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException;

/**
 * Basic auth handler. Generates a base64-encoded string from 'username:password' to allow further access to an external system.
 *
 * The <code>BasicAuthHandler</code> generates a base64-encoded string 'username:password'.
 * The base64-encoded string will be returned inside the <code>TokenData</code> and added to the TokenCache.
 */
@Component
public class BasicAuthHandler extends AbstractAuthHandler {

    @Override
    public String authType() {
        return AUTH_TYPE_BASIC_AUTH;
    }

    @Override
    protected void validate(final Subsystem subsystem) {
        final Set<String> errorPropertyItems = new HashSet<>();
        if (StringUtils.isBlank(subsystem.getConnection().getAuthUsername())) {
            errorPropertyItems.add(AUTH_USER_NAME);
        }
        if (StringUtils.isBlank(subsystem.getConnection().getAuthPassword())) {
            errorPropertyItems.add(AUTH_PASSWORD);
        }
        if (!CollectionUtils.isEmpty(errorPropertyItems)) {
            final var errorMessage = StringUtils.join(errorPropertyItems, ", ");
            throw new RestServiceException(ErrorCode.AUTH_PROPERTIES_MISSING, errorMessage);
        }
    }

    @Override
    protected TokenData generateTokenData(final Subsystem subsystem) {
        final var tokenData = new TokenData();
        tokenData.setToken(generateToken(subsystem));
        tokenData.setExpireSeconds(NumberUtils.toLong(subsystem.getConnection().getAuthExpireSeconds()));
        return tokenData;
    }

    private String generateToken(final Subsystem subsystem) {
        final String userName = subsystem.getConnection().getAuthUsername();
        final String password = subsystem.getConnection().getAuthPassword();
        return Base64.getEncoder().encodeToString(StringUtils.join(userName, ":", password).getBytes(UTF_8));
    }
}
