package uav.generic.hardware.sensors;

/**
 * Classe que modela o status do drone (mode, systemstatus, armed, isArmable, ekfOk).
 * @author Jesimar S. Arantes
 */
public class StatusUAV {
    
    /**
     * Possible autopilot flight mode values:.
     *     AUTO: 
     *     GUIDED: 
     *     STABILIZE: 
     */
    public String mode;
    
    /**
    * Possible autopilot status values:.
    *     UNINIT: Uninitialized system, state is unknown.
    *     BOOT: System is booting up.
    *     CALIBRATING: System is calibrating and not flight-ready.
    *     STANDBY: System is grounded and on standby. It can be launched any time.
    *     ACTIVE: System is active and might be already airborne. Motors are engaged.
    *     CRITICAL: System is in a non-normal flight mode. It can however still navigate.
    *     EMERGENCY: System is in a non-normal flight mode. It lost control over parts or over the whole airframe. It is in mayday and going down.
    *     POWEROFF: System just initialized its power-down sequence, will shut down now.
    */
    public String systemStatus;
    public boolean armed;
    public boolean isArmable;    
    public boolean ekfOk;

    /**
     * Class constructor.
     */
    public StatusUAV() {
        
    }

    /**
     * Class constructor.
     * @param mode mode of flight [AUTO, GUIDED, STABILIZE, ...]
     * @param systemStatus status of the system [UNINIT, BOOT, CALIBRATING, STANDBY, ACTIVE, CRITICAL, EMERGENCY, POWEROFF]
     * @param armed drone is armed
     * @param isArmable drone is Armable
     * @param ekfOk Extended Kalman Filter is Ok
     */
    public StatusUAV(String mode, String systemStatus, boolean armed, 
            boolean isArmable, boolean ekfOk) {
        this.mode = mode;
        this.systemStatus = systemStatus;
        this.armed = armed;
        this.isArmable = isArmable;        
        this.ekfOk = ekfOk;
    }
    
    /**
     * Converts line in JSON format to mode values.
     * @param line FORMAT: {"mode": "STABILIZE"}
     */
    public void parserInfoMode(String line) {
        try{
            line = line.substring(10, line.length() - 2);        
            this.mode = line;
        }catch (Exception ex){
            
        }
    }
    
    /**
     * Converts line in JSON format to systemStatus values.
     * @param line FORMAT: {"system-status": "STANDBY"}
     */
    public void parserInfoSystemStatus(String line) {
        try{
            line = line.substring(19, line.length() - 2);        
            this.systemStatus = line;
        }catch (Exception ex){
            
        }
    }
    
    /**
     * Converts line in JSON format to armed values.
     * @param line FORMAT: {"armed": false}
     */
    public void parserInfoArmed(String line) {
        try{
            line = line.substring(10, line.length() - 1);        
            this.armed = Boolean.parseBoolean(line);
        }catch (Exception ex){
            
        }
    }
    
    /**
     * Converts line in JSON format to isArmable values.
     * @param line FORMAT: {"is-armable": true}
     */
    public void parserInfoIsArmable(String line) {
        try{
            line = line.substring(15, line.length() - 1);        
            this.isArmable = Boolean.parseBoolean(line);
        }catch (Exception ex){
            
        }
    }        
    
    /**
     * Converts line in JSON format to ekfOk values.
     * @param line FORMAT: {"ekf-ok": true}
     */
    public void parserInfoEkfOk(String line) {
        try{
            line = line.substring(11, line.length() - 1);        
            this.ekfOk = Boolean.parseBoolean(line);
        }catch (Exception ex){
            
        }
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
    
    public void setSystemStatus(String systemStatus) {
        this.systemStatus = systemStatus;
    }
    
    public void setArmed(String armed) {
        try{
            this.armed = Boolean.parseBoolean(armed);
        }catch (Exception ex){
            
        }
    }

    public void setArmed(boolean armed) {
        this.armed = armed;
    }
    
    public void setIsArmable(String isArmable) {
        try{
            this.isArmable = Boolean.parseBoolean(isArmable);
        }catch (Exception ex){
            
        }
    }

    public void setIsArmable(boolean isArmable) {
        this.isArmable = isArmable;
    }   
    
    public void setEkfOk(String ekfOk) {
        try{
            this.ekfOk = Boolean.parseBoolean(ekfOk);
        }catch (Exception ex){
            
        }
    }

    public void setEkfOk(boolean ekfOk) {
        this.ekfOk = ekfOk;
    }    

    @Override
    public String toString() {
        return "StatusUAV{" + "mode=" + mode + ", systemStatus=" + systemStatus + 
                ", armed=" + armed + ", isArmable=" + isArmable + ", ekfOk=" + ekfOk + '}';
    }
            
}
