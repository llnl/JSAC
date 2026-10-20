/*-
 * #%L
 * Java Seismic Analysis Code (JSAC)
 *  LLNL-CODE-855505
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2022 - 2026 Lawrence Livermore National Laboratory
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
package gov.llnl.gnem.jsac.arrayProcessing;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import java.util.Scanner;

public class ArrayDefinitions {

    private final Map<String, ArrayDefinition> elementMap;

    private ArrayDefinitions() {
        elementMap = new HashMap<>();
    }

    public static ArrayDefinitions getInstance() {
        return ArrayDefinitionsHolder.INSTANCE;
    }

    ArrayDefinition findArray(String stationCode) {
        ArrayDefinition result = null;
        for (ArrayDefinition ad : elementMap.values()) {
            if (ad.hasStation(stationCode)) {
                if (result == null) {
                    result = ad;
                } else if (!result.equals(ad)) {
                    throw new IllegalStateException("Station code " + stationCode + " belongs to more than one array!");
                }
            }
        }
        return result;
     }

    public boolean isEmpty() {
        return elementMap.isEmpty();
    }

    public void clear() {
        elementMap.clear();
    }

    private static class ArrayDefinitionsHolder {

        private static final ArrayDefinitions INSTANCE = new ArrayDefinitions();
    }

    public void loadDefinitionsFromFile(Path fileName) throws FileNotFoundException {
        elementMap.clear();
        try (Scanner in = new Scanner(new FileReader(fileName.toString()))) {
            while (in.hasNext()) {
                String line = in.nextLine();
                ArrayElement element = new ArrayElement(line);
                ArrayDefinition tmp = elementMap.get(element.getArrayName());
                if (tmp == null) {
                    tmp = new ArrayDefinition(element);
                    elementMap.put(element.getArrayName(), tmp);
                } else {
                    tmp.addElement(element);
                }
            }
        }
        int numArrays = elementMap.size();
        System.out.println("Loaded definitions for "+numArrays+" arrays.");
    }

}
