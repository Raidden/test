public class Pipeframe {

    public double temperature;
    public double time;
    public double altitude;
    public double pressure;
    public boolean valid;
    public boolean extrapolated;

    public  Pipeframe (double t, double temp, double a, double p){
        temperature = temp;
        time = t;
        pressure = p;
        altitude = a;
        valid = false;
        extrapolated = false;
    }
}