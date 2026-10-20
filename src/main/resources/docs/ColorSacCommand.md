## COLOR

**SUMMARY**

    Controls color selection for color graphics devices.
****

**SYNTAX**

    COLOR  {color} options

    where options are one or more of the following:

    {INCREMENT {ON|OFF}}

    {SKELETON color}

    {BACKGROUND color}

    {LIST STANDARD|colorlist}

    and where color is one of the following:

    WHITE|RED|GREEN|YELLOW|BLUE|MAGENTA|CYAN|BLACK|DARK_GRAY|GRAY|LIGHT_GRAY|PINK

    SPECIAL NOTE The LIST option must appear last in this command.
****

**INPUT**

    COLOR color:	Change data color.

    INCREMENT {ON}:	Increment data color from color list after each data file is plotted.

    INCREMENT OFF:	Do not increment data color.

    SKELETON color:	Change color of skeleton to standard color name.

    BACKGROUND color: Change background color to standard color name.

    LIST colorlist:	Change the content of the color list. Enter list of standard color names. Sets data color to first color in list.

    LIST STANDARD:	Change to the standard color list. Sets data color to first color in list.
****

**DEFAULT VALUES**

    COLOR BLACK INCREMENT OFF SKELETON BLACK BACKGROUND WHITE LIST STANDARD
