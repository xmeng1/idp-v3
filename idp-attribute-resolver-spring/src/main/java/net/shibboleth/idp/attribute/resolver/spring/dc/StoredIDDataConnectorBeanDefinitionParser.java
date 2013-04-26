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

package net.shibboleth.idp.attribute.resolver.spring.dc;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.resolver.impl.dc.StoredIDDataConnector;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Spring bean definition parser for {@link StoredIDDataConnector}.
 */
public class StoredIDDataConnectorBeanDefinitionParser extends BaseComputedIDDataConnectorBeanDefinitionParser {

    /** Schema type name. */
    public static final QName TYPE_NAME = new QName(DataConnectorNamespaceHandler.NAMESPACE, "StoredId");


    /** {@inheritDoc} */
    protected Class getBeanClass(Element element) {
        return StoredIDDataConnector.class;
    }

    /** {@inheritDoc} */
    protected void doParse(Element config, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder, "storedId");
        builder.addPropertyValue("dataSource", getDataSource(config));
    }
    
    /**
     * Get the dataSource from the configuration.
     * @param config the DOM element under consideration. 
     * @return the DataSource
     */
    protected DataSource getDataSource(Element config) {
        // TODO
        return null;
    }
}