## MERGE

**SUMMARY**

    Merges (concatenates) a set of files to data in memory.
****
**SYNTAX**

    MERGE  {GAP ZERO|INTERP}
    {MAX_SAMPLE_INCONSISTENCY v} {filelist}
****
**INPUT**

    GAP ZERO | INTERP:

    ZERO - Fill gap samples with 0.0 amplitude [default]

    INTERP - Use linear prediction filter to fill gap samples

    MAX_SAMPLE_INCONSISTENCY For input files, predicted NPTS error based on (B,E,DELTA) must be less than this (default = 0.5)

    FILELIST: A list of SAC binary data files. This list must contain simple filenames  (full or relative pathnames)
****
**DESCRIPTION**

    The data in the files in this merge list is appended or concatenated to the data in memory. Each pair of files to be merged is checked to make sure they have the same sampling interval and StreamKey. 

    Any number of file, in any order are able to be merged. Data currently in memory and data identified in the merge command are merged together. If no data is specified with the merge command, data currently in memory will be merged.