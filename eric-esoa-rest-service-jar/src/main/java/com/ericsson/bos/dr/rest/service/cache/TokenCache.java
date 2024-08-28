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
package com.ericsson.bos.dr.rest.service.cache;

import java.util.function.Supplier;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.ericsson.bos.dr.rest.service.auth.TokenData;

/**
 * TokenData cache.
 * Loads cache with <code>TokenData</code> from supplier on first call or when entry has expired.
 * Retrieves TokenData from cache on subsequent calls
 */
@Component
public class TokenCache {

    /**
     * Loads cache with <code>TokenData</code> from supplier on first call or when entry has expired.
     * Retrieves TokenData from cache on subsequent calls
     *
     * @param authKey key for which to retrieve the TokenData
     * @param tokenDataSupplier the TokenData supplier
     * @return the TokenData
     */
    @Cacheable(value = "auth_token_cache", key = "#authKey")
    public TokenData getTokenData(String authKey, Supplier<TokenData> tokenDataSupplier) {
        return tokenDataSupplier.get();
    }

}
