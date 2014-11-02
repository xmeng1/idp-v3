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

import net.shibboleth.idp.cas.config.ServiceTicketConfiguration;
import net.shibboleth.idp.cas.protocol.ServiceTicketRequest;
import net.shibboleth.idp.cas.protocol.ServiceTicketResponse;
import net.shibboleth.idp.cas.ticket.ServiceTicket;
import net.shibboleth.idp.cas.ticket.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit test for {@link GrantServiceTicketAction}.
 *
 * @author Marvin S. Addison
 */
public class GrantServiceTicketActionTest extends AbstractFlowActionTest {

    @Autowired
    private GrantServiceTicketAction action;

    private ServiceTicketConfiguration serviceTicketConfiguration;


    @DataProvider(name = "messages")
    public Object[][] provideMessages() {
        final ServiceTicketRequest renewedRequest = new ServiceTicketRequest("https://www.example.com/beta");
        renewedRequest.setRenew(true);
        return new Object[][] {
                { new ServiceTicketRequest("https://www.example.com/alpha") },
                { renewedRequest },
        };
    }

    @BeforeTest
    public void setUp() throws Exception {
        serviceTicketConfiguration = new ServiceTicketConfiguration();
        serviceTicketConfiguration.setTicketValidityPeriod(15000);
        serviceTicketConfiguration.initialize();
    }

    @Test(dataProvider = "messages")
    public void testExecute(final ServiceTicketRequest message) throws Exception {
        final RequestContext context = new TestContextBuilder(ServiceTicketConfiguration.PROFILE_ID)
                .addSessionContext(mockSession("1234567890", true))
                .addRelyingPartyContext(message.getService(), true, serviceTicketConfiguration)
                .build();
        FlowStateSupport.setServiceTicketRequest(context, message);
        final Event result = action.execute(context);
        assertEquals(result.getId(), Events.Success.id());
        final ServiceTicketResponse response = FlowStateSupport.getServiceTicketResponse(context);
        final ServiceTicket ticket = ticketService.removeServiceTicket(response.getTicket());
        assertNotNull(ticket);
        assertEquals(ticket.isRenew(), message.isRenew());
        assertEquals(ticket.getId(), response.getTicket());
        assertEquals(ticket.getService(), response.getService());
    }
}