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
package gov.llnl.gnem.jsac.plots.picking;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.BackingStoreException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import llnl.gnem.core.util.UserObjectPreferences;

public class KeyPhaseMapper {

    private static final Logger log = LoggerFactory.getLogger(KeyPhaseMapper.class);

    private final static String NODE_NAME = "JSAC/KEY_PHASE_MAP";
    private final Map<Character, PhaseUsage> charPhaseMap;

    private KeyPhaseMapper() {
        charPhaseMap = new HashMap<>();
        initialize();
    }

    public static KeyPhaseMapper getInstance() {
        return KeyPhaseMapperHolder.INSTANCE;
    }

    public PhaseUsage getMappedPhase(char keyChar) {
        return charPhaseMap.get(keyChar);
    }

    private void initialize() {
        Map<Character, PhaseUsage> tmp = null;
        try {
            tmp = (Map<Character, PhaseUsage>) UserObjectPreferences.getInstance().retrieveObjectFromPrefs(NODE_NAME, Map.class);
            if (tmp == null) {
                tmp = createDefaultMap();
            }
        } catch (IOException | ClassNotFoundException | BackingStoreException ex) {
            log.error("Failed retrieving phase map!", ex);
        }
        if (tmp == null) {
            tmp = new HashMap<>();
            tmp.put('1', new PhaseUsage("Pn", true));
            tmp.put('2', new PhaseUsage("Pg", true));
            tmp.put('3', new PhaseUsage("Sn", true));
            tmp.put('4', new PhaseUsage("Lg", true));
        }
        charPhaseMap.putAll(tmp);
    }

    private Map<Character, PhaseUsage> createDefaultMap() throws IOException, BackingStoreException {
        Map<Character, PhaseUsage> keyPhaseMap = new HashMap<>();
        keyPhaseMap.put('1', new PhaseUsage("Pn", true));
        keyPhaseMap.put('2', new PhaseUsage("Pg", true));
        keyPhaseMap.put('3', new PhaseUsage("Sn", true));
        keyPhaseMap.put('4', new PhaseUsage("Lg", true));
        keyPhaseMap.put('p', new PhaseUsage("Pn", true));
        keyPhaseMap.put('g', new PhaseUsage("Pg", true));
        keyPhaseMap.put('s', new PhaseUsage("Sn", true));
        keyPhaseMap.put('l', new PhaseUsage("Lg", true));

        keyPhaseMap.put('5', new PhaseUsage("P", true));
        keyPhaseMap.put('6', new PhaseUsage("S", true));
        keyPhaseMap.put('7', new PhaseUsage("PcS", true));
        keyPhaseMap.put('8', new PhaseUsage("SS", true));
        keyPhaseMap.put('9', new PhaseUsage("PcP", true));
        keyPhaseMap.put('0', new PhaseUsage("ScS", true));
        UserObjectPreferences.getInstance().saveObjectToPrefs(NODE_NAME, keyPhaseMap);
        return keyPhaseMap;
    }

    public void saveState() {
        try {
            UserObjectPreferences.getInstance().saveObjectToPrefs(NODE_NAME, charPhaseMap);
        } catch (IOException | BackingStoreException ex) {
            log.error("Failed saving phase map!", ex);
        }
    }

    public Collection<String> getAllowablePhases() {
        Set<String> result = new HashSet<>();
        int radix = 10;
        for (int j = 0; j < 10; ++j) {
            char vChar = Character.forDigit(j, radix);
            PhaseUsage pu = charPhaseMap.get(vChar);
            if (pu != null && pu.isEnable()) {
                result.add(pu.getPhase());
            }
        }
        return result;
    }

    private static class KeyPhaseMapperHolder {

        private static final KeyPhaseMapper INSTANCE = new KeyPhaseMapper();
    }

    public static class PhaseUsage implements Serializable {

        private static final long serialVersionUID = -5682714109233453773L;
        private String phase;
        private boolean enable;

        public PhaseUsage(String phase, boolean enable) {
            this.phase = phase;
            this.enable = enable;
        }

        public String getPhase() {
            return phase;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        @Override
        public String toString() {
            return "PhaseUsage{" + "phase=" + phase + ", enable=" + enable + '}';
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 73 * hash + Objects.hashCode(this.phase);
            hash = 73 * hash + (this.enable ? 1 : 0);
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
            final PhaseUsage other = (PhaseUsage) obj;
            if (this.enable != other.enable) {
                return false;
            }
            if (!Objects.equals(this.phase, other.phase)) {
                return false;
            }
            return true;
        }

        void setPhase(String phase) {
            this.phase = phase;
        }

    }
}
