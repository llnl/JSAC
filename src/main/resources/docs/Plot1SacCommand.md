## PLOT1

**SUMMARY**

    Generates a multi-trace multi-window plot.
    
****

**SYNTAX**

	[P]LOT[1] {ABSOLUTE|RELATIVE|EPOCH},{[P]ERPLOT {n|OFF|ON}} {SPACING v} {PRINT options }

****

**INPUT**

	ABSOLUTE:		Plots files treating time as an absolute. Files with different begin times will be shifted relative to each other.
	RELATIVE {M}:	Plots files relative to that file's value for marker (M) where M is one of (B,E,O,A,F,T0 -- T9).
	EPOCH:          Plots files relative to earliest file's begin time as an epoch time.
	PERPLOT n:		Plots n files per frame.
	SPACING v:		Separate subplots by v mm.
	PERPLOT ON:		Plots n files per frame. Use last value for n.
	PERPLOT OFF:	Plots all files on one frame.
     PRINT :			Prints the resulting plot to a printer or to a file.
	 
		FORMAT			When printing to a file, FORMAT controls the output format.
						Allowable formats are PNG,GIF,SVG. Default format is PNG
						
		FILENAME		The name of the output file. If multiple SAC files are in memory
						then the file number is appended to the file name, 
						e.g., filename.1.png, filename.2.png, etc.
						
		PRINTERNAME		If no filename is specified then output is sent to a printer. If no
						printer name is specified, then output is sent to the default printer.
						Printer names must be specified as shown by the LIST_PRINTERS option.
						Names with spaces must be enclosed in quotes.
		{LIST}_PRINTERS	If the LIST sub-option is specified then JSAC will simply list the
						available printers.
						
		DPI				For raster formats (PNG,GIF) This specifies the resolution in dots-per-inch.
						The default is 300 dpi.

**ALTERNATE FORMS**

	PERPLOT ALL has the same meaning as PERPLOT OFF.

****
**DEFAULT VALUES**

	PLOT1 ABSOLUTE PERPLOT OFF SPACING 2

****    
**DESCRIPTION**

	Each data file shares a common axis in the x direction, but each has a separate subplot region 
	in the y direction. The y axis limits for each subplot can be scaled to that data file's extrema 
	or they can have fixed limits. See the YLIM command for details. The x axis limits can also be 
	fixed (see XLIM) or scaled to the data. There are three types of x axis scaling for this type 
	of plot: absolute, relative, and epoch. 
	
	In absolute scaling the x axis limits become the smallest minimum and the largest maximum for 
	the active memory files. Time differences measured between points on different subplots will be correct. 
	
	In relative scaling mode the traces are aligned on a marker. Allowable markers are:
	B, E, O, A, F, and T0 through T9. By default the B marker is used but any of the allowable
    markers may be specified by following the "REL" option with the desired marker name. For the
	chosen marker each file must have a value for that marker. 
	
	Each plotted trace will be shifted as required so that chosen marker is displayed with the value zero. 
	The actual value corresponding to this zero for each file will be given below the name of the file. 
	Axis limits are adjusted to allow the entirety of each shifted trace to be visible. 
	
	This type of scaling is useful if you are cutting the files relative to some time pick, 
	say the first arrival time. It is then easy to see the similarities or differences between 
	the wave forms of each file. A user controllable file identification (see FILEID) 
	is generated for each file in the plot. Time picks can be displayed (see PICKS).	
	
	The epoch scaling mode is equivalent to absolute except that the xaxis units are chosen dynamically
	based on the length of data. Available units are seconds, hours, days, months, years.

****					
**EXAMPLES**

	To list printers only:
		plot1 print list
		
	To send plots to the default printer:
		fg seis
		plot1 print
	
	To send plots to a named printer:
		fg seis
		plot1 print printername "Adobe PDF"
	
	To export the plot to an SVG-format file named example.svg
		fg seis
		plot1 print filename example format svg	