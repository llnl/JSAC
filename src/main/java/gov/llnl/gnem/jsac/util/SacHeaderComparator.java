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

package gov.llnl.gnem.jsac.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import gov.llnl.gnem.jsac.io.SACHeader;

/**
 *
 * @author dodge1
 */
public class SacHeaderComparator implements Comparator<SACHeader> {

    private final List<String> fieldNames = new ArrayList<>();
    private final List<Boolean> sortAscending = new ArrayList<>();

    @Override
    public int compare(SACHeader t1, SACHeader t2) {
        for (int j = 0; j < fieldNames.size(); ++j) {
            String fieldName = fieldNames.get(j);
            boolean ascending = sortAscending.get(j);
            Object v1 = t1.getValue(fieldName);
            Object v2 = t2.getValue(fieldName);
            int result = compareObjects(v1, v2);
            if (result != 0) {
                return ascending ? result : -result;
            }
        }
        return 0;
    }

    public void addField(String fieldName, boolean ascending) {
        fieldNames.add(fieldName);
        sortAscending.add(ascending);
    }

    private int compareObjects(Object v1, Object v2) {
        if (v1 == null && v2 == null) {
            return 0;
        } else if (v1 == null && v2 != null) {
            return 1;
        } else if (v2 == null && v1 != null) {
            return -1;
        } else if (v1 instanceof Double && v2 instanceof Double) {
            Double t1 = (Double) v1;
            Double t2 = (Double) v2;
            return t1.compareTo(t2);
        } else if (v1 instanceof Float && v2 instanceof Float) {
            Float t1 = (Float) v1;
            Float t2 = (Float) v2;
            return t1.compareTo(t2);
        } else if (v1 instanceof Integer && v2 instanceof Integer) {
            Integer t1 = (Integer) v1;
            Integer t2 = (Integer) v2;
            return t1.compareTo(t2);
        } else if (v1 instanceof Long && v2 instanceof Long) {
            Long t1 = (Long) v1;
            Long t2 = (Long) v2;
            return t1.compareTo(t2);
        }else if (v1 instanceof String && v2 instanceof String) {
            String t1 = (String) v1;
            String t2 = (String) v2;
            return t1.compareTo(t2);
        }
        return 0;
    }

    public boolean hasSortFields() {
        return !fieldNames.isEmpty();
    }

    public void clear() {
        fieldNames.clear();
        sortAscending.clear();
    }
}
