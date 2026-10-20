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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author dodge1
 */
public class FileIDInfo {

    public static enum Type {
        DEFAULT, NAME, LIST
    }

    public static enum Location {
        UR, UL, LR, LL
    }

    public static enum Format {
        EQUALS, COLON, NONAMES
    }

    public static enum FieldLayout {
        VERTICAL, HORIZONTAL
    }
    private static final List<String> defaults;

    static {
        String[] tmp = {"KEVNM", "KSTCMP", "KZDATE", "KZTIME"};
        defaults = Arrays.asList(tmp);
    }

    private boolean on;
    private Type type;
    private Location location = Location.UR;
    private Format format;
    private FieldLayout fieldLayout;
    private String fontName;
    private int fontSize;
    private final List<String> hdrlist;
    private double spacingAdjustment;
    private String fieldDelimiter;
    private Color textColor;
    private boolean forceColor;

    private FileIDInfo() {
        on = true;
        type = Type.DEFAULT;
        format = Format.NONAMES;
        fieldLayout = FieldLayout.VERTICAL;
        hdrlist = new ArrayList<>(defaults);
        fontName = "Arial";
        fontSize = 12;
        spacingAdjustment = 1;
        fieldDelimiter = ",";
        textColor = Color.BLACK;
        forceColor = false;
    }

    String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public void setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public FieldLayout getFieldLayout() {
        return fieldLayout;
    }

    public void setFieldLayout(FieldLayout fieldLayout) {
        this.fieldLayout = fieldLayout;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public List<String> getHdrlist() {
        return new ArrayList<>(hdrlist);
    }

    public void replaceHdrList(List<String> alist) {
        hdrlist.clear();
        hdrlist.addAll(alist);
    }

    public static List<String> getDefaultHdrlist() {
        return new ArrayList<>(defaults);
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public void setForceColor(boolean forceColor) {
        this.forceColor = forceColor;
    }

    public boolean isForceColor() {
        return forceColor;
    }

    public static FileIDInfo getInstance() {
        return FileIDInfoHolder.INSTANCE;
    }

    public double getSpacingAdjustment() {
        return spacingAdjustment;
    }

    public void setSpacingAdjustment(double spacingAdjustment) {
        this.spacingAdjustment = spacingAdjustment;
    }

    public Color getTextColor(Color lineColor) {
        Color tmp = lineColor;
        if (isForceColor()) {
            tmp = getTextColor();
        }
        return tmp;
    }

    private static class FileIDInfoHolder {

        private static final FileIDInfo INSTANCE = new FileIDInfo();
    }
}
