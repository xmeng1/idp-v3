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

package net.shibboleth.idp.attribute.filter.matcher.impl;

import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.filter.Matcher;

/**
 * General {@link Matcher} for {@link String} comparison of strings in Attribute Filters.   
 */
public abstract class AbstractStringMatcher extends AbstractMatcher implements Matcher {

    /** String to match for a positive evaluation. */
    private String matchString;

    /** Whether the match evaluation is case sensitive. */
    private boolean caseSensitive;

    /**
     * Gets the string to match for a positive evaluation.
     * 
     * @return string to match for a positive evaluation
     */
    @Nullable public String getMatchString() {
        return matchString;
    }

    /**
     * Sets the string to match for a positive evaluation.
     * 
     * @param match string to match for a positive evaluation
     */
    public void setMatchString(@Nullable final String match) {
        matchString = match;
    }

    /**
     * Gets whether the match evaluation is case sensitive.
     * 
     * @return whether the match evaluation is case sensitive
     */
    public boolean getCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Sets whether the match evaluation is case sensitive.
     * 
     * @param isCaseSensitive whether the match evaluation is case sensitive
     */
    public void setCaseSensitive(boolean isCaseSensitive) {
        caseSensitive = isCaseSensitive;
    }

    /**
     * Matches the given value against the provided match string. 
     * 
     * @param value the value to evaluate
     * 
     * @return true if the value matches the given match string, false if not
     */
    protected boolean stringCompare(@Nullable final String value) {
        if (value == null) {
            return matchString == null;
        }

        if (caseSensitive) {
            return value.equals(matchString);
        } else {
            return value.equalsIgnoreCase(matchString);
        }
    }
}