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
package gov.llnl.gnem.jsac.plots;

import java.awt.Color;
import java.util.Objects;

public class PLabel {

    private final String labelText;
    private final String fontName;
    private final Color textColor;
    private final double xValue;
    private final double yValue;
    private final int fontSize;
    private static final double defaultX = 0.05;
    private static final double defaultY = 0.05;

    public PLabel(String labelText, String fontName, Color color, double xValue, double yValue, int fontSize) {
        this.labelText = labelText;
        this.fontName = fontName;
        this.textColor = color;
        this.xValue = xValue;
        this.yValue = yValue;
        this.fontSize = fontSize;
    }

    public PLabel(String labelText, String fontName, Color color, int fontSize) {
        this.labelText = labelText;
        this.fontName = fontName;
        this.textColor = color;
        this.xValue = defaultX;
        this.yValue = defaultY;
        this.fontSize = fontSize;
    }

    public String getLabelText() {
        return labelText;
    }

    public double getxValue() {
        return xValue;
    }

    public double getyValue() {
        return yValue;
    }

    public String getFontName() {
        return fontName;
    }

    public int getFontSize() {
        return fontSize;
    }

    public Color getTextColor() {
        return textColor;
    }

    @Override
    public String toString() {
        return "PLabel{" + "labelText=" + labelText + ", fontName=" + fontName + ", textColor=" + textColor + ", xValue=" + xValue + ", yValue=" + yValue + ", fontSize=" + fontSize + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + Objects.hashCode(this.labelText);
        hash = 43 * hash + Objects.hashCode(this.fontName);
        hash = 43 * hash + Objects.hashCode(this.textColor);
        hash = 43 * hash + (int) (Double.doubleToLongBits(this.xValue) ^ (Double.doubleToLongBits(this.xValue) >>> 32));
        hash = 43 * hash + (int) (Double.doubleToLongBits(this.yValue) ^ (Double.doubleToLongBits(this.yValue) >>> 32));
        hash = 43 * hash + this.fontSize;
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
        final PLabel other = (PLabel) obj;
        if (Double.doubleToLongBits(this.xValue) != Double.doubleToLongBits(other.xValue)) {
            return false;
        }
        if (Double.doubleToLongBits(this.yValue) != Double.doubleToLongBits(other.yValue)) {
            return false;
        }
        if (this.fontSize != other.fontSize) {
            return false;
        }
        if (!Objects.equals(this.labelText, other.labelText)) {
            return false;
        }
        if (!Objects.equals(this.fontName, other.fontName)) {
            return false;
        }
        return Objects.equals(this.textColor, other.textColor);
    }


}
