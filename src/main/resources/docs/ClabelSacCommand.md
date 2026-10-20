## CLABEL

**SUMMARY**

    Defines general plot labels and their attributes.
****

**SYNTAX**

    CLABEL {CLEAR}, text,{SIZE size}, POSITION x y , {HORIZ_ALIGN value,{VERT_ALIGN value}, {FONT value}, {COLOR value}

    where size is one of the following:

    TINY|SMALL|MEDIUM|LARGEINPUT

    text:		The text of plot label.

    SIZE size:	Set the plot label size.

    TINY:		Tiny text size has 132 characters per line.

    SMALL:		Small text size has 100 characters per line.

    MEDIUM:		Medium text size has 80 characters per line.

    LARGE:		Large text size has 50 characters per line.

    HUGE:		Huge text size has 33 characters per line.

    POSITION 	x y Define a specific position for this label.

    The text is plotted at the point in the plot coordinate system given by (x,y).

    HORIZ_ALIGN LEFT|CENTER|RIGHT

    VERT_ALIGN	TOP|CENTER|BOTTOM

    FONT	The font to use for the label text. Multi-word font names must be enclosed with single- or double quotes.

    COLOR		The color of the label text. See the COLOR command for the list of available colors.

    CLEAR		removes all labels created using CLABEL
****

**DEFAULT VALUES**

    Default size is small.

    Default HORIZ_ALIGN is LEFT

    Default VERT_ALIGN is BOTTOM

    Default font is Arial

    Default color is BLACK
****

**DESCRIPTION**

    This command lets you define general purpose plot labels for subsequent plot commands. You can define the location and size of each label. You can also generate a title and axes labels using the TITLE, XLABEL, and YLABEL commands.
****

 **EXAMPLES**

    The following command will generate a label at the position (14.37, 0.345):

    u:  CLABEL 'Local maximum' POSITION 14.37 0.345

