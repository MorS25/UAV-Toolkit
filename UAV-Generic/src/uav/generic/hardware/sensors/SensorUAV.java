package uav.generic.hardware.sensors;

/**
 * Classe que modela alguns sensores do drone (heading, groundspeed, airspeed).
 * @author Jesimar S. Arantes
 */
public class SensorUAV {
    
    public double heading;    //in degrees (0 ... 360)
    public double groundspeed;//in metres/second
    public double airspeed;   //in metres/second

    /**
     * Class constructor.
     */
    public SensorUAV() {
        
    }

    /**
     * Class constructor.
     * @param heading angle of aircraft (in degrees) (range 0 to 360)
     * @param groundspeed velocity in m/s
     * @param airspeed velocity in m/s
     */
    public SensorUAV(double heading, double groundspeed, double airspeed) {
        this.heading = heading;
        this.groundspeed = groundspeed;
        this.airspeed = airspeed;
    }
    
    /**
     * Converts line in JSON format to heading values.
     * @param line FORMAT: {"heading": 110}
     */
    public void parserInfoHeading(String line) {
        try{
            line = line.substring(12, line.length() - 1);        
            this.heading = Double.parseDouble(line);
        }catch (NumberFormatException ex){
            
        }
    }
    
    /**
     * Converts line in JSON format to groundspeed values.
     * @param line FORMAT: {"groundspeed": 2.21}
     */
    public void parserInfoGroundSpeed(String line) {
        try{
            line = line.substring(16, line.length() - 1);        
            this.groundspeed = Double.parseDouble(line);
        }catch (NumberFormatException ex){
            
        }
    }
    
    /**
     * Converts line in JSON format to airspeed values.
     * @param line FORMAT: {"airspeed": 1.53}
     */
    public void parserInfoAirSpeed(String line) {
        try{
            line = line.substring(13, line.length() - 1);        
            this.airspeed = Double.parseDouble(line);
        }catch (NumberFormatException ex){
            
        }
    }
    
    public void setHeading(String heading) {
        try{
            this.heading = Double.parseDouble(heading);
        }catch (NumberFormatException ex){
            
        }        
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }
    
    public void setGroundspeed(String groundspeed) {
        try{
            this.groundspeed = Double.parseDouble(groundspeed);
        }catch (NumberFormatException ex){
            
        }
    }
    
    public void setGroundspeed(double groundspeed) {
        this.groundspeed = groundspeed;
    }
    
    public void setAirspeed(String airspeed) {
        try{
            this.airspeed = Double.parseDouble(airspeed);
        }catch (NumberFormatException ex){
            
        }        
    } 

    public void setAirspeed(double airspeed) {
        this.airspeed = airspeed;
    }        

    @Override
    public String toString() {
        return "SensorUAV{" + "heading=" + heading + ", groundspeed=" + groundspeed + ", airspeed=" + airspeed + '}';
    }        
        
}
