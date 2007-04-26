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

package edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.match;

import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.FilterProcessingException;
import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.ShibbolethFilteringContext;

/**
 * A match function that evaluates to true if {@link SAMLFilterContext#getRequesterMetadata()} matches the provided
 * entity group name.
 */
public class AttributeRequesterInEntityGroupMatchFunctor extends AbstractEntityGroupMatchFunctor {

    /** {@inheritDoc} */
    public boolean evaluatePolicyRequirement(ShibbolethFilteringContext filterContext) throws FilterProcessingException {
        return isEntityInGroup(filterContext.getAttribtueRequestContext().getAttributeRequesterMetadata());
    }

    /** {@inheritDoc} */
    public boolean evaluatePermitValue(ShibbolethFilteringContext filterContext, String attributeId,
            Object attributeValue) throws FilterProcessingException {
        return isEntityInGroup(filterContext.getAttribtueRequestContext().getAttributeRequesterMetadata());
    }
}