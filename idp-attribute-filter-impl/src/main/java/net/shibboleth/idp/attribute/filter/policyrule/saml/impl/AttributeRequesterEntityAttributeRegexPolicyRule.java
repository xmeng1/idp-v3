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

package net.shibboleth.idp.attribute.filter.policyrule.saml.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matcher functor that checks, via matching against a regular expression, if the attribute requester contains an entity
 * attribute with a given value.
 */
public class AttributeRequesterEntityAttributeRegexPolicyRule extends AbstractEntityAttributePolicyRule {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AttributeRequesterEntityAttributeRegexPolicyRule.class);

    /** The value of the entity attribute the entity must have. */
    private Pattern valueRegex;

    /**
     * Gets the value of the entity attribute the entity must have.
     * 
     * @return value of the entity attribute the entity must have
     */
    @NonnullAfterInit public Pattern getValueRegex() {
        return valueRegex;
    }

    /**
     * Sets the value of the entity attribute the entity must have.
     * 
     * @param attributeValueRegex value of the entity attribute the entity must have
     */
    public void setValueRegex(final Pattern attributeValueRegex) {
        valueRegex = attributeValueRegex;
    }

    /** {@inheritDoc} */
    @Override protected boolean entityAttributeValueMatches(final String entityAttributeValue) {
        final Matcher valueMatcher = valueRegex.matcher(StringSupport.trim(entityAttributeValue));
        return valueMatcher.matches();
    }

    /** {@inheritDoc} */
    @Override @Nullable protected EntityDescriptor getEntityMetadata(final AttributeFilterContext filterContext) {
        final SAMLMetadataContext metadataContext = filterContext.getRequesterMetadataContext();

        if (null == metadataContext) {
            log.warn("{} Could not locate SP metadata context", getLogPrefix());
            return null;
        }
        return metadataContext.getEntityDescriptor();
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (valueRegex == null) {
            throw new ComponentInitializationException(getLogPrefix() + " No regexp supplied to compare with");
        }
    }

}
