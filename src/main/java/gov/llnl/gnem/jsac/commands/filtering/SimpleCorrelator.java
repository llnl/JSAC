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

import java.util.Arrays;
import java.util.List;

import com.oregondsp.signalProcessing.fft.RDFT;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACHeader;
import llnl.gnem.core.signalprocessing.Sequence;

public class SimpleCorrelator {
	
	public static void correlateAll( List< SacTraceData > traces, float[] master, boolean normalize ) {
		
		int       ntraces      = traces.size();			
		int       maxLength    = traces.get(0).getNsamp();
		for ( int itrace = 0;  itrace < ntraces;  itrace++ ) maxLength = Math.max( maxLength, traces.get(itrace).getNsamp() );

		double    E            = 0.0;
		for ( int i = 0;  i < master.length;  i++ ) E += master[i]*master[i];
		
		int log2N = 0;
		int N     = 1;
		while( N < master.length + maxLength - 1 ) {
			log2N++;
			N *= 2;
		}
		
		RDFT    dft = new RDFT( log2N );
		float[] X   = new float[ N ];
		float[] Y   = new float[ N ];
		float[] tmp = new float[ N ];
		System.arraycopy( master, 0, tmp, 0, master.length );
		dft.evaluate( tmp, X );
		
		for ( int itrace = 0;  itrace < ntraces;  itrace++ ) {
				
			Arrays.fill( tmp, 0.0f );
			float[] y = traces.get( itrace ).getData();
			System.arraycopy( y, 0, tmp, 0, y.length );
			dft.evaluate( tmp, Y );
			RDFT.dftProduct( X, Y, -1.0f );
			dft.evaluateInverse( Y, tmp );
			Sequence.cshift( tmp, master.length );
			int resultLength = master.length + y.length - 1;
			float[] correlation = new float[ resultLength ];
			System.arraycopy( tmp, 0, correlation, 0, resultLength );
			
			// find maximum
			
			float maxC = Float.MIN_VALUE;
			int   imax = -1;
			for ( int i = 0;  i < resultLength;  i++ ) {
				if( correlation[i] > maxC ) {
					maxC = correlation[i];
					imax = i;
				}
			}
			
			// refine maximum by fitting quadratic over 5 successive samples centered on imax
			
			float[] peak = refine( correlation, imax );
			
			float lag = peak[0] - (float) master.length;
				
			float scale = 1.0f;
			if ( normalize ) {
				double e = 0.0;
				for ( int i = 0;  i < y.length;  i++ ) e += y[i]*y[i];
				scale = (float) ( 1.0 / Math.sqrt(e*E) );
				for ( int i = 0;  i < resultLength;  i++ ) {
					correlation[i] *= scale;
				}
			}
				
			traces.get( itrace ).setData( correlation );
			
			SACHeader header = traces.get( itrace ).getSacFileHeader();
			double    dt     = header.getDelta();
			header.setB( -master.length*dt );
			header.setUser( 0, lag*dt );
			header.setKuser0( "Lag" );
			header.setUser( 1, (double) peak[1]*scale );
			header.setKuser1( "Peak" );
			header.setUser( 2, (double) peak[1]/E );
			header.setKuser2( "Relative" );
			header.setReferenceTime(null);

		}
		
	}
	
	
	public static float[] refine( float[] correlation, int imax ) {
		
		float[] c = new float[3];
		// three-point fit
		
		c[0] = 0.5f*( correlation[imax-1] + correlation[imax+1] ) - correlation[imax];
		c[1] = 0.5f*( correlation[imax+1] - correlation[imax-1] );
		c[2] = correlation[imax];
		
		float[] retval = new float[2];
		retval[0] = imax - c[1]/(2*c[0]);
		retval[1] = c[2] - c[1]*c[1]/(4*c[0]);
			
		return retval;
	}

}
