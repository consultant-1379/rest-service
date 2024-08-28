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
package com.ericsson.bos.dr.rest.service.substitution;

import com.ericsson.bos.dr.rest.service.utils.JQ;
import com.ericsson.bos.dr.rest.service.utils.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;

/**
 * Built-in jinja function for JqFunction.
 */
public class JqFunction extends ELFunctionDefinition {

    /**
     * Constructor
     */
    public JqFunction() {
        super( "fn", "jq", JqFunction.class, "jq", String.class, String.class);
    }

    /**
     * Implementation of jq function
     * @param json json
     * @param jqExpressions jqExpressions
     * @return substituted jq result
     */
    public static String jq(String json, String jqExpressions) {
        return JQ.query(jqExpressions, JSON.read(json, JsonNode.class)).getObject().toString();
    }
}
