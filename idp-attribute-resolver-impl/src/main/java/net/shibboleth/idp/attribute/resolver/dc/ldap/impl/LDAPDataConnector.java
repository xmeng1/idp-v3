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

package net.shibboleth.idp.attribute.resolver.dc.ldap.impl;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.dc.impl.AbstractSearchDataConnector;
import net.shibboleth.idp.attribute.resolver.dc.impl.ValidationException;
import net.shibboleth.idp.attribute.resolver.dc.impl.Validator;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link net.shibboleth.idp.attribute.resolver.DataConnector} that queries an LDAP in order to retrieve attribute
 * data.
 */
public class LDAPDataConnector extends AbstractSearchDataConnector<ExecutableSearchFilter> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(LDAPDataConnector.class);

    /** Factory for retrieving LDAP connections. */
    private ConnectionFactory connectionFactory;

    /** For executing LDAP searches. */
    private SearchExecutor searchExecutor;

    /**
     * Constructor.
     */
    public LDAPDataConnector() {
        setValidator(new DefaultValidator());
        setMappingStrategy(new StringAttributeValueMappingStrategy());

    }

    /**
     * Gets the connection factory for retrieving {@link Connection}s.
     * 
     * @return connection factory for retrieving {@link Connection}s
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Sets the connection factory for retrieving {@link Connection}s.
     * 
     * @param factory connection factory for retrieving {@link Connection}s
     */
    public synchronized void setConnectionFactory(@Nonnull final ConnectionFactory factory) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        connectionFactory = Constraint.isNotNull(factory, "LDAP connection factory can not be null");
    }

    /**
     * Gets the search executor for executing searches.
     * 
     * @return search executor for executing searches
     */
    public SearchExecutor getSearchExecutor() {
        return searchExecutor;
    }

    /**
     * Sets the search executor for executing searches.
     * 
     * @param executor search executor for executing searches
     */
    public synchronized void setSearchExecutor(@Nonnull final SearchExecutor executor) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        searchExecutor = Constraint.isNotNull(executor, "LDAP search executor can not be null");
    }

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (connectionFactory == null) {
            throw new ComponentInitializationException("Data connector '" + getId()
                    + "': no connection factory was configured");
        }
        if (searchExecutor == null) {
            throw new ComponentInitializationException("Data connector '" + getId()
                    + "': no search executor was configured");
        }

        try {
            getValidator().validate();
        } catch (ValidationException e) {
            log.error("Data connector '{}': invalid connector configuration", getId(), e);
            throw new ComponentInitializationException("Data connector '" + getId()
                    + "': invalid connector configuration", e);
        }
    }

    /**
     * Attempts to retrieve attributes from the LDAP.
     * 
     * @param filter search filter used to retrieve data from the LDAP
     * 
     * @return search result from the LDAP
     * 
     * @throws ResolutionException thrown if there is a problem retrieving data from the LDAP
     */
    @Nullable protected Map<String, IdPAttribute> retrieveAttributes(final ExecutableSearchFilter filter)
            throws ResolutionException {

        if (filter == null) {
            throw new ResolutionException("Search filter cannot be null");
        }
        try {
            final SearchResult result = filter.execute(searchExecutor, connectionFactory);
            log.trace("Data connector '{}': search returned {}", getId(), result);
            if (result.size() == 0) {
                if (isNoResultAnError()) {
                    throw new ResolutionException("No attributes returned from search");
                } else {
                    return null;
                }
            }
            return getMappingStrategy().map(result);
        } catch (LdapException e) {
            throw new ResolutionException("Unable to execute LDAP search", e);
        }
    }

    /** Validator that opens a connection. */
    public class DefaultValidator implements Validator {

        /** {@inheritDoc} */
        public void validate() throws ValidationException {
            Connection connection = null;
            try {
                connection = connectionFactory.getConnection();
                if (connection == null) {
                    throw new LdapException("Unable to retrieve connection from connection factory");
                }
                connection.open();
            } catch (LdapException e) {
                throw new ValidationException(e);
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        }
    }

    /** Validator that executes a search filter. */
    public class SearchValidator implements Validator {

        /** Search filter for validating this connector. */
        private SearchFilter validateFilter;

        /**
         * Constructor.
         * 
         * @param filter to execute for validation
         */
        public SearchValidator(final SearchFilter filter) {
            validateFilter = filter;
        }

        /** {@inheritDoc} */
        public void validate() throws ValidationException {
            try {
                searchExecutor.search(connectionFactory, validateFilter);
            } catch (LdapException e) {
                throw new ValidationException(e);
            }
        }

    }

}