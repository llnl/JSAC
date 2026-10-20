## CCB ##

**SUMMARY**

Restructures the files in memory to consist of uniform-length, consecutive, contiguous blocks ordered by increasing time.  
Requires all files to contain data from the same station and channel.

**** 
**SYNTAX**

CCB [B]LOCK s {[MAX]_DURATION v}
	 
****
**INPUT**

[B]LOCK        s    Specifies the block length in one of several pre-defined, intuitive durations.
                      {[MIN]UTE} one minute (60 second) blocks
                      {QUARTER}  fifteen minute (900 second) blocks
                      {HOUR}     hour (3600 second) blocks
                      {HALFDAY}  12 hour (43200 second) blocks
                      {DAY}      24 hour (86400 second) blocks
[MAX]_DURATION v    Maximum duration of all files in memory in hours
        
****
**DEFAULT VALUES**

 CCB MAX_DURATION 240.0

****
**DESCRIPTION**

This command was added to prototype data processing in consecutive, contiguous blocks, as with the FIR command.  It is 
useful to construct helicorder-like records of data from a single station.  The start times of the second and later traces
are equal to the end time of the previous trace plus delta, the sampling increment.
 
****
**EXAMPLES**

SAC>r file1 file2 file3 file4 file5
SAC>ccb b hour
SAC>p1

****
**HEADER CHANGES**
 
Since the traces in SAC are replaced by entirely new traces of uniform length, new headers are created.  The start time
or reference time of the first new trace is equal to that of the original earliest trace, then the start times of
subsequent traces increase progressively by the requested block duration.  The following header values are changed:
B, E, NPTS, NZYEAR, NZJDAY, NZHOUR, NZMIN, NZSEC, NZMSEC, DEPMEN, DEPMAX, DEPMIN