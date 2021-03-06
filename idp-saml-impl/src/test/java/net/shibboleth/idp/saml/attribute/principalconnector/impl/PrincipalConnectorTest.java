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

package net.shibboleth.idp.saml.attribute.principalconnector.impl;

import java.util.Collections;

import javax.annotation.Nonnull;

import net.shibboleth.idp.authn.SubjectCanonicalizationException;
import net.shibboleth.idp.authn.context.SubjectCanonicalizationContext;
import net.shibboleth.idp.saml.nameid.NameDecoderException;
import net.shibboleth.idp.saml.nameid.NameIDDecoder;
import net.shibboleth.idp.saml.nameid.NameIdentifierDecoder;
import net.shibboleth.idp.saml.nameid.impl.TransformingNameIDDecoder;
import net.shibboleth.idp.saml.nameid.impl.TransformingNameIdentifierDecoder;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.opensaml.saml.saml2.core.NameID;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * tests for {@link PrincipalConnector}.
 */
public class PrincipalConnectorTest extends OpenSAMLInitBaseTestCase {
    
    private static final String NAMEID_TEXT = "NAMEID_TEXT";

    private static final String NAMEIDENTIFIER_TEXT = "SAML1_TEXT";

    private static final String RP1 = "https://shibboleth.net/rp1";

    private static final String RP2 = "https://shibboleth.net/rp2";
    
    private final TransformingNameIDDecoder nameIDDecoder = new TransformingNameIDDecoder();
    
    private final TransformingNameIdentifierDecoder nameIdentifierDecoder = new TransformingNameIdentifierDecoder();
    
    @BeforeClass public void setup() throws ComponentInitializationException {
        nameIDDecoder.setId("nameIDDecoder");
        ComponentSupport.initialize(nameIDDecoder);
        nameIdentifierDecoder.setId("nameIdentifierDecoder");
        ComponentSupport.initialize(nameIdentifierDecoder);
    }
    
    @Test public void format() {
        final PrincipalConnector connector = newPrincipalConnector(nameIDDecoder, nameIdentifierDecoder, NameID.KERBEROS);
        
        Assert.assertEquals(connector.getFormat(), NameID.KERBEROS);
    }
    
    @Test public void relyingParties() throws ComponentInitializationException {
        
        PrincipalConnector connector = newPrincipalConnector(nameIDDecoder, nameIdentifierDecoder, NameID.KERBEROS);
        connector.setId("relyingParties");
        connector.initialize();
        
        Assert.assertTrue(connector.requesterMatches(null));
        Assert.assertTrue(connector.requesterMatches(RP1));
        Assert.assertTrue(connector.requesterMatches(RP2));
        
        try {
            connector.setRelyingParties(Collections.singleton(RP1));
            Assert.fail();
        } catch (final UnmodifiableComponentException e) {
            // OK
        }

        connector = newPrincipalConnector(nameIDDecoder, nameIdentifierDecoder, NameID.KERBEROS);
        connector.setId("relyingParties");
        connector.setRelyingParties(Collections.singleton(RP1));
        connector.initialize();
        
        Assert.assertTrue(connector.requesterMatches(null));
        Assert.assertTrue(connector.requesterMatches(RP1));
        Assert.assertFalse(connector.requesterMatches(RP2));        
    }
    
    @Test public void saml1() throws ComponentInitializationException, SubjectCanonicalizationException, NameDecoderException {
        final PrincipalConnector connector = newPrincipalConnector(nameIDDecoder, nameIdentifierDecoder, NameID.KERBEROS);
        connector.setId("saml1");
        connector.initialize();
        
        final SAMLObjectBuilder<NameIdentifier> builder = (SAMLObjectBuilder<NameIdentifier>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<NameIdentifier>getBuilderOrThrow(
                        NameIdentifier.DEFAULT_ELEMENT_NAME);        
        final NameIdentifier nameIdentifier = builder.buildObject();
        nameIdentifier.setValue(NAMEIDENTIFIER_TEXT);
        
        final SubjectCanonicalizationContext scc = new SubjectCanonicalizationContext();
        scc.setRequesterId(RP1);

        Assert.assertEquals(connector.decode(scc, nameIdentifier), NAMEIDENTIFIER_TEXT);
    }
    
    @Test public void saml2() throws ComponentInitializationException, SubjectCanonicalizationException, NameDecoderException {
        final PrincipalConnector connector = newPrincipalConnector(nameIDDecoder, nameIdentifierDecoder, NameID.KERBEROS);
        connector.setId("saml1");
        connector.initialize();
        
        final SAMLObjectBuilder<NameID> builder = (SAMLObjectBuilder<NameID>)
                XMLObjectProviderRegistrySupport.getBuilderFactory().<NameID>getBuilderOrThrow(
                        NameID.DEFAULT_ELEMENT_NAME);        
        final NameID nameID = builder.buildObject();
        nameID.setValue(NAMEID_TEXT);

        final SubjectCanonicalizationContext scc = new SubjectCanonicalizationContext();
        scc.setRequesterId(RP1);

        Assert.assertEquals(connector.decode(scc, nameID), NAMEID_TEXT);
    }

    public static PrincipalConnector newPrincipalConnector(@Nonnull  final NameIDDecoder saml2Decoder,
            @Nonnull final NameIdentifierDecoder saml1Decoder,
            @Nonnull final String theFormat) {
        
        final PrincipalConnector pc = new PrincipalConnector();
        pc.setFormat(theFormat);
        pc.setNameIDDecoder(saml2Decoder);
        pc.setNameIdentifierDecoder(saml1Decoder);
        return pc;
    }
}
