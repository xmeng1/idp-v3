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

package net.shibboleth.idp.saml.profile.logic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Predicate;

/**
 * Predicate to determine whether the supplied name matches any of an entity's containing
 * {@link EntitiesDescriptor} groups. 
 */
public class EntitiesDescriptorPredicate implements Predicate<ProfileRequestContext> {

    /** Group to match. */
    @Nonnull @NotEmpty private final String groupName;
    
    /**
     * Constructor.
     * 
     * @param name group name to match
     */
    public EntitiesDescriptorPredicate(@Nonnull @NotEmpty final String name) {
        groupName = Constraint.isNotNull(StringSupport.trimOrNull(name), "Group name cannot be null or empty");
    }

    /** {@inheritDoc} */
    @Override public boolean apply(@Nullable ProfileRequestContext arg0) {
        // TODO
        return false;
    }

}