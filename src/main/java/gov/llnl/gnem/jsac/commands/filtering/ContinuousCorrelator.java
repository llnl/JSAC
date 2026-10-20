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
package gov.llnl.gnem.jsac.commands.filtering;

import java.util.Arrays;

import com.oregondsp.signalProcessing.Sequence;
import com.oregondsp.signalProcessing.filter.fir.OverlapAdd;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;

public class ContinuousCorrelator {
	
	private final OverlapAdd overlapAdd;
	private final float[]    template;
	private final int        blockSize;
	
	
	public ContinuousCorrelator( float[] template, boolean normalize ) {
		
		int fftsize = 128;
		while ( fftsize < 2*template.length - 1 ) {
			fftsize *= 2;
		}
		
		blockSize = fftsize - template.length;
		
		float[] tmp = template.clone();
		Sequence.reverse( tmp );
		
		overlapAdd    = new OverlapAdd( tmp, blockSize );
		this.template = template;
		
		if ( normalize ) {
			tmp = new float[ template.length ];
			Arrays.fill( tmp, 1.0f );
                        new OverlapAdd( tmp, blockSize );
		}
		
	}
	
	
	public void correlate( SacTraceData std ) {
		
		float[] x      = std.getData();
		int     n      = template.length;
		int     m      = x.length;
		int     ntotal = m + n -1;
		float[] block  = new float[ blockSize ];
		
		float[] y  = new float[ ntotal ];
		
		int ptr = 0;
		int nRemaining = m;
		while ( ptr < m ) {
			Arrays.fill( block, 0.0f );
			System.arraycopy( x, ptr, block, 0, Math.min( blockSize, nRemaining ) );
			overlapAdd.filter( block, 0, block, 0 );     
			System.arraycopy( block, 0, y, ptr, Math.min( blockSize, ntotal-ptr ) );
			ptr += blockSize;
			nRemaining -= blockSize;
		}
		
		std.setData( y );
	}

}
