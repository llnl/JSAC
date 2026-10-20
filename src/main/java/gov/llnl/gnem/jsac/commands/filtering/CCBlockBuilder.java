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

import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.oregondsp.signalProcessing.Sequence;

import gov.llnl.gnem.jsac.SacDataModel;
import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;
import gov.llnl.gnem.jsac.io.SACFile;
import gov.llnl.gnem.jsac.io.SACHeader;

// Builds consecutive, contiguous blocks from random segments

public class CCBlockBuilder {
	
	private static final double DELTA_TOLERANCE = 1.0e-5;
	
	
	class IndexedHeader implements Comparable< IndexedHeader > {
		
		int       index;
		SACHeader header;
		
		IndexedHeader( int index, SACHeader header ) {
			this.index  = index;
			this.header = header;
		}

		@Override
		public int compareTo( IndexedHeader other ) {
			return header.getBeginTimeAsInstant().compareTo( other.header.getBeginTimeAsInstant() );
		}
		
	}
	

	private double                     segmentLength;
	private ArrayList< IndexedHeader > headerList;
	private List< SacTraceData >       originalTraceData;
	private int                        original_ntraces;
	
	private double                     computedDelta;
	private int                        ntotal;

	
	
	public CCBlockBuilder( double segmentLength, double maxDuration ) {
		
		this.segmentLength = segmentLength;
		
		// assemble all trace headers in memory
		
		originalTraceData = SacDataModel.getInstance().getData();
		original_ntraces  = originalTraceData.size();
		if ( original_ntraces < 1 )
			throw new IllegalStateException( "No traces" );
		
		headerList = new ArrayList<>();
		for ( int i = 0;  i < original_ntraces;  i++ ) {
			headerList.add( new IndexedHeader( i, originalTraceData.get(i).getSacFileHeader() ) );
		}
		
		// sort on start times of traces
		
		Collections.sort( headerList );
		
		// check total duration of available data
		
		SACHeader lastHeader = headerList.get( original_ntraces - 1 ).header;
		double endTime   = lastHeader.getBeginTime() + (lastHeader.getNpts() - 1)*lastHeader.getDelta();
		double startTime = headerList.get(0).header.getBeginTime();
		double actualDuration = endTime - startTime;
		
		if ( actualDuration > maxDuration )
			throw new IllegalStateException( "Duration of trace data exceeds maximum duration specified." );
		
		// check consistency of data
		
		boolean consistent = true;
		SACHeader first  = headerList.get(0).header;
		String    key    = first.getKnetwk().trim() + "_" + first.getKstnm() + "_" + first.getKcmpnm();
		double    delta0 = first.getDelta();
		for ( IndexedHeader iheader : headerList ) {
			SACHeader other = iheader.header;
			String otherKey = other.getKnetwk().trim() + "_" + other.getKstnm() + "_" + other.getKcmpnm();
			if ( !key.equals( otherKey ) ) consistent = false;
			double odelta = other.getDelta();
			if ( Math.abs( delta0 - odelta ) / ( (delta0 + odelta)/2 )  > DELTA_TOLERANCE )
				consistent = false;
		}
		if ( !consistent )
			throw new IllegalStateException( "Inconsistent data files" );
		
		// Calculate total number of samples (including gaps) and estimate delta
		
		ntotal = 0;
		int ndelta = 0;
		
		for ( int ib = 0;  ib < original_ntraces;  ib++ ) {
			
			SACHeader header  = headerList.get(ib).header;
			double    delta   = header.getDelta();
			int       npts    = header.getNpts();
			Instant   T0      = header.getBeginTimeAsInstant();
			double    dur     = (npts-1)*delta;
			Instant   T1      = T0.plusMillis( (long) (dur*1000) );
			ndelta            = ntotal;
			ntotal           += npts;
			
			System.out.println( T0 + "    " + T1 + "    " + npts );
			
			if ( ib == original_ntraces-1 )
				break;
			
			// check for gap/overlap
			
			Instant T2    = headerList.get(ib+1).header.getBeginTimeAsInstant();
			double offset = ( T2.toEpochMilli() - T1.toEpochMilli() ) / 1000.0  -  delta;
			int    ngo    = (int) Math.round( offset / delta );
			ntotal       += ngo;
			
			DecimalFormat F = new DecimalFormat( "0.000000" );
			System.out.println( "   " + F.format( offset ) + "    " + ngo  + "  " + ntotal );
		}
		
		System.out.println( "ntotal:   " + ntotal );
		
		double bdif   = lastHeader.getBeginTime() - startTime;
		computedDelta = bdif / ndelta;
		
		System.out.println( "start time difference:  " + bdif + "  computed delta: " + computedDelta + "  estimated # samples: " + ndelta );
	}
	
	
	public void build() {
		
		int nSegmentSamples;
		int nSegments;
		if ( segmentLength < 0.0 ) {
			nSegments = 10;
			nSegmentSamples = ntotal / nSegments;
		}
		else {
			nSegmentSamples = (int) Math.round( segmentLength / computedDelta );
			nSegments       = ntotal / nSegmentSamples;
		}
		if ( nSegments * nSegmentSamples < ntotal ) nSegments++;
		
		float[][] scratch   = new float[ nSegments ][ nSegmentSamples ];
		
		// place trace data into scratch space
		
		double refTime = headerList.get(0).header.getBeginTime();
		
		for ( IndexedHeader iheader : headerList ) {
			
			double startTime = iheader.header.getBeginTime();
			int    offset    = (int) Math.round( ( startTime - refTime ) / computedDelta );
			
			float[] data  = originalTraceData.get( iheader.index ).getData();
			Sequence.rmean( data );
			int     dptr  = 0;
			int     ndpts = data.length;
			int     iseg  = (int) Math.floor( offset / nSegmentSamples );
			int     sptr  = offset - iseg*nSegmentSamples;
			
			while ( dptr < ndpts ) {
				int nToCopy = Math.min( ndpts-dptr, nSegmentSamples-sptr );
				if ( nToCopy == 0 ) { 
					iseg++;
					sptr = 0;
				}
				else {
					System.arraycopy( data, dptr, scratch[iseg], sptr, nToCopy );
					dptr += nToCopy;
					sptr += nToCopy;
				}
			}
			
		}
		
		// Build new SACTraceData list
		
		List< SacTraceData > newTraceList = new ArrayList< SacTraceData >();
		
		// get initial header
		
		SACHeader header0 = headerList.get(0).header;
		for ( int iseg = 0;  iseg < nSegments;  iseg++ ) {
			SACHeader header = new SACHeader( header0 );
			header.setNpts( nSegmentSamples );
			header.setReferenceTime( refTime + iseg*nSegmentSamples*computedDelta );
			header.setB( 0.0 );
			header.setE( (nSegmentSamples-1)*computedDelta );
			SacTraceData std = new SacTraceData( Paths.get( "." ), new SACFile( header, scratch[iseg] ) );
			newTraceList.add( std );
		}
		
		SacDataModel.getInstance().clear();
		SacDataModel.getInstance().addAll( newTraceList, 0.0 );
		
	}

}
