## ARRAYMAP

**SUMMARY**

    Produces a map of the array or "coarray" using all files in SAC memory.
****
**SYNTAX**

    ARRAYMAP {ARRAY | COARRAY} {TITLE value}
****

**INPUT**

    ARRAY:		This option maps the offsets EAST and NORTH of the array elements. Units are km.
    COARRAY:		This option plots delta EAST and delta NORTH for all pairs of stations.
    TITLE value:	This option allows setting the plot title to a string of the user's choice.

**DEFAULT VALUES**

    ARRAYMAP ARRAY
 
**DESCRIPTION**

        The offsets plotted by this command are determined in one of two ways. In the absense of an array
	definition the software computes the centroid of the array elements using the STLA,STLO,STEL values
	in the SAC headers. Then offsets are computed using a sperical Earth model. If an array definition
	has previously been loaded using the DEFINEARRAYS command then the software takes offset values
	from the array definition. 
	When the ARRAY option is specified each element is plotted as a filled circle with the color 
	corresponding to the elevation relative to the centroid elevation. Each element is labeled with
	the station code. The array centroid is plotted as a cross with the centroid lat,lon labeled below.
	In COARRAY mode the differences are simply plotted as filled circles with no annotations.

**EXAMPLES**

        To plot the array corresponding to a set of stations using the computed centroid and with a user-
	specified title:
		r  AU* 
		arraymap title "my Plot Title"
	To plot the coarray corresponding to a set of stations using an array definition and with the title
		taken from the array definition.
		da filename array_definitions.txt
		r  AU* 
		arraymap coarray	
 
**LIMITATIONS**

	Currently each station must have only a single channel.		
		
**HEADER CHANGES**

    USER7, USER8, USER9 contain respectively the east, north, and vertical offsets in km.      