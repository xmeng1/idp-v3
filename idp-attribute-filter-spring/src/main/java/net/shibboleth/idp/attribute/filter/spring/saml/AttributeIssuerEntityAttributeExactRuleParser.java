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

package net.shibboleth.idp.attribute.filter.spring.saml;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.filter.impl.policyrule.saml.AttributeIssuerEntityAttributeExactPolicyRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;


/**
 * Parser for {@link AttributeIssuerEntityAttributeExactPolicyRule}.
 */
public class AttributeIssuerEntityAttributeExactRuleParser extends AbstractEntityAttributeExactRuleParser {

    /** Schema name. */
    public static final String SCHEMA_NAME = "AttributeIssuerEntityAttributeExactMatch";

    /** Schema type. */
    public static final QName SCHEMA_TYPE = new QName(AttributeFilterSAMLNamespaceHandler.NAMESPACE, SCHEMA_NAME);
    
    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AttributeIssuerEntityAttributeExactRuleParser.class);

    /** {@inheritDoc} */
    @Override @Nonnull protected Class<AttributeIssuerEntityAttributeExactPolicyRule> getNativeBeanClass() {

        log.error("Unimplemented Attribute Filter {}.  Consider other implementation methods.", SCHEMA_NAME);
        throw new BeanCreationException("Unimplemented filter " + SCHEMA_NAME);
    }

}
