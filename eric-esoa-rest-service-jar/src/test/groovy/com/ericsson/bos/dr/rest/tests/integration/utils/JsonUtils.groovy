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
package com.ericsson.bos.dr.rest.tests.integration.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

abstract class JsonUtils {

    private static final ObjectMapper OM = new ObjectMapper()

    private JsonUtils() {}

    static <T> T read(final String value, final Class<T> type) {
        try {
            return OM.readValue(value, type)
        } catch (IOException e) {
            throw new RuntimeException(e)
        }
    }

    static <T> List<T> readList(final String value, final Class<T> type) {
        try {
            return OM.readValue(value, OM.getTypeFactory().constructCollectionType(List.class, type))
        } catch (IOException e) {
            throw new RuntimeException(e)
        }
    }

    static <T> List<T> readList(final InputStream inputStream, final Class<T> type) {
        try {
            return OM.readValue(inputStream, OM.getTypeFactory().constructCollectionType(List.class, type))
        } catch (IOException e) {
            throw new RuntimeException(e)
        }
    }

    static String toJsonString(final Object value) {
        try {
            return OM.writeValueAsString(value)
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e)
        }
    }

    static <T> T read(final String value, TypeReference<T> typeRef) {
        try {
            return OM.readValue(value, typeRef);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
