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
package com.ericsson.bos.dr.rest.service.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * JSON operations.
 */
public abstract class JSON {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ObjectMapper RAW_STRING_MAPPER = new ObjectMapper()
            .registerModule(new SimpleModule().addDeserializer(String.class, new RawStringDeserializer()));

    private JSON() {}

    /**
     * checks if a string is valid json
     * @param jsonStr String to be checked
     * @return boolean true (json string) or false (not json string)
     */
    public static boolean isJsonStr(String jsonStr) {
        try {
            readObjectForSubstitution(jsonStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Read json string to an appropriate object, which may
     * be a list, map, string, number or boolean.
     * <p>
     * This method is intended for use when deserializing a json
     * string for use in jinja substitution context. There is special handling for
     * string values, whereby the raw string value is used so that escape
     * characters in the original json response are retained. By default the escape
     * characters such as '\' would be removed during deserialization, leading to
     * invalid json after substitution.
     * For example a json string <code>{"message": "invalid attribute \"a1\"}</code>
     * would be deserialized as <code>{"message": "invalid attribute "a1"}</code> leading
     * to invalid json returned after substitution.
     * </p>
     * @param json json value
     * @return Object
     */
    public static Object readObjectForSubstitution(String json) {
        try {
            final var jsonNode = MAPPER.readTree(json);
            if (jsonNode.isArray()) {
                return RAW_STRING_MAPPER.readValue(json,
                        RAW_STRING_MAPPER.getTypeFactory().constructCollectionType(List.class, Object.class));
            } else if (jsonNode.isObject()) {
                return RAW_STRING_MAPPER.readValue(json, Map.class);
            } else {
                return RAW_STRING_MAPPER.readValue(json, Object.class);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Read json string to a target class type.
     * @param value json string
     * @param type target class type
     * @param <T> target type
     * @return object of target type
     */
    public static <T> T read(final String value, final Class<T> type) {
        try {
            return MAPPER.readValue(value, type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Read json array string to list of target class type.
     * @param value json array string
     * @param type target class type
     * @param <T> target type
     * @return list of target object types
     */
    public static <T> List<T> readList(final String value, final Class<T> type) {
        try {
            return MAPPER.readValue(value,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, type));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Read json string to a target class type.
     * @param value json string
     * @param typeRef target type reference
     * @param <T> target type
     * @return object of target type
     */
    public static <T> T read(final String value, TypeReference<T> typeRef) {
        try {
            return MAPPER.readValue(value, typeRef);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Write object to json string.
     * @param value object value
     * @return json string
     */
    public static String toString(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Custom string deserializer to retain the raw value including
     * escape character '\'.
     */
    private static class RawStringDeserializer extends StdDeserializer<String> {

        RawStringDeserializer() {
            this(null);
        }

        RawStringDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            final TreeNode node = jsonParser.readValueAsTree();
            return node.toString().substring(1, node.toString().length() - 1);
        }
    }
}