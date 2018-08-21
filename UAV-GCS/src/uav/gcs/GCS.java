package uav.gcs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.concurrent.Executors;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import lib.color.StandardPrints;
import uav.gcs.struct.Drone;
import uav.gcs.communication.CommunicationIFA;
import uav.gcs.conection.SaveDB;
import uav.gcs.commands.keyboard.KeyboardCommands;
import uav.gcs.commands.voice.VoiceCommands;
import uav.gcs.communication.CommunicationMOSA;
import uav.gcs.map.PanelPlotGoogleMaps;
import uav.gcs.map.PanelPlotMission;
import uav.gcs.window.LabelsInfo;
import uav.generic.struct.constants.TypeInputCommand;
import uav.generic.struct.geom.PointGeo;
import uav.generic.struct.reader.ReaderFileConfig;
import uav.generic.util.UtilGeo;

/**
 * @author Jesimar S. Arantes
 */
public final class GCS extends JFrame {

    public static PointGeo pointGeo;
    private final JPanel panelTop;
    private final JPanel panelMain;
    private final JPanel panelLeft;
    private final JPanel panelRightStatus;
    private final JPanel panelRight;
    private final PanelPlotMission panelPlotMission;
    private PanelPlotGoogleMaps panelPlotGoogleMaps;
    private final LabelsInfo labelsInfo;
    private final JTabbedPane tab;

    private final JButton btnBadWeather;
    private final JButton btnPathReplanningEmergency;
    private final JButton btnLand;
    private final JButton btnRTL;

    private JButton btnBuzzer;
    private JButton btnAlarm;
    private JButton btnPicture;
    private JButton btnVideo;
    private JButton btnPhotoInSequence;
    private JButton btnLED;
    private JButton btnBlink;
    private JButton btnSpraying;
    private JButton btnParachute;

    private final JButton btnKeyboardCommands;
    private final JButton btnVoiceCommands;

    private final JLabel labelIsConnectedIFA;
    private final JLabel labelIsConnectedMOSA;
    private final JLabel labelIsConnectedDB;
    private final JLabel labelIsRunningPathPlanner;
    private final JLabel labelIsRunningPathReplanner;

    private final int width = 900;
    private final int height = 620;

    private final Drone drone;
    private final ReaderFileConfig config;
    private final CommunicationIFA communicationIFA;
    private final CommunicationMOSA communicationMOSA;

    private SaveDB saveDB;
    private final KeyboardCommands keyboard;
    private final VoiceCommands voice;

    public static void main(String[] args) {
        GCS gcs = new GCS();
    }

    public GCS() {
        Locale.setDefault(Locale.US);
        config = ReaderFileConfig.getInstance();
        if (!config.read()) {
            System.exit(1);
        }
        if (!config.checkReadFields()) {
            System.exit(1);
        }
        if (!config.parseToVariables()) {
            System.exit(1);
        }
        
        execScript("../Scripts/exec-swap-mission.sh " + config.getDirMission());
        
        try{
            pointGeo = UtilGeo.getPointGeo(config.getDirFiles()+config.getFileGeoBase());
        }catch (FileNotFoundException ex){
            StandardPrints.printMsgError2("Error [FileNotFoundException] pointGeo");
            ex.printStackTrace();
            System.exit(1);
        }
        
        drone = new Drone(config.getUserEmail());
        communicationIFA = new CommunicationIFA(drone, config.getHostIFA(),
                config.getPortNetworkIFAandGCS());
        communicationMOSA = new CommunicationMOSA(drone, config.getHostMOSA(),
                config.getPortNetworkMOSAandGCS());
        keyboard = new KeyboardCommands(communicationIFA);
        voice = new VoiceCommands(communicationIFA);
        if (config.hasDB()) {
            saveDB = new SaveDB(config.getHostOD(), config.getPortOD());
        }

        this.setTitle("UAV-GCS");
        this.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.setLocationRelativeTo(null);

        panelTop = new JPanel();
        panelTop.setLayout(new FlowLayout(FlowLayout.LEFT));
        panelTop.setBackground(new Color(166, 207, 255));
        panelTop.setVisible(true);

        JButton btnAbout = new JButton("About");
        btnAbout.setPreferredSize(new Dimension(120, 25));
        btnAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAbout();
            }
        });
        panelTop.add(btnAbout);

        JButton btnExit = new JButton("Exit");
        btnExit.setPreferredSize(new Dimension(120, 25));
        btnExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        panelTop.add(btnExit);

        panelMain = new JPanel();
        panelMain.setLayout(new FlowLayout(FlowLayout.CENTER));
        panelMain.setBackground(Color.LIGHT_GRAY);
        panelMain.setVisible(true);

        panelLeft = new JPanel();
        panelLeft.setLayout(new FlowLayout(FlowLayout.CENTER));
        panelLeft.setBackground(new Color(95, 161, 255));
        panelLeft.setVisible(true);
        panelMain.add(panelLeft);

        JLabel labelActions = new JLabel("Actions");
        labelActions.setPreferredSize(new Dimension(160, 20));
        labelActions.setForeground(Color.BLACK);
        panelLeft.add(labelActions);

        btnBadWeather = new JButton("BAD-WEATHER");
        btnBadWeather.setPreferredSize(new Dimension(185, 25));
        btnBadWeather.setEnabled(false);
        btnBadWeather.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                communicationIFA.sendData(TypeInputCommand.CMD_BAD_WEATHER);
            }
        });
        panelLeft.add(btnBadWeather);

        btnPathReplanningEmergency = new JButton("EMERGENCY-LANDING");
        btnPathReplanningEmergency.setPreferredSize(new Dimension(185, 25));
        btnPathReplanningEmergency.setEnabled(false);
        btnPathReplanningEmergency.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                communicationIFA.sendData(TypeInputCommand.CMD_EMERGENCY_LANDING);
            }
        });
        panelLeft.add(btnPathReplanningEmergency);

        btnLand = new JButton("LAND");
        btnLand.setPreferredSize(new Dimension(185, 25));
        btnLand.setEnabled(false);
        btnLand.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                communicationIFA.sendData(TypeInputCommand.CMD_LAND);
            }
        });
        panelLeft.add(btnLand);

        btnRTL = new JButton("RTL");
        btnRTL.setPreferredSize(new Dimension(185, 25));
        btnRTL.setEnabled(false);
        btnRTL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                communicationIFA.sendData(TypeInputCommand.CMD_RTL);
            }
        });
        panelLeft.add(btnRTL);

        if (config.hasBuzzer()) {
            btnBuzzer = new JButton("BUZZER-TURN-ON");
            btnBuzzer.setPreferredSize(new Dimension(185, 25));
            btnBuzzer.setEnabled(false);
            btnBuzzer.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    communicationIFA.sendData(TypeInputCommand.CMD_BUZZER);
                }
            });
            panelLeft.add(btnBuzzer);

            btnAlarm = new JButton("BUZZER-ALARM");
            btnAlarm.setPreferredSize(new Dimension(185, 25));
            btnAlarm.setEnabled(false);
            btnAlarm.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    communicationIFA.sendData(TypeInputCommand.CMD_ALARM);
                }
            });
            panelLeft.add(btnAlarm);
        }

        if (config.hasCamera()) {
            btnPicture = new JButton("CAMERA-PICTURE");
            btnPicture.setPreferredSize(new Dimension(185, 25));
            btnPicture.setEnabled(false);
            btnPicture.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    communicationIFA.sendData(TypeInputCommand.CMD_PICTURE);
                }
            });
            panelLeft.add(btnPicture);

            btnVideo = new JButton("CAMERA-VIDEO");
            btnVideo.setPreferredSize(new Dimension(185, 25));
            btnVideo.setEnabled(false);
            btnVideo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    communicationIFA.sendData(TypeInputCommand.CMD_VIDEO);
                }
            });
            panelLeft.add(btnVideo);
            
            btnPhotoInSequence = new JButton("CAMERA-PHOTO-IN-SEQ");
            btnPhotoInSequence.setPreferredSize(new Dimension(185, 25));
            btnPhotoInSequence.setEnabled(false);
            btnPhotoInSequence.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    communicationIFA.sendData(TypeInputCommand.CMD_PHOTO_IN_SEQUENCE);
                }
            });
            panelLeft.add(btnPhotoInSequence);
        }

        if (config.hasLED()) {
            btnLED = new JButton("LED-TURN-ON");
            btnLED.setPreferredSize(new Dimension(185, 25));
            btnLED.setEnabled(false);
            btnLED.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    communicationIFA.sendData(TypeInputCommand.CMD_LED);
                }
            });
            panelLeft.add(btnLED);

            btnBlink = new JButton("LED-BLINK");
            btnBlink.setPreferredSize(new Dimension(185, 25));
            btnBlink.setEnabled(false);
            btnBlink.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    communicationIFA.sendData(TypeInputCommand.CMD_BLINK);
                }
            });
            panelLeft.add(btnBlink);
        }

        if (config.hasSpraying()) {
            btnSpraying = new JButton("SPRAYING-START");
            btnSpraying.setPreferredSize(new Dimension(185, 25));
            btnSpraying.setEnabled(false);
            btnSpraying.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    communicationIFA.sendData(TypeInputCommand.CMD_SPRAYING);
                }
            });
            panelLeft.add(btnSpraying);
        }

        if (config.hasParachute()) {
            btnParachute = new JButton("PARACHUTE-OPEN");
            btnParachute.setPreferredSize(new Dimension(185, 25));
            btnParachute.setEnabled(false);
            btnParachute.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    communicationIFA.sendData(TypeInputCommand.CMD_OPEN_PARACHUTE);
                }
            });
            panelLeft.add(btnParachute);
        }

        btnKeyboardCommands = new JButton("Keyboard Commands");
        btnKeyboardCommands.setPreferredSize(new Dimension(185, 25));
        btnKeyboardCommands.setEnabled(false);
        btnKeyboardCommands.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keyboard.openTheWindow();
                keyboard.listenerTheKeyboard();
            }
        });
        panelLeft.add(btnKeyboardCommands);

        btnVoiceCommands = new JButton("Voice Commands");
        btnVoiceCommands.setPreferredSize(new Dimension(185, 25));
        btnVoiceCommands.setEnabled(false);
        btnVoiceCommands.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                voice.openTheWindow();
                voice.listenerTheSpeech();
            }
        });
        panelLeft.add(btnVoiceCommands);

        communicationIFA.connectServerIFA();
        communicationIFA.receiveData();

        communicationMOSA.connectServerMOSA();
        communicationMOSA.receiveData();

        if (config.hasDB()) {
            saveDB.saveDB(drone, communicationIFA);
        }

        panelRightStatus = new JPanel();
        panelRightStatus.setLayout(new FlowLayout(FlowLayout.CENTER));
        panelRightStatus.setBackground(new Color(95, 161, 255));
        panelRightStatus.setVisible(true);

        tab = new JTabbedPane();

        panelRight = new JPanel();
        panelRight.setLayout(new FlowLayout(FlowLayout.CENTER));
        panelRight.setBackground(new Color(85, 141, 255));
        panelRight.setVisible(true);

        panelPlotMission = new PanelPlotMission();
        panelPlotMission.waitingForRoutes();
        panelPlotMission.plotDroneInRealTime(drone);
        
        if (config.hasGoogleMaps()){
            panelPlotGoogleMaps = new PanelPlotGoogleMaps();
            panelPlotGoogleMaps.init(width - 200, height - 100);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            panelPlotGoogleMaps.drawMap();
            panelPlotGoogleMaps.waitingForRoutes();
            panelPlotGoogleMaps.plotDroneInRealTime(drone);
        }

        tab.add("UAV Data", panelRight);
        tab.add("Plot Simple Map", panelPlotMission);
        if (config.hasGoogleMaps()){
            tab.add("Plot Google Maps", panelPlotGoogleMaps);
        }
        
        labelIsConnectedIFA = new JLabel("Connected IFA: False");
        labelIsConnectedIFA.setPreferredSize(new Dimension(170, 20));
        labelIsConnectedIFA.setForeground(Color.RED);
        panelRightStatus.add(labelIsConnectedIFA);

        labelIsConnectedMOSA = new JLabel("Connected MOSA: False");
        labelIsConnectedMOSA.setPreferredSize(new Dimension(170, 20));
        labelIsConnectedMOSA.setForeground(Color.RED);
        panelRightStatus.add(labelIsConnectedMOSA);

        labelIsConnectedDB = new JLabel("Connected DB: False");
        labelIsConnectedDB.setPreferredSize(new Dimension(170, 20));
        labelIsConnectedDB.setForeground(Color.RED);
        panelRightStatus.add(labelIsConnectedDB);

        labelIsRunningPathPlanner = new JLabel("Run Planner: False");
        labelIsRunningPathPlanner.setPreferredSize(new Dimension(160, 20));
        labelIsRunningPathPlanner.setForeground(Color.RED);
        panelRightStatus.add(labelIsRunningPathPlanner);

        labelIsRunningPathReplanner = new JLabel("Run Replanner: False");
        labelIsRunningPathReplanner.setPreferredSize(new Dimension(160, 20));
        labelIsRunningPathReplanner.setForeground(Color.RED);
        panelRightStatus.add(labelIsRunningPathReplanner);

        panelRight.add(panelRightStatus);

        labelsInfo = new LabelsInfo(panelRight);
        this.add(panelTop);
        this.add(panelMain);

        panelMain.add(tab);
        
        enableComponentsInterface();

        this.setSize(width, height);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.addWindowStateListener(new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                setSize(getWidth(), getHeight());
            }
        });
        this.setVisible(true);
        
        SwingUtilities.invokeLater(()->{
            tryLookAndFeel("Nimbus");
            SwingUtilities.updateComponentTreeUI(this);
        });
    }
    
    public void tryLookAndFeel(String part_name){
        UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
        int index = -1;
        for(int i = 0; i < lookAndFeels.length; i++){
            if(lookAndFeels[i].getName().contains(part_name)){
                index = i;
            }
        }
        if (index!=-1) {
            this.setLookAndFeel(lookAndFeels[index].getClassName());
        }
    }
    
    public void setLookAndFeel(String lookAndFeelds){
        try {
            UIManager.setLookAndFeel(lookAndFeelds);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private void enableComponentsInterface() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                        if (communicationIFA.isConnected()) {
                            btnBadWeather.setEnabled(true);
                            btnPathReplanningEmergency.setEnabled(true);
                            btnLand.setEnabled(true);
                            btnRTL.setEnabled(true);
                            if (config.hasBuzzer()) {
                                btnBuzzer.setEnabled(true);
                                btnAlarm.setEnabled(true);
                            }
                            if (config.hasCamera()) {
                                btnPicture.setEnabled(true);
                                btnVideo.setEnabled(true);
                                btnPhotoInSequence.setEnabled(true);
                            }
                            if (config.hasLED()) {
                                btnLED.setEnabled(true);
                                btnBlink.setEnabled(true);
                            }
                            if (config.hasSpraying()) {
                                btnSpraying.setEnabled(true);
                            }
                            if (config.hasParachute()) {
                                btnParachute.setEnabled(true);
                            }
                            btnKeyboardCommands.setEnabled(true);
                            btnVoiceCommands.setEnabled(true);
                            labelIsConnectedIFA.setText("Connected IFA: True");
                            labelIsConnectedIFA.setForeground(Color.GREEN);
                            if (communicationIFA.isRunningReplanner()) {
                                labelIsRunningPathReplanner.setText("Run Replanner: True");
                                labelIsRunningPathReplanner.setForeground(Color.GREEN);
                            } else {
                                labelIsRunningPathReplanner.setText("Run Replanner: False");
                                labelIsRunningPathReplanner.setForeground(Color.RED);
                            }
                            labelsInfo.updateInfo(drone);
                        } else {
                            labelIsConnectedIFA.setText("Connected IFA: False");
                            labelIsConnectedIFA.setForeground(Color.RED);
                        }
                        if (communicationMOSA.isConnected()) {
                            labelIsConnectedMOSA.setText("Connected MOSA: True");
                            labelIsConnectedMOSA.setForeground(Color.GREEN);
                            if (communicationMOSA.isRunningPlanner()) {
                                labelIsRunningPathPlanner.setText("Run Planner: True");
                                labelIsRunningPathPlanner.setForeground(Color.GREEN);
                            } else {
                                labelIsRunningPathPlanner.setText("Run Planner: False");
                                labelIsRunningPathPlanner.setForeground(Color.RED);
                            }
                        } else {
                            labelIsConnectedMOSA.setText("Connected MOSA: False");
                            labelIsConnectedMOSA.setForeground(Color.RED);
                        }
                        if (config.hasDB()) {
                            if (saveDB.isConnected()) {
                                labelIsConnectedDB.setText("Connected DB: True");
                                labelIsConnectedDB.setForeground(Color.GREEN);
                            } else {
                                labelIsConnectedDB.setText("Connected DB: False");
                                labelIsConnectedDB.setForeground(Color.RED);
                            }
                        } else {
                            labelIsConnectedDB.setText("Connected DB: False");
                            labelIsConnectedDB.setForeground(Color.RED);
                        }
                    } catch (InterruptedException ex) {

                    }
                }
            }
        });
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        panelTop.setPreferredSize(new Dimension(width - 30, 70));
        panelMain.setPreferredSize(new Dimension(width - 30, height - 110));
        panelLeft.setPreferredSize(new Dimension(200, height - 120));
        panelRightStatus.setPreferredSize(new Dimension(width - 30 - 200 - 20 - 20, 50));
        panelRight.setPreferredSize(new Dimension(width - 30 - 200 - 20, height - 120));
        panelPlotMission.setNewDimensions(width - 30 - 200 - 20, height - 120);
    }

    public void showAbout() {
        String msgAbout = "Program: UAV-GCS\n"
                + "Author: Jesimar da Silva Arantes\n"
                + "Version: 3.0.0\n"
                + "Date: 21/08/2018";
        JOptionPane.showMessageDialog(null, msgAbout, "About",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void execScript(String cmd) {
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            BufferedReader read = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            while (read.ready()) {
                System.out.println(read.readLine());
            }
        }catch (IOException ex){
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
