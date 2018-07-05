
public class ConversionFilter extends FilterFramework {
    public void run() {
        /************************************************************************************
         * TimeStamp is used to compute time using java.util's Calendar class.
         * TimeStampFormat is used to format the time value so that it can be
         * easily printed to the terminal.
         *************************************************************************************/

        int MeasurementLength = 8; // This is the length of all measurements
                                   // (including time) in bytes
        int IdLength = 4; // This is the length of IDs in the byte stream

        byte databyte = 0; // This is the data byte read from the stream
        int bytesread = 0; // This is the number of bytes read from the stream
        int byteswritten = 0; // Number of bytes written to the stream.
        byte[] alldata = new byte[8];

        long measurement; // This is the word used to store all measurements -
                          // conversions are illustrated.
        int id; // This is the measurement id
        int i; // This is a loop counter

        /*************************************************************
         * First we announce to the world that we are alive...
         **************************************************************/


        while (true) {
            try {
                /***************************************************************************
                 * // We know that the first data coming to this filter is going
                 * to be an ID and // that it is IdLength long. So we first
                 * decommutate the ID bytes.
                 ****************************************************************************/

                id = 0;

                //System.out.print(this.getName() + "::Conversion Reading (x " + IdLength + ") ");
                for (i = 0; i < IdLength; i++) {

                    databyte = ReadFilterInputPort(); // This is where we read
                                                      // the byte from the
                                                      // stream...

                    bytesread++;
                   // System.out.print(databyte + " ");
                    id = id | (databyte & 0xFF); // We append the byte on to
                                                 // ID...

                    if (i != IdLength - 1) // If this is not the last byte, then
                                           // slide the
                    { // previously appended byte to the left by one byte
                        id = id << 8; // to make room for the next byte we
                                      // append to the ID

                    } // if

                    bytesread++; // Increment the byte count

                    WriteFilterOutputPort(databyte); // directly write the ID
                                                     // since we're not changing
                                                     // that one
                    byteswritten++;

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

                //System.out.print("\n" + this.getName() + " Conversion Reading (x " + MeasurementLength + ")");
                for (i = 0; i < MeasurementLength; i++) {
                    databyte = ReadFilterInputPort();
                   // System.out.print(databyte + " ");
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
                    alldata[i] = databyte;
                } // if

               // System.out.println();
                /****************************************************************************
                 * // Here we look for an ID of 4 which indicates this is a
                 * temperature measurement.
                 ****************************************************************************/

                if (id == 4) {
                    double celsius = ((Double.longBitsToDouble(measurement) - 32) * 5.0 / 9.0);
                    // System.out.println("\nMeasurement in celsius: " +
                    // celsius);

                    byte[] result = new byte[8];
                    long lng = Double.doubleToLongBits(celsius);
                    for (int b = 0; b < 8; b++) {
                        alldata[b] = (byte) ((lng >> ((7 - b) * 8)) & 0xff);
                        WriteFilterOutputPort(alldata[b]);
                        byteswritten++;
                    }

                } // if

                /****************************************************************************
                 * // Here we look for an ID of 2 which indicates this is a
                 * altitude measurement.
                 *****************************************************************************/

                else if (id == 2) {
                    double meters = (Double.longBitsToDouble(measurement)) * 0.3048;
                  
                    byte[] result = new byte[8];
                    long lng = Double.doubleToLongBits(meters);
                    for (int b = 0; b < 8; b++) {
                        alldata[b] = (byte) ((lng >> ((7 - b) * 8)) & 0xff);
                        WriteFilterOutputPort(alldata[b]);
                        byteswritten++;

                    }
                }
                
                
                else {
                    for (int b = 0; b < 8; b++) {
                        WriteFilterOutputPort(alldata[b]);
                        byteswritten++;
                    }
                }

            } // try

            /*******************************************************************************
             * The EndOfStreamExeception below is thrown when you reach end of
             * the input stream (duh). At this point, the filter ports are
             * closed and a message is written letting the user know what is
             * going on.
             ********************************************************************************/

            catch (EndOfStreamException e) {
                ClosePorts();
                System.out.print("\n" + this.getName() + ":: Conversion Exiting; bytes read: " + bytesread + "; bytes written: " + byteswritten + "\n");
                break;

            } // catch

        } // while

    } // run

} // SingFilter