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

package net.shibboleth.idp.saml.impl.profile.saml2;

import java.util.List;

import javax.annotation.Nonnull;

import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.ActionSupport;
import net.shibboleth.idp.saml.profile.SAMLEventIds;
import net.shibboleth.idp.saml.profile.saml2.Saml2ActionSupport;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.profile.ProfileException;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.OneTimeUse;
import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Adds a {@link OneTimeUse} to every {@link Assertion} in the outgoing {@link Response} retrieved from the
 * {@link ProfileRequestContext#getOutboundMessageContext()}. If no {@link Conditions} is present on the
 * {@link Assertion} one will be created.
 * 
 * This action requires that the outbound message context to contain a {@link Response} with one, or more,
 * {@link Assertion}.
 */
public class AddOneTimeUseConditionToAssertions extends AbstractProfileAction<Object, Response> {

    /** Class logger. */
    private Logger log = LoggerFactory.getLogger(AddOneTimeUseConditionToAssertions.class);

    /** {@inheritDoc} */
    protected Event doExecute(@Nonnull final RequestContext springRequestContext,
            @Nonnull final ProfileRequestContext<Object, Response> profileRequestContext) throws ProfileException {
        log.debug("Action {}: Attempting to add DoNotCache condition to every Assertion in outgoing Response", getId());

        final Response response = profileRequestContext.getOutboundMessageContext().getMessage();
        if (response == null) {
            log.error("Action {}: No SAML response located in current profile request context", getId());
            return ActionSupport.buildEvent(this, SAMLEventIds.NO_RESPONSE);
        }

        final List<Assertion> assertions = response.getAssertions();
        if (assertions.isEmpty()) {
            log.error("Action {}: Unable to add DoNotCacheCondition, Response does not contain an Asertion", getId());
            return ActionSupport.buildEvent(this, SAMLEventIds.NO_ASSERTION);
        }

        final SAMLObjectBuilder<OneTimeUse> conditionBuilder =
                (SAMLObjectBuilder<OneTimeUse>) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(
                        OneTimeUse.TYPE_NAME);

        Conditions conditions;
        for (Assertion assertion : assertions) {
            conditions = Saml2ActionSupport.addConditionsToAssertion(this, assertion);
            if (conditions.getOneTimeUse() == null) {
                conditions.getConditions().add(conditionBuilder.buildObject());
                log.debug("Action {}: Added OneTimeUser condition to Assertion {}", getId(), assertion.getID());
            } else {
                log.debug("Action {}: Assertion {} already contained OneTimeUser condition, another was not added",
                        getId(), assertion.getID());
            }

        }

        return ActionSupport.buildProceedEvent(this);
    }
}