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
package com.ericsson.bos.dr.rest.tests.unit.substitution

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration

import com.ericsson.bos.dr.rest.service.exceptions.ErrorCode
import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException
import com.ericsson.bos.dr.rest.service.substitution.SubstitutionEngine
import com.google.common.collect.Maps

import spock.lang.Specification

@ContextConfiguration(classes = [SubstitutionEngine.class])
class SubstitutionEngineSpec extends Specification{

    @Autowired
    SubstitutionEngine substitutionEngine

    def "Successful substitution using replaceAtSymbol function"() {

        setup: "set substitution context map"
        Map<String, Object> contextMap = Maps.newHashMap()
        contextMap.put("originalStr", testString)

        when: "render the substitution result"
        String result = substitutionEngine.render("{{fn:replaceAtSymbol(originalStr)}}", contextMap)

        then: "substitution result is as expected"
        result == expectedResult

        where:
        testString                 | expectedResult
        "someone@ericsson.com"     | "someone__ericsson.com"
        "someone@e@r@icsson.com"   | "someone__e__r__icsson.com"
        "someone@e*r!>?icsson.com" | "someone__e*r!>?icsson.com"
        '''someone@ericsson.com''' | '''someone__ericsson.com'''
    }

    def "Successful substitution using jq function"() {

        when: "render the substitution result"
        String result = substitutionEngine.render("{{fn:jq('" + json + "','" + jqExpression + "')}}", [:])

        then: "substitution result is as expected"
        result == expectedResult

        where:
        json                     | jqExpression | expectedResult
        '{"id": 1}'              | ".id"        | "1"
        '{"value": true}'        | ".value"     | "true"
        '[{"id": 1}, {"id": 2}]' | ".[] | {id}" |"[{id=1}, {id=2}]"
    }

    def "Successful substitution using Groovy function"() {
        when: "substitutionEngine renders the substitution result with a Groovy script"
        String result = substitutionEngine.render("{{fn:groovy(${script})}}", substitutionContext)

        then: "substitution result is as expected"
        result == expectedResult

        where:
        script                                                                     || expectedResult
        "'\"testing\"'"                                                            || "testing"
        "'\"testing\".replace(\"ing\", \"ing a method call\")'"                    || "testing a method call"
        "'2 + 2'"                                                                  || "4"
        "'\"\${arg1} \${arg2}\"', 'testing', 'concatenation'"                      || "testing concatenation"
        "'\"\${arg1[arg2]} \${arg1[arg3]}\"', inputs, 'entry1', 'additionalEntry'" || "testing a map and concatenation"
        _________________________________________

        substitutionContext << [
            [:],
            [:],
            [:],
            [:],
            [inputs: [
                    entry1: "testing a map",
                    additionalEntry: "and concatenation"
                ]]
        ]
    }

    def "Successful substitution using currentTimeStamp function"() {

        when: "render the result for currentTimeStamp"
        String result = substitutionEngine.render("{{fn:currentTimeStamp(\"yyyy-MM-dd'T'HH:mm:ss.SSSxxx\")}}", [:])

        then: "substitution result is as expected"
        !result.isBlank()
    }

    def "Successful substitution using currentTimeMillis function"() {

        when: "render the result for currentTimeMillis"
        String result = substitutionEngine.render("{{fn:currentTimeMillis()}}", [:])

        then: "substitution result is as expected"
        !result.isBlank()
    }

    def "Exception is thrown when property is incorrect or missing"() {

        setup: "set substitution context map"
        Map<String, Object> contextMap = Maps.newHashMap()
        contextMap.put(contextMapKey, "someone@ericsson.com")

        when: "render the substitution result"
        String template = "{{" + namespace + ":" + functionName + "(" + param + ")"+ "}}"
        substitutionEngine.render(template, contextMap)

        then: "RestServiceException is thrown"
        RestServiceException restServiceException = thrown(RestServiceException)
        restServiceException.errorMessage.errorCode == ErrorCode.SUBSTITUTION_FAILED.errorCode

        where:
        namespace          | functionName      | param          | contextMapKey
        "fn"               | "replaceAtSymbol" | "unknownParam" | "originalStr"
        "fn"               | "replaceAtSymbol" | null           | "originalStr"
        "unknownNamespace" | "replaceAtSymbol" | "originalStr"  | "originalStr"
        null               | "replaceAtSymbol" | "originalStr"  | "originalStr"
        "fn"               | "unknownMethod"   | "originalStr"  | "originalStr"
        "fn"               | null              | "originalStr"  | "originalStr"
        "fn"               | "replaceAtSymbol" | "originalStr"  | "unknownKey"
    }

    def "Exception is thrown when Groovy script is invalid"() {
        when: "substitutionEngine renders the substitution result with an invalid Groovy script"
        def script = "'System.currentTime()'"
        String result = substitutionEngine.render("{{fn:groovy(${script})}}", [:])

        then: "substitution result is an exception as expected"
        def exception = thrown(RestServiceException)
        exception.getMessage().contains("No signature of method: static java.lang.System.currentTime() is applicable for argument")
    }
}
