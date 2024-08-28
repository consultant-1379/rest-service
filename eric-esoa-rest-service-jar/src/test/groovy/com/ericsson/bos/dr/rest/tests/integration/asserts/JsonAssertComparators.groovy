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
package com.ericsson.bos.dr.rest.tests.integration.asserts

import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.RegularExpressionValueMatcher
import org.skyscreamer.jsonassert.comparator.CustomComparator

class JsonAssertComparators {

    public static CustomComparator RC_COMPARATOR = new CustomComparator(JSONCompareMode.STRICT,
            new Customization("id", new RegularExpressionValueMatcher()),
            new Customization("createdAt", new RegularExpressionValueMatcher()),
            new Customization("modifiedAt", new RegularExpressionValueMatcher()))

    public static CustomComparator RC_LIST_COMPARATOR = new CustomComparator(JSONCompareMode.STRICT,
            new Customization("[*].id", new RegularExpressionValueMatcher()),
            new Customization("[*].createdAt", new RegularExpressionValueMatcher()),
            new Customization("[*].modifiedAt", new RegularExpressionValueMatcher()))
}
