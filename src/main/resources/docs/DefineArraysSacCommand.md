## DEFINEARRAYS

**SUMMARY**
    Reads values from an array definition file for use by the array processing commands.
****
**SYNTAX**
    [D]EFINE[A]RRAY {FILENAME value} | CLEAR
****
**INPUT**
    FILENAME value The name of the file containing definitions for one or maore arrays.
    CLEAR no sub-arguments, clears all array definitions.
****
**DEFAULT VALUES**
    None
****
**DESCRIPTION**
   In the absence of an array definition, JSAC computes the array reference point as the 
   centroid of the lat, lon, and elevation found in the SAC files. Offsets of
   individual elements are calculated relative to the centroid. However, it is
   common practice to designate one of the array elements as the reference point
   and to compute offsets relative to that point. If an array definition file
   is supplied using this command JSAC will use the reference supplied in the file
   and will also use the offsets specified in the file.

   The CLEAR and FILENAME arguments are mutually-exclusive.
   

   The array definition file must contain the following nine columns for each array element
   epoch. No header is allowed.
   ARRAY_NAME   ELEMENT_NAME    BEGIN_TIME  END_TIME	DNORTH	DEAST	LAT	LON	ELEVATION

****
**EXAMPLES**

    Assuming an array definition file named "array_definitions.txt":
    u:  defineArray filename array_definitions.txt