## LH

**SUMMARY**

    Lists the values of selected header fields.
****

**SYNTAX**

    LISTHDR {listops} {hdrlist}

    where listops are one or more of the following:

    DEFAULT|PICKS|SPECIAL

    FILES ALL|NONE|list

    INCLUSIVE ON|OFF
****

**INPUT**

    DEFAULT:	Use the default list, which includes all defined header fields.

    PICKS:		Use the picks list, which includes those header fields used to define time picks.

    SPECIAL:	Use the special user defined list.

    FILES ALL:	List headers from all files in data file list.

    FILES NONE:	Don't list headers, set defaults for future commands.

    FILES list:	List headers from a subset of the files in the data file list. The subset is defined as a list of file numbers.

    INCLUSIVE:	ON includes header variables which are undefined. OFF excludes them.

    hdrlist:	List of header fields to be included in the special list.
****

**DEFAULT VALUES**

    LISTHDR DEFAULT FILES ALL INCLUSIVE OFF