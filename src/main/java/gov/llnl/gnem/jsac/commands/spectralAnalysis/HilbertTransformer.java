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
package gov.llnl.gnem.jsac.commands.spectralAnalysis;

import com.oregondsp.signalProcessing.filter.fir.OverlapAdd;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;


public class HilbertTransformer {
	
	private static final int       BLOCK_SIZE = 1500;
	private static final int       ORDER      = 100;
	
	private HilbertTransformerType type;
	private OverlapAdd             F          = null;
	
	private static final float[] impulseResponse = { -0.0f,          -8.401275E-5f, -2.7939677E-9f,  -6.6146255E-5f,  2.7939677E-9f,
										       -9.168312E-5f,   6.519258E-9f, -1.2335554E-4f,  -6.519258E-9f,  -1.6206503E-4f,
										        9.313226E-10f, -2.0878017E-4f,  6.519258E-9f,  -2.6477687E-4f, -1.8626451E-8f,
										       -3.3117086E-4f,  1.4901161E-8f, -4.092753E-4f,  -3.7252903E-9f, -5.0049834E-4f,
										        0.0f, 		    -6.0632266E-4f,  5.5879354E-9f, -7.2847866E-4f, -5.5879354E-9f,
										       -8.6851977E-4f,  0.0f, 		    -0.0010284148f,  0.0f,			-0.0012100171f,
										        9.313226E-10f, -0.0014155377f, -1.3969839E-8f, -0.0016470179f,  4.1909516E-9f,
										       -0.0019070823f,  8.8475645E-9f, -0.0021979827f, -2.0954758E-8f, -0.0025227591f,
										        4.1909516E-9f, -0.0028841365f,  1.5832484E-8f, -0.0032854807f,  9.313226E-10f,
										       -0.0037302338f,  0.0f, 		    -0.0042222794f,  1.4901161E-8f, -0.004765872f,
										       -5.5879354E-9f, -0.0053658895f, -9.313226E-9f,  -0.0060277097f,  0.0f,
										       -0.006757617f,   3.7252903E-9f, -0.0075628087f,  7.4505806E-9f, -0.00845173f,
										       -3.7252903E-9f, -0.009434432f,   6.519258E-9f,  -0.010522962f,  -1.3969839E-8f,
										       -0.011732012f,   9.313226E-10f, -0.013079643f,  -9.313226E-10f, -0.014588878f,
									            2.7939677E-9f, -0.0162884f,    -2.7939677E-9f, -0.018216059f,   1.9557774E-8f,
									           -0.02042143f,   -1.3969839E-8f, -0.02297178f,    1.1175871E-8f, -0.025959745f,
									           -1.3038516E-8f, -0.029517666f,   1.8626451E-9f, -0.03384024f,   -5.5879354E-9f,
									           -0.03922671f,    0.0f, 		    -0.046160966f,  -3.7252903E-9f, -0.055479556f,
									            9.313226E-9f,  -0.06876403f,   -3.7252903E-9f, -0.08940482f,   -4.656613E-9f,
									           -0.12621921f,   -4.656613E-9f,  -0.21154219f,    1.6298145E-8f, -0.6363981f,
									           -8.8475645E-9f,  0.6363981f,    -8.8475645E-9f,  0.21154219f,    1.6298145E-8f,
									            0.12621921f,   -4.656613E-9f,   0.08940482f,	-4.656613E-9f,   0.06876403f,
									           -3.7252903E-9f,  0.055479556f,   9.313226E-9f,   0.046160966f,  -3.7252903E-9f,
									            0.03922671f, 	 0.0f, 		     0.03384024f,   -5.5879354E-9f,  0.029517666f,
									            1.8626451E-9f,  0.025959745f,  -1.3038516E-8f,  0.02297178f,    1.1175871E-8f,
									            0.02042143f,   -1.3969839E-8f,  0.018216059f,   1.9557774E-8f,  0.0162884f,
									           -2.7939677E-9f,  0.014588878f,   2.7939677E-9f,  0.013079643f, 	-9.313226E-10f,
									            0.011732012f,   9.313226E-10f,  0.010522962f,  -1.3969839E-8f,  0.009434432f,
									            6.519258E-9f,   0.00845173f,   -3.7252903E-9f,  0.0075628087f,  7.4505806E-9f,
									            0.006757617f,   3.7252903E-9f,  0.0060277097f,  0.0f,           0.0053658895f,
									           -9.313226E-9f,   0.004765872f,  -5.5879354E-9f,  0.0042222794f,  1.4901161E-8f,
									            0.0037302338f,  0.0f,           0.0032854807f,  9.313226E-10f,  0.0028841365f,
									            1.5832484E-8f,  0.0025227591f,  4.1909516E-9f,  0.0021979827f, -2.0954758E-8f,
									            0.0019070823f,  8.8475645E-9f,  0.0016470179f,  4.1909516E-9f,  0.0014155377f,
									           -1.3969839E-8f,  0.0012100171f,  9.313226E-10f,  0.0010284148f,  0.0f,
									            8.6851977E-4f,  0.0f,           7.2847866E-4f, -5.5879354E-9f,  6.0632266E-4f,
									            5.5879354E-9f,  5.0049834E-4f,  0.0f,           4.092753E-4f,  -3.7252903E-9f,
									            3.3117086E-4f,  1.4901161E-8f,  2.6477687E-4f, -1.8626451E-8f,  2.0878017E-4f,
									            6.519258E-9f,   1.6206503E-4f,  9.313226E-10f,  1.2335554E-4f, -6.519258E-9f,
									            9.168312E-5f,   6.519258E-9f,   6.6146255E-5f,  2.7939677E-9f,  8.401275E-5f,
									           -2.7939677E-9f };
	
	
	public HilbertTransformer( HilbertTransformerType type ) {
		
		this.type = type;
		
		if ( type == HilbertTransformerType.FIR ) {
			F = new OverlapAdd( impulseResponse, BLOCK_SIZE );
		}
	}
	
	
	public void transform( SacTraceData std ) {
		
		switch ( type ) {
		case SPECTRAL:
			transform_sp( std );
			break;
		case FIR:
			transform_fir( std );
			break;
		default:
			transform_sp( std );
			break;
		}
		
	}
	
	
	public void transform_sp( SacTraceData std ) {
		std.applyHilbert();
	}
	
	
	public void transform_fir( SacTraceData std ) {
		std.setData( transform_fir( std.getData() ) );
		std.resetStatistics();
	}
	
	
	public float[] transform_fir( float[] x ) {
		
		int N = x.length;
		int M = N + 2*ORDER;
		
		int nblocks = 1;
		while ( nblocks * BLOCK_SIZE < M ) {
			nblocks++;
		}
		
		float[] tmp = new float[ nblocks*BLOCK_SIZE ];
		System.arraycopy( x,  0,  tmp,  0, N );

		int ptr = 0;
		
		for ( int i = 0;  i < nblocks;  i++ ) {
			F.filter( tmp, ptr, tmp, ptr );
			ptr += BLOCK_SIZE;
		}
		
		System.arraycopy( tmp, ORDER, x, 0, N );
		
		return x;
	}

}
