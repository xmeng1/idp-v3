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

package net.shibboleth.idp.attribute.resolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.Attribute;
import net.shibboleth.idp.attribute.AttributeEncoder;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.collection.CollectionSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.component.ComponentValidationException;
import net.shibboleth.utilities.java.support.component.DestructableComponent;
import net.shibboleth.utilities.java.support.component.InitializableComponent;
import net.shibboleth.utilities.java.support.component.ValidatableComponent;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/** Base class for attribute definition resolver plugins. */
@ThreadSafe
public abstract class BaseAttributeDefinition extends BaseResolverPlugin<Attribute> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(BaseAttributeDefinition.class);

    /** Whether this attribute definition is only a dependency and thus its values should never be released. */
    private boolean dependencyOnly;

    /** Attribute encoders associated with this definition. */
    private Set<AttributeEncoder<?>> encoders = Collections.emptySet();

    /** Localized human intelligible attribute name. */
    private Map<Locale, String> displayNames = Collections.emptyMap();

    /** Localized human readable description of attribute. */
    private Map<Locale, String> displayDescriptions = Collections.emptyMap();

    /**
     * Gets whether this attribute definition is only a dependency and thus its values should never be released outside
     * the resolver.
     * 
     * @return true if this attribute is only used as a dependency, false otherwise
     */
    public boolean isDependencyOnly() {
        return dependencyOnly;
    }

    /**
     * Sets whether this attribute definition is only a dependency and thus its values should never be released outside
     * the resolver.
     * 
     * @param isDependencyOnly whether this attribute definition is only a dependency
     */
    public synchronized void setDependencyOnly(final boolean isDependencyOnly) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        dependencyOnly = isDependencyOnly;
    }

    /**
     * Gets the localized human readable descriptions of attribute.
     * 
     * @return human readable descriptions of attribute
     */
    @Nonnull @NonnullElements @Unmodifiable public Map<Locale, String> getDisplayDescriptions() {
        return displayDescriptions;
    }

    /**
     * Sets the localized human readable descriptions of attribute.
     * 
     * @param descriptions localized human readable descriptions of attribute
     */
    public synchronized void setDisplayDescriptions(@Nullable @NullableElements final Map<Locale, String> descriptions) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        HashMap<Locale, String> checkedDescriptions = new HashMap<Locale, String>();
        String trimmedDescription;
        for (Entry<Locale, String> entry : descriptions.entrySet()) {
            trimmedDescription = StringSupport.trimOrNull(entry.getValue());
            if (trimmedDescription != null) {
                checkedDescriptions.put(entry.getKey(), trimmedDescription);
            }
        }

        displayDescriptions = ImmutableMap.copyOf(checkedDescriptions);
    }

    /**
     * Gets the localized human readable names of the attribute.
     * 
     * @return human readable names of the attribute
     */
    @Nonnull @NonnullElements @Unmodifiable public Map<Locale, String> getDisplayNames() {
        return displayNames;
    }

    /**
     * Sets the localized human readable names of the attribute.
     * 
     * @param names localized human readable names of the attribute
     */
    public synchronized void setDisplayNames(@Nullable @NullableElements final Map<Locale, String> names) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        HashMap<Locale, String> checkedNames = new HashMap<Locale, String>();
        String trimmedName;
        for (Entry<Locale, String> entry : names.entrySet()) {
            trimmedName = StringSupport.trimOrNull(entry.getValue());
            if (trimmedName != null) {
                checkedNames.put(entry.getKey(), trimmedName);
            }
        }

        displayNames = ImmutableMap.copyOf(checkedNames);
    }

    /**
     * Gets the unmodifiable encoders used to encode the values of this attribute in to protocol specific formats. The
     * returned collection is never null nor contains any null.
     * 
     * @return encoders used to encode the values of this attribute in to protocol specific formats, never null
     */
    @Nonnull @NonnullElements @Unmodifiable public Set<AttributeEncoder<?>> getAttributeEncoders() {
        return encoders;
    }

    /**
     * Sets the encoders used to encode the values of this attribute in to protocol specific formats.
     * 
     * @param attributeEncoders encoders used to encode the values of this attribute in to protocol specific formats
     */
    public synchronized void setAttributeEncoders(
            @Nullable @NullableElements final List<AttributeEncoder<?>> attributeEncoders) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        Set<AttributeEncoder<?>> checkedEncoders = new HashSet<AttributeEncoder<?>>();
        CollectionSupport.addIf(checkedEncoders, attributeEncoders, Predicates.notNull());
        encoders = ImmutableSet.copyOf(checkedEncoders);
    }

    /** {@inheritDoc} */
    protected void doDestroy() {
        for (AttributeEncoder encoder : encoders) {
            if (encoder instanceof DestructableComponent) {
                ((DestructableComponent) encoder).destroy();
            }
        }

        encoders = Collections.emptySet();
        displayDescriptions = Collections.emptyMap();
        displayNames = Collections.emptyMap();

        super.doDestroy();
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        for (AttributeEncoder encoder : encoders) {
            if (encoder instanceof InitializableComponent) {
                ((InitializableComponent) encoder).initialize();
            }
        }
    }

    /** {@inheritDoc} */
    protected void doValidate() throws ComponentValidationException {
        super.doValidate();

        for (AttributeEncoder encoder : encoders) {
            if (encoder instanceof ValidatableComponent) {
                ((ValidatableComponent) encoder).validate();
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * This method delegates the actual resolution of the attribute's values to the
     * {@link #doResolve(AttributeResolutionContext)} method. Afterwards, if {@link Optional#absent()} was not returned,
     * this method will attach the registered display names, descriptions, and encoders to the resultant attribute.
     */
    @Nonnull protected Optional<Attribute> doResolve(@Nonnull final AttributeResolutionContext resolutionContext)
            throws AttributeResolutionException {

        final Optional<Attribute> optionalAttribute = doAttributeDefinitionResolve(resolutionContext);
        assert optionalAttribute != null : "return of doAttributeResolution was null";

        if (!optionalAttribute.isPresent()) {
            log.debug("Attribute definition '{}': no attribute was produced during resolution", getId());
            return optionalAttribute;
        }

        Attribute resolvedAttribute = optionalAttribute.get();

        if (resolvedAttribute.getValues().isEmpty()) {
            log.debug("Attribute definition '{}': produced an attribute with no values", getId());
        } else {
            log.debug("Attribute definition '{}': produced an attribute with the following values", getId(),
                    resolvedAttribute.getValues());
        }

        log.debug(
                "Attribute definition '{}': associating the following display descriptions with the resolved attribute: {}",
                getId(), getDisplayDescriptions());
        resolvedAttribute.setDisplayDescriptions(getDisplayDescriptions());

        log.debug("Attribute definition '{}': associating the following display names with the resolved attribute: {}",
                getId(), getDisplayNames());
        resolvedAttribute.setDisplayNames(getDisplayNames());

        log.debug("Attribute definition '{}': associating the following encoders with the resolved attribute: {}",
                getId(), getAttributeEncoders());
        resolvedAttribute.setEncoders(getAttributeEncoders());

        return optionalAttribute;
    }

    /**
     * Creates and populates the values for the resolved attribute. Implementations should <strong>not</strong> set, or
     * otherwise manage, the resolved attribute's display name, description or encoders. Nor should the resultant
     * attribute be recorded in the given resolution context.
     * 
     * @param resolutionContext current attribute resolution context
     * 
     * @return resolved attribute
     * 
     * @throws AttributeResolutionException thrown if there is a problem resolving and creating the attribute
     */
    @Nonnull protected abstract Optional<Attribute> doAttributeDefinitionResolve(
            @Nonnull final AttributeResolutionContext resolutionContext) throws AttributeResolutionException;
}