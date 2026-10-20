## SUBF

**SUMMARY**

    Subtracts a set of data files to data in memory.
****
**SYNTAX**

    SUBF {NEWHDR ON|OFF} filelist
****
**INPUT**

    NEWHDR ON|OFF:	By default, the resultant file will take its header field from the original file in memory. Turning NEWHDR ON, causes the header fields to be taken from the new file in the filelist.

    filelist:	A list of SAC binary data files. This list may contain simple filenames, full or relative pathnames.
****
**DESCRIPTION**

    This command can be used to subtract a single file from a set of files or to subtract one set of files from another set. An example of each case is presented below. The files must be evenly spaced and must have the same sampling interval and number of data points. If there are more data files in memory than in the filelist, then the last file in the filelist is used for the remainder of the data files in memory.
****
**EXAMPLES**

    To subtract one file from three other files:

    SAC> READ FILE1 FILE2 FILE3

    SAC> SUBF FILE4


    To subtract two files from two other files:

    SAC> READ FILE1 FILE2

    SAC> SUBF FILE3 FILE4
****
**HEADER CHANGES**

    If NEWHDR is OFF (the default) the headers in memory are unchanged.

    If NEWHDR is ON, the headers are replaced with the headers from the files in the filelist.

    DEPMIN, DEPMAX, DEPMEN