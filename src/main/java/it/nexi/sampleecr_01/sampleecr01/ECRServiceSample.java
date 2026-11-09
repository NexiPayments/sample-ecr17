
package it.nexi.sampleecr_01.sampleecr01;

import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ACK;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_ADDITIONAL_DATA;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_PAYMENT_EXTENDED_REQUEST;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_REPRINT_TICKET;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_REPRINT_TICKET_FLAG_ENABLE_ON_PRINTER;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_REVERSAL;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_SEND_LAST_RESULT;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_TERMINAL_STATUS;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.NAK;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.STX;
import static it.nexi.sampleecr_01.sampleecr01.ECRBuildFramesSample.buildAdditionalDataFrame;
import static it.nexi.sampleecr_01.sampleecr01.ECRBuildFramesSample.buildEnableDisablePrintingReceiptFrame;
import static it.nexi.sampleecr_01.sampleecr01.ECRBuildFramesSample.buildPaymentExtendedFrame;
import static it.nexi.sampleecr_01.sampleecr01.ECRBuildFramesSample.buildReprintTicketFrame;
import static it.nexi.sampleecr_01.sampleecr01.ECRBuildFramesSample.buildReversalFrame;
import static it.nexi.sampleecr_01.sampleecr01.ECRBuildFramesSample.buildSendLastResultFrame;
import static it.nexi.sampleecr_01.sampleecr01.ECRBuildFramesSample.buildTerminalStatusFrame;
import static it.nexi.sampleecr_01.sampleecr01.LogPosToEcrFramesSample.logAllFramesFromPos;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.containsEndOfTicket;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.getFlagFromReprintTicketMessage;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.getMessageCode;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.getRawAscii;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.sanitizeFrame;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.sendAckToPos;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.toHex;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.TIMEOUT_SHORT_DATA_FROM_POS;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.TIMEOUT_CONNECTION_TO_POS;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.TIMEOUT_LONG_DATA_FROM_POS;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.DELAY_SEND_COMMAND_TO_POS;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_CARD_VERIFICATION;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_CLOSE_SESSION;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_PAYMENT;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_PREAUTHORIZATION;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_PREAUTHORIZATION_CLOSURE;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_PREAUTHORIZATION_INCREMENTAL;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_TERMINAL_TOTALS;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ETX;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.FLAG_ADDITIONAL_DATA_ENABLED;
import static it.nexi.sampleecr_01.sampleecr01.ECRBuildFramesSample.buildCardVerificationFrame;
import static it.nexi.sampleecr_01.sampleecr01.ECRBuildFramesSample.buildCloseSessionFrame;
import static it.nexi.sampleecr_01.sampleecr01.ECRBuildFramesSample.buildPaymentFrame;
import static it.nexi.sampleecr_01.sampleecr01.ECRBuildFramesSample.buildPreauthorizationClosureFrame;
import static it.nexi.sampleecr_01.sampleecr01.ECRBuildFramesSample.buildPreauthorizationFrame;
import static it.nexi.sampleecr_01.sampleecr01.ECRBuildFramesSample.buildPreauthorizationIncrementalFrame;
import static it.nexi.sampleecr_01.sampleecr01.ECRBuildFramesSample.buildTerminalTotalsFrame;

/**
 *
 * Nexi Payments
 * 
 * Sample ECR commands
 * 
 */
public class ECRServiceSample {
    
    private String posIP;
    private int posPort;

    public ECRServiceSample(String posIP, int posPort) {
        
        this.posIP = posIP;
        this.posPort = posPort;
        
    }

    public String getPosIP() {
        return posIP;
    }

    public void setPosIP(String posIP) {
        this.posIP = posIP;
    }

    public int getPosPort() {
        return posPort;
    }

    public void setPosPort(int posPort) {
        this.posPort = posPort;
    }
    
    
     /**
     * 
     * Executes the command Payment 'P'
     * 
     * @param amountCents           123 -> 1,23 € in cents
     * @param terminalId            "00000000" if all zeros, it is not verified; 8 digits
     * @param ecrId =               "00000000" ECR ID : if all zeros, it is not verified; 8 digits
     * @param additionalDataFlag    '0' = no additional data U-message
     * @param listAdditionalData    additional data to be sent to the GT
     * @param startWithCardPresent  '0' = card is not inserted at the time of the request.
     * @param paymentType           '0' = standard payment type (see Nexi doc)
     * @param textInput             text to print (up to 128 characters)
     * @param isReceiptOnECR        if set to true, the receipt is sent to the ECR
     * 
     * @throws java.lang.Exception
     */
    public void payment(String amountCents,
                        String terminalId,
                        String ecrId,
                        char additionalDataFlag,
                        List<MapAdditionalData> listAdditionalData,
                        char startWithCardPresent,
                        char paymentType,
                        String textInput,
                        boolean isReceiptOnECR
                       ) throws Exception {
        
        byte[] additionalDataFrame = null;
        
        try {
             
            // Builds the complete binary frame for the PAY request:
            // STX + BODY(167 char ASCII) + ETX + LRC
            byte[] request = buildPaymentFrame(terminalId,
                                               ECR_COMMAND_PAYMENT,
                                               ecrId,
                                               additionalDataFlag,
                                               startWithCardPresent,
                                               paymentType,
                                               amountCents,
                                               textInput
                                              );
            
            if (additionalDataFlag == FLAG_ADDITIONAL_DATA_ENABLED) {
                    
                additionalDataFrame = buildAdditionalDataFrame(terminalId, 
                                                               ECR_COMMAND_ADDITIONAL_DATA,
                                                               listAdditionalData
                                                              );
                    
            }
            
            System.out.println(" >>> START PAYMENT - COMMAND : " + ECR_COMMAND_PAYMENT);
            System.out.println(" >>> REQ HEX : ");
            System.out.println(toHex(request));
            
            System.out.println(" >>> REQ ASCII : ");
            System.out.println(getRawAscii(request));
            
            System.out.println();
            
            // Displays on screen the content of the request that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosPaymentTransactionRequest(request);
            
            // If set, displays on screen the content of the request for 
            // additional data that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosAdditionalDataRequest(additionalDataFrame);
            
            // Sends the request to the POS and returns the list of response messages 
            // issued by the POS
            List<byte[]> listframeFromPos = sendMessageToPos(posIP, 
                                                             posPort, 
                                                             terminalId,
                                                             request, 
                                                             additionalDataFrame, 
                                                             isReceiptOnECR
                                                            );
            
            // Display on screen each response message received from the POS
            logAllFramesFromPos(listframeFromPos);

        } catch (Exception ex) {
             
            throw ex;
             
        } finally {
            
             System.out.println(" >>> END PAYMENT - COMMAND : " + ECR_COMMAND_PAYMENT);
        }
            
    } // public void payment
    
     /**
     * 
     * Executes the command Payment Extended 'X'
     * 
     * @param amountCents           123 -> 1,23 € in cents
     * @param terminalId            "00000000" if all zeros, it is not verified; 8 digits
     * @param ecrId =               "00000000" ECR ID : if all zeros, it is not verified; 8 digits
     * @param additionalDataFlag    '0' = no additional data U-message
     * @param listAdditionalData    additional data to be sent to the GT
     * @param startWithCardPresent  '0' = card is not inserted at the time of the request.
     * @param paymentType           '0' = standard payment type (see Nexi doc)
     * @param textInput             text to print (up to 128 characters)
     * @param isReceiptOnECR        if set to true, the receipt is sent to the ECR
     * 
     * @throws java.lang.Exception
     */
    public void paymentExtended(String amountCents,
                                String terminalId,
                                String ecrId,
                                char additionalDataFlag,
                                List<MapAdditionalData> listAdditionalData,
                                char startWithCardPresent,
                                char paymentType,
                                String textInput,
                                boolean isReceiptOnECR
                               ) throws Exception {
        
        byte[] additionalDataFrame = null;
        
        try {
        
            // Builds the complete binary frame for the PAY_X request:
            // STX + BODY(167 char ASCII) + ETX + LRC
            byte[] request = buildPaymentExtendedFrame(terminalId,
                                                       ECR_COMMAND_PAYMENT_EXTENDED_REQUEST,
                                                       ecrId,
                                                       additionalDataFlag,
                                                       startWithCardPresent,
                                                       paymentType,
                                                       amountCents,
                                                       textInput
                                                      );
            
            if (additionalDataFlag == FLAG_ADDITIONAL_DATA_ENABLED) {
                    
                additionalDataFrame = buildAdditionalDataFrame(terminalId, 
                                                               ECR_COMMAND_ADDITIONAL_DATA,
                                                               listAdditionalData
                                                              );
                    
            }
            
            System.out.println(" >>> START PAYMENT EXTENDED - COMMAND : " + ECR_COMMAND_PAYMENT_EXTENDED_REQUEST);
            System.out.println(" >>> REQ HEX : ");
            System.out.println(toHex(request));
            
            System.out.println(" >>> REQ ASCII : ");
            System.out.println(getRawAscii(request));
            
            System.out.println();
            
            // Displays on screen the content of the request that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosPaymentTransactionRequest(request);
            
            System.out.println();
            
            // If set, displays on screen the content of the request for 
            // additional data that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosAdditionalDataRequest(additionalDataFrame);

            // Sends the request to the POS and returns the list of response messages 
            // issued by the POS
            List<byte[]> listframeFromPos = sendMessageToPos(posIP, 
                                                             posPort, 
                                                             terminalId,
                                                             request, 
                                                             additionalDataFrame, 
                                                             isReceiptOnECR
                                                            );
            
            // Display on screen each response message received from the POS
            logAllFramesFromPos(listframeFromPos);
            
        } catch (Exception ex ) {
            
            throw ex;
            
        } finally {
            
            System.out.println(" >>> END PAYMENT EXTENDED - COMMAND : " + ECR_COMMAND_PAYMENT_EXTENDED_REQUEST);
            
        }
            
    } // public void paymentExtended(String host
    
    
     /**
     * 
     * Executes the command Reversal 'S'
     * 
     * @param terminalId            "00000000" if all zeros, it is not verified; 8 digits
     * @param ecrId                 "00000000" ECR ID : if all zeros, it is not verified; 8 digits
     * @param stan                  it can also be empty.
     * @param additionalDataFlag    '0' = no additional data U-message
     * @param listAdditionalData    additional data to be sent to the GT
     * @param isReceiptOnECR        if set to true, the receipt is sent to the ECR
     * 
     * @throws java.lang.Exception 
     * 
     */
    public void reversal(String terminalId,
                         String ecrId,
                         String stan,
                         char additionalDataFlag,
                         List<MapAdditionalData> listAdditionalData,
                         boolean isReceiptOnECR
                        ) throws Exception {
        
        byte[] additionalDataFrame = null;
        
        try {
       
            // Builds the complete binary frame for the Reversal:
            // STX + BODY(167 char ASCII) + ETX + LRC
            byte[] request = buildReversalFrame(terminalId,
                                                ECR_COMMAND_REVERSAL,
                                                ecrId,
                                                stan,
                                                additionalDataFlag
                                              );
            
            if (additionalDataFlag == FLAG_ADDITIONAL_DATA_ENABLED ) {
                
                additionalDataFrame = buildAdditionalDataFrame(terminalId, 
                                                               ECR_COMMAND_ADDITIONAL_DATA,
                                                               listAdditionalData
                                                               );
                
            }
            
            System.out.println(" >>> START REVERSAL - COMMAND : " + ECR_COMMAND_REVERSAL);
            System.out.println(" >>> REQ HEX : ");
            System.out.println(toHex(request));
            
            System.out.println(" >>> REQ ASCII : ");
            System.out.println(getRawAscii(request));
            
            System.out.println();
            
            // Displays on screen the content of the request that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosReversalTransactionRequest(request);
            
            // If set, displays on screen the content of the request for 
            // additional data that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosAdditionalDataRequest(additionalDataFrame);
            
            // Sends the request to the POS and returns the list of response messages 
            // issued by the POS
            List<byte[]> listframeFromPos = sendMessageToPos(posIP, 
                                                             posPort, 
                                                             terminalId,
                                                             request, 
                                                             additionalDataFrame,
                                                             isReceiptOnECR
                                                            );
            
            // Display on screen each response message received from the POS
            logAllFramesFromPos(listframeFromPos);
            
        } catch (Exception ex) {
                          
            ex.printStackTrace();
            
        } finally {
            
             System.out.println(" >>> END REVERSAL - COMMAND : " +ECR_COMMAND_REVERSAL);
           
        }
            
    } // public void reversal
    
    
     /**
     * 
     * Executes the command Terminal Status 's'
     * 
     * @param terminalId   "00000000" if all zeros, it is not verified; 8 digits
     * @throws java.lang.Exception
     * 
     */
    public void terminalStatus(String terminalId) throws Exception {
        
        try {
            
            // Builds the complete binary frame for the request Terminal Status:
            // STX + BODY(167 char ASCII) + ETX + LRC
            byte[] request = buildTerminalStatusFrame(terminalId,
                                                      ECR_COMMAND_TERMINAL_STATUS
                                                     );

            System.out.println(" >>> START TERMINAL STATUS - COMMAND : " + ECR_COMMAND_TERMINAL_STATUS);
            System.out.println(" >>> REQ HEX : ");
            System.out.println(toHex(request));
            
            System.out.println(" >>> REQ ASCII : ");
            System.out.println(getRawAscii(request));
            
            System.out.println();
            
            // Displays on screen the content of the request that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosTerminalStatusRequest(request);
            
            // Sends the request to the POS and returns the list of response messages 
            // issued by the POS
            List<byte[]> listframeFromPos = sendMessageToPos(posIP, 
                                                             posPort, 
                                                             terminalId,
                                                             request,
                                                             null, // additional data not requested
                                                             false // receipt on ECR not requested
                                                            );
            
            // Display on screen each response message received from the POS
            logAllFramesFromPos(listframeFromPos);
        
        } catch (Exception ex) {
            
             throw ex;
             
        } finally {
            
            System.out.println(" >>> END TERMINAL STATUS - COMMAND : " + ECR_COMMAND_TERMINAL_STATUS);
            
           
        }
            
    } // public void terminalStatus
    
     /**
     * 
     * Executes the command Send Last Result 'G'
     * which returns the message associated with the last transaction
     * 
     * @param terminalId            "00000000" if all zeros, it is not verified; 8 digits
     * @param ecrId                 "00000000" ECR ID : if all zeros, it is not verified; 8 digits
     * @param additionalDataFlag    '0' = no additional data U-message
     * @param mapAdditionalData     additional data to be sent to the GT
     * 
     * @throws java.lang.Exception
     * 
     */
    public void sendLastResult(String terminalId,
                               String ecrId,
                               char additionalDataFlag,
                               MapAdditionalData mapAdditionalData
                              ) throws Exception {
        
        byte[] additionalDataFrame = null;
        
        try {
       
            // uilds the complete binary frame for the Reversal request:
            // STX + BODY(167 char ASCII) + ETX + LRC
            byte[] request = buildSendLastResultFrame(terminalId,
                                                      ECR_COMMAND_SEND_LAST_RESULT,
                                                      ecrId,
                                                      additionalDataFlag
                                                     );
            
            // Additional data is generally not sent with the
            // 'G' command
            /**
            if (additionalDataFlag == FLAG_ADDITIONAL_DATA_ENABLED) {
                
                   
                    additionalDataFrame = buildAdditionalDataFrame(terminalId, 
                                                                   ECR_COMMAND_ADDITIONAL_DATA,
                                                                   additionalData
                                                                  );
                   
                    
            }
            **/
            
           
           
            System.out.println(" >>> START SEND LAST RESULT - COMMAND : " + ECR_COMMAND_SEND_LAST_RESULT);
            System.out.println(" >>> REQ HEX : ");
            System.out.println(toHex(request));
            
            System.out.println(" >>> REQ ASCII : ");
            System.out.println(getRawAscii(request));
            
            System.out.println();
            
            // Displays on screen the content of the request that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosSendLastResultRequest(request);
            
            //LogEcrToPosFramesSample.logEcrToPosAdditionalDataRequest(additionalDataFrame);
            
            // Sends the request to the POS and returns the list of response messages 
            // issued by the POS
            List<byte[]> listframeFromPos = sendMessageToPos(posIP, 
                                                             posPort, 
                                                             terminalId,
                                                             request, 
                                                             additionalDataFrame, 
                                                             false
                                                            );
            
            // Display on screen each response message received from the POS	
            logAllFramesFromPos(listframeFromPos);
            
        } catch (Exception ex) {
                           
        } finally {
            
            System.out.println(" >>> END SEND LAST RESULT - COMMAND : " + ECR_COMMAND_SEND_LAST_RESULT);
            
        }
            
    } // public void sendLastResult
    
    
    /**
     * 
     * Executes the command Carc Verification 'H'
     * 
     * @param terminalId            "00000000" if all zeros, it is not verified; 8 digits
     * @param ecrId =               "00000000" ECR ID : if all zeros, it is not verified; 8 digits
     * @param additionalDataFlag    '0' = no additional data U-message
     * @param listAdditionalData    additional data to be sent to the GT
     * @param cardVerificationType  '0' = standard card verification
     * @param paymentType           '0' = standard payment type (see Nexi doc)
     * @param isReceiptOnECR        if set to true, the receipt is sent to the ECR
     * 
     * @throws java.lang.Exception
     */
    public void cardVerfication(String terminalId,
                                String ecrId,
                                char additionalDataFlag,
                                List<MapAdditionalData> listAdditionalData,
                                char cardVerificationType,
                                char paymentType,
                                boolean isReceiptOnECR
                               ) throws Exception {
        
        byte[] additionalDataFrame = null;
        
        try {
             
            // Builds the complete binary frame for the Card Verification request:
            // STX + BODY(167 char ASCII) + ETX + LRC
            byte[] request = buildCardVerificationFrame(terminalId,
                                                        ECR_COMMAND_CARD_VERIFICATION,
                                                        ecrId,
                                                        additionalDataFlag,
                                                        cardVerificationType,
                                                        paymentType
                                                       );
            
            if (additionalDataFlag == FLAG_ADDITIONAL_DATA_ENABLED ) {
                    
                additionalDataFrame = buildAdditionalDataFrame(terminalId, 
                                                               ECR_COMMAND_ADDITIONAL_DATA,
                                                               listAdditionalData
                                                              );
                    
            }
            
            System.out.println(" >>> START CARD VERIFICATION - COMMAND : " + ECR_COMMAND_CARD_VERIFICATION);
            System.out.println(" >>> REQ HEX : ");
            System.out.println(toHex(request));
            
            System.out.println(" >>> REQ ASCII : ");
            System.out.println(getRawAscii(request));
            
            System.out.println();
            
            // Displays on screen the content of the request that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosCardVerificationRequest(request);
            
            // If set, displays on screen the content of the request for 
            // additional data that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosAdditionalDataRequest(additionalDataFrame);
            
            // Sends the request to the POS and returns the list of response messages 
            // issued by the POS
            List<byte[]> listframeFromPos = sendMessageToPos(posIP, 
                                                             posPort, 
                                                             terminalId,
                                                             request, 
                                                             additionalDataFrame, 
                                                             isReceiptOnECR
                                                            );
            
            // Display on screen each response message received from the POS	
            logAllFramesFromPos(listframeFromPos);

        } catch (Exception ex) {
             
            throw ex;
             
        } finally {
            
             System.out.println(" >>> END CARD VERIFICATION - COMMAND : " + ECR_COMMAND_CARD_VERIFICATION);
        }
            
    } // public void cardVerfication
    
    
     /**
     * 
     * Executes the command Reprint ticket 'R'
     * which allows the last financial/service receipt to be reprinted
     * 
     * @param terminalId                    "00000000" if all zeros, it is not verified; 8 digits
     * @param enableDisablePrinttingOnECR   '0' : reprint ticket
     *                                      '1' : send receipt to ECR
     * 
     * @param typeTicket                    '0' : financial receipt
     *                                      '1' : service receipt
     * 
     * @throws java.lang.Exception
     * 
     */
    public void reprintTicket(String terminalId,
                              char enableDisablePrinttingOnECR,   
                              char typeTicket
                             ) throws Exception {
        
        try {
            
            // Builds the complete binary frame for the Reprint Ticket request:
            // STX + BODY(167 char ASCII) + ETX + LRC
            byte[] request = buildReprintTicketFrame(terminalId, 
                                                     ECR_COMMAND_REPRINT_TICKET,
                                                     enableDisablePrinttingOnECR,   
                                                     typeTicket   
                                                    );

            boolean isReceiptOnECR = enableDisablePrinttingOnECR == '1';
            
            System.out.println(" >>> START REPRINT TICKET - COMMAND : " + ECR_COMMAND_REPRINT_TICKET);
            System.out.println(" >>> REQ HEX : ");
            System.out.println(toHex(request));
            
            System.out.println(" >>> REQ ASCII : ");
            System.out.println(getRawAscii(request));
            
            System.out.println();
            
            // Displays on screen the content of the request that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosReprintTicketRequest(request);
            
            // Sends the request to the POS and returns the list of response messages 
            // issued by the POS
            List<byte[]> listframeFromPos = sendMessageToPos(posIP, 
                                                             posPort, 
                                                             terminalId,
                                                             request, 
                                                             null, 
                                                             isReceiptOnECR
                                                            );
            
            // Display on screen each response message received from the POS
            logAllFramesFromPos(listframeFromPos);
        
        } catch (Exception ex) {
                           
        } finally {
            
            System.out.println(" >>> END REPRINT TICKET - COMMAND : " + ECR_COMMAND_REPRINT_TICKET);
            
        }
            
    } // public void reprintTicket
    
    
    /**
     * 
     * Executes the command Pre-authorization 'p'
     * 
     * @param amountCents           123 -> 1,23 € in cents
     * @param terminalId            "00000000" if all zeros, it is not verified; 8 digits
     * @param ecrId =               "00000000" ECR ID : if all zeros, it is not verified; 8 digits
     * @param additionalDataFlag    '0' = no additional data U-message
     * @param listAdditionalData    additional data to be sent to the GT
     * @param startWithCardPresent  '0' = card is not inserted at the time of the request.
     * @param paymentType           '0' = standard payment type (see Nexi doc)
     * @param textInput             testo da stampare (fino a 128 caratteri)
     * @param isReceiptOnECR        if set to true, the receipt is sent to the ECR
     * 
     * @throws java.lang.Exception
     * 
     */
    public void preAuthorization(String amountCents,
                                 String terminalId,
                                 String ecrId,
                                 char additionalDataFlag,
                                 List<MapAdditionalData>  listAdditionalData,
                                 char startWithCardPresent,
                                 char paymentType,
                                 String textInput,
                                 boolean isReceiptOnECR
                                ) throws Exception {
        
       
        byte[] additionalDataFrame = null;
        
        try {
        
            // Builds the complete binary frame for the Pre-authorization :
            // STX + BODY(167 char ASCII) + ETX + LRC
            byte[] request = buildPreauthorizationFrame(terminalId,
                                                        ECR_COMMAND_PREAUTHORIZATION,
                                                        ecrId,
                                                        additionalDataFlag,
                                                        startWithCardPresent,
                                                        paymentType,
                                                        amountCents,
                                                        textInput
                                                       );
            
            if (additionalDataFlag == FLAG_ADDITIONAL_DATA_ENABLED ) {
                
                additionalDataFrame = buildAdditionalDataFrame(terminalId, 
                                                          ECR_COMMAND_ADDITIONAL_DATA,
                                                          listAdditionalData
                                                         );
            }
            
            System.out.println(" >>> START PRE AUTHORIZATION - COMMAND : " + ECR_COMMAND_PREAUTHORIZATION);
            System.out.println(" >>> REQ HEX : ");
            System.out.println(toHex(request));

            System.out.println(" >>> REQ ASCII : ");
            System.out.println(getRawAscii(request));
            
            System.out.println();
            
            // Displays on screen the content of the request that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosPreAuthorizationRequest(request);
            
            // If set, displays on screen the content of the request for 
            // additional data that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosAdditionalDataRequest(additionalDataFrame);
            
            // Sends the request to the POS and returns the list of response messages 
            // issued by the POS
            List<byte[]> listframeFromPos = sendMessageToPos(posIP, 
                                                             posPort, 
                                                             terminalId,
                                                             request,
                                                             additionalDataFrame, 
                                                             isReceiptOnECR
                                                            );

            // Display on screen each response message received from the POS	
            logAllFramesFromPos(listframeFromPos);
            
        } catch (Exception ex ) {
            
            throw ex;
            
        } finally {
            
            System.out.println(" >>> END PRE AUTHORIZATION - COMMAND : " + ECR_COMMAND_PREAUTHORIZATION);
            
        }
            
    } // public void preAuthorization(String host
    
    
    /**
     * 
     * Executes the command Pre-authorization incremental 'i'
     * 
     * @param amountCents           123 -> 1,23 € espressi in cents
     * @param terminalId            "00000000" if all zeros, it is not verified; 8 digits
     * @param ecrId =               "00000000" ECR ID : if all zeros, it is not verified; 8 digits
     * @param additionalDataFlag    '0' = no additional data U-message
     * @param listAdditionalData    additional data to be sent to the GT
     * @param preAuthorizationCode  pre-authorization code to be used 
     * @param textInput             testo da stampare (fino a 128 caratteri)
     * @param isScontrinoSuECR      if set to true, the receipt is sent to the ECR
     * 
     * @throws java.lang.Exception
     */
    public void preAuthorizationIncremental(String amountCents,
                                            String terminalId,
                                            String ecrId,
                                            char additionalDataFlag,
                                            List<MapAdditionalData> listAdditionalData,
                                            String preAuthorizationCode,
                                            String textInput,
                                            boolean isScontrinoSuECR 
                                           ) throws Exception {
        
        byte[] additionalDataFrame = null;
        
        try {
        
            // Builds the complete binary frame for the Pre-authorization :
            // STX + BODY(167 char ASCII) + ETX + LRC
            byte[] request = buildPreauthorizationIncrementalFrame(terminalId,
                                                                   ECR_COMMAND_PREAUTHORIZATION_INCREMENTAL,
                                                                   ecrId,
                                                                   additionalDataFlag,
                                                                   amountCents,
                                                                   preAuthorizationCode,
                                                                   textInput
                                                                  );
            
            if (additionalDataFlag == FLAG_ADDITIONAL_DATA_ENABLED ) {
                
                additionalDataFrame = buildAdditionalDataFrame(terminalId, 
                                                               ECR_COMMAND_ADDITIONAL_DATA,
                                                               listAdditionalData
                                                              );
            }
            
            System.out.println(" >>> START PRE AUTHORIZATION INCREMENTAL - COMMAND : " + ECR_COMMAND_PREAUTHORIZATION_INCREMENTAL);
            System.out.println(">>> REQ HEX:");
            System.out.println(toHex(request));

            System.out.println(">>> REQ ASCII : ");
            System.out.println(getRawAscii(request));
            
            System.out.println();
            
            // Displays on screen the content of the request that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosPreAuthorizationIncrementalRequest(request);
            
            // If set, displays on screen the content of the request for 
            // additional data that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosAdditionalDataRequest(additionalDataFrame);
            
            // Sends the request to the POS and returns the list of response messages 
            // issued by the POS
            List<byte[]> listframeFromPos = sendMessageToPos(posIP, 
                                                             posPort, 
                                                             terminalId,
                                                             request, 
                                                             additionalDataFrame, 
                                                             isScontrinoSuECR
                                                            );
            
            // Display on screen each response message received from the POS	
            logAllFramesFromPos(listframeFromPos);
            
        } catch (Exception ex ) {
            
            throw ex;
            
        } finally {
            
            System.out.println(" >>> END PRE AUTHORIZATION INCREMENTAL - COMMAND : " + ECR_COMMAND_PREAUTHORIZATION_INCREMENTAL);
            
           
        }
            
    } // public void preAuthorizationIncremental(String host
    
    /**
     * 
     * Executes the command Pre-authorization closure 'c'
     * 
     * @param amountCents           123 -> 1,23 € in cents
     * @param terminalId            "00000000" if all zeros, it is not verified; 8 digits
     * @param ecrId                 "00000000" if all zeros, it is not verified; 8 digits
     * @param additionalDataFlag    '0' = no additional data U-message
     * @param listAdditionalData    additional data to be sent to the GT
     * @param preAuthorizationCode  pre-authorization code to be used for settling 
     *                              the payment transaction.
     * @param textInput             testo da stampare (fino a 128 caratteri)
     * @param isReceiptOnECR        if set to true, the receipt is sent to the ECR
     * 
     * @throws java.lang.Exception
     */
    public void preAuthorizationClosure(String amountCents,
                                        String terminalId,
                                        String ecrId,
                                        char additionalDataFlag,
                                        List<MapAdditionalData> listAdditionalData,
                                        String preAuthorizationCode,
                                        String textInput,
                                        boolean isReceiptOnECR
                                       ) throws Exception {
        

        byte[] additionalDataFrame = null;
        
        try {
             
            // Builds the complete binary frame for the Pre-authorization closure :
            // STX + BODY(167 char ASCII) + ETX + LRC
            byte[] request = buildPreauthorizationClosureFrame(terminalId,
                                                                   ECR_COMMAND_PREAUTHORIZATION_CLOSURE,
                                                                   ecrId,
                                                                   additionalDataFlag,
                                                                   amountCents,
                                                                   preAuthorizationCode,
                                                                   textInput
                                                                  );
            if (additionalDataFlag == FLAG_ADDITIONAL_DATA_ENABLED ) {
                
                additionalDataFrame = buildAdditionalDataFrame(terminalId, 
                                                               ECR_COMMAND_ADDITIONAL_DATA,
                                                               listAdditionalData
                                                              );
            }
            
            System.out.println(" >>> START PRE-AUTHORIZATION CLOSURE - COMMAND : " + ECR_COMMAND_PREAUTHORIZATION_CLOSURE);
            System.out.println("> >> REQ HEX : ");
            System.out.println(toHex(request));

            System.out.println("> >> REQ ASCII : ");
            System.out.println(getRawAscii(request));
            
            System.out.println();
            
            // Displays on screen the content of the request that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosPreAuthorizationClosureRequest(request);
            
            // If set, displays on screen the content of the request for 
            // additional data that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosAdditionalDataRequest(additionalDataFrame);
            
            // Sends the request to the POS and returns the list of response messages 
            // issued by the POS
            List<byte[]> listframeFromPos = sendMessageToPos(posIP, 
                                                                posPort, 
                                                                terminalId,
                                                                request, 
                                                                additionalDataFrame, 
                                                                isReceiptOnECR
                                                               );

            // Display on screen each response message received from the POS	
            logAllFramesFromPos(listframeFromPos);
            
        } catch (Exception ex ) {
            
            throw ex;
            
        } finally {
            
            System.out.println(" >>> END PRE AUTHORIZATION CLOSURE - COMMAND : " + ECR_COMMAND_PREAUTHORIZATION_CLOSURE);
             
        }
            
    } // public void preAuthorizationClosure(String host
    
    
     /**
     * 
     * @param terminalId            "00000000" if all zeros, it is not verified; 8 digits
     * @param ecrId                 "00000000" if all zeros, it is not verified; 8 digits
     * @param additionalDataFlag    '0' = no additional data U-message
     * @param listAdditionalData    additional data to be sent to the GT
     * @param isReceiptOnECR        if set to true, the receipt is sent to the ECR
     * 
     * @throws java.lang.Exception
     * 
     */
    public void closeSession(String terminalId,
                             String ecrId,
                             char additionalDataFlag,
                             List<MapAdditionalData> listAdditionalData,
                             boolean isReceiptOnECR
                            ) throws Exception {
        
        byte[] additionalDataFrame = null;
        
        try {
            
            // Builds the complete binary frame for the Pre-authorization closure :
            // STX + BODY(167 char ASCII) + ETX + LRC
            byte[] request = buildCloseSessionFrame(terminalId,
                                                    ECR_COMMAND_CLOSE_SESSION,
                                                    ecrId,
                                                    additionalDataFlag
                                                   );

            if (additionalDataFlag == FLAG_ADDITIONAL_DATA_ENABLED) {
                
                additionalDataFrame = buildAdditionalDataFrame(terminalId, 
                                                               ECR_COMMAND_ADDITIONAL_DATA,
                                                               listAdditionalData
                                                              );
            }
            
            System.out.println(" >>> START CLOSE SESSION - COMMAND : " + ECR_COMMAND_CLOSE_SESSION);
            System.out.println(" >>> REQ HEX : ");
            System.out.println(toHex(request));
            
            System.out.println(" >>> REQ ASCII : ");
            System.out.println(getRawAscii(request));
            
            System.out.println();
            
            // Displays on screen the content of the request that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosCloseSessionRequest(request);
            
            // If set, displays on screen the content of the request for 
            // additional data that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosAdditionalDataRequest(additionalDataFrame);
           
            // Sends the request to the POS and returns the list of response messages 
            // issued by the POS
            List<byte[]> listframeFromPos = sendMessageToPos(posIP, 
                                                             posPort, 
                                                             terminalId,
                                                             request, 
                                                             additionalDataFrame, 
                                                             isReceiptOnECR
                                                            );
            
            // Display on screen each response message received from the POS	
            logAllFramesFromPos(listframeFromPos);
        
        } catch (Exception ex) {
                
            throw ex;
            
        } finally {
            
            System.out.println(" >>> END CLOSE SESSION - COMMAND : " + ECR_COMMAND_CLOSE_SESSION);
            
          
        }
            
    } // public void closeSession
    
     /**
     * 
     * @param terminalId            "00000000" if all zeros, it is not verified; 8 digits
     * @param ecrId                 "00000000" if all zeros, it is not verified; 8 digits
     * @param additionalDataFlag    '0' = no additional data U-message
     * @param listAdditionalData    additional data to be sent to the GT
     * @param isReceiptOnECR        if set to true, the receipt is sent to the ECR
     * 
     * @throws java.lang.Exception
     * 
     */
    public void terminalTotals(String terminalId,
                               String ecrId,
                               char additionalDataFlag,
                               List<MapAdditionalData> listAdditionalData,
                               boolean isReceiptOnECR
                              ) throws Exception {
        
        byte[] additionalDataFrame = null;
        
        try {
       
            // Costruisce il frame binario completo per la richiesta reversal:
            // STX + BODY(167 char ASCII) + ETX + LRC
            byte[] request = buildTerminalTotalsFrame(terminalId,
                                                      ECR_COMMAND_TERMINAL_TOTALS,
                                                      ecrId,
                                                      additionalDataFlag
                                                     );

            if (additionalDataFlag == FLAG_ADDITIONAL_DATA_ENABLED ) {
                
                additionalDataFrame = buildAdditionalDataFrame(terminalId, 
                                                          ECR_COMMAND_ADDITIONAL_DATA,
                                                          listAdditionalData
                                                         );
            }
            
            System.out.println(" >>> START TERMINAL TOTALS - COMMAND : " + ECR_COMMAND_TERMINAL_TOTALS);
            System.out.println(" >>> REQ HEX : ");
            System.out.println(toHex(request));

            System.out.println(" >>> REQ ASCII : ");
            System.out.println(getRawAscii(request));
            
            System.out.println();
            
            // Displays on screen the content of the request that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosTerminalTotalsRequest(request);
            
            // If set, displays on screen the content of the request for 
            // additional data that will be sent to the POS
            LogEcrToPosFramesSample.logEcrToPosAdditionalDataRequest(additionalDataFrame);
            
            
            // Sends the request to the POS and returns the list of response messages 
            // issued by the POS
            List<byte[]> listframeFromPos = sendMessageToPos(posIP, 
                                                             posPort, 
                                                             terminalId,
                                                             request,
                                                             additionalDataFrame, 
                                                             isReceiptOnECR
                                                            );
            
            // Display on screen each response message received from the POS
            logAllFramesFromPos(listframeFromPos);
            
        } catch (Exception ex) {
            
            throw ex;
                           
        } finally {
            
            System.out.println(" >>> END TERMINAL TOTALS - COMMAND : " + ECR_COMMAND_TERMINAL_TOTALS);
        
        }
            
    } // public void terminalTotals
    
    
    // SEZIONE I/O
    
    /**
     * 
     * Example of TCP connection management between the ECR and the POS.
     * In this method, the connection is managed at the command level.
     * Consequently, it is opened when the command starts and closed when it finishes.
     * 
     * The method returns a list of frames received from the POS.
     * 
     * @param posIP                 pos IP
     * @param posPort               pos Port
     * @param terminalId            terminal pos identifier       
     * @param command               command to send to the POS
     * @param additionalData        additional data to be sent to the POS
     * @param isReceiptOnECR        flag indicating whether sending the 
     *                              receipt to the ECR is required.
     * 
     * @return                      list of frames returned by the POS
     * 
     * @throws Exception 
     * 
     */
    private List<byte[]> sendMessageToPos(String host, 
                                          int port, 
                                          String terminalId,
                                          byte[] command,
                                          byte[] additionalData,
                                          boolean isReceiptOnECR
                                         ) throws Exception{
        
        InputStream inputStream = null;
        OutputStream outputStream = null;
        Socket socket = null;
         
        List<byte[]> listFrame = null;
        
        System.out.println("\n");
        System.out.println("### START COMMUNICATION WITH THE POS");
        System.out.println("POS IP : " + host + " - POS IP : " + port + "\n");
        
        Thread.sleep(DELAY_SEND_COMMAND_TO_POS);
        
        try {
            
            socket = new Socket() ;
           
            socket.setSoLinger(false, 0);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);

            // Connection timeout
            socket.connect(new InetSocketAddress(host, port), TIMEOUT_CONNECTION_TO_POS);
            socket.setSoTimeout(TIMEOUT_LONG_DATA_FROM_POS); 
            
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            
            if (command == null) {
                System.out.println("Command empty");
                return null;
            }
            
            // Retrieves the command's message code.
            char messageCodeCommand = getMessageCode(command);
            
            // ===== START OF COMMAND SENDING AND ACK/NAK RECEIVING PHASE FROM POS =====
            
            // If sending the receipt to the ECR has been requested, it is necessary
            // to send the E1 command to the POS.
            if (isReceiptOnECR) {
                
                byte[] requestEnableScontrinoSuECR = buildEnableDisablePrintingReceiptFrame(terminalId,
                                                                                            ConstantSample.ECR_COMMAND_ENABLE_DISABLE_PRINTING_RECEIPT,
                                                                                            '1'
                                                                                           );
                
                System.out.println(" >>> SEND ENABLE PRINTING RECEIPT ON ECR");
                System.out.println(" >>> REQ HEX : ");
                System.out.println(toHex(requestEnableScontrinoSuECR));
            
                System.out.println(" >>> REQ ASCII : ");
                System.out.println(getRawAscii(requestEnableScontrinoSuECR));
                
                outputStream.write(requestEnableScontrinoSuECR);
                outputStream.flush();
                
                // Receives ACK/NAK from pos
                List<byte[]> listAckEnableTicketOnECR = receiveResponseFromPos(socket,inputStream, outputStream);
                
                if (listAckEnableTicketOnECR == null || listAckEnableTicketOnECR.isEmpty()) {
                    
                    System.out.println(" ACK NOT RECEIVED");
                    return listAckEnableTicketOnECR;
                        
                }
                
                // Retrieve ACK/NAK
                byte[] frameAckEnableTicketOnECR  = listAckEnableTicketOnECR.get(0);
                
                if (frameAckEnableTicketOnECR[0] == NAK) {

                    System.out.println(" <<< RECEIVED FROM POS -> NAK HEX : " + toHex(frameAckEnableTicketOnECR));
                    System.out.println(" <<< RECEIVED FROM POS -> NAK ASCII : " + getRawAscii(frameAckEnableTicketOnECR));
                    
                    return listAckEnableTicketOnECR;

                }
                
                if (!(frameAckEnableTicketOnECR[0] == ACK)) {

                    System.out.println(" received value is not ACK : " + toHex(frameAckEnableTicketOnECR));
                    return listAckEnableTicketOnECR;

                }

                // Received ACK from POS
                System.out.println(" <<< RECEIVED FROM POS -> ACK HEX : " + toHex(frameAckEnableTicketOnECR));
                System.out.println(" <<< RECEIVED FROM POS -> ACK ASCII : " + getRawAscii(frameAckEnableTicketOnECR) + "\n");
                
            } // if (isReceiptOnECR) {
            
            System.out.println(" >>> SEND COMMAND TO POS - " + "'" + messageCodeCommand + "'");
            
            // Send command to the pos
            outputStream.write(command);
            outputStream.flush();
            
            // Receives ACK/NAK
            List<byte[]> listAckComando = receiveResponseFromPos(socket,inputStream, outputStream);

            if (listAckComando == null || listAckComando.isEmpty()) {

                System.out.println(" ACK NOT RECEIVED");
                return listAckComando;

            }

            byte[] frameAckComando = listAckComando.get(0);

            if (frameAckComando[0] == NAK) {

                System.out.println(" <<< RECEIVED FROM POS -> NAK HEX : " + toHex(frameAckComando));
                System.out.println(" <<< RECEIVED FROM POS -> NAK ASCII : " + getRawAscii(frameAckComando));

                return listAckComando;

            }

            if (!(frameAckComando[0] == ACK)) {

                System.out.println(" The received value is not an ACK. : " + toHex(frameAckComando));
                return listAckComando;

            }

            System.out.println(" <<< RECEIVED FROM POS -> ACK HEX : " + toHex(frameAckComando));
            System.out.println(" <<< RECEIVED FROM POS -> ACK ASCII : " + getRawAscii(frameAckComando) + "\n");
            
            listFrame = new ArrayList<>();
            
            // If a frame for additional data has been compiled,
            // the frame is sent to the post
            if (additionalData != null) {
                
                System.out.println(" >>> SEND ADDTIONAL DATA TO POS");
                
                System.out.println(" >>> ADDITIONAL DATA TO POS -> HEX : " + toHex(additionalData));
                System.out.println(" >>> ADDITIONAL DATA TO POS -> ASCII : " + getRawAscii(additionalData));
                
                // Send additional data command to pos
                outputStream.write(additionalData);
                outputStream.flush();
                
                // Receives response from POS
                List<byte[]> listAckAdditionalData = receiveResponseFromPos(socket,inputStream, outputStream);

                if (listAckAdditionalData == null || listAckAdditionalData.isEmpty()) {

                    System.out.println(" ACK ADDITIONAL DATA NON RICEVUTO");
                    return listAckAdditionalData;

                }

                // Retrieves the ACK for the additional data command
                byte[] frameAckAdditionalData = listAckAdditionalData.get(0);

                if (frameAckAdditionalData[0] == NAK) {

                    System.out.println(" <<< RECEIVED FROM POS -> NAK HEX : " + toHex(frameAckAdditionalData));
                    System.out.println(" <<< RECEIVED FROM POS -> NAK ASCII : " + getRawAscii(frameAckAdditionalData));

                    return listAckAdditionalData;

                }

                if (!(frameAckAdditionalData[0] == ACK)) {

                    System.out.println("THE VALUE RECEIVED IS NOT AN ACK : " + toHex(frameAckAdditionalData));
                    return listAckAdditionalData;

                }
                
               // ACK received
                System.out.println(" <<< RECEIVED FROM POS -> ACK HEX : " + toHex(frameAckAdditionalData));
                System.out.println(" <<< RECEIVED FROM POS -> ACK ASCII : " + getRawAscii(frameAckAdditionalData) + "\n");
               
                
            } // if (additionalData != null) {
            
            // ===== START OF PHASE READING RESULTS OF COMMANDS SENT BY THE POS ====
            // At this stage, the ACKs for the sent commands have been received, so
            // we proceed to read the response messages sent by the POS
            
            // If the command sent to the POS is a Reprint Ticket ('R'),
            // and the flag for sending the receipt to the POS printer is enabled ('1'),
            // the POS sends no response message, so the method can be exited.
            
            // The method checks whether the command sent to the POS is of type 'R'; 
            // if so, it returns the activated flag.
            // If the command is not of type 'R', the method returns the
            // value 'x'.
            char flagReprintTicket = getFlagFromReprintTicketMessage(command);
            
            if (flagReprintTicket == ECR_COMMAND_REPRINT_TICKET_FLAG_ENABLE_ON_PRINTER) {
                
                return listFrame;
                
            }
            
            // The command sent to the POS is not a Reprint Ticket ('R') with the flag to send the receipt
            // to the POS printer.
            // Therefore, the outcome of the command received from the ECR is awaited from the POS.
            // Depending on the command sent by the ECR, the POS might respond with a 
            // sequence of messages.
                
            List<byte[]> listResponseDataFromPos = receiveResponseFromPos(socket, inputStream, outputStream);

            if (listResponseDataFromPos == null || listResponseDataFromPos.isEmpty()) {

                System.out.println("NO RESPONSE FROM THE POS");
                return listResponseDataFromPos;

            }

            // The POS has sent messages to the ECR, so
            // the list of messages is sanitized and returned
            // to the calling method.
            
            byte[] frameEsitoComando = null;
            
            for(byte[] frame : listResponseDataFromPos) {

                frameEsitoComando = sanitizeFrame(frame, frame.length);
                listFrame.add(frameEsitoComando) ;

            }
                
            // ===== END OF THE READING PHASE OF THE RESULTS OF THE COMMANDS SENT FROM THE POS ====
            
        } catch (SocketTimeoutException ex) {
            
            if (outputStream != null ) {
                
                sendAckToPos(outputStream, "Exception : " + ex.getMessage());
                
            }
             
            System.out.println("SocketTimeoutException : " + ex.getMessage());
             
        } catch (IOException ex) {
            
            if (outputStream != null ) {
                
                sendAckToPos(outputStream, "Exception : " + ex.getMessage());
                
            }
            
            System.out.println("IOException : " + ex.getMessage());
            
        } catch (Exception ex) {
            
            System.out.println("Exception : " + ex.getMessage());
            
        } finally {
            
            if (outputStream != null ) {
                
                outputStream.flush();
                outputStream.close();
                
            }
            
            if (inputStream != null ) {
                
                inputStream.close();
                
            }
           
            if (socket != null) {
                
                if (socket.isConnected()) {
                    
                    socket.close();
                    
                }
            }
            
        }
        
        int numeroFrames = 0;
        
        if (listFrame != null) {
            
            numeroFrames = listFrame.size();
                    
        }
        
        System.out.println(" <<< RETURN LIST FRAMES FROM POS, TOTAL FRAMES : " + numeroFrames + "\n");
        System.out.println("### CLOSING COMMUNICATION WITH THE POS\n");
        
        return listFrame;
        
    } // private byte[] sendMessageToPos
    
    /**
     * 
     * It retrieves the frames sent by the POS and adds them to a list..
     * 
     * @param socket
     * @param in
     * @param out
     * 
     * @return          list of frames received from the pos
     * 
     * @throws IOException 
     */
    private List<byte[]> receiveResponseFromPos(Socket socket, 
                                                InputStream in, 
                                                OutputStream out
                                               ) throws IOException {
        
        List<byte[]> allFrames = new ArrayList<>();
        boolean firstDataFrameReceived = false;
        
        // Save the original read timeout set for the socket
        int originalTimeout = socket.getSoTimeout();

        try {
            
            while (true) {
                
                try {
                    
                    byte[] frame = readSingleFrame(in);
                    
                    if (frame == null) break;

                    allFrames.add(frame);

                    if (frame.length < 3) {
                        
                        // The frame does not meet the required minimum length.
                        break; // Exits and returns the result to the caller
                        
                    }
                    
                    // --- ACK/NAK HANDLING ---
                    if (frame.length == 3 && (frame[0] == ACK || frame[0] == NAK)) {
                        // Received ACK or NAK
                        break; // Exits and returns the result to the caller
                    }
                    
                    char msgCode = getMessageCode(frame);
                    System.out.println(" <<< RECEIVED FRAME DATI FROM POS - '" + msgCode + "'");
                    
                    // --- DATA FRAME HANDLING (STX) ---
                    if (frame[0] == STX) {
                        
                        sendAckToPos(out, "ACK DATA FRAME");

                        // After receiving the first frame, the timeout is lowered so that
                        // subsequent frames are received without waiting for the standard read timeout.
                        if (!firstDataFrameReceived) {
                            
                            socket.setSoTimeout(TIMEOUT_SHORT_DATA_FROM_POS); // e.g.. 2000ms
                            
                            firstDataFrameReceived = true;
                            
                             System.out.println(" >>> SET SHORT TIMEOUT FOR MULTI-FRAME");
                            
                        }
                        
                        if (msgCode == 'S' && containsEndOfTicket(frame)) {
                            break; // End receipt
                        }
                        // For 'E' or 'U' messages, the loop restarts: the timeout is now the reduced one.
                        // If nothing arrives within x seconds, the exception below is triggered.
                        
                    } //  if (frame[0] == STX) {
                    
                } catch (SocketTimeoutException e) {
                    
                    if (!firstDataFrameReceived) {
                        // It's a LONG timeout: the user has not swiped the card on the POS
                        throw e; 
                        
                    } else {
                        // It's the SHORT timeout: the burst of frames has ended.
                        break; 
                    }
                    
                } catch (Exception ex ){
                    
                    System.out.println("Generata Excpetion : " + ex.getMessage());
                    break;
                }
                
            } // while (true) {
            
        } finally {
            
            // RESTORE NORMAL READ TIMEOUT (THE LONG VALUE)
            socket.setSoTimeout(originalTimeout);
        }

        return allFrames;
        
    } // public List<byte[]> receiveResponseFromPos(InputStream in, OutputStream out) throws IOException {
    
    private byte[] readSingleFrame(InputStream in) throws IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
        // Read the first byte (could be STX, ACK, or NAK)
        int b = in.read();
        
        if (b == -1) return null;

        baos.write(b);

        // CASE 1: ACK (0x06) or NAK (0x15) -> Always followed by 2 bytes (ETX + LRC)
        if (b == ACK || b == NAK) {
            
            for (int i = 0; i < 2; i++) {
                
                int next = in.read();
                
                if (next != -1) {
                    
                    baos.write(next);
                    
                }
                
            }
            
            // The complete frame has been read.
            return baos.toByteArray();
            
        } // if (b == 0x06 || b == 0x15) {

        // CASE 2: STANDARD FRAME (Starts with STX 0x02)
        if (b == STX) {
            
            int next;
            
            while ((next = in.read()) != -1) {
                
                baos.write(next);
                
                if (next == ETX) { // ETX trovato
                    
                   // Read the last mandatory byte (LRC)
                    int lrc = in.read();
                    if (lrc != -1) {
                        baos.write(lrc);
                    }
                    
                    // The complete frame has been read.
                    return baos.toByteArray();
                }
                
            } // while ((next = in.read()) != -1) {
            
        } // if (b == STX) {

        // If we reach this point, we have received an unexpected byte (line noise).
        return baos.toByteArray();
        
    } // private byte[] readSingleFrame(InputStream in) throws IOException {
    
} // end class
