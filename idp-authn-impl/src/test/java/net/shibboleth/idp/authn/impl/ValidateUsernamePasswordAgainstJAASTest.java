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

package net.shibboleth.idp.authn.impl;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.security.URIParameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.AuthenticationErrorContext;
import net.shibboleth.idp.authn.context.RequestedPrincipalContext;
import net.shibboleth.idp.authn.context.UsernamePasswordContext;
import net.shibboleth.idp.authn.principal.TestPrincipal;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.authn.principal.impl.ExactPrincipalEvalPredicateFactory;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.net.URISupport;

import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;

/** {@link ValidateUsernamePasswordAgainstJAAS} unit test. */
public class ValidateUsernamePasswordAgainstJAASTest extends BaseAuthenticationContextTest {

    private static final String DATA_PATH = "src/test/resources/net/shibboleth/idp/authn/impl/";
    
    private ValidateUsernamePasswordAgainstJAAS action;

    private InMemoryDirectoryServer directoryServer;

    /**
     * Creates an UnboundID in-memory directory server. Leverages LDIF found in test resources.
     * 
     * @throws LDAPException if the in-memory directory server cannot be created
     */
    @BeforeClass public void setupDirectoryServer() throws LDAPException {

        final InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=shibboleth,dc=net");
        config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("default", 10389));
        config.addAdditionalBindCredentials("cn=Directory Manager", "password");
        directoryServer = new InMemoryDirectoryServer(config);
        directoryServer.importFromLDIF(true, DATA_PATH + "loginLDAPTest.ldif");
        directoryServer.startListening();
    }

    /**
     * Shutdown the in-memory directory server.
     */
    @AfterClass public void teardownDirectoryServer() {
        directoryServer.shutDown(true);
    }

    @BeforeMethod public void setUp() throws Exception {
        super.setUp();

        action = new ValidateUsernamePasswordAgainstJAAS();
        
        final Map<String,Collection<String>> mappings = new HashMap<>();
        mappings.put("UnknownUsername", Collections.singleton("DN_RESOLUTION_FAILURE"));
        mappings.put("InvalidPassword", Collections.singleton("INVALID_CREDENTIALS"));
        action.setClassifiedMessages(mappings);
        
        action.setHttpServletRequest(new MockHttpServletRequest());
    }

    @Test public void testMissingFlow() throws Exception {
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_PROFILE_CTX);
    }

    @Test public void testMissingUser() throws Exception {
        prc.getSubcontext(AuthenticationContext.class).setAttemptedFlow(authenticationFlows.get(0));
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.NO_CREDENTIALS);
    }

    @Test public void testMissingUser2() throws Exception {
        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        ac.getSubcontext(UsernamePasswordContext.class, true);
        action.initialize();

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.NO_CREDENTIALS);
    }

    @Test public void testNoConfig() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "foo");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "bar");

        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_CREDENTIALS);
        AuthenticationErrorContext errorCtx = ac.getSubcontext(AuthenticationErrorContext.class);
        Assert.assertEquals(errorCtx.getExceptions().size(), 1);
        Assert.assertTrue(errorCtx.getExceptions().get(0) instanceof LoginException);
    }

    @Test public void testBadConfig() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "foo");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "bar");

        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        action.setLoginConfigNames(Collections.singletonList("ShibBadAuth"));
        action.setLoginConfigType("JavaLoginConfig");
        System.out.println(getCurrentDir());
        action.setLoginConfigParameters(new URIParameter(URISupport.fileURIFromAbsolutePath(getCurrentDir()
                + '/' + DATA_PATH + "jaas.config")));
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_CREDENTIALS);
        AuthenticationErrorContext errorCtx = ac.getSubcontext(AuthenticationErrorContext.class);
        Assert.assertEquals(errorCtx.getExceptions().size(), 1);
        Assert.assertTrue(errorCtx.getExceptions().get(0) instanceof LoginException);
    }

    @Test public void testUnsupportedConfig() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "foo");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "bar");

        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        
        final RequestedPrincipalContext rpc = ac.getSubcontext(RequestedPrincipalContext.class, true);
        rpc.getPrincipalEvalPredicateFactoryRegistry().register(
                TestPrincipal.class, "exact", new ExactPrincipalEvalPredicateFactory());
        rpc.setOperator("exact");
        rpc.setRequestedPrincipals(Collections.<Principal>singletonList(new TestPrincipal("test1")));

        action.setLoginConfigurations(Collections.singletonList(new Pair<String,Collection<Principal>>("ShibUserPassAuth",
                Collections.<Principal>singletonList(new TestPrincipal("test2")))));
        action.setLoginConfigType("JavaLoginConfig");
        System.out.println(getCurrentDir());
        action.setLoginConfigParameters(new URIParameter(URISupport.fileURIFromAbsolutePath(getCurrentDir()
                + '/' + DATA_PATH + "jaas.config")));
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.REQUEST_UNSUPPORTED);
    }
    
    @Test public void testUnmatchedUser() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "foo");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "bar");

        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        ac.getSubcontext(UsernamePasswordContext.class, true);
        
        action.setMatchExpression(Pattern.compile("foo.+"));
        action.initialize();
        
        doExtract(prc);

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, AuthnEventIds.INVALID_CREDENTIALS);
    }

    @Test public void testBadUsername() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "foo");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "bar");

        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        action.setLoginConfigType("JavaLoginConfig");
        System.out.println(getCurrentDir());
        action.setLoginConfigParameters(new URIParameter(URISupport.fileURIFromAbsolutePath(getCurrentDir()
                + '/' + DATA_PATH + "jaas.config")));
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, "UnknownUsername");
        AuthenticationErrorContext errorCtx = ac.getSubcontext(AuthenticationErrorContext.class);
        Assert.assertTrue(errorCtx.getExceptions().get(0) instanceof LoginException);
        Assert.assertTrue(errorCtx.isClassifiedError("UnknownUsername"));
        Assert.assertFalse(errorCtx.isClassifiedError("InvalidPassword"));
    }

    @Test public void testBadPassword() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "PETER_THE_PRINCIPAL");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "bar");

        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        action.setLoginConfigType("JavaLoginConfig");
        System.out.println(getCurrentDir());
        action.setLoginConfigParameters(new URIParameter(URISupport.fileURIFromAbsolutePath(getCurrentDir()
                + '/' + DATA_PATH + "jaas.config")));
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, "InvalidPassword");
        AuthenticationErrorContext errorCtx = ac.getSubcontext(AuthenticationErrorContext.class);
        Assert.assertTrue(errorCtx.getExceptions().get(0) instanceof LoginException);
        Assert.assertFalse(errorCtx.isClassifiedError("UnknownUsername"));
        Assert.assertTrue(errorCtx.isClassifiedError("InvalidPassword"));
    }

    @Test public void testAuthorized() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "PETER_THE_PRINCIPAL");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "changeit");

        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
        ac.setAttemptedFlow(authenticationFlows.get(0));

        action.setLoginConfigType("JavaLoginConfig");
        System.out.println(getCurrentDir());
        action.setLoginConfigParameters(new URIParameter(URISupport.fileURIFromAbsolutePath(getCurrentDir()
                + '/' + DATA_PATH + "jaas.config")));
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        
        Assert.assertNull(ac.getSubcontext(UsernamePasswordContext.class));
        Assert.assertNotNull(ac.getAuthenticationResult());
        Assert.assertEquals(ac.getAuthenticationResult().getSubject().getPrincipals(UsernamePrincipal.class).iterator()
                .next().getName(), "PETER_THE_PRINCIPAL");
    }

    @Test public void testAuthorizedAndKeep() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "PETER_THE_PRINCIPAL");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "changeit");

        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
        ac.setAttemptedFlow(authenticationFlows.get(0));

        action.setLoginConfigType("JavaLoginConfig");
        System.out.println(getCurrentDir());
        action.setLoginConfigParameters(new URIParameter(URISupport.fileURIFromAbsolutePath(getCurrentDir()
                + '/' + DATA_PATH + "jaas.config")));
        action.setRemoveContextAfterValidation(false);
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        
        Assert.assertNotNull(ac.getSubcontext(UsernamePasswordContext.class));
        Assert.assertNotNull(ac.getAuthenticationResult());
        Assert.assertEquals(ac.getAuthenticationResult().getSubject().getPrincipals(UsernamePrincipal.class).iterator()
                .next().getName(), "PETER_THE_PRINCIPAL");
    }

    @Test public void testSupported() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "PETER_THE_PRINCIPAL");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "changeit");

        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
        ac.setAttemptedFlow(authenticationFlows.get(0));
        
        final RequestedPrincipalContext rpc = ac.getSubcontext(RequestedPrincipalContext.class, true);
        rpc.getPrincipalEvalPredicateFactoryRegistry().register(
                TestPrincipal.class, "exact", new ExactPrincipalEvalPredicateFactory());
        rpc.setOperator("exact");
        rpc.setRequestedPrincipals(Collections.<Principal>singletonList(new TestPrincipal("test1")));

        action.setLoginConfigurations(Collections.singletonList(new Pair<String,Collection<Principal>>("ShibUserPassAuth",
                Collections.<Principal>singletonList(new TestPrincipal("test1")))));
        action.setLoginConfigType("JavaLoginConfig");
        System.out.println(getCurrentDir());
        action.setLoginConfigParameters(new URIParameter(URISupport.fileURIFromAbsolutePath(getCurrentDir()
                + '/' + DATA_PATH + "jaas.config")));
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        
        Assert.assertNull(ac.getSubcontext(UsernamePasswordContext.class));
        Assert.assertNotNull(ac.getAuthenticationResult());
        Assert.assertEquals(ac.getAuthenticationResult().getSubject().getPrincipals(UsernamePrincipal.class).iterator()
                .next().getName(), "PETER_THE_PRINCIPAL");
        Assert.assertEquals(ac.getAuthenticationResult().getSubject().getPrincipals(TestPrincipal.class).iterator()
                .next().getName(), "test1");
    }
    
    @Test public void testMultiConfigAuthorized() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "PETER_THE_PRINCIPAL");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "changeit");

        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
        ac.setAttemptedFlow(authenticationFlows.get(0));

        action.setLoginConfigNames(Arrays.asList("ShibBadAuth", "ShibUserPassAuth"));
        action.setLoginConfigType("JavaLoginConfig");
        System.out.println(getCurrentDir());
        action.setLoginConfigParameters(new URIParameter(URISupport.fileURIFromAbsolutePath(getCurrentDir()
                + '/' + DATA_PATH + "jaas.config")));
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        
        Assert.assertNull(ac.getSubcontext(UsernamePasswordContext.class));
        Assert.assertNotNull(ac.getAuthenticationResult());
        Assert.assertEquals(ac.getAuthenticationResult().getSubject().getPrincipals(UsernamePrincipal.class).iterator()
                .next().getName(), "PETER_THE_PRINCIPAL");
    }
    
    @Test public void testMatchAndAuthorized() throws Exception {
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("username", "PETER_THE_PRINCIPAL");
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter("password", "changeit");

        final AuthenticationContext ac = prc.getSubcontext(AuthenticationContext.class);
        ac.setAttemptedFlow(authenticationFlows.get(0));

        action.setLoginConfigType("JavaLoginConfig");
        System.out.println(getCurrentDir());
        action.setLoginConfigParameters(new URIParameter(URISupport.fileURIFromAbsolutePath(getCurrentDir()
                + '/' + DATA_PATH + "jaas.config")));
        action.setMatchExpression(Pattern.compile(".+_THE_.+"));
        action.initialize();

        doExtract(prc);

        final Event event = action.execute(src);
        ActionTestingSupport.assertProceedEvent(event);
        Assert.assertNull(ac.getSubcontext(UsernamePasswordContext.class));
        
        Assert.assertNotNull(ac.getAuthenticationResult());
        Assert.assertEquals(ac.getAuthenticationResult().getSubject().getPrincipals(UsernamePrincipal.class).iterator()
                .next().getName(), "PETER_THE_PRINCIPAL");
    }
    
    private void doExtract(ProfileRequestContext prc) throws Exception {
        final ExtractUsernamePasswordFromFormRequest extract = new ExtractUsernamePasswordFromFormRequest();
        extract.setHttpServletRequest(action.getHttpServletRequest());
        extract.initialize();
        extract.execute(src);
    }

    private String getCurrentDir() throws IOException {

        final String currentDir = new java.io.File(".").getCanonicalPath();

        return currentDir.replace(File.separatorChar, '/');
    }
    
}