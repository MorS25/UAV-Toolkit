package uav.hardware.aircraft;

import uav.hardware.data.HomeLocation;
import uav.hardware.data.ListParameters;
import uav.hardware.sensors.Attitude;
import uav.hardware.sensors.Battery;
import uav.hardware.sensors.GPS;
import uav.hardware.sensors.GPSInfo;
import uav.hardware.sensors.SensorUAV;
import uav.hardware.sensors.StatusUAV;
import uav.hardware.sensors.Velocity;

/**
 *
 * @author jesimar
 */
public class iDroneAlpha extends Drone{
    
    public iDroneAlpha(){
        SPEED_MAX = 10.0;
        SPEED_CRUIZE = 1.0;
        MASS = 2.828;
        PAYLOAD = 0.600;
        ENDURANCE = 420.0;
        time = 0.0;
        distanceToHome = 0.0;
        homeLocation = new HomeLocation();
        listParameters = new ListParameters();
        battery = new Battery(11.1, 0.0, 100.0);
        gps = new GPS();        
        attitude = new Attitude();
        velocity = new Velocity();
        gpsinfo = new GPSInfo();
        sensorUAV = new SensorUAV();
        statusUAV = new StatusUAV();
    }
}