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

import java.util.Objects;

/**
 *
 * @author dodge1
 */
public class KeyArgumentSpec {
    private final String keyword;
    private final int minValueCount;
    private final int maxValueCount;

    public KeyArgumentSpec(String keyword, int minValueCount, int maxValueCount) {
        this.keyword = keyword;
        this.minValueCount = minValueCount;
        this.maxValueCount = maxValueCount;
    }

    @Override
    public String toString() {
        return "KeyArgumentSpec{" + "keyword=" + keyword + ", minValueCount=" + minValueCount + ", maxValueCount=" + maxValueCount + '}';
    }

    public String getKeyword() {
        return keyword;
    }

    public int getMinValueCount() {
        return minValueCount;
    }

    public int getMaxValueCount() {
        return maxValueCount;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.keyword);
        hash = 37 * hash + this.minValueCount;
        hash = 37 * hash + this.maxValueCount;
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
        final KeyArgumentSpec other = (KeyArgumentSpec) obj;
        if (this.minValueCount != other.minValueCount) {
            return false;
        }
        if (this.maxValueCount != other.maxValueCount) {
            return false;
        }
        if (!Objects.equals(this.keyword, other.keyword)) {
            return false;
        }
        return true;
    }
    
}
