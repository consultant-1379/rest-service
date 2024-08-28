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

import com.ericsson.bos.dr.rest.service.utils.Groovy;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;

/**
 * Built-in Jinja function to support substitution using the result of Groovy expressions.
 */
public class GroovyFunction extends ELFunctionDefinition {

    /**
     * GroovyFunction.
     */
    public GroovyFunction() {
        super("fn", "groovy", GroovyFunction.class, "eval", String.class, Object[].class);
    }

    /**
     * Evaluate the Groovy expression with the provided array of arguments. The arguments will be
     * accessible in the expression via the (1-based) arg index - e.g arg1,arg2..argn
     *
     * @param expression groovy expression
     * @param args arguments
     * @return evaluated value
     */
    public static Object eval(final String expression, final Object... args) {
        try {
            return Groovy.evalExpression(expression, args);
        } catch (final Exception e) {
            JinjavaInterpreter.getCurrent().addError(TemplateError.fromException(e));
            return null;
        }
    }
}