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
 * 
 * Author:  Dave Harris
 */
package gov.llnl.gnem.jsac.arrayProcessing;

import java.util.ArrayList;

import com.oregondsp.signalProcessing.Sequence;
import com.oregondsp.signalProcessing.filter.iir.ThiranAllpass;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;

public class Aligner {
	
	public static float[][] align( ArrayData data, double bearing, double velocity, boolean useElevationCorrections, 
			                       double anginc, double survel, double[] reference ) {
		
		float[][] waveforms = data.getWaveforms();
		int       nch       = waveforms.length;
		
		double[][] staCoords = data.getArray();
		
		double c = Math.cos( bearing/180.0*Math.PI );
		double s = Math.sin( bearing/180.0*Math.PI );
		
		double[] delays = new double[ nch ];
		
		float[][] retval = new float[nch][];
		
		for ( int ich = 0;  ich < nch; ich++ ) {
			
			double x = staCoords[ich][0] - reference[0];
			double y = staCoords[ich][1] - reference[1];
			double z = staCoords[ich][2] - reference[2];
			
			delays[ich] = (s*x + c*y)/velocity;
			
			if ( useElevationCorrections ) {
				delays[ich] -= z / ( Math.cos( anginc*Math.PI/180.0 ) * survel);
			}

			
			retval[ich] = waveforms[ich];
			double shift  = delays[ich] / data.getSamplingInterval();
			if ( shift != 0.0 ) {
				int    ishift   = (int) Math.floor( shift ) - 3; 
				double D        = shift - ishift;
				ThiranAllpass A = new ThiranAllpass( 4, D );
				Sequence.zeroShift( retval[ich], ishift );
				A.filter( retval[ich] );
			}
			

			
		}
		
		return retval;		
	}
	
	
	public static void alignWaveformsInMemory( ArrayData data, double bearing, double velocity, boolean useElevationCorrections, double anginc, double survel, double[] reference ) {
		
		float[][] waveforms = align( data, bearing, velocity, useElevationCorrections, anginc, survel, reference);
		
		// replace waveform data in memory, change referenceTime, B, E, npts in SACHeader
		
		ArrayList< SacTraceData > traces = (ArrayList<SacTraceData>) SacDataModel.getInstance().getData();
		
		int nch = waveforms.length;
		
		for ( int ich = 0;  ich < nch;  ich++ ) {
			traces.get(ich).setData( waveforms[ich] );
		}
		
	}

}
