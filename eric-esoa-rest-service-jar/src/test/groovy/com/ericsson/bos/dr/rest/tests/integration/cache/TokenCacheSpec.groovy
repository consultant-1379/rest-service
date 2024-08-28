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
package com.ericsson.bos.dr.rest.tests.integration.cache

import com.ericsson.bos.dr.rest.service.auth.TokenData
import com.ericsson.bos.dr.rest.service.cache.TokenCache
import com.ericsson.bos.dr.rest.tests.integration.BaseSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.caffeine.CaffeineCache

import java.util.function.Supplier

class TokenCacheSpec extends BaseSpec {

    @Autowired
    TokenCache tokenCache

    @Autowired
    CaffeineCache caffeineCache

    def "token is read only once from TokenSupplier and subsequently from cache"() {
        setup: "invalidate cache"
        caffeineCache.getNativeCache().invalidateAll()

        and: "Mock the TokenData Supplier"
        def tokenData = new TokenData(token: "abcd", expireSeconds: 5)
        Supplier<TokenData> suppplier = Mock(Supplier)

        when: "multiple calls to get token"
        tokenCache.getTokenData("tokenCacheKey", suppplier)
        tokenCache.getTokenData("tokenCacheKey", suppplier)
        TokenData actualTokenData = tokenCache.getTokenData("tokenCacheKey", suppplier)

        then: "supplier of TokenData only called once"
        1 * suppplier.get() >> tokenData

        and: "TokenData correctly retrieved using TokenCache"
        assert tokenData == actualTokenData

        and: "cache is as expected"
        assert caffeineCache.getName() == "auth_token_cache"
        assert caffeineCache.getNativeCache().estimatedSize() == 1
        assert (TokenData) caffeineCache.getNativeCache().getIfPresent("tokenCacheKey") == tokenData
    }

}
