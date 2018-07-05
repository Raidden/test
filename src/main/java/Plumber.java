/******************************************************************************************************************
* File:Plumber.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*   1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example to illstrate how to use the PlumberTemplate to create a main thread that
* instantiates and connects a set of filters. This example consists of three filters: a source, a middle filter
* that acts as a pass-through filter (it does nothing to the data), and a sink filter which illustrates all kinds
* of useful things that you can do with the input stream of data.
*
* Parameters:       None
*
* Internal Methods: None
*
******************************************************************************************************************/
public class Plumber
{
   public static void main( String argv[])
   {
        /****************************************************************************
        * Here we instantiate three filters.
        ****************************************************************************/

        SourceFilter Filter1a = new SourceFilter("/home/bill/IdeaProjects/RESA/src/main/datasets/FlightData.dat");
        SourceFilter Filter1b = new SourceFilter("/home/bill/IdeaProjects/RESA/src/main/datasets/FlightData.dat");
        YFilter Filter2 = new YFilter();
        MiddleFilter FilterM = new MiddleFilter();
        LimitFilter Limit = new LimitFilter();        
        ConversionFilter Filter3 = new ConversionFilter();        
        ExtrapolationSinkFilter Filter4 = new ExtrapolationSinkFilter();

        
        /****************************************************************************
        * Here we connect the filters starting with the sink filter (Filter 1) which
        * we connect to Filter2 the middle filter. Then we connect Filter2 to the
        * source filter (Filter3).
        ****************************************************************************/
        
        Filter2.ConnectA(Filter1a);
        Filter2.ConnectB(Filter1b);
        FilterM.ConnectY(Filter2);
        Limit.Connect(FilterM);
        Filter3.Connect(Limit);
        Filter4.Connect(Filter3);
        
        /****************************************************************************
        * Here we start the filters up. All-in-all,... its really kind of boring.
        ****************************************************************************/

        Filter1a.start();
        Filter1b.start();
        Filter2.start();
        FilterM.start();
        Limit.start();
        Filter3.start();
        Filter4.start();
        
   } // main

} // Plumber