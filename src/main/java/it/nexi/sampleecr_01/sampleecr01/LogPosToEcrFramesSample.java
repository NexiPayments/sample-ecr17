package it.nexi.sampleecr_01.sampleecr01;

import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.TRANSACTION_RESULT_NEGATIVE;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.TRANSACTION_RESULT_POSIVITE;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.getMessageCode;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.getPayloadLength;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.getRawAscii;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.getTransactionResult;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.normalize;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.toHex;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.validateLrc;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 *
 * Nexi Payments
 * 
 * Contains logs of frames sent by the POS
 * 
 */
public class LogPosToEcrFramesSample {
    
    // defines the log format
    private static final String FMT = "%-9s %-22s: %s";
    
     /**
     * 
     * Parses frames received from the POS
     * 
     * @param listframeFromPos 
     * 
     */
    public static void logAllFramesFromPos(List<byte[]> listframeFromPos){
        
        System.out.println(" <<< PARSING FRAME RECEIVED FROM POS");
        System.out.println();
        
        if (listframeFromPos == null || listframeFromPos.isEmpty()){
            
            System.out.println(" <<< LIST FRAMES FROM POS EMPTY");
            return;
            
        }
        
        for (byte[] frame : listframeFromPos){

            char messageCode = getMessageCode(frame);
            
            System.out.println(" <<< START FRAME : " + messageCode);
            System.out.println(toHex(frame));
            System.out.println(getRawAscii(frame));
            
            // LRC verification according to Nexi specifications:
            // - initial base 0x7F
            // - XOR of all bytes from STX (inclusive) to ETX (inclusive)
            // - The result must match the final LRC byte.
            boolean lrcOk = validateLrc(frame);

            System.out.println(" <<< LRC OK? " + lrcOk);

            // Parsing the first frame, which should contain
            // the transaction result
            if (frame == null || frame.length < 4) {

                System.out.println("Frame too short for parsing.");

            } else {

                  switch (messageCode) {

                    case 'C' :
                        
                        logPosToEcrCloseSessionResponse(frame);
                        break;
                        
                    case 'c' :
                        
                        logPosToEcrPreauthorizationClosureResponse(frame);
                        break;
                        
                    case 'E':
                        
                        // In some implementations, the payload length varies
                        // depending on the command sent
                        
                        int lengthPayload = getPayloadLength(frame);
                        
                        switch (lengthPayload) {
                             
                            case 71:
                                
                                 logPosToEcrPaymentTransactionResponsePayload71(frame);
                                 break;
                            
                            case 75:
                                
                                //logPosToEcrPaymentTransactionResponsePayload75(frame);
                                break;
                            
                            case 83:
                                
                                //logPosToEcrPaymentTransactionResponsePayload83(frame);
                                break;
                                
                            case 84:
                                
                                logPosToEcrReversalTransactionResponse(frame);
                                break;
                                
                            case 92:
                                
                                logPosToEcrPaymentExtendedTransactionResponse(frame);
                                break;
                            
                            default :
                                
                                 System.out.println("UNABLE TO FIND MSG PARSER FOR POS 'E' AND LENGTH : " + lengthPayload);
                                 
                        }
                        
                        break;
                    
                    case 'e':
                        
                        logPosToEcrPreauthorizationResponse(frame);
                        break;    
                    
                    case 'i':
                        
                        logPosToEcrPreauthorizationIncrementatlResponse(frame);
                        break;    
                        
                    case 'S':
                        
                        logPosToEcrReceipt(frame);
                        break;
                    
                    case 's':
                        
                        logPosToEcrTerminalStatus(frame);
                        break;   
                    
                    case 'T':
                        
                        logPosToEcrTerminalTotalsResult(frame);
                        break;       
                        
                    case 'U':
                        
                        logPosToEcrAdditionalDataResponse(frame);
                        break;    

                    default:

                        System.out.println(" <<< UNHANDLED MESSAGE CODE: " + messageCode);

                } // switch

                System.out.println(" <<<< END FRAME " + messageCode);  
                System.out.println();
                  
            } // if (frame == null || frame.length < 4) {

        }// for (byte[] frame : listframeFromPos){
        
    } // private void logAllFramesFromPos(List<byte[]> listframeFromPos){
    
    /**
     * 
     * Parses the frame received from the POS.
     * 
     * @param frameFromPos 
     * 
     */
    public static void logPosToEcrPaymentExtendedTransactionResponse(byte[] frameFromPos){
       
        if (frameFromPos == null || frameFromPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameFromPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("POS RESPONSE [RESPONSE PAYMENT EXTENDED FIELD ANALYSIS]");
        System.out.println("[LENGTH PAYLOAD [WITHOUT STX, ETX, LRC] : " + payload.length());
        
        // Initial part of the response, the same for everyone - START
        // [1..8] TID: pos  → substring(0, 8)
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved ", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT, "[10]", "Message code", msgCode));

        // [11-12] Transaction result: pos 11..12 → substring(10, 12)
        String transactionResultCode = getTransactionResult(payload);
        System.out.println(String.format(FMT,"[11-12]", "Transaction result",transactionResultCode));
        
        // Initial part of the response, the same for everyone - END
        
        if (transactionResultCode.equalsIgnoreCase(TRANSACTION_RESULT_POSIVITE)) {
                
            // Section for Transaction result = "00" - START
            // [13-31] pan
            String pan = len >= 31 ? payload.substring(12, 31) : "";
            System.out.println(String.format(FMT,"[13-31]", "pan", pan));

            // [32-34] transaction type : 
            String transactionType = len >= 34 ? payload.substring(31, 34) : "";
            System.out.println(String.format(FMT,"[32-34]", "transactionType", transactionType));

            // [35-40] transaction type : 
            String authorizationCode = len >= 40 ? payload.substring(34, 40) : "";
            System.out.println(String.format(FMT,"[35-40]", "authorizationCode", authorizationCode));

            // [41-47] transaction type : 
            String transactionDateTime = len >= 47 ? payload.substring(40, 47) : "";
            System.out.println(String.format(FMT,"[41-47]", "transactionDateTime", transactionDateTime));

            // Section for Transaction result = "00" - END
            
        } else if (transactionResultCode.equalsIgnoreCase(TRANSACTION_RESULT_NEGATIVE)){
            
            // Section for Transaction result = "01" - START
        
            // [13-36] resultDescription : 
            String resultDescription = len >= 36 ? payload.substring(12, 36) : "";
            System.out.println(String.format(FMT,"[13-36]", "Result description", resultDescription));

            // [37-47] Reserved : 
            String reserved_37_47 = len >= 47 ? payload.substring(36, 47) : "";
            System.out.println(String.format(FMT,"[37-47]", "Reserved", reserved_37_47));

            // Section for Transaction result = "01" - END
        }
       
        // common to all responses - START
        
        // [48] cardType : 
        char cardType = len >= 48 ? payload.charAt(47) : '?';
        System.out.println(String.format(FMT,"[48]", "cardType", cardType));
        
        // [49-59] acquirerID : 
        String acquirerID = len >= 59 ? payload.substring(48, 59) : "";
        System.out.println(String.format(FMT,"[49-59]", "acquirerID", acquirerID));
        
        // [60-65] stan : 
        String stan = len >= 65 ? payload.substring(59, 65) : "";
        System.out.println(String.format(FMT,"[60-65]", "stan", stan));
        
        // [66-71] ID online : 
        String idOnline = len >= 71 ? payload.substring(65, 71) : "";
        System.out.println(String.format(FMT,"[66-71]", "idOnline", idOnline));
        
        // [72-74] actionCode : 
        String actionCode = len >= 74 ? payload.substring(71, 74) : "";
        System.out.println(String.format(FMT,"[72-74]", "actionCode", actionCode));
        
        // [75-82] transaction amount : 
        String transactionAmount = len >= 82 ? payload.substring(74, 82) : "";
        System.out.println(String.format(FMT,"[75-82]", "transactionAmount", transactionAmount));
        
        // [83-92] reserved fixed to "0" : 
        String reserved_83_92 = len >= 92 ? payload.substring(83, 92) : "";
        System.out.println(String.format(FMT,"[83-92]", "reserved", reserved_83_92));
        
        // common to all responses - END
        
        System.out.println("END OF ANALYSIS: EXTENDED PAYMENT RESPONSE FIELDS");
        
    } // logPaymentExtendedTransactionResult
    
     /**
     * 
     * Parses the frame received from the POS.
     * 
     * @param frameFromPos 
     * 
     */
    public static void logPosToEcrPaymentTransactionResponsePayload71(byte[] frameFromPos){
       
        if (frameFromPos == null || frameFromPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameFromPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("POS RESPONSE [PAYMENT FIELD ANALYSIS]");
        System.out.println("[PAYLOAD LENGTH [WITHOUT STX, ETX, LRC] : " + payload.length());
        
        // Initial part of the response, the same for everyone - START
        // [1..8] TID: pos  → substring(0, 8)
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "Message code", msgCode));

        // [11-12] Transaction result: pos 11..12 → substring(10, 12)
        String transactionResultCode = getTransactionResult(payload);
        System.out.println(String.format(FMT,"[11-12]", "Transaction result",transactionResultCode));
        
        // Initial part of the response, the same for everyone - END
        
        if (transactionResultCode.equalsIgnoreCase(TRANSACTION_RESULT_POSIVITE)) {
                
            // Section for Transaction result = "00" - START
            // [13-31] pan
            String pan = len >= 31 ? payload.substring(12, 31) : "";
            System.out.println(String.format(FMT,"[13-31]", "pan", pan));

            // [32-34] transaction type : 
            String transactionType = len >= 34 ? payload.substring(31, 34) : "";
            System.out.println(String.format(FMT,"[32-34]", "transactionType", transactionType));

            // [35-40] transaction type : 
            String authorizationCode = len >= 40 ? payload.substring(34, 40) : "";
            System.out.println(String.format(FMT,"[35-40]", "authorizationCode", authorizationCode));

            // [41-47] transaction type : 
            String transactionDateTime = len >= 47 ? payload.substring(40, 47) : "";
            System.out.println(String.format(FMT,"[41-47]", "transactionDateTime", transactionDateTime));

            // Section for Transaction result = "00" - END
            
        } else if (transactionResultCode.equalsIgnoreCase(TRANSACTION_RESULT_NEGATIVE)){
            
            // Section for Transaction result = "01" - START
        
            // [13-36] result Description : 
            String resultDescription = len >= 36 ? payload.substring(12, 36) : "";
            System.out.println(String.format(FMT,"[13-36]", "Result description", resultDescription));

            // [37-47] Reserved : 
            String reserved_37_47 = len >= 47 ? payload.substring(36, 47) : "";
            System.out.println(String.format(FMT,"[37-47]", "Reserved", reserved_37_47));

            // Section for Transaction result = "01" - END
        }
       
        // common to all responses - START
        
        // [48] cardType : 
        char cardType = len >= 48 ? payload.charAt(47) : '?';
        System.out.println(String.format(FMT,"[48]", "cardType",cardType));
        
        // [49-59] acquirerID : 
        String acquirerID = len >= 59 ? payload.substring(48, 59) : "";
        System.out.println(String.format(FMT,"[49-59]", "acquirerID", acquirerID));
        
        // [60-65] stan : 
        String stan = len >= 65 ? payload.substring(59, 65) : "";
        System.out.println(String.format(FMT,"[60-65]", "stan", stan));
        
        // [66-71] ID online : 
        String idOnline = len >= 71 ? payload.substring(65, 71) : "";
        System.out.println(String.format(FMT,"[66-71]", "idOnline", idOnline));
        
        // common to all responses - END
        
        System.out.println("END OF ANALYSIS: PAYMENT RESPONSE FIELDS");
        
    } // logPosToEcrPaymentTransactionResponsePayload71
    
     /**
     * 
     * Parses the frame received from the POS.
     * 
     * @param frameFromPos 
     * 
     */
    public static void logPosToEcrCardVerification(byte[] frameFromPos){
       
        if (frameFromPos == null || frameFromPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameFromPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("POS RESPONSE [CARD VERIFICATION FIELD ANALYSIS]");
        System.out.println("[PAYLOAD LENGTH [WITHOUT STX, ETX, LRC] : " + payload.length());
        
        // Initial part of the response, the same for everyone - START
        // [1..8] TID: pos  → substring(0, 8)
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "Message code", msgCode));

        // [11-12] Transaction result: pos 11..12 → substring(10, 12)
        String transactionResultCode = getTransactionResult(payload);
        System.out.println(String.format(FMT,"[11-12]", "Transaction result",transactionResultCode));
        
        // Initial part of the response, the same for everyone - END
        
        if (transactionResultCode.equalsIgnoreCase(TRANSACTION_RESULT_POSIVITE)) {
                
            // Section for Transaction result = "00" - START
            // [13-31] pan
            String pan = len >= 31 ? payload.substring(12, 31) : "";
            System.out.println(String.format(FMT,"[13-31]", "pan", pan));

            // [32-34] transaction type : 
            String transactionType = len >= 34 ? payload.substring(31, 34) : "";
            System.out.println(String.format(FMT,"[32-34]", "transactionType", transactionType));

            // [35-40] transaction type : 
            String authorizationCode = len >= 40 ? payload.substring(34, 40) : "";
            System.out.println(String.format(FMT,"[35-40]", "authorizationCode", authorizationCode));

            // [41-47] transaction type : 
            String transactionDateTime = len >= 47 ? payload.substring(40, 47) : "";
            System.out.println(String.format(FMT,"[41-47]", "transactionDateTime", transactionDateTime));

            // Section for Transaction result = "00" - END
            
        } else if (transactionResultCode.equalsIgnoreCase(TRANSACTION_RESULT_NEGATIVE)){
            
            // Section for Transaction result = "01" - START
           
            // [13-36] resultDescription : 
            String resultDescription = len >= 36 ? payload.substring(12, 36) : "";
            System.out.println(String.format(FMT,"[13-36]", "Result description", resultDescription));
            
            // [37-39] actionCode : 
            String actionCode = len >= 39 ? payload.substring(37, 39) : "";
            System.out.println(String.format(FMT,"[37-39]", "action code", actionCode));

            // [40-71] Reserved '0': 
            String reserved_40_71 = len >= 71 ? payload.substring(40, 71) : "";
            System.out.println(String.format(FMT,"[40-71]", "Reserved", reserved_40_71));
            
            // Section for Transaction result = "01" - END
        }
       
        // common to all responses - START
        
        // [48] cardType : 
        char cardType = len >= 48 ? payload.charAt(47) : '?';
        System.out.println(String.format(FMT,"[48]", "cardType",cardType));
        
        // [49-59] acquirerID : 
        String acquirerID = len >= 59 ? payload.substring(48, 59) : "";
        System.out.println(String.format(FMT,"[49-59]", "acquirerID", acquirerID));
        
        // [60-65] stan : 
        String stan = len >= 65 ? payload.substring(59, 65) : "";
        System.out.println(String.format(FMT,"[60-65]", "stan", stan));
        
        // [66-71] ID online : 
        String idOnline = len >= 71 ? payload.substring(65, 71) : "";
        System.out.println(String.format(FMT,"[66-71]", "idOnline", idOnline));
        
        // common to all responses - END
        
        System.out.println("END OF ANALYSIS: PAYMENT RESPONSE FIELDS");
        
    } // logPosToEcrCardVerification
    
    
    /**
     * 
     * Parses the frame received from the POS.
     * 
     * @param frameFromPos 
     * 
     */
    public static void logPosToEcrReversalTransactionResponse(byte[] frameFromPos){
       
        if (frameFromPos == null || frameFromPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameFromPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("POS RESPONSE [RESPONSE REVERSAL FIELD ANALYSIS]");
        System.out.println("[LENGTH PAYLOAD [WITHOUT STX, ETX, LRC] : " + payload.length());
        
        // Initial part of the response, the same for everyone - START
        // [1..8] TID: pos  → substring(0, 8) 
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID",tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "Message code", msgCode));

        // [11-12] Transaction result: pos 11..12 → substring(10, 12)
        String transactionResultCode = getTransactionResult(payload);
        System.out.println(String.format(FMT,"[11-12]", "Transaction result", transactionResultCode));
        
        // Initial part of the response, the same for everyone - END
        
        if (transactionResultCode.equalsIgnoreCase(TRANSACTION_RESULT_POSIVITE)) {
                
            // Section for Transaction result = "00" - START
            // [13-31] pan
            String pan = len >= 31 ? payload.substring(12, 31) : "";
            System.out.println(String.format(FMT,"[13-31]", "pan", pan));

            // [32-34] transaction type : 
            String transactionType = len >= 34 ? payload.substring(31, 34) : "";
            System.out.println(String.format(FMT,"[32-34]", "transactionType", transactionType));

            // [35-40] transaction type : 
            String authorizationCode = len >= 40 ? payload.substring(34, 40) : "";
            System.out.println(String.format(FMT,"[35-40]", "authorizationCode", authorizationCode));

            // [41-47] transaction date time : 
            String transactionDateTime = len >= 47 ? payload.substring(40, 47) : "";
            System.out.println(String.format(FMT,"[41-47]", "transactionDateTime", transactionDateTime));

            // Section for Transaction result = "00" - END
            
        } else if (transactionResultCode.equalsIgnoreCase(TRANSACTION_RESULT_NEGATIVE)){
            
            // Section for Transaction result = "01" - START
        
            // [13-36] resultDescription : 
            String resultDescription = len >= 36 ? payload.substring(12, 36) : "";
            System.out.println(String.format(FMT,"[13-36]", "Result description", resultDescription));

            // [37-47] Reserved : 
            String reserved_37_47 = len >= 47 ? payload.substring(36, 47) : "";
            System.out.println(String.format(FMT,"[37-47]", "Reserved", reserved_37_47));

            // Section for Transaction result = "01" - END
        }
       
        // common to all responses - START
        
        // [48] cardType : 
        char cardType = len >= 47 ? payload.charAt(47) : '?';
        System.out.println(String.format(FMT,"[48]", "cardType", cardType));
        
        // [49-59] acquirerID : 
        String acquirerID = len >= 59 ? payload.substring(48, 59) : "";
        System.out.println(String.format(FMT,"[49-59]", "acquirerID", acquirerID));
        
        // [60-65] stan : 
        String stan = len >= 65 ? payload.substring(59, 65) : "";
        System.out.println(String.format(FMT,"[60-65]", "stan", stan));
        
        // [66-71] ID online : 
        String idOnline = len >= 71 ? payload.substring(65, 71) : "";
        System.out.println(String.format(FMT,"[66-71]", "idOnline", idOnline));
        
        // [72-74] actionCode : 
        String actionCode = len >= 74 ? payload.substring(71, 74) : "";
        System.out.println(String.format(FMT,"[72-74]", "actionCode", actionCode));
        
        // [75-84] resersved '0' : 
        String reserved_75_84 = len >= 84 ? payload.substring(74, 84) : "";
        System.out.println(String.format(FMT,"[75-84]", "reserved ", reserved_75_84));
        
        // common to all responses - END
        
        System.out.println("END [RESPONSE REVERSAL FIELD ANALYSIS]");
        
    } // logReversalTransactionResult
    
    /**
     * 
     * Parses the frame received from the POS.
     * 
     * @param frameFromPos 
     * 
     */
    public static void logPosToEcrTerminalStatus(byte[] frameFromPos){
       
        if (frameFromPos == null || frameFromPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameFromPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("POS RESPONSE [TERMINAL STATUS FIELD ANALYSIS]");
        System.out.println("[LENTGH PAYLOAD [WITHOUT STX, ETX, LRC] : " + payload.length());
        
        // [1..8] TID: pos  → substring(0, 8) 
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "Message code", msgCode));

        // [11-20] Reserved
        String reserved_11_20 = len >= 20 ? payload.substring(10, 20) : "";
        System.out.println(String.format(FMT,"[11-20]", "Reserved '0'", reserved_11_20));
        
        // [21-30] Terminal date and time "DDMMYYhhmm"
        String terminalDataAndTime = len >= 30 ? payload.substring(20, 30) : "";
        System.out.println(String.format(FMT,"[21-30]", "Terminal date and time (DDMMYYhhmm)", terminalDataAndTime));
        
        // [31] Terminal status"
        char terminalStatus = len >= 30 ? payload.charAt(30) : '?';
        System.out.println(String.format(FMT,"[31]", "Terminal status" , terminalStatus));
       
        // [32-N*8] Terminal status"
        String softwareRelease = len >= 40 ? payload.substring(31, 40) : "";
        System.out.println(String.format(FMT,"[32-N*8]", "Software release", softwareRelease));
        
        System.out.println("END [TERMINAL STATUS] FIELDS ANALISYS");
        
    } // logTerminalStatus 
    
    /**
     * 
     * Parses the frame received from the POS.
     * 
     * @param frameFromPos 
     * 
     */
    public static void logPosToEcrAdditionalDataResponse(byte[] frameFromPos) {

        if (frameFromPos == null || frameFromPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameFromPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("POS RESPONSE [ADDITIONAL DATA FIELD ANALYSIS]");
        System.out.println("[LENTGH PAYLOAD [WITHOUT STX, ETX, LRC] : " + payload.length());
       
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        char reserved_09 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "RESERVED", reserved_09));
        
        char messageCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "MESSAGE CODE", messageCode));
        
        String reserved_11_16 = len >= 16 ? payload.substring(11, 16) : "";
        System.out.println(String.format(FMT,"[11-16]", "RESERVED", reserved_11_16));

        String strLengthData = len >= 19 ? payload.substring(16, 19) : "";
        System.out.println(String.format(FMT,"[17-19]", "DATA LENGTH", strLengthData));
        
        String data = "";
        
        if (len >= 20) {
            
            data = payload.substring(20);
            
        }
        
        System.out.println("[20-V] DATA : " + data);
        
        System.out.println("END [ADDITIONAL DATA] FIELDS ANALISYS");
       
    } // public void logAdditionalDataResponse(byte[] frame) 
    
    /**
     * 
     * Parses the frame received from the POS.
     * 
     * @param frameFromPos 
     * 
     */
    public static void logPosToEcrReceipt(byte[] frameFromPos){
       
        if (frameFromPos == null || frameFromPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameFromPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        
        System.out.println("POS RESPONSE [ANALISI CAMPI RECEIPT FROM POS FIELDS ANALYSIS]");
        System.out.println("[LENGTH PAYLOAD [WITHOUD STX, ETX, LRC] : " + payload.length());
        
        ECRTicketParserSample parser = new ECRTicketParserSample();
        List<ECRTicketParserSample.TicketLine> lines = parser.processFrame(frameFromPos);

        for (ECRTicketParserSample.TicketLine line : lines) {

            // Prepare the prefix for bold text (optional)
            String prefix = line.isBold ? "[B] " : "    "; 

            // Line composition.
            // NOTE: We do not use .trim() here, because the POS uses leading 
            // spaces to center the text (e.g., "      RECEIPT      ")
            String logEntry = prefix + line.text;
           
            System.out.println(logEntry); 
        }

        System.out.println("END [RECEIPT FROM POS FIELD ANALYSIS]");
        
    } // logPosToEcrReceipt
    
    /**
     * 
     * Parses the frame received from the POS.
     * 
     * @param frameFromPos 
     * 
     */
    public static void logPosToEcrPreauthorizationResponse(byte[] frameFromPos){
       
        if (frameFromPos == null || frameFromPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameFromPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("POS RESPONSE [RESPONSE PRE-AUTHORIZATION FIELDS ANALYSIS]");
        System.out.println("[PAYLOAD LENGTH [WITHOUT STX, ETX, LRC] : " + payload.length());
        
        // Initial part of the response, the same for everyone - START
        // [1..8] TID: pos  → substring(0, 8) in Java
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "Message code", msgCode));

        // [11-12] Transaction result: pos 11..12 → substring(10, 12)
        String transactionResultCode = getTransactionResult(payload);
        System.out.println(String.format(FMT,"[11-12]", "Transaction result", transactionResultCode));
        
        // Initial part of the response, the same for everyone - END
        
        if (transactionResultCode.equalsIgnoreCase(TRANSACTION_RESULT_POSIVITE)) {
                
            // Section for Transaction result = "00" - START
            // [13-31] pan
            String pan = len >= 31 ? payload.substring(12, 31) : "";
            System.out.println(String.format(FMT,"[13-31]", "pan", pan));

            // [32-34] transaction type : 
            String transactionType = len >= 34 ? payload.substring(31, 34) : "";
            System.out.println(String.format(FMT,"[32-34]", "transactionType", transactionType));

            // [35-40] transaction type : 
            String authorizationCode = len >= 40 ? payload.substring(34, 40) : "";
            System.out.println(String.format(FMT,"[35-40]", "authorizationCode", authorizationCode));
            
            // [41-48] amount pre authorized by host: 
            String amountPreauthorizedByHost = len >= 47 ? payload.substring(40, 47) : "";
            System.out.println(String.format(FMT,"[41-47]", "amount pre auth by host", amountPreauthorizedByHost));
            
            // [49-57] unique identifier  pre authorized by host: 
            String uniqueIdentifier = len >= 57 ? payload.substring(48, 57) : "";
            System.out.println(String.format(FMT,"[49-57]", "unique identifier by host (PRE-AUTH CODE)", uniqueIdentifier));
            
            // [58-60] action code : 
            String actionCode = len >= 60 ? payload.substring(58, 60) : "";
            System.out.println(String.format(FMT,"[58-60]", "action code", actionCode));
            
            // [61-67] date transaction DDDHHMM format : 
            String dateTransaction = len >= 67 ? payload.substring(60, 67) : "";
            System.out.println(String.format(FMT,"[61-67]", "date transaction", dateTransaction));

            // [68-70] date transaction DDDHHMM format : 
            String reserved_68_70 = len >= 70 ? payload.substring(68, 70) : "";
            System.out.println(String.format(FMT,"[68-70]", "reserved", reserved_68_70));
            
            // Section for Transaction result = "00" - END
            
        } else if (transactionResultCode.equalsIgnoreCase(TRANSACTION_RESULT_NEGATIVE)){
            
            // Section for Transaction result = "01" - START
        
            // [13-36] resultDescription : 
            String resultDescription = len >= 36 ? payload.substring(12, 36) : "";
            System.out.println(String.format(FMT,"[13-36]", "Result description", resultDescription));
            
            // [37-39] actionCode : 
            String actionCode = len >= 39 ? payload.substring(37, 39) : "";
            System.out.println(String.format(FMT,"[37-39]", "action code", actionCode));

             // [40-71] Reserved '0': 
            String reserved_40_71 = len >= 71 ? payload.substring(40, 71) : "";
            System.out.println(String.format(FMT,"[40-71]", "Reserved", reserved_40_71));

            // Section for Transaction result = "01" - END
            
        } // else if (transactionResultCode.equalsIgnoreCase(TRANSACTION_RESULT_NEGATIVE))
       
        // common to all responses - START
        
        // [48] cardType : 
        char cardType = len >= 47 ? payload.charAt(47) : '?';
        System.out.println(String.format(FMT,"[48]", "cardType", cardType));
        
        // [72-82] acquirerID : 
        String acquirerID = len >= 82 ? payload.substring(71, 82) : "";
        System.out.println(String.format(FMT,"[72-82]", "acquirerID", acquirerID));
        
        // [83-88] stan : 
        String stan = len >= 88 ? payload.substring(82, 88) : "";
        System.out.println(String.format(FMT,"[83-88]", "stan", stan));
        
        // [89-94] ID online : 
        String idOnline = len >= 94 ? payload.substring(88, 94) : "";
        System.out.println(String.format(FMT,"[89-94]", "idOnline", idOnline));
        
        // [95-106] Reserved : 
        String reserved_95_106 = len >= 106 ? payload.substring(94, 106) : "";
        System.out.println(String.format(FMT,"[95-106]", "Reserved", reserved_95_106));
        
        // common to all responses - START
        
        System.out.println("END OF ANALYSIS: PRE-AUTHORIZATION RESPONSE FIELDS");
        
    } // logPosToEcrPreauthorizationResponse
    
     /**
     * 
     * Parses the frame received from the POS.
     * 
     * @param frameFromPos 
     * 
     */
    public static void logPosToEcrPreauthorizationIncrementatlResponse(byte[] frameFromPos){
       
        if (frameFromPos == null || frameFromPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameFromPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("POS RESPONSE [RESPONSE PRE-AUTHORIZATION INCREMENTAL FIELDS ANALYSIS]");
        System.out.println("[PAYLOAD LENGTH [WITHOUT STX, ETX, LRC] : " + payload.length());
        
        // [1..8] TID: pos  → substring(0, 8) 
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "Message code", msgCode));

        // [11-12] Transaction result: pos 11..12 → substring(10, 12)
        String transactionResultCode = getTransactionResult(payload);
        System.out.println(String.format(FMT,"[11-12]", "Transaction result", transactionResultCode));
                
        // [13-31] pan
        String pan = len >= 31 ? payload.substring(12, 31) : "";
        System.out.println(String.format(FMT,"[13-31]", "pan", pan));

        // [32-34] transaction type : 
        String transactionType = len >= 34 ? payload.substring(31, 34) : "";
        System.out.println(String.format(FMT,"[32-34]", "transactionType", transactionType));
      
        // [35-45] acquirerID : 
        String acquirerID = len >= 45 ? payload.substring(34, 45) : "";
        System.out.println(String.format(FMT,"[35-45]", "acquirerID", acquirerID));

        // [46-51] authorization code : 
        String authorizaationCode = len >= 51 ? payload.substring(45, 51) : "";
        System.out.println(String.format(FMT,"[46-51]", "authorizaation Code", authorizaationCode));
       
        // [52-57] stan : 
        String stan = len >= 57 ? payload.substring(51, 57) : "";
        System.out.println(String.format(FMT,"[52-57]", "stan", stan)); 
        
        // [58-63] ID online : 
        String idOnline = len >= 63 ? payload.substring(57, 63) : "";
        System.out.println(String.format(FMT,"[58-63]", "idOnline", idOnline)); 
        
        // [64-70] transaction date time DDDHHMM : 
        String transactionDateTime = len >= 70 ? payload.substring(63, 70) : "";
        System.out.println(String.format(FMT,"[64-70]", "Transaction date time DDDHHMM", transactionDateTime));
        
        // [71-73] 
        String actionCode  = len >= 73 ? payload.substring(70, 73) : "";
        System.out.println(String.format(FMT,"[71-73]", "actionCode", actionCode));
        
        // [74-75] reserved 0 : 
        String reserved_74_75 = len >= 75 ? payload.substring(73, 75) : "";
        System.out.println(String.format(FMT,"[74-75]", "reserved", reserved_74_75));
        
        System.out.println("END OF ANALYSIS: PRE-AUTHORIZATION INCREMENTAL RESPONSE FIELDS\"");
        
    } // logPreauthorizationIncrementatlResponse
    
    /**
     * 
     * Parses the frame received from the POS.
     * 
     * @param frameFromPos 
     * 
     */
    public static void logPosToEcrPreauthorizationClosureResponse(byte[] frameFromPos){
       
        if (frameFromPos == null || frameFromPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameFromPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("POS RESPONSE [RESPONSE PRE-AUTHORIZATION CLOSURE FIELDS ANALYSIS]");
        
        System.out.println("[PAYLOAD LENGTH [WITHOUT STX, ETX, LRC] : " + payload.length());
    
        // Initial part of the response, the same for everyone - START
        
        // [1..8] TID: pos  → substring(0, 8) in Java
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "Message code", msgCode));

        // [11-12] Transaction result: pos 11..12 → substring(10, 12)
        String transactionResultCode = getTransactionResult(payload);
        System.out.println(String.format(FMT,"[11-12]", "Transaction result", transactionResultCode));
        
        // Initial part of the response, the same for everyone - END
        
        if (transactionResultCode.equalsIgnoreCase(TRANSACTION_RESULT_POSIVITE)) {
                
            // Section for Transaction result = "00" - START
            
            // [13-31] pan
            String pan = len >= 31 ? payload.substring(12, 31) : "";
            System.out.println(String.format(FMT, "[13-31]", "pan", pan));

            // [32-34] transaction type : 
            String transactionType = len >= 34 ? payload.substring(31, 34) : "";
            System.out.println(String.format(FMT,"[32-34]", "transactionType", transactionType));

            // [35-40] transaction type : 
            String authorizationCode = len >= 40 ? payload.substring(34, 40) : "";
            System.out.println(String.format(FMT,"[35-40]", "authorizationCode", authorizationCode));

            // [41-47] transaction type : 
            String transactionDateTime = len >= 47 ? payload.substring(40, 47) : "";
            System.out.println(String.format(FMT,"[41-47]", "transactionDateTime", transactionDateTime));

            // Section for Transaction result = "00" - END
            
        } else if (transactionResultCode.equalsIgnoreCase(TRANSACTION_RESULT_NEGATIVE)){
            
             // Section for Transaction result = "01" - START
        
            // [13-36] resultDescription : 
            String resultDescription = len >= 36 ? payload.substring(12, 36) : "";
            System.out.println(String.format(FMT,"[13-36]", "Result description", resultDescription));

            // [37-47] Reserved : 
            String reserved_37_47 = len >= 47 ? payload.substring(36, 47) : "";
            System.out.println(String.format(FMT,"[37-47]", "Reserved", reserved_37_47));

            // Section for Transaction result = "01" - END
        }
       
        // common to all responses - START
        
        // [48] cardType : 
        char cardType = len >= 48 ? payload.charAt(47) : '?';
        System.out.println(String.format(FMT,"[48]", "cardType", cardType));
        
        // [49-59] acquirerID : 
        String acquirerID = len >= 59 ? payload.substring(48, 59) : "";
        System.out.println(String.format(FMT,"[49-59]", "acquirerID", acquirerID));
        
        // [60-65] stan : 
        String stan = len >= 65 ? payload.substring(59, 65) : "";
        System.out.println(String.format(FMT,"[60-65]", "stan", stan));
        
        // [66-71] ID online : 
        String idOnline = len >= 71 ? payload.substring(65, 71) : "";
        System.out.println(String.format(FMT,"[66-71]", "idOnline", idOnline));
        
        // [72-74] actionCode : 
        String actionCode = len >= 74 ? payload.substring(71, 74) : "";
        System.out.println(String.format(FMT,"[72-74]", "actionCode", actionCode));
        
        // common to all responses - END
        
        System.out.println("END OF ANALYSIS: PRE-AUTHORIZATION CLOSURE RESPONSE FIELDS]");
        
    } // logPreauthorizationClosureResponse
    
    
    public static void logPosToEcrCloseSessionResponse(byte[] frameFromPos){
       
        if (frameFromPos == null || frameFromPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameFromPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("POS RESPONSE [CLOSE SESSION FIELDS ANALYSIS]");
        System.out.println("[PAYLOAD LENGTH [WITHOUT STX, ETX, LRC] : " + payload.length());
        
        // Initial part of the response, the same for everyone - START
        
        // [1..8] TID: pos  → substring(0, 8)
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "Message code", msgCode));

        // [11-12] Transaction result: pos 11..12 → substring(10, 12)
        String transactionResultCode = getTransactionResult(payload);
        System.out.println(String.format(FMT,"[11-12]", "Transaction result", transactionResultCode));
        
        // Initial part of the response, the same for everyone - END
        
        if (transactionResultCode.equalsIgnoreCase(TRANSACTION_RESULT_POSIVITE)) {
                
            // Section for Transaction result = "00" - START
            
            // [13-28] EFT-POS Total amount
            String eftPosTotalAmount = len >= 28 ? payload.substring(12, 28) : "";
            System.out.println(String.format(FMT,"[13-28]", "EFT-POS Total amount", eftPosTotalAmount));

            // [29-35] Host Total amount : 
            String hostTotalAmount = len >= 35 ? payload.substring(28, 35) : "";
            System.out.println(String.format(FMT,"[29-35]", "HOST total amount", hostTotalAmount));

            // Section for Transaction result = "00" - END
            
        } else if (transactionResultCode.equalsIgnoreCase(TRANSACTION_RESULT_NEGATIVE)){
            
            // Section for Transaction result = "00" - START
        
            // [13-31] resultDescription : 
            String resultDescription = len >= 131 ? payload.substring(12, 31) : "";
            System.out.println(String.format(FMT,"[13-31]", "Result description", resultDescription));

            // [32-34] Action Code : 
            String actionCode = len >= 34 ? payload.substring(31, 34) : "";
            System.out.println(String.format(FMT,"[32-34]", "Action Code", actionCode));
            
            // [35-44] Reserved : 
            String reserved_35_44 = len >= 44 ? payload.substring(34, 44) : "";
            System.out.println(String.format(FMT,"[35-44]", "Reserved", reserved_35_44));

            // Section for Transaction result = "00" - END
        }
       
        System.out.println("END RESPONSE [CLOSE SESSION FIELDS ANALYSIS]");
        
    } // logCloseSessionResponse
    
    
    public static void logPosToEcrTerminalTotalsResult(byte[] frameFromPos){
       
        if (frameFromPos == null || frameFromPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameFromPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("POS RESPONSE [TERMINAL TOTALS FIELDS ANALYSIS]");
        System.out.println("[PAYLOAD LENGTH [WITHOUT STX, ETX, LRC] : " + payload.length());
        
        // Initial part of the response, the same for everyone - START
        
        // [1..8] TID: pos  → substring(0, 8) 
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "Message code", msgCode));

        // [11-12] Transaction result: pos 11..12 → substring(10, 12)
        String transactionResultCode = getTransactionResult(payload);
        System.out.println(String.format(FMT,"[11-12]", "Transaction result", transactionResultCode));
        
        // [13-28] Total amount pos 13..28 → substring(12, 28)
        String totalAmount =  len >= 28 ? payload.substring(12, 28) : "";
        System.out.println(String.format(FMT,"[13-28]", "Total amountt", totalAmount));
        
        // [29-34] Total amount pos 29..34 → substring(28, 34)
        String reserved_29_34 =  len >= 34 ? payload.substring(28, 34) : "";
        System.out.println(String.format(FMT,"[29-34]", "Reserved", reserved_29_34));
        
        // Initial part of the response, the same for everyone - END
       
        System.out.println("END [TERMINAL TOTALS FIELDS ANALYSIS]");
        
    } // logTerminalTotalsResponse
    
} // end class
