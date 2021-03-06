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

package net.shibboleth.idp.attribute.resolver.spring.enc;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Base class for Spring bean definition parser for Shibboleth scoped attribute encoders.
 */
public abstract class BaseScopedAttributeEncoderParser extends BaseAttributeEncoderParser {

    /** Local name of scope type attribute. */
    @Nonnull @NotEmpty public static final String SCOPE_TYPE_ATTRIBUTE_NAME = "scopeType";

    /** Local name of scope delimiter attribute. */
    @Nonnull @NotEmpty public static final String SCOPE_DELIMITER_ATTRIBUTE_NAME = "scopeDelimiter";

    /** Local name of scope attribute attribute. */
    @Nonnull @NotEmpty public static final String SCOPE_ATTRIBUTE_ATTRIBUTE_NAME = "scopeAttribute";

    /** {@inheritDoc} */
    @Override protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);

        if (config.hasAttributeNS(null, SCOPE_DELIMITER_ATTRIBUTE_NAME)) {
            builder.addPropertyValue("scopeDelimiter",
                    StringSupport.trimOrNull(config.getAttributeNS(null, SCOPE_DELIMITER_ATTRIBUTE_NAME)));
        }

        if (config.hasAttributeNS(null, SCOPE_ATTRIBUTE_ATTRIBUTE_NAME)) {
            builder.addPropertyValue("scopeAttributeName",
                    StringSupport.trimOrNull(config.getAttributeNS(null, SCOPE_ATTRIBUTE_ATTRIBUTE_NAME)));
        }
    }

}