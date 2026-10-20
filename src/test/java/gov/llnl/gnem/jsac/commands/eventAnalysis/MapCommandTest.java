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
package gov.llnl.gnem.jsac.commands.eventAnalysis;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import gov.llnl.gnem.apps.coda.common.mapping.StaticHtmlLeafletMap;
import gov.llnl.gnem.apps.coda.common.mapping.api.Icon.IconTypes;
import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACFile;
import gov.llnl.gnem.jsac.io.SACHeader;

public class MapCommandTest {

    private static final String[] DEFAULT_TOKENS = new String[] { "map", "STANAMES", "on", "RMEAN_RESIDUAL", "on", "plotlegend", "on", "PLOTEVENTS", "on", "PLOTSTATIONS", "on", "WEST", "-120", "east",
            "-119", "north", "48.0", "south", "30", "eventfile", MapCommand.class.getResource("eventfile.txt").toExternalForm() };

    private static final String[] SHORT_TOKENS = new String[] { "map", "stan", "on", "RM", "on", "plotlegend", "on", "PLOTEVENTS", "on", "PLOTSTATIONS", "on", "WEST", "-120", "east", "-119", "north",
            "48.0", "south", "30", "ev", MapCommand.class.getResource("eventfile.txt").toExternalForm() };

    @BeforeAll
    protected static void setUpBeforeClass() throws Exception {
        SacDataModel.getInstance().clear();
        List<SacTraceData> traces = new ArrayList<>();
        SacTraceData trace = new SacTraceData(Paths.get("seismogram"), new SACFile(new SACHeader(), new float[0]));
        trace.setStationCode("Station A");
        trace.getSACHeader().setStla(40.0);
        trace.getSACHeader().setStlo(-110.0);
        trace.getSACHeader().setEvla(44.0);
        trace.getSACHeader().setEvlo(-114.0);
        trace.getSACHeader().setNevid(1);
        trace.getSACHeader().setKuser0("5.0");
        traces.add(trace);

        trace = new SacTraceData(Paths.get("seismogram"), new SACFile(new SACHeader(), new float[0]));
        trace.setStationCode("Station B");
        trace.getSACHeader().setStla(48.0);
        trace.getSACHeader().setStlo(-120.0);
        trace.getSACHeader().setEvla(46.0);
        trace.getSACHeader().setEvlo(-116.0);
        trace.getSACHeader().setNevid(2);
        trace.getSACHeader().setKuser0("3.0");
        traces.add(trace);

        SacDataModel.getInstance().addAll(traces, 0.0);
    }

    @AfterAll
    protected static void tearDownAfterClass() throws Exception {
        SacDataModel.getInstance().clear();
    }

    @BeforeEach
    protected void setUp() throws Exception {
    }

    @AfterEach
    protected void tearDown() throws Exception {
    }

    private static List<StaticHtmlLeafletMap> buildBasicMap() {

        List<StaticHtmlLeafletMap> maps = new ArrayList<>();
        maps.add(buildMap(DEFAULT_TOKENS));
        maps.add(buildMap(SHORT_TOKENS));

        return maps;
    }

    private static StaticHtmlLeafletMap buildMap(String[] tokens) {
        MapCommand mc = new MapCommand();
        mc.initialize(tokens);
        return mc.createMapFromOptions();
    }

    @ParameterizedTest
    @MethodSource("buildBasicMap")
    public void testEventCountsWithEventFile(StaticHtmlLeafletMap map) {
        assertEquals(5l, map.getIcons().stream().filter(icon -> IconTypes.CIRCLE.equals(icon.getType())).count(), "Expected event counts to match.");
    }

    @ParameterizedTest
    @MethodSource("buildBasicMap")
    public void testStationCountsWithPlotStations(StaticHtmlLeafletMap map) {
        assertEquals(2l, map.getIcons().stream().filter(icon -> IconTypes.TRIANGLE_UP.equals(icon.getType())).count(), "Expected station counts to match.");
    }

    @ParameterizedTest
    @MethodSource("buildBasicMap")
    public void testBoundsExist(StaticHtmlLeafletMap map) {
        assertArrayEquals(
                new double[] { -119.0, 30.0, -120.0, 48.0 },
                    new double[] { map.getBounds().getMinX(), map.getBounds().getMinY(), map.getBounds().getMaxX(), map.getBounds().getMaxY() },
                    "Expected bounds to be set.");
    }

    @ParameterizedTest
    @MethodSource("buildBasicMap")
    public void testLegendEnabled(StaticHtmlLeafletMap map) {
        assertEquals(Boolean.TRUE, map.getEventLegendSizes() != null && map.getEventLegendSizes().length > 0, "Expected legend to have entries.");
    }

    @ParameterizedTest
    @MethodSource("buildBasicMap")
    public void testStationNamesEnabled(StaticHtmlLeafletMap map) {
        assertEquals(
                Boolean.TRUE,
                    map.getIcons().stream().filter(icon -> IconTypes.TRIANGLE_UP.equals(icon.getType())).allMatch(icon -> icon.shouldBeAnnotated() == true),
                    "Expected station names to be flagged for annotation.");
    }

    @Test
    public void testOnlyWestBounds() {
        StaticHtmlLeafletMap map = buildMap(new String[] { "map", "WEST", "-120" });
        assertArrayEquals(
                new double[] { -119.0, -90.0, -120.0, 90.0 },
                    new double[] { map.getBounds().getMinX(), map.getBounds().getMinY(), map.getBounds().getMaxX(), map.getBounds().getMaxY() },
                    "Expected all bounds to be set.");
    }

    @Test
    public void testOnlyEastBounds() {
        StaticHtmlLeafletMap map = buildMap(new String[] { "map", "EAST", "-117" });
        assertArrayEquals(
                new double[] { -117.0, -90.0, -118.0, 90.0 },
                    new double[] { map.getBounds().getMinX(), map.getBounds().getMinY(), map.getBounds().getMaxX(), map.getBounds().getMaxY() },
                    "Expected all bounds to be set.");
    }

    @Test
    public void testOnlyNorthBounds() {
        StaticHtmlLeafletMap map = buildMap(new String[] { "map", "NORTH", "80" });
        assertArrayEquals(
                new double[] { -180.0, 79.0, 180.0, 80.0 },
                    new double[] { map.getBounds().getMinX(), map.getBounds().getMinY(), map.getBounds().getMaxX(), map.getBounds().getMaxY() },
                    "Expected all bounds to be set.");
    }

    @Test
    public void testOnlySouthBounds() {
        StaticHtmlLeafletMap map = buildMap(new String[] { "map", "SOUTH", "-33" });
        assertArrayEquals(
                new double[] { -180.0, -33.0, 180.0, -32.0 },
                    new double[] { map.getBounds().getMinX(), map.getBounds().getMinY(), map.getBounds().getMaxX(), map.getBounds().getMaxY() },
                    "Expected all bounds to be set.");
    }

    @Test
    public void testMagnitude() {
        StaticHtmlLeafletMap map = buildMap(new String[] { "map", "MAG", "on" });
        assertEquals(
                Boolean.TRUE,
                    map.getIcons().stream().filter(icon -> IconTypes.CIRCLE.equals(icon.getType())).allMatch(icon -> icon.getIconSize() > 0.0),
                    "Expect that icons should have sizes when MAG is set");

        map = buildMap(new String[] { "map", "MAGNITUDE", "on" });
        assertEquals(
                Boolean.TRUE,
                    map.getIcons().stream().filter(icon -> IconTypes.CIRCLE.equals(icon.getType())).allMatch(icon -> icon.getIconSize() > 0.0),
                    "Expect that icons should have sizes when MAGNITUDE is set");
    }

    @Test
    public void testMean() {
        StaticHtmlLeafletMap map = buildMap(new String[] { "map", "RESIDUAL", "on" });
        assertEquals(
                Boolean.TRUE,
                    map.getIcons().stream().filter(icon -> IconTypes.CIRCLE.equals(icon.getType())).allMatch(icon -> icon.getIconSize() > 0.0),
                    "Expect that icons should have sizes when RESIDUAL is set");

        map = buildMap(new String[] { "map", "RE", "on" });
        assertEquals(
                Boolean.TRUE,
                    map.getIcons().stream().filter(icon -> IconTypes.CIRCLE.equals(icon.getType())).allMatch(icon -> icon.getIconSize() > 0.0),
                    "Expect that icons should have sizes when RE is set");
    }

    @Test
    public void testMapScaleOff() {
        StaticHtmlLeafletMap map = buildMap(new String[] { "map", "MAPSCALE", "off" });
        assertEquals(Boolean.FALSE, map.getPlotScale(), "Expect that map shouldn't plot scale when MAPSCALE is off");
    }

    @Test
    public void testNetworkNameSet() {
        MapCommand mc = new MapCommand();

        List<SacTraceData> traces = new ArrayList<>();
        SacTraceData trace = new SacTraceData(Paths.get("seismogram"), new SACFile(new SACHeader(), new float[0]));
        trace.setStationCode("Station A");
        trace.getSACHeader().setKnetwk("Network A");
        trace.getSACHeader().setStla(40.0);
        trace.getSACHeader().setStlo(-110.0);
        traces.add(trace);

        List<SacTraceData> state = SacDataModel.getInstance().getData();
        SacDataModel.getInstance().clear();

        SacDataModel.getInstance().addAll(traces, 0.0);

        mc.initialize(DEFAULT_TOKENS);

        try {
            assertTrue(
                    mc.createMapFromOptions().getIcons().stream().filter(icon -> IconTypes.TRIANGLE_UP.equals(icon.getType())).anyMatch(icon -> icon.getId().startsWith("Network A")),
                        "Expect that if the network code is set it should be prepended to the station ID on the map.");

        } finally {
            SacDataModel.getInstance().clear();
            SacDataModel.getInstance().addAll(state, 0.0);
        }
    }

    @Test
    public void testFilenameChange() {
        MapCommand mc = new MapCommand();
        mc.initialize(new String[] { "map", "FILE", "C:\\fake\\path\\absolute\\output" });
        assertTrue(mc.resolveFilePath().endsWith("map.html"), () -> "Expect that map command should generate a map.html file if given an absolute directory. " + mc.resolveFilePath().toString());

        mc.initialize(new String[] { "map", "FILE", "/fake/path/absolute/output/" });
        assertTrue(mc.resolveFilePath().endsWith("map.html"), () -> "Expect that map command should generate a map.html file if given an absolute directory. " + mc.resolveFilePath().toString());

        mc.initialize(new String[] { "map", "FILE", "output" });
        assertTrue(
                mc.resolveFilePath().endsWith("output/map.html"),
                    () -> "Expect that map command should generate a {filename}/map.html file if given just a filename. " + mc.resolveFilePath().toString());

        mc.initialize(new String[] { "map", "FILE", "output.html" });
        assertTrue(
                mc.resolveFilePath().endsWith("output.html"),
                    () -> "Expect that map command should generate a {filename}.html file if given a filename that ends in .html. " + mc.resolveFilePath().toString());

        mc.initialize(new String[] { "map", "FILE", "output.txt" });
        assertTrue(
                mc.resolveFilePath().endsWith("output.txt.html"),
                    () -> "Expect that map command should generate a {filename}.{ext}.html file if given a filename that ends in something other than .html " + mc.resolveFilePath().toString());
    }
}
