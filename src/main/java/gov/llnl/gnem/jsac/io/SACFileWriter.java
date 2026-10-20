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
package gov.llnl.gnem.jsac.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import gov.llnl.gnem.jsac.io.enums.FileType;

public class SACFileWriter {
	
	private Path        path;
	private SACHeaderIO headerIO;
	private SACHeader   header;
	private FileChannel fileChannel;
	
	
	public SACFileWriter( Path path ) throws IOException {
		
		this.path   = path;
		fileChannel = FileChannel.open( path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING );
		header      = new SACHeader();
		headerIO    = new SACHeaderIO( fileChannel );
		headerIO.write( header );
		
	}
	
	
	
	public static SACFileWriter  fromStringPath( String filePath ) throws IOException {
		return new SACFileWriter( Paths.get( filePath ) );
	}
	
	
	
	public SACHeader getHeader() {
		return header;
	}
	
	
	
	public void setHeader( SACHeader header ) {
		this.header = header;
	}
	
	
	
	public int write( float[] waveform ) throws IOException {
		return write( waveform, FileType.ITIME );
	}
	
	
	
	public int write( float[] x, FileType fileType ) throws IOException {
		
		switch ( fileType ) {
		
		case ITIME:  case IGRP:  case IREAL:
			
			ByteBuffer buffer = ByteBuffer.allocate( 4*x.length );
			buffer.order( headerIO.getByteOrder() );
			FloatBuffer fbuffer = buffer.asFloatBuffer();
			fbuffer.put( x );
			fbuffer.flip();
			fileChannel.write( buffer );
			header.setIftype( fileType );
			break;
			
		case IRLIM:  case IAMPH:  case IXY:
			
			System.out.println( "Need two float[] arguments" );
			break;
			
		case IXYZ:
			
			System.out.print( "Need three float[] arguments" );
			break;
			
		}
		
		
		if ( fileType == FileType.ITIME ||  fileType == FileType.IREAL ) {
			header.setNpts( header.getNpts() + x.length );
		}
		else if ( fileType == FileType.IGRP ) {
			header.setNpts( x.length );
		}
		
		return x.length;
	}
	
	
	
	public int write( float[] x, float[] y, FileType fileType ) throws IOException {
		
		if ( x.length != y.length ) {
			System.out.println( "The two float[] arguments must have the same length" );
		}
		else {
		
			switch ( fileType ) {
		
			case IRLIM:  case IAMPH:  case IXY:
			
				ByteBuffer buffer = ByteBuffer.allocate( 2 * 4*x.length );
				FloatBuffer fbuffer = buffer.asFloatBuffer();
				fbuffer.put( x );
				fbuffer.put( y );
				fbuffer.flip();
				fileChannel.write( buffer );
				header.setIftype( fileType );
				header.setNpts( x.length );
				break;

			case ITIME:  case IGRP:  case IREAL:

				System.out.println( "Needs one float[] argument" );
				break;
			
			case IXYZ:
			
				System.out.print( "Need three float[] arguments" );
				break;
			
			}
			
		}
		
		return x.length;
	}
	
	
	
	public void close() throws IOException {
		
		if ( fileChannel.isOpen() ) {
			headerIO.write( header );
			fileChannel.close();
		}
		
		if ( header.getNvhdr() == 7 ) {
			fileChannel = FileChannel.open( path, StandardOpenOption.WRITE, StandardOpenOption.APPEND );
			headerIO    = new SACHeaderIO( fileChannel );
			headerIO.writeFooter( header );
			fileChannel.close();
		}

	}

}
