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
package gov.llnl.gnem.jsac.io.enums;

public enum Body {
	
	ISUN(      98, "Sun",     696000000.0, 8.189e-6 ),
	IMERCURY(  99, "Mercury",   2439700.0, 0.0 ),
	IVENUS(   100, "Venus",     6051800.0, 0.0 ),
	IEARTH(   101, "Earth",     6378137.0, 0.0033528106647474805 ),
	IMOON(    102, "Moon",      1737400.0, 0.0 ),
	IMARS(    103, "Mars",      3396190.0, 0.005886007555525457 ),
	UNDEF( -12345, "SAC Historical Spheroid", 6378160.0, 0.00335293 );
	
    private final int    code;
    private final String description;
    private final double a;
    private final double f;

    private Body( int code, String description, double a, double f ) {
        this.code = code;
        this.description = description;
        this.a = a;
        this.f = f;
    }
	

    public static Body getBodyFromCode( int code ) {
        for ( Body body : Body.values()) {
            if ( body.code == code ) {
                return body;
            }
        }
        return null;
    }
    

    public int getCode() {
        return code;
    }
    

    public String getDescription() {
        return description;
    }
    
    
    public double getA() {
    	return a;
    }
    
    
    public double getF() {
    	return f;
    }
    
}
