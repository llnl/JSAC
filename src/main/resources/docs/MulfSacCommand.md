## MULF 

**SUMMARY**

    Multiplies a set of data files by the data in memory.
****
**SYNTAX**
    MULF {NEWHDR ON|OFF} filelist
****
**INPUT**
    NEWHDR ON|OFF:	By default, the resultant file will take its header field from the original file in memory.

    Turning NEWHDR ON, causes the header fields to be taken from the new file in the filelist.

    filelist:	A list of SAC binary data files. This list may contain simple filenames, full or relative pathnames.
****
**DESCRIPTION**

    This command can be used to multiply a single file by a set of files or to multiply one set of files by another set.An example of each case is presented below. The files must be evenly spaced and must have the same sampling interval and number of data points. If there are more data files in memory than in the filelist, then the last file in the filelist is used for the remainder of the data files in memory.
****
**EXAMPLES**

    To multiply three files by a single file :

    SAC> READ FILE1 FILE2 FILE3

    SAC> MULF FILE4

    To multiply two files by two other files:

    SAC> READ FILE1 FILE2
    SAC> MULF FILE3 FILE4
****
**HEADER CHANGES**

    If NEWHDR is OFF (the default) the headers in memory are unchanged.

    If NEWHDR is ON, the headers are replaced with the headers from the files in the filelist.

    DEPMIN, DEPMAX, DEPMEN
 