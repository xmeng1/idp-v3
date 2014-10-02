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

package net.shibboleth.idp.consent.flow.tou;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.consent.context.TermsOfUseContext;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.interceptor.AbstractProfileInterceptorAction;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action that creates a {@link TermsOfUseContext} and attaches it to the current {@link ProfileRequestContext}.
 * 
 * TODO details
 */
public class InitializeTermsOfUseContext extends AbstractProfileInterceptorAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(InitializeTermsOfUseContext.class);

    /** Consent flow descriptor. */
    @Nullable private TermsOfUseFlowDescriptor termsOfUseFlowDescriptor;

    /**
     * Set the consent flow descriptor.
     * 
     * @param descriptor the consent flow descriptor
     */
    public void setConsentFlowDescriptor(@Nonnull final TermsOfUseFlowDescriptor descriptor) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        termsOfUseFlowDescriptor = Constraint.isNotNull(descriptor, "Terms of use flow descriptor cannot be null");
    }

    /** {@inheritDoc} */
    @Override protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        if (termsOfUseFlowDescriptor == null) {
            log.warn("{} No terms of use flow descriptor available", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }

        return super.doPreExecute(profileRequestContext, interceptorContext);
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        final TermsOfUseContext termsOfUseContext = new TermsOfUseContext();

        log.debug("{} Created terms of use context '{}'", getLogPrefix(), termsOfUseContext);

        profileRequestContext.addSubcontext(termsOfUseContext, true);

        super.doExecute(profileRequestContext, interceptorContext);
    }
}