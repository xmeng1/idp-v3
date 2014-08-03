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

package net.shibboleth.idp.test;

import java.io.IOException;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.logic.Constraint;

import org.springframework.core.io.Resource;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFReader;

/**
 * Manages an instance of the in-memory directory server.
 */
public class InMemoryDirectory {

    /** Directory server. */
    @Nonnull private final InMemoryDirectoryServer directoryServer;

    /**
     * Default constructor.
     * 
     * @param ldif the LDIF resource to be imported
     * 
     * @throws LDAPException if the in-memory directory server cannot be created
     * @throws IOException if the LDIF resource cannot be imported
     */
    public InMemoryDirectory(@Nonnull final Resource ldif) throws LDAPException, IOException {
        Constraint.isNotNull(ldif, "LDIF resource cannot be null");
        final InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=example,dc=org", "ou=system");
        config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("default", 10389));
        config.addAdditionalBindCredentials("cn=Directory Manager", "password");
        directoryServer = new InMemoryDirectoryServer(config);
        directoryServer.importFromLDIF(true, new LDIFReader(ldif.getInputStream()));
    }

    /**
     * Starts the directory server.
     * 
     * @throws LDAPException if the in-memory directory server cannot be started
     */
    public void start() throws LDAPException {
        directoryServer.startListening();
    }

    /**
     * Stops the directory server.
     */
    public void stop() {
        directoryServer.shutDown(true);
    }
}
