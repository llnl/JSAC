## MAP

**SUMMARY**

    Generate an HTML map which can include station/event symbols, topography,
    and station names using all the files in SAC memory and an optional event file specified on the
    command line. Event symbols can be scaled according to magnitude, residual, etc. 
    The result of this command is an html file and a screen display of that file.

****        
**SYNTAX**

    MAP 
    {WEST minlon} {EAST maxlon} {NORTH maxlat} {SOUTH minlat}
    {MAGnitude | REsidual | RMean_residual}
    {EVentfile filename} {STANames}
    {MAPSCALE on|off} {PLOTSTATIONS on|off} {PLOTEVENTS on|off} {PLOTCOUNTRIES on|off}
    {PLOTLEGEND on|off} {LEGENDXY x y} {FILE output-file}
    Note Shorthand notations for keywords are in capital letters.

    INPUT OPTIONS
    The following options allow the user to specify the map region. The default is to use the min and
    max defined by the plotted stations and events.
    WEST: Define minimum longitude for map window.
    EAST: Define maximum longitude for map window.
    NORTH: Define maximum latitude for map window.
    SOUTH: Define minimum latitude for map window.

    The following options allow the user to add locations and annotations to the map.
    STANames: on | off [ Off ]
    MAPSCALE: on | off [ On ] Plot a Distance Scale on the Map
    PLOTSTATIONS: on | off [ On ] Plot all the Stations from seismograms
    PLOTEVENTS: on | off [ On ] Plot all the Events from eventfile and/or seismograms
    PLOTCOUNTRIES: on | off [ On ] Include a basic vector layer with country boundaries as a layer on the map

    The following options allow the user to scale the event symbols. The default is a constant symbol
    size.
    MAGnitude: Scale event symbols linearly with user0. [ Off ]
    REsidual: Scale event symbols linearly with abs(user0). [ Off ] Positive values are (+)
    negatives are (-).

    RMean_residual: Same as residual except mean is removed [ Off ] from all the residuals.

    PLOTLEGEND: on | off [ Off ] Plot a legend for Earthquake Magnitudes and Residuals

    EVENTFILE: Specify an ASCII text file containing additional event data. Each
    line in the file contains data for a single event. The first two columns of each line must
    contain latitude and longitude (in degrees), respectively. The third column is optional
    and contains symbol size information (e.g., magnitudes, depth, travel-time residual,
    ...). The following is an example of a few lines in an eventfile:
    38.5 42.5 6.5
    25.5 37.3 5.5
    44.2 40.9 5.7

    A TITLE can be specified using the TITLE command.

    The default output file is map.html. An alternative file name can be specified using the FILE option.

****        
**DEFAULT VALUES**

    MAP STAN off FILE map.html PLOTSTATIONS on PLOTEVENTS on PLOTCOUNTRIES off
****
**HEADER DATA**

    Station latitudes (stla) and longitudes (stlo) must be set. If event latitudes (evla) and longitudes
    (evlo) are set they will be included in the map.

    The results of each MAP command are written to a file in the current directory called map.html unless 
    otherwise specified using the FILE argument.
    The results of each MAP command will automatically be displayed. 
    The default program used to create the display is the default handler for HTML files specified 
    by the host operating system.