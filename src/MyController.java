

// File:          MyController.java
// Date:
// Description:
// Author:
// Modifications:

import com.cyberbotics.webots.controller.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyController extends Robot {
    
    private Base base = null;
    private Arm arm = null;
    private Gripper gripper = null;
    private ArrayList<String> instructionFromSuperController = null;
    
    public MyController() {
        super();
        
        // initialization for working with simulation devices
        gripper = new Gripper(
                getServo("finger1"),
                getServo("finger2")
                );
        arm = new Arm(
                getServo("arm1"),
                getServo("arm2"),
                getServo("arm3"),
                getServo("arm4"),
                getServo("arm5")
                );
        
        // instruction list initialization
        instructionFromSuperController = new ArrayList<String>();
                
    }
    
    public void run() throws IOException, InterruptedException {
        while (step(Util.TIME_STEP) != -1) {
            getAJob();
            instructionFromSuperController.clear();
        }
        System.out.println("help");
    }
    
    private void getAJob() throws UnknownHostException, IOException, ConnectException{
        // TODO: Double check this method
        
        // Connect to controllerserver and send instruction from chromosome
        System.out.println("Creating socket");
        Socket socket = new Socket(InetAddress.getLocalHost(), Util.CTRL_PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        // Wait until ControllerServer is ready
        String inputLine;
        while((inputLine = in.readLine()) != null){
            instructionFromSuperController.add(inputLine);
        }
        
        // Do the job
        controllerJob(instructionFromSuperController);
                
        // Tell controllerServer we're done
        out.println("Done");
        
        // Closing
        out.close();
        in.close();
        socket.close();
    }
    
    private void controllerJob(ArrayList<String> instructionFromSuperController){
        double[] armPos = new double[instructionFromSuperController.size()];
        for(int i=0; i<armPos.length; i++){
            armPos[i] = Double.parseDouble(instructionFromSuperController.get(i));
        }
        arm.setPosition(armPos[0], armPos[1], armPos[2], armPos[3], armPos[4]);
        step(Util.TIME_STEP);
    }
    
    public static void main(String[] args) throws InterruptedException {
        MyController controller = new MyController();
        try {
            System.out.println("Launching controller");
            controller.run();
        } catch (IOException ex) {
            System.err.println("Error while launching controller");
            ex.printStackTrace();
        }
    }
}
