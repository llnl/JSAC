/*-
 * #%L
 * Java Seismic Analysis Code (JSAC)
 *  LLNL-CODE-855505
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2022 - 2023 Lawrence Livermore National Laboratory
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package gov.llnl.gnem.jsac.commands;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 *
 * @author dodge1
 */
public class AttributeDescriptor {
    private final String attributeName;
    private final ValuePossibilities numExpectedValues;
    private final Type attributeType;

    public AttributeDescriptor(String attributeName, ValuePossibilities numExpectedValues, Type attributeType) {
        this.attributeName = attributeName;
        this.numExpectedValues = numExpectedValues;
        this.attributeType = attributeType;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public ValuePossibilities getNumExpectedValues() {
        return numExpectedValues;
    }

    public Type getAttributeType() {
        return attributeType;
    }

    @Override
    public String toString() {
        return "AttributeDescriptor{" + "attributeName=" + attributeName + ", numExpectedValues=" + numExpectedValues + ", attributeType=" + attributeType + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.attributeName);
        hash = 53 * hash + Objects.hashCode(this.numExpectedValues);
        hash = 53 * hash + Objects.hashCode(this.attributeType);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AttributeDescriptor other = (AttributeDescriptor) obj;
        if (!Objects.equals(this.attributeName, other.attributeName)) {
            return false;
        }
        if (this.numExpectedValues != other.numExpectedValues) {
            return false;
        }
        if (!Objects.equals(this.attributeType, other.attributeType)) {
            return false;
        }
        return true;
    }
    
    
}
