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
package gov.llnl.gnem.jsac.commands;

import java.util.ServiceLoader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestHelpExists {

    private static final Logger log = LoggerFactory.getLogger(TestHelpExists.class);

    private static final SacCommand defaultCommand = new SacCommand() {
        @Override
        public void initialize(String[] tokens) {
        }

        @Override
        public void execute() {
        }
    };

    @Test
    public void testAllHelpFilesExist() {
        ServiceLoader<SacCommand> sacCommandLoader = ServiceLoader.load(SacCommand.class);

        int missingHelpCount = 0;
        for (SacCommand command : sacCommandLoader) {
            if (command.getHelpString().equals(defaultCommand.getHelpString())) {
                log.error("No help command found for command: {}", command.getClass().getSimpleName());
                missingHelpCount++;
            }
        }
        Assertions.assertEquals(0, missingHelpCount, "Expected all implemented commands to have help text available.");
    }
}
