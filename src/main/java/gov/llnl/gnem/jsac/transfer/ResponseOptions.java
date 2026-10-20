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

package gov.llnl.gnem.jsac.transfer;

import gov.llnl.gnem.response.ResponseType;
import java.nio.file.Path;
import java.util.Objects;


public class ResponseOptions {

    private final ResponseType responseType;
    private final String filename;
    private final Path searchDir;

    public ResponseOptions(ResponseType responseType,String filename, Path searchDir) {
        this.filename = filename;
        this.searchDir = searchDir;
        this.responseType = responseType;
    }

    public String getFilename() {
        return filename;
    }

    public Path getSearchDir() {
        return searchDir;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    @Override
    public String toString() {
        return "ResponseOptions{" + "ResponseType=" + responseType + ", filename=" + filename + ", searchDir=" + searchDir + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.responseType);
        hash = 79 * hash + Objects.hashCode(this.filename);
        hash = 79 * hash + Objects.hashCode(this.searchDir);
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
        final ResponseOptions other = (ResponseOptions) obj;
        if (!Objects.equals(this.filename, other.filename)) {
            return false;
        }
        if (this.responseType != other.responseType) {
            return false;
        }
        return Objects.equals(this.searchDir, other.searchDir);
    }

}
