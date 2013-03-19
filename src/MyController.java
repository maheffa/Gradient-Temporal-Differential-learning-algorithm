

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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyController extends Robot {
    
    private Base base = null;
    private Arm arm = null;
    private Gripper gripper = null;
    private ServerSocket controllerServerSocket = null;
    private Socket superControllerClientSocket = null;
    private ArrayList<String> instructionFromSuperController = null;
    private BufferedReader inSuperController = null;
    private PrintWriter outSuperController = null;
    
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
        
        // serversocket for listening to the SuperController's instruction
        try {
            controllerServerSocket = 
                    new ServerSocket(Util.CONTROLLER_PORT, 0, InetAddress.getLocalHost());
        } catch (IOException e) {
            System.err.println("Could not listen on port: "+Util.CONTROLLER_PORT);
            System.exit(1);
        }
        
        // instruction list initialization
        instructionFromSuperController = new ArrayList<String>();
                
    }
    
    public void run() throws IOException {
        do {
            try {
                System.out.println("Waiting for serversocket to connect");
                superControllerClientSocket = controllerServerSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            // Preapre to read and write into RL's socket
            inSuperController = new BufferedReader( new InputStreamReader(superControllerClientSocket.getInputStream()));
            outSuperController = new PrintWriter(superControllerClientSocket.getOutputStream(), true);
            
            // Tell superController, we are ready
            outSuperController.println("READY");
            System.out.println("SuperController instructions request");
            
            // Read the instructions
            String inputLine;
            while((inputLine = inSuperController.readLine()) != null){
                instructionFromSuperController.add(inputLine);
            }
            System.out.println("Received instructions "+Util.printArrayList(instructionFromSuperController));
            
            // Begin the job
            controllerJob(instructionFromSuperController);
            
            // Tell SuperController that we're done
            outSuperController.println("Done");
            
            // closing
            inSuperController.close();
            superControllerClientSocket.close();
            instructionFromSuperController.clear();
            
        } while (step(Util.TIME_STEP) != -1);
    }
    
    private void controllerJob(ArrayList<String> instructionFromSuperController){
        double[] armPos = new double[instructionFromSuperController.size()];
        for(int i=0; i<armPos.length; i++){
            armPos[i] = Double.parseDouble(instructionFromSuperController.get(i));
        }
        arm.setPosition(armPos[0], armPos[1], armPos[2], armPos[3], armPos[4]);
        step(Util.TIME_STEP);
    }
    
    public static void main(String[] args) {
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
