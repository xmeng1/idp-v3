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

package net.shibboleth.idp.attribute.resolver.ad.impl;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.UnsupportedAttributeTypeException;
import net.shibboleth.idp.attribute.resolver.AbstractAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.PluginDependencySupport;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link net.shibboleth.idp.attribute.resolver.AttributeDefinition} that produces its attribute values by taking the
 * first group match of a regular expression evaluating against the values of this definition's dependencies.
 */
@ThreadSafe
public class RegexSplitAttributeDefinition extends AbstractAttributeDefinition {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(RegexSplitAttributeDefinition.class);

    /** Regular expression used to split values. */
    @Nullable private Pattern regexp;

    /**
     * Gets the regular expression used to split input values.
     * 
     * @return regular expression used to split input values
     */
    @Nullable @NonnullAfterInit public Pattern getRegularExpression() {
        return regexp;
    }

    /**
     * Sets the regular expression used to split input values.
     * 
     * @param expression regular expression used to split input values
     */
    public synchronized void setRegularExpression(@Nonnull final Pattern expression) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        regexp = Constraint.isNotNull(expression, "Regular expression cannot be null");
    }

    /** {@inheritDoc} */
    @Override @Nullable protected IdPAttribute doAttributeDefinitionResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
        Constraint.isNotNull(workContext, "AttributeResolverWorkContext cannot be null");

        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        final IdPAttribute resultantAttribute = new IdPAttribute(getId());

        final Set<IdPAttributeValue<?>> dependencyValues =
                PluginDependencySupport.getMergedAttributeValues(workContext, getDependencies());

        for (final IdPAttributeValue dependencyValue : dependencyValues) {
            if (!(dependencyValue instanceof StringAttributeValue)) {
                throw new ResolutionException(new UnsupportedAttributeTypeException(getLogPrefix()
                        + "This attribute definition only operates on attribute values of type "
                        + StringAttributeValue.class.getName() + "; was given " + 
                        dependencyValue.getClass().getName()));
            }

            log.debug("{} applying regexp '{}' to input value '{}'", new Object[] {getLogPrefix(), regexp.pattern(),
                    dependencyValue.getValue(),});
            final Matcher matcher = regexp.matcher((String) dependencyValue.getValue());
            if (matcher.matches()) {
                log.debug("{} computed the value '{}' by apply regexp '{}' to input value '{}'", new Object[] {
                        getLogPrefix(), matcher.group(1), regexp.pattern(), dependencyValue.getValue(),});
                resultantAttribute.setValues(Collections.singleton(new StringAttributeValue(matcher.group(1))));
            } else {
                log.debug("{} Regexp '{}' did not match anything in input value '{}'", new Object[] {getLogPrefix(),
                        regexp.pattern(), dependencyValue.getValue(),});
            }
        }
        return resultantAttribute;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (null == regexp) {
            throw new ComponentInitializationException("Attribute definition '" + getId()
                    + "': no regular expression was configured");
        }

        if (getDependencies().isEmpty()) {
            throw new ComponentInitializationException("Attribute definition '" + getId()
                    + "': no dependencies were configured");
        }
    }
}