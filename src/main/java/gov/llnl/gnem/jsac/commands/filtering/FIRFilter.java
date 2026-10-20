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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACHeader;
import llnl.gnem.core.signalprocessing.fft.OverlapAdd;

public class FIRFilter {
	
	private static final double  THRESHOLD      = 1.0e-5;
	private static final int     MAX_BLOCK_SIZE = 8000;
	private static final int     MIN_BLOCK_SIZE = 1000;
	
	private float[]                    coefficients;
	private List< SacTraceData >       traceData;
	private int                        ntraces;
	private boolean                    CCB;
	private double                     delta;
	
	
	
	public FIRFilter( float[] coefficients, boolean CCB ) {
		
		traceData         = SacDataModel.getInstance().getData();
		ntraces           = traceData.size();
		this.coefficients = coefficients;
	    this.CCB          = CCB;
	    
	    if ( CCB ) {  // check that data are consistent
	    	
		    // check consistency of sampling rates
    		
		    for ( int ich = 0;  ich < ntraces;  ich++ ) {
		    	
		    	SACHeader header = traceData.get(ich).getSACHeader();
		    	if ( ich == 0 ) {
		    		delta       = header.getDelta();
		    	}
		    	else {
		    		double num   = Math.abs( delta - header.getDelta() );
		    		double denom = ( delta + header.getDelta() ) / 2.0;
		    		if ( num/denom > THRESHOLD )
		    			throw new IllegalStateException( "Inconsistent sampling rates" );
		    	}

		    }
	    	
	    	for ( int ich = 1;  ich < ntraces;  ich++ ) {
	    		
	    		// compare predicted start time of current file to actual start time, if discrepant, bail
	    		// also check that the network, station and component are the same
	    		
	    		SACHeader previous = traceData.get( ich - 1 ).getSACHeader();
	    		SACHeader current  = traceData.get(ich).getSACHeader();
	    		double predictedTime   = previous.getBeginTime() + previous.getNpts()*delta;
	    		double startTime       = current.getBeginTime();
	    		if ( Math.abs( predictedTime - startTime ) > delta*0.01 ) {
	    			throw new IllegalStateException( "Not all files are consecutive and contiguous" );
	    		}
	    		String key1 = previous.getKnetwk().trim() + "_" + previous.getKstnm().trim() + "_" + previous.getKcmpnm().trim();
	    		String key2 = current.getKnetwk().trim()  + "_" + current.getKstnm().trim()  + "_" + current.getKcmpnm().trim();
	    		
	    		if ( !key1.equals(key2) ) 
	    			throw new IllegalStateException( "Discrepancy in network, station or component" );
	    		
	    	}
	    		
	    }

	}
	
	
	public void apply() {
		
		if ( CCB ) 
			applyContiguous();
		else
			applyStandard();
		
	}
	
	
	public void applyStandard() {

		for ( int itrace = 0;  itrace < ntraces;  itrace++ ) {
			
			float[]    data      = traceData.get(itrace).getData();
			int        N         = data.length;
			int        blockSize = findBlockSize( N );
			OverlapAdd F         = new OverlapAdd( coefficients, blockSize );
			float[]    block     = new float[ blockSize ];
			int        nblocks   = N / blockSize + 1;
			float[]    result    = new float[ nblocks*blockSize ];
			int        ptr       = 0;
			
			for ( int i = 0;  i < nblocks;  i++ ) {
				
				Arrays.fill( block, 0.0f );
				System.arraycopy( data, ptr, block, 0, Math.min( blockSize, N-ptr ) );

				F.filter( block, result, ptr );
				
				ptr += blockSize;
			}
			
			float[] cropped = new float[ N ];
			int half = coefficients.length/2;
			System.arraycopy( result, half, cropped, 0, N - half );
			
			traceData.get(itrace).setData( cropped );
			
		}
		
	}
	
	
	public void applyContiguous() {
		
		int        blockSize = findBlockSize( traceData.get(0).getNsamp() );
		System.out.println( "Using block size: " + blockSize );
		OverlapAdd F         = new OverlapAdd( coefficients, blockSize );
		float[]    block     = new float[ blockSize ];
		
		for ( int itrace = 0;  itrace < ntraces;  itrace++ ) {
			
			float[]    data      = traceData.get(itrace).getData();
			int        N         = data.length;

			if ( N % blockSize != 0 ) 
				throw new IllegalStateException( "A trace is found with length not an integer multiple of the block length" );

			int        nblocks   = N / blockSize;
			float[]    scratch   = new float[ nblocks*blockSize ];
			int        ptr       = 0;
			
			for ( int i = 0;  i < nblocks;  i++ ) {
				
				Arrays.fill( block, 0.0f );
				System.arraycopy( data, ptr, block, 0, Math.min( blockSize, N-ptr ) );

				F.filter( block, scratch, ptr );
				
				ptr += blockSize;
			}
			
			traceData.get(itrace).setData( scratch );
			
		}
		
		float[] leftOvers = new float[ blockSize ];
		Arrays.fill( block, 0.0f );
		F.filter( block, leftOvers, 0 );
		
		// now need to left shift all traceData by half the filter length
		
		int     nshift = coefficients.length / 2;
		float[] data   = traceData.get(0).getData();
		for ( int itrace = 0;  itrace < ntraces-1;  itrace++ ) {
			int        N         = data.length;
			float[]    scratch   = new float[ N ];
			System.arraycopy( data, nshift, scratch, 0, N-nshift );
			data = traceData.get( itrace + 1 ).getData();
			System.arraycopy( data, 0, scratch, N-nshift, nshift );
			traceData.get( itrace ).setData( scratch );
		}
		int itrace = ntraces - 1;
		data = traceData.get( itrace ).getData();
		int N = data.length;
		float[] scratch = new float[ N ];
		System.arraycopy( data, nshift, scratch, 0, N-nshift );
		System.arraycopy( leftOvers, 0, scratch, N-nshift, nshift );
		traceData.get( itrace ).setData( scratch );
		
	}
	
	
	public static int findBlockSize( int N ) {
		
		int blockSize = 0;
		
		List< Integer > factors = factor(N);
		
		// try to get an exact factor
		
		int i = 0;
		blockSize = factors.get(i);
		while ( blockSize > MAX_BLOCK_SIZE ) {
			i++;
			blockSize = factors.get(i);
		}
		
		// if the factor is ridiculously small, increase the block size
		
		if ( blockSize < MIN_BLOCK_SIZE ) {
			if ( N > 2*MAX_BLOCK_SIZE ) 
				blockSize = MAX_BLOCK_SIZE;
			else 
				blockSize = N/2 + 1;
		}
		
		if ( blockSize == N ) blockSize = N/2 + 1;
		
		return blockSize;
	}
	
	
	public static List< Integer > factor( int N ) {
		
		ArrayList< Integer > retval = new ArrayList<>();
		
		for ( int i = 1;  i * i < N;  i++ ) {
			
			if ( N % i == 0 ) {
				retval.add(i);
				if ( i*i != N )
					retval.add( N/i );
			}
			
		}

		Collections.sort( retval );
		Collections.reverse( retval );
		
		return retval;
	}

}
