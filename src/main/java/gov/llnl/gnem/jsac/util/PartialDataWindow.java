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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dodge1
 */
public class PartialDataWindow {

    private static final Logger log = LoggerFactory.getLogger(PartialDataWindow.class);

    private boolean valid = false;
    private boolean enabled = false;
    private String startReference = "B";
    private double startOffset = 0.0;
    private Integer startIntegerOffset = null;
    private String endReference = "E";
    private double endOffset = 0.0;
    private Integer endIntegerOffset = null;

    public String getStartReference() {
        return startReference;
    }

    public double getStartOffset() {
        return startOffset;
    }

    public String getEndReference() {
        return endReference;
    }

    public double getEndOffset() {
        return endOffset;
    }

    public void setStartReference(String startReference) {
        this.startReference = startReference.toUpperCase();
    }

    public void setStartOffset(double startOffset) {
        this.startOffset = startOffset;
    }

    public void setEndReference(String endReference) {
        this.endReference = endReference.toUpperCase();
    }

    public void setEndOffset(double endOffset) {
        this.endOffset = endOffset;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setNoReferences() {
        startReference = null;
        endReference = null;
    }

    @Override
    public String toString() {
        return "PartialDataWindow{"
                + "valid="
                + valid
                + ", enabled="
                + enabled
                + ", startReference="
                + startReference
                + ", startOffset="
                + startOffset
                + ", startIntegerOffset="
                + startIntegerOffset
                + ", endReference="
                + endReference
                + ", endOffset="
                + endOffset
                + ", endIntegerOffset="
                + endIntegerOffset
                + '}';
    }

    public void validate() {
        if (startReference == null) {
            valid = false;
            enabled = false;
            String msg = String.format("Invalid PDW! Start Reference is not set.");
            log.warn(msg);
            return;
        }
        if (endReference == null) {
            valid = false;
            enabled = false;
            String msg = String.format("Invalid PDW! End Reference is not set.");
            log.warn(msg);
            return;
        }
        if (startReference.equals(endReference) && (endOffset <= startOffset)) {
            valid = false;
            enabled = false;
            String msg = String.format("Invalid PDW! End offset must be larger than start offset when start reference = end reference.");
            log.warn(msg);
            return;
        }
        valid = true;
    }

    public void setStartIntegerOffset(Integer npts) {
        startIntegerOffset = npts;
    }

    public void setEndIntegerOffset(Integer npts) {
        endIntegerOffset = npts;
    }

    public boolean isValid() {
        return valid;
    }

    public Integer getStartIntegerOffset() {
        return startIntegerOffset;
    }

    public Integer getEndIntegerOffset() {
        return endIntegerOffset;
    }

    public Double getStartOffset(double delta) {
        if (startIntegerOffset != null) {
            return startIntegerOffset * delta;
        } else {
            return getStartOffset();
        }
    }

    public Double getEndOffset(double delta) {
        if (endIntegerOffset != null) {
            return endIntegerOffset * delta;
        } else {
            return getEndOffset();
        }
    }

    public void setDefaults() {
        startReference = "B";
        startOffset = 0.0;
        startIntegerOffset = null;
        endReference = "E";
        endOffset = 0.0;
        endIntegerOffset = null;
    }

}
