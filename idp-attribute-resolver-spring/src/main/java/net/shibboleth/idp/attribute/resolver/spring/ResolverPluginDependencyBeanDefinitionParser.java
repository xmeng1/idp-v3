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

package net.shibboleth.idp.attribute.resolver.spring;

import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.resolver.ResolverPluginDependency;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/** Bean definition parser for a {@link ResolverPluginDependency}. */
public class ResolverPluginDependencyBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(AttributeResolverNamespaceHandler.NAMESPACE, "Dependency");

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ResolverPluginDependencyBeanDefinitionParser.class);

    /** {@inheritDoc} */
    protected Class<?> getBeanClass(Element element) {
        return ResolverPluginDependency.class;
    }

    /** {@inheritDoc} */
    protected void doParse(Element config, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String pluginId = StringSupport.trimOrNull(config.getAttributeNS(null, "ref"));
        log.info("Parsing configuration for {} with pluginId : {}", config.getLocalName(), pluginId);
        builder.addConstructorArgValue(pluginId);

        NamedNodeMap parentAttr = config.getParentNode().getAttributes();
        String attributeId;
        if (null == parentAttr) {
            log.error("Parsing configuration for {}: no parent element or no attributes.",  config.getLocalName());
            attributeId = config.getLocalName() + "MISSING_PARENT";
        } else {
            Node attr = parentAttr.getNamedItemNS(null, "id");
            if (null == attr) {
                log.error("Parsing configuration for {}: no 'id' in parent element.",  config.getLocalName());
                attributeId = config.getLocalName() + "MISSING_PARENTS_ID";
            } else {
                attributeId = attr.getNodeValue();
            }
        }
        
        log.info("Parsing configuration for {} with attributeId : {}", config.getLocalName(), attributeId);
        builder.addConstructorArgValue(attributeId);
    }

    /** {@inheritDoc} */
    protected boolean shouldGenerateId() {
        return true;
    }
}
