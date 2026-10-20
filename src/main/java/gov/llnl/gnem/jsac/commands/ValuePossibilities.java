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

/**
 *
 * @author dodge1
 */
public enum ValuePossibilities {
    NO_VALUE(0), ZERO_OR_MORE(-1),ONE_VALUE(1), ONE_OR_MORE(-1), TWO_VALUES(2), TWO_OR_MORE(-1), 
    THREE_VALUES(3), THREE_OR_MORE(-1), FOUR_VALUES(4), FOUR_OR_MORE(-1), FIVE_VALUES(5), SIX_VALUES(6), EIGHT_VALUES(8);
    private final int numValues;

    private ValuePossibilities(int count) {
        numValues = count;
    }
    
    public int getNumValues()
    {
        return numValues;
    }
}
