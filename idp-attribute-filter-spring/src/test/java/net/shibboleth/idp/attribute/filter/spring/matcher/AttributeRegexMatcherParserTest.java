/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.idp.attribute.filter.spring.matcher;

import net.shibboleth.idp.attribute.filter.matcher.impl.AttributeValueRegexpMatcher;
import net.shibboleth.idp.attribute.filter.spring.BaseAttributeFilterParserTest;
import net.shibboleth.idp.attribute.filter.spring.matcher.impl.AttributeValueRegexMatcherParser;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * test for {@link AttributeValueRegexMatcherParser}.
 */
public class AttributeRegexMatcherParserTest extends BaseAttributeFilterParserTest {

    @Test public void matcher() throws ComponentInitializationException {
        AttributeValueRegexpMatcher what = (AttributeValueRegexpMatcher) getMatcher("attributeRegex.xml", true);
        
        Assert.assertEquals(what.getRegularExpression(), "^jsmit.*$");

        what = (AttributeValueRegexpMatcher) getMatcher("attributeRegex.xml", false);
        
        Assert.assertEquals(what.getRegularExpression(), "^jsmit.*$");
    }
}
