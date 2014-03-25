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

import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for all types which extend the &lt;ReloadingMetadataProviderType&gt;.
 */
public abstract class AbstractReloadingMetadataProviderParser extends AbstractMetadataProviderParser {
    
    /** The reference to the system parser pool that we set up. */
    private static final String DEFAULT_PARSER_POOL_REF = "shibboleth.ParserPool";

    /** The reference to the system wide timer that we set up. */
    private static final String DEFAULT_TIMER_REF = "shibboleth.TaskTimer";
    
    /** The default delay factor. */
    private static final String DEFAULT_DELAY_FACTOR = "0.75";

    /**
     * 
     * {@inheritDoc}
     * 
     * We assume that we will be summoning up a class which extends an
     * {@link org.opensaml.saml.metadata.resolver.impl.AbstractReloadingMetadataResolver}.
     */
    @Override protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

        super.doParse(element, parserContext, builder);
        builder.addConstructorArgReference(getTaskTimerRef(element));
        
        if (element.hasAttributeNS(null, "refreshDelayFactor")) {
            builder.addPropertyValue("refreshDelayFactor", element.getAttributeNS(null,"refreshDelayFactor"));
        } else {
            builder.addPropertyValue("refreshDelayFactor", DEFAULT_DELAY_FACTOR);
        }
        
        if (element.hasAttributeNS(null, "maxRefreshDelay")) {
            builder.addPropertyValue("maxRefreshDelay", element.getAttributeNS(null,"maxRefreshDelay"));
        }
        
        if (element.hasAttributeNS(null, "minRefreshDelay")) {
            builder.addPropertyValue("minRefreshDelay", element.getAttributeNS(null,"minRefreshDelay"));
        }
        
        builder.addPropertyReference("parserPool", getParserPoolRef(element));
    }
    
    /**
     * Gets the default task timer reference for the metadata provider.
     * 
     * @param element metadata provider configuration element
     * 
     * @return task timer reference
     */
    protected String getTaskTimerRef(Element element) {
        String taskTimerRef = null;
        if (element.hasAttributeNS(null, "taskTimerRef")) {
            taskTimerRef = StringSupport.trimOrNull(element.getAttributeNS(null, "taskTimerRef"));
        }

        if (taskTimerRef == null) {
            taskTimerRef = DEFAULT_TIMER_REF;
        }

        return taskTimerRef;
    }
    
    /**
     * Gets the default parser pool reference for the metadata provider.
     * 
     * @param element metadata provider configuration element
     * 
     * @return parser pool reference
     */
    protected String getParserPoolRef(Element element) {
        String parserPoolRef = null;
        if (element.hasAttributeNS(null, "parerPoolRef")) {
            parserPoolRef = StringSupport.trimOrNull(element.getAttributeNS(null, "parserPoolRef"));
        }

        if (parserPoolRef == null) {
            parserPoolRef = DEFAULT_PARSER_POOL_REF;
        }

        return parserPoolRef;
    }
}