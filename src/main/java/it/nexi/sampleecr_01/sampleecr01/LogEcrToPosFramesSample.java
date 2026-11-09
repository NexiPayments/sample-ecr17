package it.nexi.sampleecr_01.sampleecr01;

import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.getMessageCode;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.normalize;
import java.nio.charset.StandardCharsets;

/**
 *
 * Nexi Payments
 * 
 * Contains logs of frames sent by the ECR
 * 
 */
public class LogEcrToPosFramesSample {
    
    private static final String FMT = "%-9s %-22s: %s";
    
    /**
     * 
     * used for the p and x commands
     * 
     * @param frameToPos 
     * 
     */
    public static void logEcrToPosPaymentTransactionRequest(byte[] frameToPos){
       
        if (frameToPos == null || frameToPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameToPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("ECR REQUEST TO POS [FIELD ANALYSIS PAYMENT]");
        System.out.println("[PAYLOAD LENGTH [WITHOUT STX, ETX, LRC] : " + payload.length());
        
        // [1..8] TID: pos  → substring(0, 
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved",reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "Message code", msgCode));

        // [11-18] Cash register ID / ECR numeric ID, 8 digits
        String ecrID = len >= 18 ? payload.substring(10, 18) : "";
        System.out.println(String.format(FMT,"[11-18]", "ECR Id", ecrID));
        
        char additionalTagFlag = len >= 19 ? payload.charAt(18) : '?';
        System.out.println(String.format(FMT,"[19]", "Additional Flag", additionalTagFlag));
        
        // [20-21] reserved : 
        String reserved_20_21 = len >= 21 ? payload.substring(19, 21) : "";
        System.out.println(String.format(FMT,"[20-21]", "reserved", reserved_20_21));
        
        char startWithCardPresent = len >= 22 ? payload.charAt(21) : '?';
        System.out.println(String.format(FMT,"[22]", "start with card pres.", startWithCardPresent));
        
        char paymentType = len >= 23 ? payload.charAt(22) : '?';
        System.out.println(String.format(FMT,"[23]", "Payment type", paymentType));
       
        String amountInCents = len >= 31 ? payload.substring(23, 31) : "";
        System.out.println(String.format(FMT,"[24-31]", "Amount in centS", amountInCents));
        
        String textToPrint = len >= 159 ? payload.substring(31, 159) : "";
        String visibleText = (textToPrint.trim().isEmpty()) ? "(Empty spaces)" : textToPrint.trim();
        System.out.println(String.format(FMT,"[32-159]", "Text to print", visibleText));
        
        String reserved_160_167 = len >= 167 ? payload.substring(159, 167) : "";
        System.out.println(String.format(FMT,"[160-167]", "Reserved", reserved_160_167));
        
        System.out.println("END FIELDS ANALYSIS REQUEST PAYMENT ");
        
    } // logEcrToPaymentTransactionRequest
    
    public static void logEcrToPosAdditionalDataRequest(byte[] frameToPos) {

        if (frameToPos == null || frameToPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameToPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int lenPayload = payload.length();
        
        System.out.println("ECR REQUEST TO POS [FIELD ANALYSIS ADDITIONAL DATA]");
        System.out.println("[PAYLOAD LENGTH [WIHTOUT STX, ETX, LRC] : " + payload.length());

        String terminalId = lenPayload >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", terminalId));
        
        char reserved_09 = lenPayload >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "RESERVED", reserved_09));
        
        char messageCode = lenPayload >= 10 ? payload.charAt(9) : '?';
        System.out.println(String.format(FMT,"[10]", "MESSAGE CODE", messageCode));
        
        String paymentType = lenPayload >= 15 ? payload.substring(11, 15): "";
        System.out.println(String.format(FMT,"[11-16]", "PAYMENT TYPE", paymentType));

        // 17..18 : ISO Field: "62" instructs the POS to retrieve return data from the GT.
        String isoField = lenPayload >= 18 ? payload.substring(16, 18) : "";
        System.out.println(String.format(FMT,"[17-18]", "ISO Field", isoField));
        
        // 19..26 : TAG Number: Nexi/GT Identifier (Fixed DF8D01 + 2 spaces).
        String tagNumber = lenPayload >= 26 ? payload.substring(18, 26) : "";
        System.out.println(String.format(FMT,"[19-26]", "TagNumber", tagNumber));
        
        // 27 : Reserved
        char reserved_27 = lenPayload >= 27 ? payload.charAt(26) : '?';
        System.out.println(String.format(FMT,"[27]", "Reserved", reserved_27));
        
        // 28..31 : TAG Index (es. "1000" activates Tag 1; Map: 1=Tag1, 0=Disabled).
        String tagIndex = lenPayload >= 31 ? payload.substring(27, 31) : "";
        System.out.println(String.format(FMT,"[28-31]", "TAG Index", tagIndex));
        
        // 32..36 : Reserved
        String reserved_32_26 = lenPayload >= 36 ? payload.substring(31, 36) : "";
        System.out.println(String.format(FMT,"[32-36]", "Reserved", reserved_32_26));
        
        String parte_variabile = lenPayload >= 37 ? payload.substring(36) : "(empty)";
        System.out.println(String.format(FMT,"[37-N]", "Variable part", parte_variabile));
        
        
        System.out.println("END FIELDS ANALISYS REQUEST ADDITIONAL DATA");
       
    } // public void logEcrToPosAdditionalDataRequest(byte[] frame) 
    
    public static void logEcrToPosPreAuthorizationRequest(byte[] frameToPos){
        
        if (frameToPos == null || frameToPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameToPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("ECR REQUEST TO POS [FIELDS ANALYSIS PRE AUTHORIZATION]");
        System.out.println("[PAYLOAD LENGTH [WITHOUT STX, ETX, LRC] : " + payload.length());
        
        // [1..8] TID: pos  → substring(0, 8)
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10] Message code", msgCode));

        // [11-18] Cash register ID / ECR numeric ID, 8 digits
        String ecrID = len >= 18 ? payload.substring(10, 18) : "";
        System.out.println(String.format(FMT,"[11-18]", "ECR", ecrID));
        
        char additionalTagFlag = len >= 19 ? payload.charAt(18) : '?';
        System.out.println(String.format(FMT,"[19]", "Additional Flag", additionalTagFlag));
        
        // [20-21] reserved : 
        String reserved_20_21 = len >= 21 ? payload.substring(19, 21) : "";
        System.out.println(String.format(FMT,"[20-21]", "reserved", reserved_20_21));
        
        char startWithCardPresent = len >= 22 ? payload.charAt(21) : '?';
        System.out.println(String.format(FMT,"[22]", "start with card pres.", startWithCardPresent));
        
        char paymentType = len >= 23 ? payload.charAt(22) : '?';
        System.out.println(String.format(FMT,"[23]", "Payment type", paymentType));
       
        String amountInCents = len >= 31 ? payload.substring(23, 31) : "";
        System.out.println(String.format(FMT,"[24-31]", "Amount in cents", amountInCents));
        
        String textToPrint = len >= 159 ? payload.substring(31, 159) : "";
        String visibleText = (textToPrint.trim().isEmpty()) ? "(Empty spaces)" : textToPrint.trim();
        System.out.println(String.format(FMT,"[32-159]", "Text to print", visibleText));
        
        String reserved_160_167 = len >= 167 ? payload.substring(159, 167) : "";
        System.out.println(String.format(FMT,"[160-167]", "Reserved", reserved_160_167));
        
        System.out.println("END FIELDS ANALISYS REQUEST PRE AUTHORIZATION ");
        
    } // logEcrToPosPreAuthorizationRequest
    
    public static void logEcrToPosPreAuthorizationIncrementalRequest(byte[] frameToPos){
       
        if (frameToPos == null || frameToPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameToPos);
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("ECR REQUEST TO POS [FIELDS ANALISYS PRE AUTHORIZATION INCREMENTAL]");
        System.out.println("[PAYLOAD LENGTH [WITHOUT STX, ETX, LRC] : " + payload.length());
        
        // [1..8] TID: pos  → substring(0, 8)
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved ", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "Message code", msgCode));

        // [11-18] Cash register ID / ECR numeric ID, 8 digits
        String ecrID = len >= 18 ? payload.substring(10, 18) : "";
        System.out.println(String.format(FMT,"[11-18]", "ECR", ecrID));
        
        char additionalTagFlag = len >= 19 ? payload.charAt(18) : '?';
        System.out.println(String.format(FMT,"[19]", "Additional Flag", additionalTagFlag));
        
        // [20-23] reserved : 
        String reserved_20_23 = len >= 23 ? payload.substring(19, 23) : "";
        System.out.println(String.format(FMT, "[20-23]", "reserved" , reserved_20_23));
        
        String amountInCents = len >= 31 ? payload.substring(23, 31) : "";
        System.out.println(String.format(FMT,"[24-31]", "Amount in cents", amountInCents));
        
        String textToPrint = len >= 159 ? payload.substring(31, 159) : "";
        String visibleText = (textToPrint.trim().isEmpty()) ? "(Empty spaces)" : textToPrint.trim();
        System.out.println(String.format(FMT,"[32-159]", "Text to print", visibleText));
        
        String preauthCode = len >= 168 ? payload.substring(159, 168) : "";
        System.out.println(String.format(FMT,"[160-168]", "pre authorization code", preauthCode));
        
        String reserved_169_177 = len >= 176 ? payload.substring(168, 176) : "";
        System.out.println(String.format(FMT,"[169-176]", "Reserved", reserved_169_177));
        
        System.out.println("END FIELDS ANALISYS REQUEST PRE AUTHORIZATION INCREMENTAL ");
        
    } // logEcrToPosPreAuthorizationIncrementalRequest
    
    public static void logEcrToPosPreAuthorizationClosureRequest(byte[] frameToPos){
        
        if (frameToPos == null || frameToPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameToPos);
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("ECR REQUEST TO POS [ANALISI CAMPI PRE AUTHORIZATION CLOSURE]");
        System.out.println("[PAYLOAD LENGTH [WITHOUT STX, ETX, LRC] : " + payload.length());
     
        // [1..8] TID: pos  → substring(0, 8)
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "Message code",msgCode));

        // [11-18] Cash register ID / ECR numeric ID, 8 digits
        String ecrID = len >= 18 ? payload.substring(10, 18) : "";
        System.out.println(String.format(FMT,"[11-18]", "ECR Id", ecrID));
        
        char additionalTagFlag = len >= 19 ? payload.charAt(18) : '?';
        System.out.println(String.format(FMT,"[19]", "Additional Flag", additionalTagFlag));
        
        // [20-23] reserved : 
        String reserved_20_23 = len >= 23 ? payload.substring(19, 23) : "";
        System.out.println(String.format(FMT, "[20-23]", "reserved" , reserved_20_23));
        
        char paymentType = len >= 23 ? payload.charAt(22) : '?';
        System.out.println(String.format(FMT,"[23]", "Payment type", paymentType));
       
        String amountInCents = len >= 31 ? payload.substring(23, 31) : "";
        System.out.println(String.format(FMT,"[24-31]", "Amount in cents", amountInCents));
        
        String textToPrint = len >= 159 ? payload.substring(31, 159) : "";
        String visibleText = (textToPrint.trim().isEmpty()) ? "(Empty spaces)" : textToPrint.trim();
        System.out.println(String.format(FMT,"[32-159]", "Text to print", visibleText));
        
        String codicePreauth = len >= 168 ? payload.substring(159, 168) : "";
        System.out.println(String.format(FMT,"[160-168]", "Pre authorization code", codicePreauth));
        
        String reserved_169_176 = len >= 176 ? payload.substring(168, 176) : "";
        System.out.println(String.format(FMT,"[169-176]", "Reserved", reserved_169_176));
        
        System.out.println("END FIELDS ANALISYS REQUEST PRE AUTHORIZATION CLOSURE ");
        
    } // logEcrToPosPreAuthorizationClosureRequest
    
     public static void logEcrToPosReprintTicketRequest(byte[] frameToPos){
       
        if (frameToPos == null || frameToPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameToPos); 
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("ECR REQUEST TO POS [FIELDS ANALISYS REPRINT TICKET]");
        System.out.println("[LUNGHEZZA PAYLOAD [SENZA STX, ETX, LRC] : " + payload.length());
      
        // [1..8] TID: pos  → substring(0, 8)
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "Message code", msgCode));
        
        char enableDisablePrintingReceiptOnEcr = len >= 11 ? payload.charAt(10) : '?';
        System.out.println(String.format(FMT,"[11]", "En/Dis printing receipt on ECR", enableDisablePrintingReceiptOnEcr));
        
        char ticketTypeFlag = len >= 12 ? payload.charAt(11) : '?';
        System.out.println(String.format(FMT,"[12]", "Ticket type flag", ticketTypeFlag));
        
        // [13-22] reserved : 
        String reserved_13_22 = len >= 22 ? payload.substring(12, 22) : "";
        System.out.println(String.format(FMT,"[13-22]", "reserved", reserved_13_22));
        
        System.out.println("END FIELDS ANALISYS REQUEST REPRINT TICKET ");
        
    } // logEcrToPosReprintTicketRequest
     
    public static void logEcrToPosReversalTransactionRequest(byte[] frameToPos){
       
        if (frameToPos == null || frameToPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameToPos);
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("ECR REQUEST TO POS [FIELDS ANALISYS REVERSAL]");
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

        // [11-18] Cash register ID / ECR numeric ID, 8 digits
        String ecrID = len >= 18 ? payload.substring(10, 18) : "";
        System.out.println(String.format(FMT,"[11-18]", "ECR Id", ecrID));
        
        // [19-24] stan : 
        String stan = len >= 24 ? payload.substring(18, 24) : "";
        System.out.println(String.format(FMT,"[19-24]", "STAN", stan));
        
        char additionalTagFlag = len >= 25 ? payload.charAt(24) : '?';
        System.out.println(String.format(FMT,"[25]", "Additional Flag", additionalTagFlag));
        
        char reserved_26 = len >= 26 ? payload.charAt(25) : '?';
        System.out.println(String.format(FMT,"[26]", "reserved" , reserved_26));
        
        System.out.println("END FIELDS ANALISYS REQUEST REVERSAL ");
        
    } // logEcrToPosReversalTransactionRequest
    
    public static void logEcrToPosSendLastResultRequest(byte[] frameToPos){
       
        if (frameToPos == null || frameToPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameToPos);
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("ECR REQUEST TO POS [FIELDS ANALISIYS SEND LAST RESULT]");
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

        // [11-18] Cash register ID / ECR numeric ID, 8 digits
        String ecrID = len >= 18 ? payload.substring(10, 18) : "";
        System.out.println(String.format(FMT,"[11-18]", "ECR Id", ecrID));
        
        char additionalTagFlag = len >= 19 ? payload.charAt(18) : '?';
        System.out.println(String.format(FMT,"[19]", "Additional Flag", additionalTagFlag));
        
        // [20-22] reserved : 
        String reserved_20_22 = len >= 22 ? payload.substring(19, 22) : "";
        System.out.println(String.format(FMT,"[20-22]", "reserved", reserved_20_22));
        
        System.out.println("END FIELDS ANALISYS REQUEST SEND LAST RESULT");
        
    } // logEcrToPosSendLastResultRequest
    
    public static void logEcrToPosTerminalStatusRequest(byte[] frameToPos){
       
        if (frameToPos == null || frameToPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameToPos);
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("ECR REQUEST TO POS [FIELDS ANALISYS TERMINAL STATUS]");
        System.out.println("[PAYLOAD LENGTH [WITHOUT STX, ETX, LRC] : " + payload.length());
        
        // [1..8] TID: pos  → substring(0, 8) in Java
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]","TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "Message code", msgCode));

        System.out.println("END FIELDS ANALISYS REQUEST TERMINAL STATUS ");
        
    } // logEcrToPosTerminalStatusRequest
    
     public static void logEcrToPosTerminalTotalsRequest(byte[] frameToPos){
       
        if (frameToPos == null || frameToPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameToPos);
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("ECR REQUEST TO POS [FIELDS ANALISYS TERMINAL TOTALS]");
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

        // [11-18] Cash register ID / ECR numeric ID, 8 digits
        String ecrID = len >= 18 ? payload.substring(10, 18) : "";
        System.out.println(String.format(FMT,"[11-18]", "ECR Id", ecrID));
        
        char additionalTagFlag = len >= 19 ? payload.charAt(18) : '?';
        System.out.println(String.format(FMT,"[19]", "Additional Flag", additionalTagFlag));
        
        // [20-26] reserved : 
        String reserved_20_26 = len >= 26 ? payload.substring(19, 26) : "";
        System.out.println(String.format(FMT,"[20-26]", "reserved", reserved_20_26));
        
        System.out.println("END FIELDS ANALISYS REQUEST TERMINAL TOTALS");
        
    } // logEcrToPosTerminalTotalsRequest
     
    public static void logEcrToPosCloseSessionRequest(byte[] frameToPos){
       
        if (frameToPos == null || frameToPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameToPos);
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("ECR REQUEST TO POS [FIELDS ANALISYS CLOSE SESSION]");
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

        // [11-18] Cash register ID / ECR numeric ID, 8 digits
        String ecrID = len >= 18 ? payload.substring(10, 18) : "";
        System.out.println(String.format(FMT,"[11-18]", "ECR Id", ecrID));
        
        char additionalTagFlag = len >= 19 ? payload.charAt(18) : '?';
        System.out.println(String.format(FMT,"[19]", "Additional Flag", additionalTagFlag));
        
        // [20-26] reserved : 
        String reserved_20_26 = len >= 26 ? payload.substring(19, 26) : "";
        System.out.println(String.format(FMT,"[20-26]", "reserved", reserved_20_26));
        
        System.out.println("END FIELDS ANALISYS REQUEST CLOSE SESSION");
        
    } // logEcrToPosCloseSessionRequest
    
    public static void logEcrToPosCardVerificationRequest(byte[] frameToPos){
       
        if (frameToPos == null || frameToPos.length < 3) return;
        
        byte[] cleanFrame = normalize(frameToPos);
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(cleanFrame, 1, cleanFrame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        System.out.println("ECR REQUEST TO POS [FIELDS ANALISYS CARD VERIFICATION]");
        System.out.println("[PAYLOAD LENGTH [SENZA STX, ETX, LRC] : " + payload.length());
        
        // [1..8] TID: pos  → substring(0, 8)
        String tid = len >= 8 ? payload.substring(0, 8) : "";
        System.out.println(String.format(FMT,"[1-8]", "TID", tid));
        
        // [9] Reserved : 
        char reserved_9_9 = len >= 9 ? payload.charAt(8) : '?';
        System.out.println(String.format(FMT,"[9]", "reserved", reserved_9_9));
        
        // [10] Message code: pos 10 → index 9
        char msgCode = getMessageCode(payload);
        System.out.println(String.format(FMT,"[10]", "Message code", msgCode));

        // [11-18] Cash register ID / ECR numeric ID, 8 digits
        String ecrID = len >= 18 ? payload.substring(10, 18) : "";
        System.out.println(String.format(FMT,"[11-18]", "ECR Id", ecrID));
        
        char additionalTagFlag = len >= 19 ? payload.charAt(18) : '?';
        System.out.println(String.format(FMT,"[19]", "Additional Flag", additionalTagFlag));
        
        // [20-21] reserved : 
        String reserved_20_21 = len >= 21 ? payload.substring(19, 21) : "";
        System.out.println(String.format(FMT,"[20-21]", "reserved", reserved_20_21));
        
        char startWithCardPresent = len >= 22 ? payload.charAt(21) : '?';
        System.out.println(String.format(FMT,"[22]", "start with card pres.", startWithCardPresent));
        
        char paymentType = len >= 23 ? payload.charAt(22) : '?';
        System.out.println(String.format(FMT,"[23]", "Payment type", paymentType));
        
        String reserved_24_39 = len >= 39 ? payload.substring(23, 39) : "";
        System.out.println(String.format(FMT,"[24-39]", "Reserved", reserved_24_39));
        
        System.out.println("END FIELDS ANALISYS REQUEST CARD VERIFICATION ");
        
    } // logEcrToPosCardVerificationRequest
    
} // end class
