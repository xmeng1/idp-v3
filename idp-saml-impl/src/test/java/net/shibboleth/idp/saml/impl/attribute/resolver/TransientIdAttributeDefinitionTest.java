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

package net.shibboleth.idp.saml.impl.attribute.resolver;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.saml.impl.TestSources;
import net.shibboleth.idp.saml.impl.nameid.StoredTransientIdGenerationStrategy;
import net.shibboleth.idp.saml.nameid.TransientIdParameters;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.storage.StorageRecord;
import org.opensaml.storage.StorageService;
import org.opensaml.storage.impl.MemoryStorageService;
import org.testng.Assert;
import org.testng.annotations.Test;

/** test for {@link net.shibboleth.idp.saml.impl.attribute.resolver.TransientIdAttributeDefinition}. */
public class TransientIdAttributeDefinitionTest extends OpenSAMLInitBaseTestCase {

    /** The name. */
    private static final String TEST_ATTRIBUTE_NAME = "simple";

    private static final long TEST_LIFETIME = 10;

    private static final int TEST_ID_SIZE = 32;

    @Test public void single() throws ComponentInitializationException, ResolutionException, IOException {
        final StoredTransientIdGenerationStrategy strategy = new StoredTransientIdGenerationStrategy();
        strategy.setId("strategy");
        
        final StorageService store = new MemoryStorageService();
        store.initialize();
        strategy.setIdStore(store);        
        strategy.initialize();
        
        final TransientIdAttributeDefinition defn = new TransientIdAttributeDefinition(strategy);
        defn.setId(TEST_ATTRIBUTE_NAME);

        defn.setDependencies(Collections.singleton(TestSources.makeResolverPluginDependency("foo", "bar")));

        defn.initialize();

        final IdPAttribute result =
                defn.resolve(TestSources.createResolutionContext(TestSources.PRINCIPAL_ID,
                        TestSources.IDP_ENTITY_ID, TestSources.SP_ENTITY_ID));

        Set<IdPAttributeValue<?>> vals = result.getValues();
        Assert.assertEquals(vals.size(), 1);

        String val = (String) vals.iterator().next().getValue();

        StorageRecord record = store.read(TransientIdParameters.CONTEXT, val);
        
        Assert.assertNotNull(record);
        Assert.assertTrue(val.length() >= strategy.getIdSize());
 
        TransientIdParameters parms = new TransientIdParameters(record.getValue());
        
        Assert.assertNotNull(parms);
        Assert.assertEquals(parms.getAttributeRecipient(), TestSources.SP_ENTITY_ID);
        Assert.assertEquals(parms.getPrincipal(), TestSources.PRINCIPAL_ID);

        defn.destroy();
        store.destroy();
    }

    private void constructAndFail(String sp, String idp, String principal, String whyItFailed)
            throws ComponentInitializationException {
        
        final StoredTransientIdGenerationStrategy strategy = new StoredTransientIdGenerationStrategy();
        strategy.setId("strategy");
        final StorageService store = new MemoryStorageService();
        store.initialize();
        strategy.setIdStore(store);
        strategy.initialize();
        
        final TransientIdAttributeDefinition defn = new TransientIdAttributeDefinition(strategy);
        defn.setId(TEST_ATTRIBUTE_NAME);
        defn.setDependencies(Collections.singleton(TestSources.makeResolverPluginDependency("foo", "bar")));
        defn.initialize();
        try {
            defn.resolve(TestSources.createResolutionContext(principal, idp, sp));
            Assert.fail(whyItFailed);
        } catch (ResolutionException e) {
            // OK
        }

        defn.destroy();
        store.destroy();
    }

    @Test public void fails() throws ComponentInitializationException {

        constructAndFail(TestSources.SP_ENTITY_ID, TestSources.IDP_ENTITY_ID, null, "Null principal");
        constructAndFail(null, TestSources.IDP_ENTITY_ID, TestSources.PRINCIPAL_ID, "Null SP");
    }

    @Test public void testGetters() throws ComponentInitializationException {
        final StoredTransientIdGenerationStrategy strategy = new StoredTransientIdGenerationStrategy();
        strategy.setId("strategy");

        final StorageService store = new MemoryStorageService();
        store.initialize();

        strategy.setIdLifetime(TEST_LIFETIME);
        strategy.setIdSize(TEST_ID_SIZE);

        strategy.setIdStore(store);
        strategy.initialize();
        final TransientIdAttributeDefinition defn = new TransientIdAttributeDefinition(strategy);
        defn.setId(TEST_ATTRIBUTE_NAME);
        defn.setDependencies(Collections.singleton(TestSources.makeResolverPluginDependency("foo", "bar")));

        defn.initialize();

        Assert.assertEquals(defn.getId(), TEST_ATTRIBUTE_NAME);

        defn.destroy();
        store.destroy();
    }

    @Test public void rerun() throws ComponentInitializationException, ResolutionException,
            InterruptedException {
        final StoredTransientIdGenerationStrategy strategy = new StoredTransientIdGenerationStrategy();
        strategy.setId("strategy");

        final StorageService store = new MemoryStorageService();
        store.initialize();

        strategy.setIdLifetime(TEST_LIFETIME);
        strategy.setIdSize(TEST_ID_SIZE);

        strategy.setIdStore(store);
        strategy.initialize();
        final TransientIdAttributeDefinition defn = new TransientIdAttributeDefinition(strategy);

        defn.setId(TEST_ATTRIBUTE_NAME);
        defn.setDependencies(Collections.singleton(TestSources.makeResolverPluginDependency("foo", "bar")));
        defn.initialize();

        IdPAttribute result = defn.resolve(TestSources.createResolutionContext(TestSources.PRINCIPAL_ID,
                TestSources.IDP_ENTITY_ID, TestSources.SP_ENTITY_ID));

        Set<IdPAttributeValue<?>> vals = result.getValues();
        String firstTime = (String) vals.iterator().next().getValue();

        result = defn.resolve(TestSources.createResolutionContext(TestSources.PRINCIPAL_ID,
                TestSources.IDP_ENTITY_ID, TestSources.SP_ENTITY_ID));
        Assert.assertEquals(firstTime, vals.iterator().next().getValue());
        Assert.assertTrue(firstTime.length() >= strategy.getIdSize());

        Thread.sleep(TEST_LIFETIME * 2);

        result = defn.resolve(TestSources.createResolutionContext(TestSources.PRINCIPAL_ID,
                TestSources.IDP_ENTITY_ID, TestSources.SP_ENTITY_ID));
        vals = result.getValues();
        Assert.assertNotEquals(firstTime, vals.iterator().next().getValue());

        defn.destroy();
        store.destroy();
    }
    
}