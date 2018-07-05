
/******************************************************************************************************************
* File:SinkFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example for using the SinkFilterTemplate for creating a sink filter. This particular
* filter reads some input from the filter's input port and does the following:
*
*	1) It parses the input stream and "decommutates" the measurement ID
*	2) It parses the input steam for measurments and "decommutates" measurements, storing the bits in a long word.
*
* This filter illustrates how to convert the byte stream data from the upstream filterinto useable data found in
* the stream: namely time (long type) and measurements (double type).
*
*
* Parameters: 	None
*
* Internal Methods: None
*
******************************************************************************************************************/
import java.util.*; // This class is used to interpret time words
import java.util.Map.Entry;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat; // This class is used to format and write time in a string format.

public class ExtrapolationSinkFilter extends FilterFramework {
    public void run() {
        /************************************************************************************
         * TimeStamp is used to compute time using java.util's Calendar class.
         * TimeStampFormat is used to format the time value so that it can be
         * easily printed to the terminal.
         *************************************************************************************/

        String fileName = "OutputC.dat"; // Input data file.
        Vector<Pipeframe> alldata = new Vector<Pipeframe>();

        HashMap<String, Double> data = new LinkedHashMap<String, Double>();
        DataOutputStream out = null;

        try {
            out = new DataOutputStream(new FileOutputStream(fileName));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        int MeasurementLength = 8; // This is the length of all measurements
                                   // (including time) in bytes
        int IdLength = 4; // This is the length of IDs in the byte stream

        byte timebyte;
        byte databyte = 0; // This is the data byte read from the stream
        int bytesread = 0; // This is the number of bytes read from the stream

        long measurement; // This is the word used to store all measurements -
                          // conversions are illustrated.
        int id; // This is the measurement id
        int i; // This is a loop counter
        String filestring = "";
        Double pressure = 0.0;
        /*************************************************************
         * First we announce to the world that we are alive...
         **************************************************************/

        System.out.print("\n" + this.getName() + ":: Sink Reading \n");

        boolean n = true;
        Pipeframe f = new Pipeframe(0.0, 0.0, 0.0, 0.0);
        while (true) {
            try {
                /***************************************************************************
                 * // We know that the first data coming to this filter is going
                 * to be an ID and // that it is IdLength long. So we first
                 * decommutate the ID bytes.
                 ****************************************************************************/

                id = 0;

                for (i = 0; i < IdLength; i++) {
                    databyte = ReadFilterInputPort(); // This is where we read
                                                      // the byte from the
                                                      // stream...
                    //System.out.print(databyte + " ");
                    id = id | (databyte & 0xFF); // We append the byte on to
                                                 // ID...

                    if (i != IdLength - 1) // If this is not the last byte, then
                                           // slide the
                    { // previously appended byte to the left by one byte
                        id = id << 8; // to make room for the next byte we
                                      // append to the ID

                    } // if

                    bytesread++; // Increment the byte count
                } // for

                /****************************************************************************
                 * // Here we read measurements. All measurement data is read as
                 * a stream of bytes // and stored as a long value. This permits
                 * us to do bitwise manipulation that // is neccesary to convert
                 * the byte stream into data words. Note that bitwise //
                 * manipulation is not permitted on any kind of floating point
                 * types in Java. // If the id = 0 then this is a time value and
                 * is therefore a long value - no // problem. However, if the id
                 * is something other than 0, then the bits in the // long value
                 * is really of type double and we need to convert the value
                 * using // Double.longBitsToDouble(long val) to do the
                 * conversion which is illustrated. // below.
                 *****************************************************************************/

                measurement = 0;

                for (i = 0; i < MeasurementLength; i++) {
                    databyte = ReadFilterInputPort();
                    measurement = measurement | (databyte & 0xFF); // We append
                                                                   // the byte
                                                                   // on to
                                                                   // measurement...

                    if (i != MeasurementLength - 1) // If this is not the last
                                                    // byte, then slide the
                    { // previously appended byte to the left by one byte
                        measurement = measurement << 8; // to make room for the
                                                        // next byte we append
                                                        // to the
                                                        // measurement
                    } // if

                    bytesread++; // Increment the byte count

                } // if
                //System.out.println();
                /****************************************************************************
                 * // Here we look for an ID of 0 which indicates this is a time
                 * measurement. // Every frame begins with an ID of 0, followed
                 * by a time stamp which correlates // to the time that each
                 * proceeding measurement was recorded. Time is stored // in
                 * milliseconds since Epoch. This allows us to use Java's
                 * calendar class to // retrieve time and also use text format
                 * classes to format the output into // a form humans can read.
                 * So this provides great flexibility in terms of // dealing
                 * with time arithmetically or for string display purposes. This
                 * is // illustrated below.
                 ****************************************************************************/

                if (id == 0) {

                    if (!n) {
                        // System.out.println(f.time + " " + f.temperature + " "
                        // + f.altitude + " " + f.pressure);
                        alldata.add(new Pipeframe(f.time, f.temperature, f.altitude, f.pressure));
                        f = new Pipeframe(0.0, 0.0, 0.0, 0.0);

                    }
                    n = false;
                    // write String to File

                    // time = TimeStampFormat.format(TimeStamp.getTime()) + " ";
                    f.time = measurement;
                    // System.out.println(" " + f.time + " " + f.temperature+ "
                    // " + f.altitude+ " " + f.pressure);
                } // if

                /****************************************************************************
                 * // Here we pick up a measurement (ID = 3 in this case), but
                 * you can pick up // any measurement you want to. All
                 * measurements in the stream are // decommutated by this class.
                 * Note that all data measurements are double types // This
                 * illustrates how to convert the bits read from the stream into
                 * a double // type. Its pretty simple using
                 * Double.longBitsToDouble(long value). So here // we print the
                 * time stamp and the data associated with the ID we are
                 * interested // in.
                 ****************************************************************************/

                if (id == 2) {
                    f.altitude = Double.longBitsToDouble(measurement);
                    // System.out.println(" " + f.time + " " + f.temperature+ "
                    // " + f.altitude+ " " + f.pressure);
                } // if

                if (id == 3) {
                    f.pressure = Double.longBitsToDouble(measurement);
                    // System.out.println(" " + f.time + " " + f.temperature+ "
                    // " + f.altitude+ " " + f.pressure);
                } // if

                if (id == 4) {
                    f.temperature = Double.longBitsToDouble(measurement);
                    // System.out.println(" " + f.time + " " + f.temperature+ "
                    // " + f.altitude+ " " + f.pressure);
                } // if
                  // System.out.print( "\n" );

            } // try

            /*******************************************************************************
             * The EndOfStreamExeception below is thrown when you reach end of
             * the input stream (duh). At this point, the filter ports are
             * closed and a message is written letting the user know what is
             * going on.
             ********************************************************************************/

            catch (EndOfStreamException e) {

                // TODO

                alldata.add(new Pipeframe(f.time, f.temperature, f.altitude, f.pressure));

                String result = extrapolate(alldata);

                try {
                    out.writeBytes(result);
                } catch (IOException e2) {
                    e.printStackTrace();
                }

                ClosePorts();
                try {
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.out.print("\n" + this.getName() + ":: Extrapolation Sink Exiting; bytes read: " + bytesread);
                break;

            } // catch

        } // while

    } // run

    private String extrapolate(Vector<Pipeframe> alldata) {

        Calendar TimeStamp = Calendar.getInstance();
        SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy:dd:hh:mm:ss");
        Pipeframe[] array = new Pipeframe[alldata.size()];
        Iterator<Pipeframe> it = alldata.iterator();

        int i = 0;
        while (it.hasNext()) {
            array[i] = it.next();
            i++;
        }

        boolean anything_valid = false;
        i = 0;
        for (i = 1; i < array.length - 1; i++) {
            if (array[i].pressure - array[i + 1].pressure > -10 && array[i].pressure - array[i + 1].pressure < 10) {
                array[i].valid = true;
                anything_valid = true;
            }
        }

        if (array[0].pressure - array[1].pressure > -10 && array[0].pressure - array[1].pressure < 10) {
            array[0].valid = true;
            anything_valid = true;
        }

        if (array[array.length - 1].pressure - array[array.length - 2].pressure > -10
                && array[array.length - 1].pressure - array[array.length - 2].pressure < 10) {
            array[array.length - 1].valid = true;
            anything_valid = true;
        }

        String wildpoints = "";
        for (i = 0; i < array.length; i++) {
            if (!array[i].valid){
                TimeStamp.setTimeInMillis(Double.doubleToLongBits(array[i].time));
                wildpoints = wildpoints + TimeStampFormat.format(TimeStamp.getTime()) + " "
                        + String.format("%.5f", array[i].temperature) + " " + String.format("%.5f", array[i].altitude) + " "
                        + String.format("%.5f", array[i].pressure) + "\n";

            }
        }
        DataOutputStream wildpointsout = null;

        try {
            wildpointsout = new DataOutputStream(new FileOutputStream("PressureWildPoints.dat"));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        try {
            wildpointsout.writeBytes(wildpoints);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        if (!anything_valid) {
            return "";
        }

        if (!array[0].valid && array[1].valid) {
            array[0] = array[1];
            array[i].extrapolated = true;
            // System.out.println("0");
        }

        if (!array[array.length - 1].valid && array[array.length - 2].valid) {
            array[array.length - 1] = array[array.length - 2];
            array[array.length - 1].extrapolated = true;
            // System.out.println(array.length-1);
        }

        // int x = 0;
        boolean changed = true;
        while (changed) {
            // x++;
            // System.out.println("Round " + x);

            changed = false;

            for (i = 1; i < array.length - 1; i++) {
                if (!array[i].valid) {
                    double right = 0;
                    double left = 0;

                    for (int l = i; l < array.length; l++) {
                        if (array[l].valid) {
                            right = array[l].pressure;
                        }
                    }
                    for (int k = i; k >= 0; k--) {
                        if (array[k].valid) {
                            left = array[k].pressure;
                        }
                    }

                    array[i].extrapolated = true;
                    array[i].pressure = 0.5 * (right + left);
                    array[i].valid = true;
                    changed = true;
                }
            }

        }

        String output = "";
        for (i = 0; i < array.length - 1; i++) {
            if (!array[i].valid) {
                return "";
            }
            TimeStamp.setTimeInMillis(Double.doubleToLongBits(array[i].time));
            output = output + TimeStampFormat.format(TimeStamp.getTime()) + " "
                    + String.format("%.5f", array[i].temperature) + " " + String.format("%.5f", array[i].altitude) + " "
                    + String.format("%.5f", array[i].pressure);
            if (array[i].extrapolated == true) {
                output = output + "*";
            }
            output = output + "\n";

        }
        return output;

    }

} // SingFilter