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

package net.shibboleth.idp.cas.flow;

import net.shibboleth.idp.cas.config.ConfigLookupFunction;
import net.shibboleth.idp.cas.proxy.ProxyAuthenticator;
import net.shibboleth.idp.cas.proxy.ProxyIdentifiers;
import net.shibboleth.idp.cas.config.ProxyGrantingTicketConfiguration;
import net.shibboleth.idp.cas.protocol.ProtocolError;
import net.shibboleth.idp.cas.protocol.ProtocolParam;
import net.shibboleth.idp.cas.protocol.TicketValidationRequest;
import net.shibboleth.idp.cas.protocol.TicketValidationResponse;
import net.shibboleth.idp.cas.ticket.*;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.utilities.java.support.logic.Constraint;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.security.trust.TrustEngine;
import org.opensaml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Validates the proxy callback URL provided in the service ticket validation request and creates a PGT when
 * the proxy callback is successfully authenticated. Possible outcomes:
 *
 * <ul>
 *     <li>{@link Events#Success success}</li>
 *     <li>{@link Events#Failure failure}</li>
 * </ul>
 *
 * On success, the PGTIOU is accessible at {@link TicketValidationResponse#getPgtIou()}.
 *
 * @author Marvin S. Addison
 */
public class ValidateProxyCallbackAction
    extends AbstractProfileAction<TicketValidationRequest, TicketValidationResponse> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ValidateProxyCallbackAction.class);

    /** Profile configuration lookup function. */
    private final ConfigLookupFunction<ProxyGrantingTicketConfiguration> configLookupFunction =
            new ConfigLookupFunction<>(ProxyGrantingTicketConfiguration.class);

    /** Validates the proxy callback endpoint. */
    @Nonnull
    private final ProxyAuthenticator<TrustEngine<X509Credential>> proxyAuthenticator;

    /** Manages CAS tickets. */
    @Nonnull
    private final TicketService ticketService;


    /**
     * Creates a new instance.
     *
     * @param proxyAuthenticator Component that validates the proxy callback endpoint.
     * @param ticketService Ticket service component.
     */
    public ValidateProxyCallbackAction(
            @Nonnull ProxyAuthenticator<TrustEngine<X509Credential>> proxyAuthenticator,
            @Nonnull TicketService ticketService) {
        this.proxyAuthenticator = Constraint.isNotNull(proxyAuthenticator, "ProxyAuthenticator cannot be null");
        this.ticketService = Constraint.isNotNull(ticketService, "TicketService cannot be null");
    }

    @Nonnull
    @Override
    protected Event doExecute(
            final @Nonnull RequestContext springRequestContext,
            final @Nonnull ProfileRequestContext profileRequestContext) {

        final TicketValidationRequest request = FlowStateSupport.getTicketValidationRequest(springRequestContext);
        if (request == null) {
            log.info("TicketValidationRequest not found in flow state.");
            return ProtocolError.IllegalState.event(this);
        }
        final TicketValidationResponse response =
                FlowStateSupport.getTicketValidationResponse(springRequestContext);
        if (response == null) {
            log.info("TicketValidationResponse not found in flow state.");
            return ProtocolError.IllegalState.event(this);
        }
        final TicketContext ticketContext = profileRequestContext.getSubcontext(TicketContext.class);
        if (ticketContext == null) {
            log.info("TicketContext not found in profile request context.");
            return ProtocolError.IllegalState.event(this);
        }
        final ProxyGrantingTicketConfiguration config = configLookupFunction.apply(profileRequestContext);
        if (config == null) {
            log.info("Proxy-granting ticket configuration undefined");
            return ProtocolError.IllegalState.event(this);
        }
        if (config.getSecurityConfiguration() == null || config.getSecurityConfiguration().getIdGenerator() == null) {
            log.info("Invalid proxy-granting ticket configuration: SecurityConfiguration#idGenerator undefined");
            return ProtocolError.IllegalState.event(this);
        }
        if (config.getPGTIOUGenerator() == null) {
            log.info("Invalid proxy-granting ticket configuration: PGTIOUGenerator undefined");
            return ProtocolError.IllegalState.event(this);
        }
        final Ticket ticket = ticketContext.getTicket();
        final ProxyIdentifiers proxyIds = new ProxyIdentifiers(
                config.getSecurityConfiguration().getIdGenerator().generateIdentifier(),
                config.getPGTIOUGenerator().generateIdentifier());
        final URI proxyCallbackUri;
        try {
            proxyCallbackUri = new URIBuilder(request.getPgtUrl())
                    .addParameter(ProtocolParam.PgtId.id(), proxyIds.getPgtId())
                    .addParameter(ProtocolParam.PgtIou.id(), proxyIds.getPgtIou())
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error creating proxy callback URL", e);
        }
        try {
            log.debug("Attempting proxy authentication to {}", proxyCallbackUri);
            final TrustEngine<X509Credential> engine;
            if (config.getSecurityConfiguration().getClientTLSValidationConfiguration() != null) {
                engine = config.getSecurityConfiguration().getClientTLSValidationConfiguration().getX509TrustEngine();
            } else {
                log.debug("Proxy-granting ticket configuration does not define ClientTLSValidationConfiguration");
                engine = null;
            }
            proxyAuthenticator.authenticate(proxyCallbackUri, engine);
            final Instant expiration = DateTime.now().plus(config.getTicketValidityPeriod()).toInstant();
            if (ticket instanceof ServiceTicket) {
                ticketService.createProxyGrantingTicket(proxyIds.getPgtId(), expiration, (ServiceTicket) ticket);
            } else {
                ticketService.createProxyGrantingTicket(proxyIds.getPgtId(), expiration, (ProxyTicket) ticket);
            }
            response.setPgtIou(proxyIds.getPgtIou());
        } catch (Exception e) {
            log.info("Proxy authentication failed for " + request.getPgtUrl() + ": " + e);
            return Events.Failure.event(this);
        }
        return Events.Success.event(this);
    }
}