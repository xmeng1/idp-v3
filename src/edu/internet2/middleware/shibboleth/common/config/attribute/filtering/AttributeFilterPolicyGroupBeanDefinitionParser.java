/*
 * Copyright [2007] [University Corporation for Advanced Internet Development, Inc.]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.shibboleth.common.config.attribute.filtering;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

/**
 * Spring bean definition parser for Shibboleth attribute filtering engine attribute filter policy.
 */
public class AttributeFilterPolicyGroupBeanDefinitionParser extends BaseFilterBeanDefinitionParser {

    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(AttributeFilterNamespaceHandler.NAMESPACE,
            "AttributeFilterPolicyGroup");

    /** Schema type name. */
    public static final QName TYPE_NAME = new QName(AttributeFilterNamespaceHandler.NAMESPACE,
            "AttributeFilterPolicyGroupType");

    /** Local name of the policy requirement element. */
    public static final QName POLICY_REQUIREMENT_ELEMENT_NAME = new QName(AttributeFilterNamespaceHandler.NAMESPACE,
            "PolicyRequirement");

    /** Local name of the value filter element. */
    public static final QName PERMIT_VALUE_ELEMENT_NAME = new QName(AttributeFilterNamespaceHandler.NAMESPACE,
            "PermitValue");

    /** Class logger. */
    private static Logger log = Logger.getLogger(AttributeFilterPolicyGroupBeanDefinitionParser.class);

    /** {@inheritDoc} */
    protected Class getBeanClass(Element arg0) {
        return AttributeFilterPolicyGroup.class;
    }

    /** {@inheritDoc} */
    protected void doParse(Element configElement, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(configElement, parserContext, builder);

        String policyId = DatatypeHelper.safeTrimOrNullString(configElement.getAttributeNS(null, "id"));
        builder.addConstructorArg(policyId);

        if (log.isDebugEnabled()) {
            log.debug("Parsing attribute filter policy group" + policyId);
        }

        List<Element> children;
        Map<QName, List<Element>> childrenMap = XMLHelper.getChildElements(configElement);

        children = childrenMap.get(new QName(AttributeFilterNamespaceHandler.NAMESPACE, "PolicyRequirementRule"));
        builder.addPropertyValue("policyRequirementRules", SpringConfigurationUtils.parseCustomElements(children,
                parserContext));

        children = childrenMap.get(new QName(AttributeFilterNamespaceHandler.NAMESPACE, "AttributeRule"));
        builder.addPropertyValue("attributeRules", SpringConfigurationUtils
                .parseCustomElements(children, parserContext));

        children = childrenMap.get(new QName(AttributeFilterNamespaceHandler.NAMESPACE, "PermitValueRule"));
        builder.addPropertyValue("permitValueRules", SpringConfigurationUtils.parseCustomElements(children,
                parserContext));

        children = childrenMap.get(new QName(AttributeFilterNamespaceHandler.NAMESPACE, "AttributeFilterPolicy"));
        builder.addPropertyValue("attributeFilterPolicies", SpringConfigurationUtils.parseCustomElements(children,
                parserContext));
    }

    /** {@inheritDoc} */
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
        return getQualifiedId(element, null, null);
    }
}