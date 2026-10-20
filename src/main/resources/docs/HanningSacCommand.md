## HANNING

**SUMMARY**
                
    Applies a "Hanning" window to each data file.
****
**SYNTAX**

    HANNING
                
****
**DESCRIPTION**
	
    The "Hanning" window is a recursive smoothing algorithm defined at each interior data point, j, as: 

    Y(j) = 0.5 * [1 - cos(2πj/N)]
