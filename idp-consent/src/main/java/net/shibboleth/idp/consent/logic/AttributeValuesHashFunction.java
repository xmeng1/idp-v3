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

package net.shibboleth.idp.consent.logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;

import org.cryptacular.util.CodecUtil;
import org.cryptacular.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

/**
 * Function to calculate the hash of the values of an IdP attribute.
 * 
 * Returns <code>null</code> for a <code>null</code> input or empty collection of IdP attribute values.
 * <code>Null</code> IdP attribute values are ignored.
 * 
 * The hash returned is the Base64 encoded representation of the SHA-256 digest.
 */
public class AttributeValuesHashFunction implements Function<Collection<IdPAttributeValue<?>>, String> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AttributeValuesHashFunction.class);

    /** {@inheritDoc} */
    @Override
    @Nullable public String apply(@Nullable @NullableElements final Collection<IdPAttributeValue<?>> input) {

        if (input == null) {
            return null;
        }

        final Collection<IdPAttributeValue<?>> filteredInput = Collections2.filter(input, Predicates.notNull());

        if (filteredInput.isEmpty()) {
            return null;
        }

        try {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            for (final IdPAttributeValue value : filteredInput) {
                objectOutputStream.writeObject(value.getValue());
            }

            objectOutputStream.flush();
            objectOutputStream.close();
            byteArrayOutputStream.close();

            return CodecUtil.b64(HashUtil.sha256(byteArrayOutputStream.toByteArray()));

        } catch (IOException e) {
            log.error("Error while converting attribute values into a byte array", e);
            return null;
        }
    }
}
