## FIR ##

**SUMMARY**

Applies a previously-designed FIR filter to the data in memory.  Two options exist:  a default 
option and a consecutive contiguous block option.

**** 
**SYNTAX**

FIR filepath {CCB}

****
**INPUT**

filepath:    The name of/path to the file holding the FIR filter coefficients.
CCB          Apply the filter treating the data files in memory as consecutive contiguous blocks.
        
****
**DEFAULT VALUES**

 FIR FIR.txt

****
**DESCRIPTION**

The filter applied by this command typically will have been designed using the DFIR command, which 
outputs filter coefficients in an ASCII file (default name FIR.txt).  Alternatively the user can 
create the filter coefficient file with other software or an editor.  The format is simple:  one
coefficient per line.  The filter is applied using the overlap-add method (see Oppenheim and 
Schafer (1999), Prentice Hall).  Any length file can be processed with this command.

If the CCB options is invoked, the filter will treat the (usually multiple) data files in memory
as consecutive contiguous blocks of a data stream.  The same filter is used for each file, but the
filter's internal state information is preserved from one file to the next.  This option enables 
processing of a very long sequence, split into blocks, as a continuous stream and eliminates
transient artifacts at the beginning and end of each block.

****
**EXAMPLES**

This example filters the signal (with sampling interval 0.025 s) in file1 with a centered 
differentiator with 61 coefficients and designed by DFIR to be accurate to 0.8 times the
folding frequency (20*0.8 = 16 Hz).  

SAC> dfir difc 30 0.025 0.8 w differentiator.txt
SAC> r file1
SAC> fir differentiator.txt

This example filters data continuously in consecutive contiguous blocks as if the blocks were
concatenated into a continuous stream:

SAC>r file1 file2 file3 file4 file5
SAC>ccb b hour
SAC>fir ccb differentiator.txt
 
****
**HEADER CHANGES**
 
DEPMIN, DEPMAX, DEPMEN