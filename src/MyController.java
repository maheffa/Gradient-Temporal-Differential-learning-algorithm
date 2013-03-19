

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

class ControllerServerChecker extends Thread {
    private MyController controller = null;
    
    public ControllerServerChecker(MyController controller){
        this.controller = controller;
    }
    
    @Override
    public void run(){
        while(true){ try {
            while(controller.controllerClient != null){
                this.sleep(1000);
            }
            System.out.println("Creating socket");
            try {
                controller.controllerClient = new Socket(InetAddress.getLocalHost(), Util.CTRL_PORT);
            } catch (IOException ex) {
                Logger.getLogger(ControllerServerChecker.class.getName()).log(Level.SEVERE, null, ex);
                controller.controllerClient = null;
                controller.step(Util.TIME_STEP);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ControllerServerChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
    }
}

public class MyController extends Robot {
    
    private Base base = null;
    private Arm arm = null;
    private Gripper gripper = null;
    private ArrayList<String> instructionFromSuperController = null;
    public Socket controllerClient = null;
    private ControllerServerChecker checker = null;
    
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
        
        // initialize checker
        checker = new ControllerServerChecker(this);
        checker.run();
    }
    
    public void run() {
        do {
            if(controllerClient != null ) try {
                getAJob();
                instructionFromSuperController.clear();
            } catch (IOException ex) {
                Logger.getLogger(MyController.class.getName()).log(Level.SEVERE, null, ex);
            }
            else{
                continue;
            }
        } while (step(Util.TIME_STEP) != -1);
        System.out.println("help");
    }
    
    private void getAJob() throws IOException{
        // TODO: Double check this method
        System.out.println("GOT A JOOOOB!!!");
        // Connect to controllerserver and send instruction from chromosome
        PrintWriter out = new PrintWriter(controllerClient.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(controllerClient.getInputStream()));
        
        // Wait until ControllerServer is ready
        String inputLine;
        System.out.println("Receiving info from server");
        while((inputLine = in.readLine()) != null){
            instructionFromSuperController.add(inputLine);
            System.out.println(inputLine);
        }
        
        // Do the job
        controllerJob(instructionFromSuperController);
        
        // Tell controllerServer we're done
        out.println("Done");
        
        // Closing
        out.close();
        in.close();
        controllerClient.close();
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
        System.out.println("Launching controller");
        controller.run();
    }
}
