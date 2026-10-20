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
package gov.llnl.gnem.jsac.commands.filtering;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.oregondsp.signalProcessing.filter.fir.equiripple.CenteredDifferentiator;
import com.oregondsp.signalProcessing.filter.fir.equiripple.CenteredHilbertTransform;
import com.oregondsp.signalProcessing.filter.fir.equiripple.EquirippleBandpass;
import com.oregondsp.signalProcessing.filter.fir.equiripple.EquirippleHighpass;
import com.oregondsp.signalProcessing.filter.fir.equiripple.EquirippleLowpass;
import com.oregondsp.signalProcessing.filter.fir.equiripple.StaggeredDifferentiator;
import com.oregondsp.signalProcessing.filter.fir.equiripple.StaggeredHilbertTranform;

import gov.llnl.gnem.jsac.commands.AttributeDescriptor;
import gov.llnl.gnem.jsac.commands.SacCommand;
import gov.llnl.gnem.jsac.commands.TokenListParser;
import gov.llnl.gnem.jsac.commands.ValuePossibilities;
import gov.llnl.gnem.jsac.commands.filtering.enums.FIRType;
import gov.llnl.gnem.jsac.plots.plotDFIRDesign.PlotDFIRDesignFrame;
import gov.llnl.gnem.jsac.util.PathManager;
import llnl.gnem.core.signalprocessing.fft.RFFT;

import java.io.File;
import java.io.FileNotFoundException;

public class FIRDesignSacCommand implements SacCommand {
	
	private static final int log2XfmSize = 11;

    private static final List<AttributeDescriptor> descriptors = new ArrayList<>();

    static {

        descriptors.add(new AttributeDescriptor("LP", ValuePossibilities.FIVE_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("HP", ValuePossibilities.FIVE_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("BP", ValuePossibilities.EIGHT_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("DIFC", ValuePossibilities.THREE_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("HILC", ValuePossibilities.THREE_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("DIFS", ValuePossibilities.TWO_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("HILS", ValuePossibilities.TWO_VALUES, Double.class));
        descriptors.add(new AttributeDescriptor("WRITE", ValuePossibilities.ONE_VALUE, String.class));
        descriptors.add(new AttributeDescriptor("W", ValuePossibilities.ONE_VALUE, String.class));

    }

    private boolean valid;

    private FIRType      type;
    private int          N;
    private double       Omega_p;
    private double       Omega_s;
    private double       Omega_p1;
    private double       Omega_p2;
    private double       Omega_s1;
    private double       Omega_s2;
    private double       Wp;
    private double       Ws;
    private double       Ws1;
    private double       Ws2;
    private double       delta;

    private float[] coefficients;
    private String filename;

    public FIRDesignSacCommand() {
    }

    @Override
    public void execute() {

        if (valid) {

            switch (type) {

                case LOWPASS:

                    coefficients = (new EquirippleLowpass(N, Omega_p, Wp, Omega_s, Ws)).getCoefficients();
                    break;

                case HIGHPASS:

                    coefficients = (new EquirippleHighpass(N, Omega_s, Ws, Omega_p, Wp)).getCoefficients();
                    break;

                case BANDPASS:

                    coefficients = (new EquirippleBandpass(N, Omega_s1, Ws1, Omega_p1, Omega_p2, Wp, Omega_s2, Ws2)).getCoefficients();
                    break;

                case CENTERED_DIFFERENTIATOR:

                    coefficients = (new CenteredDifferentiator(N, delta, Omega_p)).getCoefficients();
                   break;

                case CENTERED_HILBERT_TRANSFORMER:

                    coefficients = (new CenteredHilbertTransform(N, Omega_p1, Omega_p2)).getCoefficients();
                    break;

                case STAGGERED_DIFFERENTIATOR:

                    coefficients = (new StaggeredDifferentiator(N, delta)).getCoefficients();
                    break;

                case STAGGERED_HILBERT_TRANSFORMER:

                    coefficients = (new StaggeredHilbertTranform(N, Omega_p)).getCoefficients();
                    break;

            }
            String dir = PathManager.getInstance().getCurrentDir().toString();
            File file = new File(dir, filename);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                try (PrintStream ps = new PrintStream(fos)) {
                    DecimalFormat F = new DecimalFormat("###0.00000000");
                    for (int i = 0; i < coefficients.length; i++) {
                        ps.println(F.format(coefficients[i]));
                    }
                }
            } catch (FileNotFoundException ex) {
                System.getLogger(FIRDesignSacCommand.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            } catch (IOException ex) {
                System.getLogger(FIRDesignSacCommand.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            
            // plot response
            
            float[] response = evaluateFrequencyResponse();
            PlotDFIRDesignFrame.getInstance().setVisible(true);
            double df = 1.0/1024.0;
            String title = "Filter Frequency Response";
            PlotDFIRDesignFrame.getInstance().plotDesignData(response, df, title);

        }

    }

    @Override
    public void initialize(String[] tokens) {

        // defaults
        valid = true;

        type     = FIRType.LOWPASS;
        N        = 30;
        Omega_p  = 0.25;
        Wp       = 1.0;
        Omega_s  = 0.4;
        Ws       = 1.0;
        filename = "FIR.txt";

        Map<String, List<Object>> parsedTokens = TokenListParser.parseTokens(descriptors, tokens);
        if (parsedTokens.isEmpty()) {
            return;
        }

        if (parsedTokens.containsKey("LP")) {

            type = FIRType.LOWPASS;

            List<Object> parameters = parsedTokens.get("LP");
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else if (parameters.size() >= 5) {
                N = (int) Math.round((double) parameters.get(0));
                Omega_p = (double) parameters.get(1);
                Wp = (double) parameters.get(2);
                Omega_s = (double) parameters.get(3);
                Ws = (double) parameters.get(4);
                valid = true;
            } else {
                throw new IllegalStateException("Too few arguments.");
            }

        }

        if (parsedTokens.containsKey("HP")) {

            type = FIRType.HIGHPASS;

            List<Object> parameters = parsedTokens.get("HP");
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else if (parameters.size() >= 5) {
                N = (int) Math.round((double) parameters.get(0));
                Omega_s = (double) parameters.get(1);
                Ws = (double) parameters.get(2);
                Omega_p = (double) parameters.get(3);
                Wp = (double) parameters.get(4);
                valid = true;
            } else {
                throw new IllegalStateException("Too few arguments.");
            }

        }

        if (parsedTokens.containsKey("BP")) {

            type = FIRType.BANDPASS;

            List<Object> parameters = parsedTokens.get("BP");
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else if (parameters.size() >= 8) {
                N = (int) Math.round((double) parameters.get(0));
                Omega_s1 = (double) parameters.get(1);
                Ws1 = (double) parameters.get(2);
                Omega_p1 = (double) parameters.get(3);
                Omega_p2 = (double) parameters.get(4);
                Wp = (double) parameters.get(5);
                Omega_s2 = (double) parameters.get(6);
                Ws2 = (double) parameters.get(7);
                valid = true;
            } else {
                throw new IllegalStateException("Too few arguments.");
            }

        }

        if (parsedTokens.containsKey("DIFC")) {

            type = FIRType.CENTERED_DIFFERENTIATOR;

            List<Object> parameters = parsedTokens.get("DIFC");
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else if (parameters.size() >= 3) {
                N = (int) Math.round((double) parameters.get(0));
                delta = (double) parameters.get(1);
                Omega_p = (double) parameters.get(2);
                valid = true;
            } else {
                throw new IllegalStateException("Too few arguments.");
            }

        }

        if (parsedTokens.containsKey("HILC")) {

            type = FIRType.CENTERED_HILBERT_TRANSFORMER;

            List<Object> parameters = parsedTokens.get("HILC");
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else if (parameters.size() >= 3) {
                N = (int) Math.round((double) parameters.get(0));
                Omega_p1 = (double) parameters.get(1);
                Omega_p2 = (double) parameters.get(2);
                valid = true;
            } else {
                throw new IllegalStateException("Too few arguments.");
            }

        }

        if (parsedTokens.containsKey("DIFS")) {

            type = FIRType.STAGGERED_DIFFERENTIATOR;

            List<Object> parameters = parsedTokens.get("DIFS");
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else if (parameters.size() >= 2) {
                N = (int) Math.round((double) parameters.get(0));
                delta = (double) parameters.get(1);
                valid = true;
            } else {
                throw new IllegalStateException("Too few arguments.");
            }

        }

        if (parsedTokens.containsKey("HILS")) {

            type = FIRType.STAGGERED_HILBERT_TRANSFORMER;

            List<Object> parameters = parsedTokens.get("HILS");
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else if (parameters.size() >= 2) {
                N = (int) Math.round((double) parameters.get(0));
                Omega_p = (double) parameters.get(1);
                valid = true;
            } else {
                throw new IllegalStateException("Too few arguments.");
            }

        }

        if (parsedTokens.containsKey("WRITE") || parsedTokens.containsKey("W")) {

            List<Object> parameters = parsedTokens.get("WRITE");
            if (parameters == null) {
                parameters = parsedTokens.get("W");
            }
            if (parameters == null) {
                valid = false;
            } else if (parameters.isEmpty()) {
                valid = false;
            } else {
                filename = (String) parameters.get(0);
            }

        }

    }
    

    @Override
    public Collection<String> getCommandNames() {
        String[] names = {"DFIR","FIRDESIGN"};
        return new ArrayList<>(Arrays.asList(names));
    }
    
    
    public float[] evaluateFrequencyResponse() {
    	
    	if ( coefficients == null ) 
    		throw new IllegalStateException( "Filter undefined" );
    	
    	int N = (int) Math.round( Math.pow( 2.0, log2XfmSize ) );
    	float[] Xfm = new float[ N ];
        System.arraycopy( coefficients, 0, Xfm, 0, coefficients.length );
        RFFT fft = new RFFT( log2XfmSize );
        fft.dft( Xfm );
        
        int N2         = N/2;
        float[] retval = new float[N2+1];
        retval[0] = Math.abs( Xfm[0] );
        for ( int i = 1;  i < N2;  i++ ) {
        	retval[i] = (float) Math.sqrt( Xfm[i]*Xfm[i] + Xfm[N-i]*Xfm[N-i] );
        }
        retval[ N2 ] = Math.abs( Xfm[N2] );
        
    	return retval;
    }

}
