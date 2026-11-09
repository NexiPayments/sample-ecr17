
package it.nexi.sampleecr_01.sampleecr01;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Nexi Payments
 *
 * Test class used to launch ECR commands.
 * 
 * 
 */
public class LaunchECRCommandSample {
    
    private ECRServiceSample ecrSample;
    
    public LaunchECRCommandSample(String posIP, int posPort ) {
    
        ecrSample = new ECRServiceSample(posIP,posPort);
        
    }

    
    // Sample Payment 
    public void startPayment() throws Exception{
        
        String amountCents                          = "280";
        String terminalId                           = "00000000";
        String ecrId                                = "";
        char additionalDataFlag                     = '0';
        MapAdditionalData mapAdditionalData         = null;
        List<MapAdditionalData> listAdditionalData  = null;
        char startWithCardPresent                   = '0';   
        char paymentType                            = '0';
        String textInput                            = "";
        boolean isReceiptOnECR                      = true;
            
        ecrSample.payment(amountCents, 
                         terminalId, 
                         ecrId, 
                         additionalDataFlag, 
                         listAdditionalData, 
                         startWithCardPresent, paymentType, 
                         textInput, 
                         isReceiptOnECR
                        );
            
        
    } // public void startPayment() throws Exception{
    
    // Sample Payment Extended
    public void startPaymentExtended() throws Exception{
        
        String amountCents                          = "350";
        String terminalId                           = "00000000";
        String ecrId                                = "";
        char additionalDataFlag                     = '0';
        MapAdditionalData mapAdditionalData         = null;
        List<MapAdditionalData> listAdditionalData  = null;
        char startWithCardPresent                   = '0';   
        char paymentType                            = '0';
        String textInput                            = "";
        boolean isReceiptOnECR                      = true;
            
        ecrSample.paymentExtended(amountCents, 
                                  terminalId, 
                                  ecrId, 
                                  additionalDataFlag, 
                                  listAdditionalData, 
                                  startWithCardPresent, paymentType, 
                                  textInput, 
                                  isReceiptOnECR
                                 );
            
        
    } // public void startPaymentExtended() throws Exception{
    
    // Sample Payment Extended with Additional Data
    public void startPaymentExtendedWithAdditionalData() throws Exception{
        
        String amountCents                          = "850";
        String terminalId                           = "00000000";
        String ecrId                                = "";
        char additionalDataFlag                     = '1';
        char startWithCardPresent                   = '0';   
        char paymentType                            = '0';
        String textInput                            = "";
        boolean isReceiptOnECR                      = true;
        
        // Instantiate the list that will hold the index-value pair 
        // to be associated with the additional data
        List<MapAdditionalData> listAdditionalData = new ArrayList<>();
        
        // Create the first index-value pair for Index
        int index = 2;
        String valueIndex = "trx0003";
        
        // Instantiate the object that will hold the values ​​for Index
        MapAdditionalData mapAdditionalData = new MapAdditionalData();
        mapAdditionalData.setIndex(index);
        mapAdditionalData.setValue(valueIndex);
        
        // Inserts the index-value pair into the list
        listAdditionalData.add(mapAdditionalData);
        
        // The list of index-value pairs is passed to the method
        ecrSample.paymentExtended(amountCents, 
                                  terminalId, 
                                  ecrId, 
                                  additionalDataFlag, 
                                  listAdditionalData, 
                                  startWithCardPresent, paymentType, 
                                  textInput, 
                                  isReceiptOnECR
                                 );
            
        
    } // public void startPaymentExtendedWithAdditionalData() throws Exception{
    
    // Sample Payment Extended with rokenization
    public void startPaymentExtendedWithTokenization() throws Exception{
        
        String amountCents                          = "650";
        String terminalId                           = "00000000";
        String ecrId                                = "";
        char additionalDataFlag                     = '1';
        char startWithCardPresent                   = '0';   
        char paymentType                            = '0';
        String textInput                            = "";
        boolean isReceiptOnECR                      = true;
        
        // Instantiate the list that will hold the index-value pair 
        // to be associated with the additional data
        List<MapAdditionalData> listAdditionalData = new ArrayList<>();
        
        // Create the first index-value pair for Index 1
        String prefixBTD = "BTD";
        String uniqueContractCode = "PC" + UtilsSample.generateRandomString(16);
        String valueIndex1 = prefixBTD + uniqueContractCode;
        
        // Instantiate the object that will hold the values ​​for Index 1
        MapAdditionalData mapAdditionalData = new MapAdditionalData();
        mapAdditionalData.setIndex(1);
        mapAdditionalData.setValue(valueIndex1);
        
        // Inserts the index-value pair into the list
        listAdditionalData.add(mapAdditionalData);
        
        // Create the second index-value pair for Index 5
        String service      = "0COF";
        //String service      = "0REC";
        String fixedValue   = "FNZ";
        String action       = "03";
        String valueIndex5 = service + fixedValue + action;
        
        // Instantiate the object that will hold the values ​​for Index 5
        mapAdditionalData = new MapAdditionalData();
        mapAdditionalData.setIndex(5);
        mapAdditionalData.setValue(valueIndex5);
        
        // Inserts the index-value pair into the list
        listAdditionalData.add(mapAdditionalData);
        
        // The list of index-value pairs is passed to the method
        ecrSample.paymentExtended(amountCents, 
                                  terminalId, 
                                  ecrId, 
                                  additionalDataFlag, 
                                  listAdditionalData, 
                                  startWithCardPresent, paymentType, 
                                  textInput, 
                                  isReceiptOnECR
                                 );
            
        
    } // public void startPaymentExtendedWithTokenization() throws Exception{
    
    
    // Sample Reversal
    public void startReversal() throws Exception{
        
        String  terminalId                          = "00000000";
        String  ecrId                               = "";
        String  stan                                = "" ;
        char    additionalDataFlag                  = '0';
        MapAdditionalData mapAdditionalData         = null;
        List<MapAdditionalData> listAdditionalData  = null;
        boolean isReceiptOnECR                      = true;
            
        ecrSample.reversal(terminalId, 
                           ecrId, 
                           stan,
                           additionalDataFlag, 
                           listAdditionalData, 
                           isReceiptOnECR
                          );
        
    } // private static void startReversal() throws Exception{
    
    // Sample Terminal Status
    public void startTerminalStatus() throws Exception {
         
        String terminalId = "00000000";
        
        ecrSample.terminalStatus(terminalId);
        
    } //  public void startTerminalStatus() throws Exception {
    
    // Sample Send Last Result
    public void startSendLastResult() throws Exception{
        
        String  terminalId                  = "00000000";
        String  ecrId                       = "";
        char    additionalDataFlag          = '0';
        MapAdditionalData mapAdditionalData = null;
        
        ecrSample.sendLastResult(terminalId,
                                 ecrId,
                                 additionalDataFlag,
                                 mapAdditionalData
                                );
        
    } // public void startSendLastResult() throws Exception{
    
    // Sample Reprint ticket
    public void startReprintTicket() throws Exception {
        
        String  terminalId                      = "00000000";
        char    enableDisablePrinttingOnECR     = '0';  
        char    typeTicket                      = '0';
        
        ecrSample.reprintTicket(terminalId, 
                                enableDisablePrinttingOnECR, 
                                typeTicket
                               );
        
    } // public void startReprintTicket() throws Exception 
    
    
     // Sample Card Verification 
    public void startCardVerification() throws Exception{
        
        
        String terminalId                           = "00000000";
        String ecrId                                = "";
        char additionalDataFlag                     = '0';
        MapAdditionalData mapAdditionalData         = null;
        List<MapAdditionalData> listAdditionalData  = null;
        char cardVerificationType                   = '0';   
        char paymentType                            = '0';
        boolean isReceiptOnECR                      = true;
            
        ecrSample.cardVerfication(terminalId, 
                                  ecrId, 
                                  additionalDataFlag, 
                                  listAdditionalData, 
                                  cardVerificationType, 
                                  paymentType, 
                                  isReceiptOnECR
                                 );
            
        
    } // public void startCardVerification() throws Exception{
    
    // Sample Pre-authorization 
    public void startPreAuthorization() throws Exception{
        
        String amountCents                          = "500";
        String terminalId                           = "00000000";
        String ecrId                                = "";
        char additionalDataFlag                     = '0';
        MapAdditionalData mapAdditionalData         = null;
        List<MapAdditionalData>  listAdditionalData = null;
        char startWithCardPresent                   = '0';   
        char paymentType                            = '0';
        String textInput                            = "";
        boolean isReceiptOnECR                      = true;
            
        ecrSample.preAuthorization(amountCents, 
                                   terminalId, 
                                   ecrId, 
                                   additionalDataFlag, 
                                   listAdditionalData, 
                                   startWithCardPresent, paymentType, 
                                   textInput, 
                                   isReceiptOnECR
                                  );
            
        
    } // public void startPreAuthorization() throws Exception{
    
     // Sample Pre-authorization incremental
    public void startPreAuthorizationIncremental() throws Exception{
        
        String amountCents                          = "100";
        String terminalId                           = "00000000";
        String ecrId                                = "";
        String preAuthorizationCode                 = "286817967";
        char additionalDataFlag                     = '0';
        MapAdditionalData mapAdditionalData         = null;
        List<MapAdditionalData>  listAdditionalData = null;
        String textInput                            = "";
        boolean isReceiptOnECR                      = true;
            
        ecrSample.preAuthorizationIncremental(amountCents,
                                              terminalId,
                                              ecrId,
                                              additionalDataFlag,
                                              listAdditionalData,
                                              preAuthorizationCode,
                                              textInput,
                                              isReceiptOnECR
                                             );
            
        
    } // public void startPreAuthorizationIncremental() throws Exception{
    
    // Sample Pre-authorization closure
    public void startPreAuthorizationClosure() throws Exception{
        
        String amountCents                          = "200";
        String terminalId                           = "00000000";
        String ecrId                                = "";
        String preAuthorizationCode                 = "286817967";
        char additionalDataFlag                     = '0';
        MapAdditionalData mapAdditionalData         = null;
        List<MapAdditionalData> listAdditionalData  = null;
        String textInput                            = "";
        boolean isReceiptOnECR                      = true;
            
        ecrSample.preAuthorizationClosure(amountCents,
                                          terminalId,
                                          ecrId,
                                          additionalDataFlag,
                                          listAdditionalData,
                                          preAuthorizationCode,
                                          textInput,
                                          isReceiptOnECR
                                         );
            
        
    } // public void startPreAuthorizationClosure() throws Exception{
    
    
    // Sample Close Sessiom
    public void startCloseSession() throws Exception{
        
        String terminalId                           = "00000000";
        String ecrId                                = "";
        char additionalDataFlag                     = '0';
        MapAdditionalData mapAdditionalData         = null; 
        List<MapAdditionalData> listAdditionalData  = null;
        boolean isReceiptOnECR                      = true;
            
        ecrSample.closeSession(terminalId,
                               ecrId,
                               additionalDataFlag,
                               listAdditionalData,
                               isReceiptOnECR
                              );
        
    } // public void startCloseSession() throws Exception{
    
    
     // Sample Terminal Totals
    public void startTerminalTotals() throws Exception{
        
        String terminalId                           = "00000000";
        String ecrId                                = "";
        char additionalDataFlag                     = '0';
        MapAdditionalData mapAdditionalData         = null; 
        List<MapAdditionalData> listAdditionalData  = null;
        boolean isReceiptOnECR                      = true;
            
        ecrSample.terminalTotals(terminalId,
                                 ecrId,
                                 additionalDataFlag,
                                 listAdditionalData,
                                 isReceiptOnECR
                                );
        
    } // public void startTerminalTotals() throws Exception{
    
}
