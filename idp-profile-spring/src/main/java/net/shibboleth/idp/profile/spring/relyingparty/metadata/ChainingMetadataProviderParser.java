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

package net.shibboleth.idp.profile.spring.relyingparty.metadata;

import java.util.List;

import javax.xml.namespace.QName;

import net.shibboleth.idp.spring.SpringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.opensaml.saml.metadata.resolver.impl.ChainingMetadataResolver;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for &pt;ChainingMetadataProvider&gt;.
 */
public class ChainingMetadataProviderParser extends AbstractMetadataProviderParser {

    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(MetadataNamespaceHandler.NAMESPACE, "ChainingMetadataProvider");

    /** {@inheritDoc} */
    @Override protected Class<ChainingMetadataResolver> getBeanClass(Element element) {
        return ChainingMetadataResolver.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        final List<Element> childProviders =
                ElementSupport.getChildElements(element, MetadataNamespaceHandler.METADATA_ELEMENT_NAME);

        builder.addPropertyValue("resolvers", SpringSupport.parseCustomElements(childProviders, parserContext));
    }
}