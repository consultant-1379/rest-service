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

import com.fasterxml.jackson.core.type.TypeReference
import com.hubspot.jinjava.Jinjava

import java.nio.charset.StandardCharsets

class IOUtils {

    static Jinjava jinjava = new Jinjava();

    static String readClasspathResourceAsString(String resource) {
        return new String(readClasspathResourceAsBytes(resource), StandardCharsets.UTF_8)
    }

    static byte[] readClasspathResourceAsBytes(String resource) {
        final InputStream is = IOUtils.class.getResourceAsStream(resource)
        return is.readAllBytes()
    }

    static <T> T readObjectFromClassPathResource(String resource, TypeReference<T> typeRef) {
        return JsonUtils.read(readClasspathResourceAsString(resource), typeRef)
    }

    static <T> T readClasspathResourceAsBytesWithSubstitution(String resource, TypeReference<T> typeRef, Map substitutionContext) {
        return JsonUtils.read(substitute(readClasspathResourceAsString(resource), substitutionContext), typeRef)
    }

    static String substitute(String template, Map substitutionContext) {
        return jinjava.renderForResult(template, substitutionContext).getOutput()
    }

}
