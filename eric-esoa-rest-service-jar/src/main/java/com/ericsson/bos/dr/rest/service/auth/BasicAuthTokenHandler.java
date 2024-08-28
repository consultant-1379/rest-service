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

import static com.ericsson.bos.dr.rest.service.connectivity.Subsystem.AUTH_METHOD;
import static com.ericsson.bos.dr.rest.service.connectivity.Subsystem.AUTH_PASSWORD;
import static com.ericsson.bos.dr.rest.service.connectivity.Subsystem.AUTH_TOKENREF;
import static com.ericsson.bos.dr.rest.service.connectivity.Subsystem.AUTH_TYPE_BASIC_AUTH_TOKEN;
import static com.ericsson.bos.dr.rest.service.connectivity.Subsystem.AUTH_URL;
import static com.ericsson.bos.dr.rest.service.connectivity.Subsystem.AUTH_USER_NAME;

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
 * Basic auth token handler. Retrieves a token of 'auth.type' BasicAuthToken to allow further access to an external system
 *
 * The <code>BasicAuthTokenHandler</code> generates a base64-encoded string 'username:password' and adds this to an authorization header in a
 * request to a token endpoint ('auth.url') of the external system.
 * It then retrieves the token of 'auth.type' BasicAuthToken from the response body using the 'auth.tokenRef' property.
 * The token will be returned inside the <code>TokenData</code> and added to the TokenCache.
 */
@Component
public class BasicAuthTokenHandler extends AbstractAuthHandler{

    @Override
    public String authType() {
        return AUTH_TYPE_BASIC_AUTH_TOKEN;
    }

    @Override
    protected void validate(final Subsystem subsystem) {
        final Set<String> errorPropertyItems = new HashSet<>();
        final var connectionProperties = subsystem.getConnection();
        if (StringUtils.isBlank(connectionProperties.getAuthUsername())) {
            errorPropertyItems.add(AUTH_USER_NAME);
        }
        if (StringUtils.isBlank(connectionProperties.getAuthPassword())) {
            errorPropertyItems.add(AUTH_PASSWORD);
        }
        if (StringUtils.isBlank(connectionProperties.getAuthTokenRef())) {
            errorPropertyItems.add(AUTH_TOKENREF);
        }
        if (StringUtils.isBlank(connectionProperties.getAuthMethod())) {
            errorPropertyItems.add(AUTH_METHOD);
        }
        if (StringUtils.isBlank(connectionProperties.getAuthUrl())) {
            errorPropertyItems.add(AUTH_URL);
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
        final var responseEntity = executeAuthRequest(subsystem, true);
        final String tokenRef = subsystem.getConnection().getAuthTokenRef();
        return TokenExtractor.extractTokenFromResponse(responseEntity, tokenRef);
    }
}
