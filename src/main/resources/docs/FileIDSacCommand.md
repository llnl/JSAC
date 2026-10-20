## FILEID

**SUMMARY**

    Controls the file id display found on most SAC plots.
****

**SYNTAX**

    FILEID {ON|OFF} {TYPE DEFAULT|NAME|LIST hdrlist}

    LOCATION {UR|UL|LR|LL},

    FIELD_LAYOUT(FL),

    {FORMAT EQUALS|COLONS|NONAMES}

    DELIMITER value
****

**INPUT**

    FILEID {ON}:	Turn on file id option. Does not change file id type or location.

    FILEID OFF:		Turn off file id option.

    TYPE DEFAULT:	Change to the default file id.

    TYPE NAME:	Use the name of the file as the file id.

    TYPE LIST hdrlist: Define a list of header fields to display in the fileid. In addition to header fields the list may contain one or more arbitrary strings.
    Multi-word strings must be enclosed by either (') or (\") characters.

    LOCATION UR:				Place file id in upper right hand corner.

    LOCATION UL:				Place file id in upper left hand corner.

    LOCATION LR:				Place file id in lower right hand corner.

    LOCATION LL:				Place file id in lower left hand corner.

    FIELD_LAYOUT VERTICAL:      Arrange fields vertically. Alternates are V or VERT

    FIELD_LAYOUT HORIZONTAL:	Arrange fields horizontally. Alternates are H or HORIZ

    FORMAT EQUALS:              Format consists of header field name, an equals sign, and the header field value.

    FORMAT COLON:				Format consists of header field name, a colon, and the value.

    FORMAT NONAMES:				Format consists of header field value only.

    SIZE size:                  Change the plot label size.

    TINY:	Tiny text size has 132 characters per line.

    SMALL:	Small text size has 100 characters per line.

    MEDIUM:	Medium text size has 80 characters per line.

    LARGE:	Large text size has 50 characters per line.

    HUGE:	Large text size has 30 characters per line.

    COLOR   			    AUTO|WHITE|RED|GREEN|YELLOW|BLUE|MAGENTA|CYAN|BLACK|DARK_GRAY|GRAY|LIGHT_GRAY|PINK

    ADJUSTMENT                      Adjust the spacing in points between lines of text (+-)

    FONT                            Set the font for the displayed text {Default is Arial} (Use the FONT command to list available fonts)

    DELIMITER                    The separator to use between tokens for horizontal layouts. The default is \",\" but any combination of characters may be used. Delimiters with whitespace must be enclosed in quotes. To specify an empty string use the keyword none (e.g. DELIMITER none)

    Note: If multiple arguments are enclosed in quotes use the same quote style (e.g. single-quote or double-quote).

    Note on COLOR: By default text color matches the line color of the seismogram. The color option allows forcing the color to the specified value. To revert to the default behavior set COLOR to AUTO.
 ****
 
 **DEFAULT VALUES**

    FILEID ON TYPE DEFAULT LOCATION UR FIELD_LAYOUT VERTICAL FORMAT NONAMES FONT Arial SIZE 12 ADJUSTMENT 0 DELIMITER COLOR AUTO