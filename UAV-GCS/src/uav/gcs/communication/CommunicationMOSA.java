package uav.gcs.communication;

import uav.generic.module.comm.Communication;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import uav.gcs.planner.AStar4m;
import uav.gcs.planner.CCQSP4m;
import uav.gcs.planner.HGA4m;
import uav.gcs.planner.Planner;
import uav.gcs.struct.Drone;
import uav.generic.module.comm.Client;
import uav.generic.struct.constants.Constants;
import uav.generic.struct.constants.TypeMsgCommunication;
import uav.generic.struct.constants.TypePlanner;
import uav.generic.struct.mission.Mission;
import uav.generic.reader.ReaderFileConfig;
import uav.generic.util.UtilRoute;
import uav.generic.struct.states.StateCommunication;

/**
 * @author Jesimar S. Arantes
 */
public class CommunicationMOSA extends Communication implements Client{

    private final ReaderFileConfig config;
    private final Drone drone;
    private boolean isRunningPlanner;

    public CommunicationMOSA(Drone drone) {
        this.drone = drone;
        this.stateCommunication = StateCommunication.WAITING;
        this.config = ReaderFileConfig.getInstance();
        this.isRunningPlanner = false;
    }

    @Override
    public void connectServer() {
        System.out.println("UAV-GCS trying connect with MOSA ...");
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        socket = new Socket(config.getHostMOSA(), config.getPortNetworkMOSAandGCS());
                        output = new PrintWriter(socket.getOutputStream(), true);
                        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        System.out.println("UAV-GCS connected in MOSA");
                        break;
                    } catch (IOException ex) {
                        stateCommunication = StateCommunication.DISABLED;
                    } catch (InterruptedException ex) {
                        stateCommunication = StateCommunication.DISABLED;
                    }
                }
            }
        });
    }

    @Override
    public void receiveData() {
        stateCommunication = StateCommunication.LISTENING;
        System.out.println("UAV-GCS trying to listen the connection of MOSA ...");
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (input != null) {
                            String answer = input.readLine();
                            if (answer != null) {
                                if (answer.contains(TypeMsgCommunication.MOSA_GCS_PLANNER)) {
                                    answer = answer.substring(18);
                                    plannerInGCS(answer);
                                }
                            }
                        } 
                        Thread.sleep(Constants.TIME_TO_SLEEP_BETWEEN_MSG);
                    }
                } catch (InterruptedException ex) {
                    System.out.println("Warning [InterruptedException] receiveData()");
                    ex.printStackTrace();
                    stateCommunication = StateCommunication.DISABLED;
                } catch (IOException ex) {
                    System.out.println("Warning [IOException] receiveData()");
                    ex.printStackTrace();
                    stateCommunication = StateCommunication.DISABLED;
                }
            }
        });
    }
    
    public boolean isRunningPlanner() {
        return isRunningPlanner;
    }

    private void plannerInGCS(String answer) {
        isRunningPlanner = true;
        String v[] = answer.split(";");
        Planner planner = null;
        if (v[0].equals(TypePlanner.HGA4M)) {
            planner = new HGA4m(drone, v[1], v[2], v[3], v[4], v[5],
                    v[6], v[7], v[8], v[9], v[10], v[11], v[12], v[13]);
            planner.clearLogs();
            int size = Integer.parseInt(v[2]);
            boolean finish = false;
            int nRoute = 0;
            while (nRoute < size - 1 && !finish) {
                System.out.println("route: " + nRoute);
                boolean respMission = ((HGA4m) planner).execMission(nRoute);
                if (!respMission) {
                    sendData(TypeMsgCommunication.UAV_ROUTE_FAILURE);
                    isRunningPlanner = false;
                    return;
                }
                nRoute++;
                if (nRoute == size - 1) {
                    finish = true;
                }
            }
            Mission mission = new Mission();
            nRoute = 0;
            while (nRoute < size - 1) {
                String path = v[5] + "routeGeo" + nRoute + ".txt";
                if (config.hasRouteSimplifier()){
                    UtilRoute.execRouteSimplifier(path, config.getDirRouteSimplifier(), 
                            config.getFactorRouteSimplifier(), ";");
                    path = config.getDirRouteSimplifier() + "output-simplifier.txt";               
                }
                boolean respFile = UtilRoute.readFileRouteMOSA(mission, path, nRoute, size);
                if (!respFile) {
                    sendData(TypeMsgCommunication.UAV_ROUTE_FAILURE);
                    isRunningPlanner = false;
                    return;
                }
                nRoute++;
            }
            mission.printMission();
            sendData(new Gson().toJson(mission));
        } else if (v[0].equals(TypePlanner.CCQSP4M)) {
            planner = new CCQSP4m(drone, v[1], v[2], v[3], v[4], v[5], v[6], v[7]);
            planner.clearLogs();
            boolean respMission = ((CCQSP4m) (planner)).execMission();
            if (!respMission) {
                sendData(TypeMsgCommunication.UAV_ROUTE_FAILURE);
                isRunningPlanner = false;
                return;
            }
            Mission mission = new Mission();
            String path = v[3] + "routeGeo.txt";
            
            if (config.hasRouteSimplifier()){
                UtilRoute.execRouteSimplifier(path, config.getDirRouteSimplifier(), 
                            config.getFactorRouteSimplifier(), ";");
                path = config.getDirRouteSimplifier() + "output-simplifier.txt";               
            }
            
            boolean respFile = UtilRoute.readFileRouteMOSA(mission, path);
            if (!respFile) {
                sendData(TypeMsgCommunication.UAV_ROUTE_FAILURE);
                isRunningPlanner = false;
                return;
            }
            mission.printMission();
            sendData(new Gson().toJson(mission));            
        } else if (v[0].equals(TypePlanner.A_STAR4M)) {
            planner = new AStar4m(drone, v[1], v[3], v[4], v[5], v[6], v[7]);
            planner.clearLogs();
            int size = Integer.parseInt(v[2]);
            boolean finish = false;
            int nRoute = 0;
            while (nRoute < size - 1 && !finish) {
                System.out.println("route: " + nRoute);
                boolean respMission = ((AStar4m) planner).execMission(nRoute);
                if (!respMission) {
                    sendData(TypeMsgCommunication.UAV_ROUTE_FAILURE);
                    isRunningPlanner = false;
                    return;
                }
                nRoute++;
                if (nRoute == size - 1) {
                    finish = true;
                }
            }
            Mission mission = new Mission();
            nRoute = 0;
            while (nRoute < size - 1) {
                String path = v[5] + "routeGeo" + nRoute + ".txt";
                if (config.hasRouteSimplifier()){
                    UtilRoute.execRouteSimplifier(path, config.getDirRouteSimplifier(), 
                            config.getFactorRouteSimplifier(), ";");
                    path = config.getDirRouteSimplifier() + "output-simplifier.txt";               
                }
                boolean respFile = UtilRoute.readFileRouteMOSA(mission, path, nRoute, size);
                if (!respFile) {
                    sendData(TypeMsgCommunication.UAV_ROUTE_FAILURE);
                    isRunningPlanner = false;
                    return;
                }
                nRoute++;
            }
            mission.printMission();
            sendData(new Gson().toJson(mission));
        } 
        isRunningPlanner = false;
    }

}
