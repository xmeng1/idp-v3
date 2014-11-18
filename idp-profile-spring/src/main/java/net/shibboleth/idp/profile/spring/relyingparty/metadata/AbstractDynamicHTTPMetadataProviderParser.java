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

import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.XMLConstants;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for abstract dynamic HTTP metadata resolvers.
 */
public abstract class AbstractDynamicHTTPMetadataProviderParser extends AbstractDynamicMetadataProviderParser {
    
    /** BASIC auth username. */
    private static final String BASIC_AUTH_USER = "basicAuthUser";

    /** BASIC auth password. */
    private static final String BASIC_AUTH_PASSWORD = "basicAuthPassword";

    /** Logger. */
    private final Logger log = LoggerFactory.getLogger(AbstractDynamicHTTPMetadataProviderParser.class);
    
    /** {@inheritDoc} */
    // Checkstyle: CyclomaticComplexity OFF -- more readable not split up
    @Override protected void doNativeParse(Element element, ParserContext parserContext, 
            BeanDefinitionBuilder builder) {
        super.doNativeParse(element, parserContext, builder);

        if (element.hasAttributeNS(null, "httpClientRef")) {
            builder.addConstructorArgReference(element.getAttributeNS(null, "httpClientRef"));
            if (element.hasAttributeNS(null, "requestTimeout")
                    || element.hasAttributeNS(null, "disregardSslCertificate")
                    || element.hasAttributeNS(null, "proxyHost") || element.hasAttributeNS(null, "proxyPort")
                    || element.hasAttributeNS(null, "proxyUser") || element.hasAttributeNS(null, "proxyPassword")) {
                log.warn("httpClientRef overrides settings for requestTimeout, "
                        + "disregardSslCertificate, proxyHost, proxyPort, proxyUser and proxyPassword");
            }
        } else {
            builder.addConstructorArgValue(buildHttpClient(element));
        }
        
        if (element.hasAttributeNS(null, "credentialsProviderRef")) {
            builder.addPropertyReference("credentialsProviderRef", 
                    element.getAttributeNS(null, "credentialsProviderRef"));
            if (element.hasAttributeNS(null, BASIC_AUTH_USER) || element.hasAttributeNS(null, BASIC_AUTH_PASSWORD)) {
                log.warn("credentialsProviderRef overrides settings for basicAuthUser and basicAuthPassword");
            }
        } else {
            if (element.hasAttributeNS(null, BASIC_AUTH_USER) || element.hasAttributeNS(null, BASIC_AUTH_PASSWORD)) {
                builder.addPropertyValue("basicCredentials", buildBasicCredentials(element));
            }
        }
        
        if (element.hasAttributeNS(null, "supportedContentTypes")) {
            List<String> supportedContentTypes = StringSupport.stringToList(
                    element.getAttributeNS(null, "supportedContentTypes"), XMLConstants.LIST_DELIMITERS);
            builder.addPropertyValue("supportedContentTypes", supportedContentTypes);
        }

    }
    // Checkstyle: CyclomaticComplexity ON

    /**
     * Build the definition of the HTTPClientBuilder which contains all our configuration.
     * 
     * @param element the HTTPMetadataProvider parser.
     * @return the bean definition with the parameters.
     */
    private BeanDefinition buildHttpClient(Element element) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(HttpClientFactoryBean.class);

        builder.setLazyInit(true);

        if (element.hasAttributeNS(null, "requestTimeout")) {
            builder.addPropertyValue("connectionTimeout", element.getAttributeNS(null, "requestTimeout"));
        }

        if (element.hasAttributeNS(null, "disregardSslCertificate")) {
            builder.addPropertyValue("connectionDisregardSslCertificate",
                    element.getAttributeNS(null, "disregardSslCertificate"));
        }

        if (element.hasAttributeNS(null, "proxyHost")) {
            builder.addPropertyValue("connectionProxyHost", element.getAttributeNS(null, "proxyHost"));
        }

        if (element.hasAttributeNS(null, "proxyPort")) {
            builder.addPropertyValue("connectionProxyPort", element.getAttributeNS(null, "proxyPort"));
        }

        if (element.hasAttributeNS(null, "proxyUser")) {
            builder.addPropertyValue("connectionProxyUsername", element.getAttributeNS(null, "proxyUser"));
        }

        if (element.hasAttributeNS(null, "proxyPassword")) {
            builder.addPropertyValue("connectionProxyPassword", element.getAttributeNS(null, "proxyPassword"));
        }

        return builder.getBeanDefinition();
    }

    /**
     * Build the POJO with the username and password.
     * 
     * @param element the HTTPMetadataProvider parser.
     * @return the bean definition with the the username and password.
     */
    private BeanDefinition buildBasicCredentials(Element element) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(UsernamePasswordCredentials.class);

        builder.setLazyInit(true);

        builder.addConstructorArgValue(element.getAttributeNS(null, BASIC_AUTH_USER));
        builder.addConstructorArgValue(element.getAttributeNS(null, BASIC_AUTH_PASSWORD));

        return builder.getBeanDefinition();
    }

}