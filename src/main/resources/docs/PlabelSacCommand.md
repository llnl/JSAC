## PLABEL


**SUMMARY**

    Defines general plot labels and their attributes.
****

**SYNTAX**

    PLABEL {n} {CLEAR} {ON|OFF|text},{SIZE size},{POSITION x y}

    where size is one of the following:

    TINY|SMALL|MEDIUM|LARGE|HUGE
****

**INPUT**

    number: Plot label number. If omitted, the previous label number is incremented by one.

    ON:	Turn this plot label on.

    OFF:	Turn this plot label off.

    text:	Change text of plot label. Also turns plot label on.

    SIZE size:	Change the plot label size.

    TINY:	Tiny text size has 132 characters per line.

    SMALL:	Small text size has 100 characters per line.

    MEDIUM:	Medium text size has 80 characters per line.

    LARGE:	Large text size has 50 characters per line.

    HUGE:	Large text size has 30 characters per line.

    POSITION x y:	Define a specific position for this label. The range of the positions are: 0. to 1. for x and 0. to 1. for y.

    FONT font:		The font to use for the label text. Multi-word font names must be enclosed with single- or double-quotes.

    COLOR color:	The color of the label text. See the COLOR command for the list of available colors.

    CLEAR {index}	If n is specified removes the nth label. If index is specified removes the index label. Otherwise, removes all labels created using PLABEL.
****

**DEFAULT VALUES**

    Default size is small.

    Default position for label 1 is 0.05 0.05

    Default position for other labels is below previous label.

    Default font is Arial

    Default color is BLACK
****

**DESCRIPTION**

    This command lets you define general purpose plot labels for subsequent plot commands. You can define the location and size of each label. You can also generate a title and axes labels using the TITLE, XLABEL, and YLABEL commands.
    You can also generate labels tied to coordinates using the CLABEL command.
****

**EXAMPLES**

    The following commands would generate a four line label in the upper left hand corner of subsequent plots:

    u:  PLABEL 'Sample seismogram' POSITION .05 .05 size large

    u:  PLABEL 'from earthquake

    u:  PLABEL 'on January 24, 1980'

    u:  PLABEL 'in Livermore Valley, CA'

    An additional tiny label could be placed in the lower left hand corner:

    u:  PLABEL 'LLNL station: CDV' S T P .05 .95

