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

import com.ericsson.bos.dr.rest.service.connectivity.Subsystem;

/**
 * Generates the auth token, required to make a REST call to an external system
 */
public interface AuthHandler {

    /**
     * returns auth key property of the subsystem, which is used as a key in the substitution context and the auth_token_cache
     * @param subsystem subsystem
     * @return auth key
     */
    String getAuthKey(Subsystem subsystem);

    /**
     * returns generated auth token, which will replace the auth key substitution tag in the auth header when making the REST call to external system
     * @param subsystem subsystem
     * @param authKey auth key property of the subsystem, which is used as a key in the substitution context and the auth_token_cache
     * @return auth token
     */
    String getAuthToken(Subsystem subsystem, String authKey);

    /**
     * auth handler type, used to instantiate the correct auth handler in the <code>AuthHandlerFactory</code>
     * @return auth handler type
     */
    String authType();
}
