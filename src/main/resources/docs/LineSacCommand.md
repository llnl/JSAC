## LINE


**SUMMARY**

    Controls the linestyle selection in plots.
****

**SYNTAX**

    LINE {ON|OFF|SOLID|DOTTED|n} {INCREMENT {ON|OFF}}, {LIST STANDARD|nlist}
****

**INPUT**

    {ON}:			Turn line-drawing on. Don't change linestyle.

    OFF:			Turn line-drawing off.

    SOLID:			Change to solid linestyle and turn line-drawing on.

    DOTTED:			Change to dotted linestyle and turn line-drawing on.

    Change to linestyle n and turn line-drawing on.

    INCREMENT {ON}:	For multiple data files in a plot, increment linestyle from linestyle list for each data file in the plot.

    INCREMENT OFF:	Do not increment linestyle for multiple data files.

    LIST STANDARD:	Change to the standard linestyle list (1 2 3 ..).

    LIST nlist:		Change the content of the linestyle list. Enter list of linestyle numbers (e.g., 3 1 2 ..).
****

**DEFAULT VALUES**

    LINE SOLID INCREMENT OFF LIST STANDARD