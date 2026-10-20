## CHNHDR

**SUMMARY**

    Changes the values of selected header fields.
****

**SYNTAX**

    CHNHDR  { file n1 n2 ... } field v  {field v ... }
****
**INPUT**

    file  :  This is an optional keyword that can be followed by a list of numbers indicating which file's headers are to be changed.

    n1 n2 ...:  Integers indicating which file's headers to change.

    field  :  The name of a SAC header variable.  

    These variables are listed in an appendix to this manual.  Also, field may be the keyword ALLT as discussed below.  

    Note:  in order to maintain internal consistency, the following header variables cannot be changed with CHNHDR: NVHDR, NPTS.


    v  :  Set the value of that field to v.  

    The type of the field and its new value must match.  Use single quotes for alphanumeric fields with embedded blanks.  Use TRUE or FALSE for logical fields.  YES or NO are also acceptable for logical fields.  Use variable names (see appendix) for value fields.  For offset time fields (B, E, O, A, F, and Tn), v may also be of the form: GMT v1 v2 v3 v4 v5 v6 where v1,v2, v3, v4, v5, and v6 are the GMT year, julian day, hour, minute, second, and millisecond of the time.  

    If v1 is a two digit number, SAC will assume it is in the current century, unless that would mean that the year is in the future yet, in which case, SAC assumes the previous century.  To be certain you get what you want, use four digits.

    UNDEF  :  Use this keyword instead of v to "undefine" a header field.

    ALLT v  :  Add v seconds to all defined header times.  Subtract v seconds from the zero time.
****

**DESCRIPTION**

    This command lets you change any of SAC's header fields. A specific file or list of files can be changed by specifying the integer value(s) corresponding to the order in which the file(s) were read in.  If no integer filelist is specified, all files in memory will have their header fields changed.  To change the headers of the files on disk follow this command with the WRITE or WRITEHDR command.

    SAC does some validity checking on the new values but you may want to verify the results using the LISTHDR command. There is a set of six variables in the header (NZYEAR, NZJDAY, NZHOUR, NZMIN, NZSEC, and NZMSEC) which contain the reference or "zero" time of the file.  This is the only GMT in the SAC header.  All other times in the header (B, E, O, A, F, and Tn) are offsets in seconds relative to this reference time. 

    You may change the reference time and all of the defined offset times by using the "ALLT v" option.  That number of seconds are added to each defined offset time.  That same number of seconds is also subtracted from the
    reference time.  This preserves the actual GMT time of the data.  As a convenience, you may enter a GMT time instead of a relative time when
    changing the offset times.  When the GMT time is entered it is converted to a relative time before storing it in the offset time field.
****

**EXAMPLES**

    To define the event latitude, longitude and name in all the files in memory:

    u:  CHNHDR EVLA 34.3 EVLO -118.5

    u:  CHNHDR KEVNM 'LA goes under'

    To define the event latitude, longitude and name in files 2 and 4:

    u:  CHNHDR file 2 4 EVLA 34.3 EVLO -118.5

    u:  CHNHDR file 2 4 KEVNM 'LA goes under'

    To change the event type to earthquake:

    u:  CHNHDR IEVTYP IQUAKE

    To set the first arrival time to its undefined state:

    u:  CHNHDR A UNDEF

    Assume you know the GMT origin time of an event and that you want to quickly
    change all the times in the header so that this origin time is the zero or reference time and all other offset times are correct relative to this time.
    First set the origin time using the GMT option:

    u:  CHNHDR O GMT 1982 123 13 37 10 103

    If no reference time was set prior to executing this command then this command will set the reference time to MAY 03 (123), 1982 13:37:10.103. Any other offset times are left unchanged.

    If the reference time was set prior to executing the command then the existing reference time is changed and all offset times except for O are changed by the difference of the supplied GMT time and the existing reference time. 

    In either case, O is set to zero
****
**HEADER CHANGES**

    Potentially all header fields.
