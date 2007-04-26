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

import java.util.List;

import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.FilterProcessingException;
import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.MatchFunctor;
import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.ShibbolethFilteringContext;

/**
 * A match function that logical ORs the results of contained functors.
 */
public class OrMatchFunctor extends AbstractMatchFunctor {

    /** Contained functors. */
    private List<MatchFunctor> targetFunctors;

    /**
     * Gets the functors whose results will be ORed.
     * 
     * @return functors whose results will be ORed
     */
    public List<MatchFunctor> getTargetFunctors() {
        return targetFunctors;
    }

    /**
     * Sets the functors whose results will be ORed.
     * 
     * @param functors functors whose results will be ORed
     */
    public void setTargetFunctors(List<MatchFunctor> functors) {
        targetFunctors = functors;
    }

    /** {@inheritDoc} */
    protected boolean doEvaluatePolicyRequirement(ShibbolethFilteringContext filterContext)
            throws FilterProcessingException {

        if (targetFunctors == null) {
            return false;
        }

        for (MatchFunctor child : targetFunctors) {
            if (child.evaluatePolicyRequirement(filterContext)) {
                return true;
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    protected boolean doEvaluatePermitValue(ShibbolethFilteringContext filterContext, String attributeId,
            Object attributeValue) throws FilterProcessingException {
        if (targetFunctors == null) {
            return false;
        }

        for (MatchFunctor child : targetFunctors) {
            if (child.evaluatePermitValue(filterContext, attributeId, attributeValue)) {
                return true;
            }
        }

        return false;
    }
}