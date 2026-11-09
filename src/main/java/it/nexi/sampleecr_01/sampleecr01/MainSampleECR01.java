
package it.nexi.sampleecr_01.sampleecr01;

/**
 *
 * Nexi
 * 
 * Main Class used to launch test commands
 * 
 */
public class MainSampleECR01 {
    
    public static void main(String[] args) {
        
        // Set the parameters for connecting to the POS
        String posIP = "xx.xxx.xxx.xx";
        int posPort = 8081;
        
        // Test class used to launch ECR commands.
        LaunchECRCommandSample launchECRCommandSample = new LaunchECRCommandSample(posIP,posPort);

        System.out.println("Sample ECR");
        
        try {
            
            // Examples of ECR ​​command execution
            
            // uncomment the command to be executed
            
            //launchECRCommandSample.startPayment();
            //launchECRCommandSample.startPaymentExtended();
            //launchECRCommandSample.startReversal();
            //launchECRCommandSample.startTerminalStatus();
            //launchECRCommandSample.startSendLastResult();
            //launchECRCommandSample.startReprintTicket();
            //launchECRCommandSample.startPreAuthorization();
            //launchECRCommandSample.startPreAuthorizationIncremental();
            //launchECRCommandSample.startPreAuthorizationClosure();
            //launchECRCommandSample.startCloseSession();
            //launchECRCommandSample.startTerminalTotals();
            //launchECRCommandSample.startCardVerification();
            //launchECRCommandSample.startPaymentExtendedWithAdditionalData();
            //launchECRCommandSample.startPaymentExtendedWithTokenization();
            
         } catch (Exception ex) {
             
             ex.printStackTrace();
            
        }
        
    } // main
    
} // end class
