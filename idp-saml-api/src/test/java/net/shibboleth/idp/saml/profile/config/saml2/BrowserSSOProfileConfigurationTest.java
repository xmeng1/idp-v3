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

package net.shibboleth.idp.saml.profile.config.saml2;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link BrowserSSOProfileConfiguration}. */
public class BrowserSSOProfileConfigurationTest {

    @Test
    public void testProfileId() {
        Assert.assertEquals(BrowserSSOProfileConfiguration.PROFILE_ID, "http://shibboleth.net/ns/profiles/saml2/sso/browser");

        BrowserSSOProfileConfiguration config = new BrowserSSOProfileConfiguration();
        Assert.assertEquals(config.getId(), BrowserSSOProfileConfiguration.PROFILE_ID);
    }

    @Test
    public void testIncludeAttributeStatement() {
        BrowserSSOProfileConfiguration config = new BrowserSSOProfileConfiguration();
        Assert.assertTrue(config.includeAttributeStatement());

        config.setIncludeAttributeStatement(false);
        Assert.assertFalse(config.includeAttributeStatement());
    }

    @Test
    public void testSkipEndpointValidationWhenSigned() {
        BrowserSSOProfileConfiguration config = new BrowserSSOProfileConfiguration();
        Assert.assertFalse(config.skipEndpointValidationWhenSigned());

        config.setSkipEndpointValidationWhenSigned(true);
        Assert.assertTrue(config.skipEndpointValidationWhenSigned());
    }
    
    @Test
    public void testMaximumSpSessionLifeTime() {
        BrowserSSOProfileConfiguration config = new BrowserSSOProfileConfiguration();
        Assert.assertEquals(config.getMaximumSPSessionLifetime(), 0);

        config.setMaximumSPSessionLifetime(1000);
        Assert.assertEquals(config.getMaximumSPSessionLifetime(), 1000);
    }

    @Test
    public void testAllowingDelegation() {
        BrowserSSOProfileConfiguration config = new BrowserSSOProfileConfiguration();
        Assert.assertFalse(config.isAllowingDelegation());

        config.setAllowingDelegation(true);
        Assert.assertTrue(config.isAllowingDelegation());
    }
}