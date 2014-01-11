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

package net.shibboleth.idp.attribute.filter.impl.policyrule.saml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.impl.policyrule.AbstractPolicyRule;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A matcher that evaluates to true if attribute requester matches the provided entity group name.
 */
public class AttributeRequesterInEntityGroupPolicyRule extends AbstractPolicyRule {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AttributeRequesterInEntityGroupPolicyRule.class);

    /** The entity group to match against. */
    private String entityGroup;

    /**
     * Gets the entity group to match against.
     * 
     * @return entity group to match against
     */
    @Nullable public String getEntityGroup() {
        return entityGroup;
    }

    /**
     * Sets the entity group to match against.
     * 
     * @param group entity group to match against
     */
    public void setEntityGroup(@Nullable String group) {
        entityGroup = StringSupport.trimOrNull(group);
    }

    /**
     * Gets the entity descriptor for the entity to check.
     * 
     * @param filterContext current filter request context
     * 
     * @return entity descriptor for the entity to check
     */
    @Nullable protected EntityDescriptor getEntityMetadata(final AttributeFilterContext filterContext) {
        final SAMLMetadataContext metadataContext = filterContext.getRequesterMetadataContext();

        if (null == metadataContext) {
            log.warn("{} Could not locate SP metadata context", getLogPrefix());
            return null;
        }
        return metadataContext.getEntityDescriptor();
    }

    /**
     * Checks if the given entity is in the provided entity group.
     * 
     * @param input the context to look at
     * 
     * @return whether the entity is in the group
     *         {@inheritDoc}
     */
    @Override
    public Tristate matches(@Nonnull AttributeFilterContext input) {

        Constraint.isNotNull(input, "Context must be supplied");
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        if (entityGroup == null) {
            log.warn("{} No entity group specified, unable to check if entity is in group", getLogPrefix());
            return Tristate.FAIL;
        }

        final EntityDescriptor entity = getEntityMetadata(input);
        if (entity == null) {
            // logged in concrete class
            return Tristate.FAIL;
        }

        EntitiesDescriptor currentGroup = (EntitiesDescriptor) entity.getParent();
        if (currentGroup == null) {
            log.warn("{} Entity descriptor does not have a parent object, unable to check if entity is in group {}",
                    getLogPrefix(), entityGroup);
            return Tristate.FAIL;
        }

        do {
            if (entityGroup.equals(currentGroup.getName())) {
                return Tristate.TRUE;
            }
            currentGroup = (EntitiesDescriptor) currentGroup.getParent();
        } while (currentGroup != null);

        return Tristate.FALSE;
    }

}
