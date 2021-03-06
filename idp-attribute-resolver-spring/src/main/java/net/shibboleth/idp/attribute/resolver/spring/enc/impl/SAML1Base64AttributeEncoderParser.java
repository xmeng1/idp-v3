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

package net.shibboleth.idp.attribute.resolver.spring.enc.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.resolver.spring.enc.BaseAttributeEncoderParser;
import net.shibboleth.idp.attribute.resolver.spring.impl.AttributeResolverNamespaceHandler;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML1ByteAttributeEncoder;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Spring Bean Definition Parser for {@link SAML1ByteAttributeEncoder}.
 */
public class SAML1Base64AttributeEncoderParser extends BaseAttributeEncoderParser {

    /** Schema type name: enc: (legacy). */
    @Nonnull public static final QName TYPE_NAME_ENC = new QName(AttributeEncoderNamespaceHandler.NAMESPACE, 
            "SAML1Base64");

    /** Schema type name: resolver:. */
    @Nonnull public static final QName TYPE_NAME_RESOLVER = new QName(AttributeResolverNamespaceHandler.NAMESPACE, 
            "SAML1Base64");

    /** Local name of namespace attribute. */
    @Nonnull @NotEmpty public static final String NAMESPACE_ATTRIBUTE_NAME = "namespace";

    /** Constructor. */
    public SAML1Base64AttributeEncoderParser() {
        setNameRequired(true);
    }

    /** {@inheritDoc} */
    @Override protected Class<SAML1ByteAttributeEncoder> getBeanClass(@Nullable final Element element) {
        return SAML1ByteAttributeEncoder.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);

        if (config.hasAttributeNS(null, NAMESPACE_ATTRIBUTE_NAME)) {
            final String namespace = StringSupport.trimOrNull(config.getAttributeNS(null, NAMESPACE_ATTRIBUTE_NAME));
            builder.addPropertyValue("namespace", namespace);
        }
    }

}