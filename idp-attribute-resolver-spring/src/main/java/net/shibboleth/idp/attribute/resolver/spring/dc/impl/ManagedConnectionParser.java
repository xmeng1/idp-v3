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

package net.shibboleth.idp.attribute.resolver.spring.dc.impl;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.resolver.spring.impl.AttributeResolverNamespaceHandler;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.w3c.dom.Element;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/** Utility class for parsing v2 managed connection configuration. */
public class ManagedConnectionParser {

    /** dc:ContainerManagedConnection (legacy).*/
    @Nonnull public static final QName CONTAINER_MANAGED_CONNECTION_DC =
            new QName(DataConnectorNamespaceHandler.NAMESPACE, "ContainerManagedConnection");
    
    /** resolver:ContainerManagedConnection.*/
    @Nonnull public static final QName CONTAINER_MANAGED_CONNECTION_RESOLVER =
            new QName(AttributeResolverNamespaceHandler.NAMESPACE, "ContainerManagedConnection");
    
    /** dc: ApplicationManagedConnection (legacy).*/
    @Nonnull public static final QName APPLICATION_MANAGED_CONNECTION_DC =
            new QName(DataConnectorNamespaceHandler.NAMESPACE, "ApplicationManagedConnection");
    
    /** resolver: ApplicationManagedConnection (legacy).*/
    @Nonnull public static final QName APPLICATION_MANAGED_CONNECTION_RESOLVER =
            new QName(AttributeResolverNamespaceHandler.NAMESPACE, "ApplicationManagedConnection");

    /** dc:ContainerManagedConnection (legacy).*/
    @Nonnull public static final QName BEAN_MANAGED_CONNECTION_DC =
            new QName(DataConnectorNamespaceHandler.NAMESPACE, "BeanManagedConnection");
    
    /** resolver:ContainerManagedConnection.*/
    @Nonnull public static final QName BEAN_MANAGED_CONNECTION_RESOLVER =
            new QName(AttributeResolverNamespaceHandler.NAMESPACE, "BeanManagedConnection");
    

    /** Data source XML element. */
    @Nonnull private final Element configElement;
    
    /**
     * Creates a new ManagedConnectionParser with the supplied element.
     * 
     * @param config element
     */
    public ManagedConnectionParser(@Nonnull final Element config) {
        Constraint.isNotNull(config, "Element cannot be null");
        configElement = config;
    }

    /**
     * Creates a data source bean definition from a v2 XML configuration.
     * 
     * @return data source bean definition
     */
    @Nullable public BeanDefinition createDataSource() {
        final List<Element> containerManagedElements =
                ElementSupport.getChildElements(configElement, CONTAINER_MANAGED_CONNECTION_DC);
        containerManagedElements.addAll(
                ElementSupport.getChildElements(configElement, CONTAINER_MANAGED_CONNECTION_RESOLVER));
        
        
        final List<Element> applicationManagedElements =
                ElementSupport.getChildElements(configElement, APPLICATION_MANAGED_CONNECTION_DC);
        applicationManagedElements.addAll(
                ElementSupport.getChildElements(configElement, APPLICATION_MANAGED_CONNECTION_RESOLVER));
        
        if ((containerManagedElements.size() + applicationManagedElements.size()) > 1) {
            LoggerFactory.getLogger(ManagedConnectionParser.class).warn("Only one <ApplicationManagedConnection> or "+
                                    "<ContainerManagedConnection> is allows per DataConnector");
        }
        
        if (!containerManagedElements.isEmpty()) {
            return createContainerManagedDataSource(containerManagedElements.get(0));
        }

        if (!applicationManagedElements.isEmpty()) {
            return createApplicationManagedDataSource(applicationManagedElements.get(0));
        }
        
        return null;
    }

    /**
     * Creates a container managed data source bean definition.
     * 
     * @param containerManagedElement to parse
     * 
     * @return data source bean definition
     */
    @Nonnull protected BeanDefinition createContainerManagedDataSource(@Nonnull final Element containerManagedElement) {
        Constraint.isNotNull(containerManagedElement, "ContainerManagedConnection element cannot be null");

        final String resourceName =
                AttributeSupport.getAttributeValue(containerManagedElement, new QName("resourceName"));

        final ManagedMap<String, String> props = new ManagedMap<>();
        final Element propertyElement =
                ElementSupport.getFirstChildElement(containerManagedElement, new QName(
                        DataConnectorNamespaceHandler.NAMESPACE, "JNDIConnectionProperty"));
        final List<Element> elements = ElementSupport.getChildElements(propertyElement);
        for (final Element e : elements) {
            props.put(AttributeSupport.getAttributeValue(e, new QName("name")),
                    AttributeSupport.getAttributeValue(e, new QName("value")));
        }

        final BeanDefinitionBuilder dataSource =
                BeanDefinitionBuilder.rootBeanDefinition(ManagedConnectionParser.class, "buildDataSource");
        dataSource.addConstructorArgValue(props);
        dataSource.addConstructorArgValue(resourceName);
        return dataSource.getBeanDefinition();
    }

    /**
     * Creates an application managed data source bean definition.
     * 
     * @param applicationManagedElement to parse
     * 
     * @return data source bean definition
     */
    // Checkstyle: CyclomaticComplexity|MethodLength OFF
    @Nonnull protected BeanDefinition createApplicationManagedDataSource(
            @Nonnull final Element applicationManagedElement) {
        Constraint.isNotNull(applicationManagedElement, "ApplicationManagedConnection element cannot be null");
        final BeanDefinitionBuilder dataSource =
                BeanDefinitionBuilder.genericBeanDefinition(ComboPooledDataSource.class);

        final BeanDefinitionBuilder jdbcDriver =
                BeanDefinitionBuilder.rootBeanDefinition(ManagedConnectionParser.class, "loadJdbcDriver");
        jdbcDriver.addConstructorArgValue(AttributeSupport.getAttributeValue(applicationManagedElement, new QName(
                "jdbcDriver")));
        dataSource.addPropertyValue("driverClass", jdbcDriver.getBeanDefinition());
        dataSource.addPropertyValue("jdbcUrl",
                AttributeSupport.getAttributeValue(applicationManagedElement, new QName("jdbcURL")));
        dataSource.addPropertyValue("user",
                AttributeSupport.getAttributeValue(applicationManagedElement, new QName("jdbcUserName")));
        dataSource.addPropertyValue("password",
                AttributeSupport.getAttributeValue(applicationManagedElement, new QName("jdbcPassword")));

        final String poolAcquireIncrement =
                AttributeSupport.getAttributeValue(applicationManagedElement, new QName("poolAcquireIncrement"));
        if (poolAcquireIncrement != null) {
            dataSource.addPropertyValue("acquireIncrement", poolAcquireIncrement);
        } else {
            dataSource.addPropertyValue("acquireIncrement", 3);
        }

        final String poolAcquireRetryAttempts =
                AttributeSupport.getAttributeValue(applicationManagedElement, new QName("poolAcquireRetryAttempts"));
        if (poolAcquireRetryAttempts != null) {
            dataSource.addPropertyValue("acquireRetryAttempts", poolAcquireRetryAttempts);
        } else {
            dataSource.addPropertyValue("acquireRetryAttempts", 36);
        }

        final String poolAcquireRetryDelay =
                AttributeSupport.getAttributeValue(applicationManagedElement, new QName("poolAcquireRetryDelay"));
        if (poolAcquireRetryDelay != null) {
            dataSource.addPropertyValue("acquireRetryDelay", poolAcquireRetryDelay);
        } else {
            dataSource.addPropertyValue("acquireRetryDelay", 5000);
        }

        final String poolBreakAfterAcquireFailure =
                AttributeSupport
                        .getAttributeValue(applicationManagedElement, new QName("poolBreakAfterAcquireFailure"));
        if (poolBreakAfterAcquireFailure != null) {
            dataSource.addPropertyValue("breakAfterAcquireFailure", poolBreakAfterAcquireFailure);
        } else {
            dataSource.addPropertyValue("breakAfterAcquireFailure", true);
        }

        final String poolMinSize =
                AttributeSupport.getAttributeValue(applicationManagedElement, new QName("poolMinSize"));
        if (poolMinSize != null) {
            dataSource.addPropertyValue("minPoolSize", poolMinSize);
        } else {
            dataSource.addPropertyValue("minPoolSize", 2);
        }

        final String poolMaxSize =
                AttributeSupport.getAttributeValue(applicationManagedElement, new QName("poolMaxSize"));
        if (poolMaxSize != null) {
            dataSource.addPropertyValue("maxPoolSize", poolMaxSize);
        } else {
            dataSource.addPropertyValue("maxPoolSize", 50);
        }

        final String poolMaxIdleTime =
                AttributeSupport.getAttributeValue(applicationManagedElement, new QName("poolMaxIdleTime"));
        if (poolMaxIdleTime != null) {
            dataSource.addPropertyValue("maxIdleTime", poolMaxIdleTime);
        } else {
            dataSource.addPropertyValue("maxIdleTime", 600);
        }

        final String poolIdleTestPeriod =
                AttributeSupport.getAttributeValue(applicationManagedElement, new QName("poolIdleTestPeriod"));
        if (poolIdleTestPeriod != null) {
            dataSource.addPropertyValue("idleConnectionTestPeriod", poolIdleTestPeriod);
        } else {
            dataSource.addPropertyValue("idleConnectionTestPeriod", 180);
        }

        return dataSource.getBeanDefinition();
    }

    // Checkstyle: MethodLength|CyclomaticComplexity ON

    /**
     * Factory builder a container managed datasource.
     *
     * @param props to create an {@link InitialContext} with
     * @param resourceName of the data source
     *
     * @return data source or null if the data source cannot be looked up
     */
    @Nullable public static DataSource buildDataSource(final Map<String, String> props, final String resourceName) {
        try {
            final InitialContext initCtx = new InitialContext(new Hashtable<>(props));
            final DataSource dataSource = (DataSource) initCtx.lookup(resourceName);
            return dataSource;
        } catch (final NamingException e) {
            final Logger log = LoggerFactory.getLogger(ManagedConnectionParser.class);
            log.error("Managed data source '{}' could not be found", resourceName, e);
            return null;
        }
    }

    /**
     * Loads the supplied JDBC driver class into the classloader for this class.
     *
     * @param jdbcDriver to load
     *
     * @return the jdbc driver supplied to the method
     */
    public static String loadJdbcDriver(final String jdbcDriver) {
        // JDBC driver must be loaded in order to register itself
        final ClassLoader classLoader = ManagedConnectionParser.class.getClassLoader();
        try {
            classLoader.loadClass(jdbcDriver);
        } catch (final ClassNotFoundException e) {
            final Logger log = LoggerFactory.getLogger(ManagedConnectionParser.class);
            log.error("JDBC driver '{}' could not be found", jdbcDriver, e);
        }
        return jdbcDriver;
    }
    
    /**
     * Get the bean ID of an externally defined data source.
     * 
     * @param config the config element
     * @return data source bean ID
     */
    @Nullable public static String getBeanDataSourceID(@Nonnull final Element config) {
        
        final List<Element> beanManagedElements = ElementSupport.getChildElements(config, BEAN_MANAGED_CONNECTION_DC);
        beanManagedElements.addAll(ElementSupport.getChildElements(config, BEAN_MANAGED_CONNECTION_RESOLVER)); 
        
        if (beanManagedElements.isEmpty()) {
            return null;
        }
        
        if (beanManagedElements.size() > 1) {
            LoggerFactory.getLogger(ManagedConnectionParser.class).
            warn("Only one <BeanManagedConnection> should be specified; the first one has been consulted");
        }

        final List<Element> managedElements = ElementSupport.getChildElements(config, CONTAINER_MANAGED_CONNECTION_DC);
        managedElements.addAll(ElementSupport.getChildElements(config, CONTAINER_MANAGED_CONNECTION_RESOLVER));
        managedElements.addAll(ElementSupport.getChildElements(config, APPLICATION_MANAGED_CONNECTION_DC));
        managedElements.addAll(ElementSupport.getChildElements(config, APPLICATION_MANAGED_CONNECTION_RESOLVER));
        
        if (managedElements.size() > 0) {
            LoggerFactory.getLogger(ManagedConnectionParser.class).
            warn("<BeanManagedConnection> is incompatible with <ContainerManagedConnection> "+
                 "or <ApplicationManagedConnection>. The <BeanManagedConnection> has been used");
        }
        
        return StringSupport.trimOrNull(ElementSupport.getElementContentAsString(beanManagedElements.get(0)));
    }

}