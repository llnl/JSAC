## WRITE

**SUMMARY**

    Writes data in memory to disk, in SAC binary format or alphanumeric format.
****
**SYNTAX**

    WRITE [options] [namingoptions]

    where options is one or more of the following:

    DIR OFF|CURRENT|name

    KSTCMP

    These options MUST precede any element in the naming options:

    OVER

    ALPHA

    filelist
****
**INPUT**

    DIR OFF:             Turn directory option off. When off, writes to current directory.

    DIR CURRENT:         Turn directory option on and set name of write directory to the \"current directory\" (e.g. the directory from which you started SAC.)

    DIR name:            Turn directory option on and set name of write directory to name. Write all filenames to the directory called name. This may be a relative or absolute directory name.

    KSTCMP:              Use the KSTNM and KCMPNM header variables to define a file name for each data file in memory. The names generated will be checked for uniqueness, and will have sequencing digits added as necessary to avoid name clashes.

    OVER:                Use current read filelist as write filelist. Overwrite files on disk with data in memory.

    ALPHA                Write files in alphanumeric format.

    APPEND text          Write filelist is created by appending text to each name in the current read filelist.

    PREPEND text         Write filelist is created by prepending text to each name in the current read filelist.

    DELETE text          Write filelist is created by deleting the first occurrence of text in each name in the current read filelist.

    CHANGE text1 text2   Write filelist is created by changing the first occurrence of text1 in each name in the current read filelist to text2.

    filelist             Write filelist is set to filelist. This list may contain simple filenames, relative pathnames, or full pathnames. IT MAY NOT CONTAIN WILDCARDS.

