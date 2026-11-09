package it.nexi.sampleecr_01.sampleecr01;

import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ACK;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_MESSAGE_CODE_UNKNOUN;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ECR_COMMAND_REPRINT_TICKET;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ETX;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.NAK;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.STX;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.TRANSACTION_RESULT_UNKNOUN;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

/**
 *
 * Nexi payments.
 * 
 * The class provides methods 
 * to support development.
 * 
 */
public class UtilsSample {
    
    /**
     * 
    * Returns the payload length of an ECR17 frame.
    * The payload is defined as the message content excluding the 3 control bytes:
    * STX (1 byte), ETX (1 byte), and LRC (1 byte).
    *
    * @param frame  byte array (raw or normalized)
    * @return       payload length
    */
    public static  int getPayloadLength(byte[] frame) {
       if (frame == null || frame.length < 3) {
           return 0;
       }

       // subtract 1 for STX, 1 for ETX, and 1 for LRC.
       return frame.length - 3;
    }
    
     /**
      * 
     * Right-aligns a string "s" within a field of width "width",
     * padding with spaces on the left. If s is longer than width, it keeps
     * only the last "width" characters.
     *
     * E.G.:
     *   padLeftSpacesToWidth("ABC", 5)    → "  ABC"
     *   padLeftSpacesToWidth("", 3)       → "   "
     *   padLeftSpacesToWidth("12345", 3)  → "345"
     * 
     * @param s 
     * @param width
     * 
     * @return 
     * 
     */
    public static String padLeftSpacesToWidth(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) {
            return s.substring(s.length() - width);
        }
        StringBuilder sb = new StringBuilder(width);
        for (int i = s.length(); i < width; i++) {
            sb.append(' ');
        }
        sb.append(s);
        return sb.toString();
        
    } // private static String padLeftSpacesToWidth
    
     /**
      * 
     * Ensures that a character is a digit ('0'..'9').
     * If it is not, it is replaced with '0'.
     * 
     * @param c
     * @return 
     */
    public static char sanitizeDigit(char c) {
        return (c >= '0' && c <= '9') ? c : '0';
    }
    
    
    /**
     * 
     * Converts a byte array into a readable hexadecimal string,
     * for example:
     *   [0x02, 0x30, 0x31] → "02 30 31"
     *
     * Useful for debug logs and comparisons with the documentation.
     * 
     * @param data
     * @return 
     * 
     */
    public static String toHex(byte[] data) {
        if (data == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
        
    } //public static String toHex(byte[] data) {
    
    /**
     * 
     * Converts a byte array into a readable ASCII string.
     * 
     * @param bytes
     * @return 
     * 
     */
    public static String getRawAscii(byte[] bytes) {
        
        StringBuilder sb = new StringBuilder();
        
        for (byte b : bytes) {
            int v = b & 0xFF; // Converts to unsigned int
            
            // Management of primary control characters
            switch (v) {
                case 0x02: sb.append("[STX]"); break;
                case 0x03: sb.append("[ETX]"); break;
                case 0x1B: sb.append("[ESC]"); break;
                case 0x7D: sb.append("[0x7D]"); break; //New line/Reset
                case 0x7F: sb.append("[BOLD]"); break;
                default:
                    // If it is a printable character (ASCII 32–126)
                    if (v >= 32 && v <= 126) {
                        sb.append((char) v);
                    } else {
                        // For other non-printing characters, show the hex value
                        sb.append(String.format("[%02X]", v));
                    }
                    break;
            }
        }
        
        return sb.toString();
        
    } // public static String getRawAscii(byte[] bytes) {
    
     /**
      * 
      * 
     *  Calculate the LRC according to the Nexi specification:
     *  - Initial value: 0x7F
     *  - For each byte of the frame (from index 'off' to 'off+len-1'):
     *     LRC = LRC XOR currentByte
     *
     * It is used both during construction (to set the last byte of the frame)
     * and during verification.
     * 
     * @param a
     * @param off
     * @param len
     * 
     * @return 
     * 
     */
    public static byte lrcWithBase0x7F(byte[] a, int off, int len) {
        
        int x = 0x7F;
        for (int j = off; j < off + len; j++) {
            x ^= (a[j] & 0xFF);
        }
        return (byte) x;
        
    } // private static byte lrcWithBase0x7F
    
    
     /**
      * 
      * 
     * Cleans the string, keeping only digits 0–9, and adjusts it to an exact "width"
     * by left-padding with '0'. If the string is longer, the prefix is ​​trimmed
     * and only the last 'width' digits are kept.
     *
     * Examples:
     *   padDigits("123", 5)     → "00123"
     *   padDigits("ABC12", 4)   → "0012"
     *   padDigits("123456", 4)  → "3456"
     * 
     * @param s
     * @param width
     * 
     * @return 
     * 
     */
    public static String padDigits(String s, int width) {
        if (s == null) s = "";
        s = s.replaceAll("\\D", ""); // removes everything that is not a digit
        
        if (s.length() >= width) {
            return s.substring(s.length() - width);
        }
        
        StringBuilder sb = new StringBuilder(width);
        for (int i = s.length(); i < width; i++) {
            sb.append('0');
        }
        
        sb.append(s);
        return sb.toString();
        
    } // private static String padDigits
    
     /**
      * 
     * Extracts from the "raw" byte stream the subarray that exactly represents
     * a Nexi frame:
     *   STX .. ​​ETX + LRC
     *
     * Strategy:
     * - Find the first STX byte (0x02).
     * - Find the subsequent ETX (0x03).
     * - Return the slice [STX .. ​​ETX+1], i.e., ETX plus 
     *   the immediately following LRC byte.
     *
     * @param raw    buffer containing the data read from the socket
     * @param length number of valid bytes in raw
     * 
     * @return 
     * 
     */
    public static  byte[] sanitizeFrame(byte[] raw, int length) {
        int n = Math.min(raw.length, Math.max(0, length));
        int stx = -1, etx = -1;

        // Trovo la posizione di STX
        for (int i = 0; i < n; i++) {
            if (raw[i] == STX) {
                stx = i;
                break;
            }
        }
        
        if (stx < 0) {
            throw new IllegalArgumentException("STX non trovato");
        }

        // Find ETX.
        for (int i = stx + 1; i < n; i++) {
            if (raw[i] == ETX) {
                etx = i;
                break;
            }
        }
        
        if (etx < 0 || etx + 1 >= n) {
            throw new IllegalArgumentException("ETX/LRC not found");
        }

        // return the subarray extending from STX to ETX + LRC (2 bytes beyond ETX).
        return Arrays.copyOfRange(raw, stx, etx + 2);
        
    } //  private static byte[] sanitizeFrame(byte[]
    
    
    /**
     * 
     * Extracts the message code from the provided byte array.
     * 
     * @param frame
     * @return 
     * 
     */
    public static char getMessageCode(byte[] frame) {
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(frame, 1, frame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        // Check if the terminal ID starts with the string 'TEST'.
        if (payload.startsWith("TEST")) {
            
            // The string 'TEST' is deleted.
            String tmpPayload = new String(frame, 1, frame.length - 3, StandardCharsets.US_ASCII);
            payload = tmpPayload.replace("TEST", "");
            
            // The payload length is recalculated.
            len = payload.length();
          
        }
       
        // [10] Message code: pos 10 → index 9
        char msgCode = len >= 10 ? payload.charAt(9) : '?';
        
        return msgCode;
        
    } // private char getMessageCode(byte[] frame) {
     
    /**
     * 
     * Extracts the message code from the provided payload.
     * 
     * @param payload
     * 
     * @return 
     * 
     */
    public static char getMessageCode(String payload) {
        
        int len = payload.length();
        
        if (len <= 0 && len < 9) {
            
            return ECR_COMMAND_MESSAGE_CODE_UNKNOUN;
            
        }
        
        // [10] Message code: pos 10 → index 9
        return payload.charAt(9);
        
    } // private char getMessageCode(String payload) { 
    
    /**
     * 
     * Check if the frame contains the character signaling the 
     * end of the receipt (ESC)
     * 
     * @param frame
     * 
     * @return 
     * 
     */
    public static boolean containsEndOfTicket(byte[] frame) {
        
        for (byte b : frame) {
            if (b == 0x1B) return true; // ESC = End receipt
        }
        
        return false;
        
    } // public static boolean containsEndOfTicket(byte[] frame) {
    
    /**
     * 
     * Send a frame receipt acknowledgment (ACK) to the POS.
     * 
     * @param out
     * @param message
     * 
     * @throws IOException 
     * 
     */
    public static void sendAckToPos(OutputStream out, String message) throws IOException{
        
        byte[] commandAck = {0x06, 0x03, 0x7a};
        out.write(commandAck);
        out.flush();
        
        System.out.println(" >>> SEND ACK TO POS " + message + "\n");
        
    }
    
     /**
      * 
     * Verify a complete frame (STX..ETX+LRC) according to the Nexi specifications:
     * - The LRC is recalculated on all bytes except the last one (which is the received LRC).
     * - The result must be equal to the received LRC (frame[frame.length-1]).
     *
     * @param frame     array containing the entire STX..ETX+LRC frame
     * 
     * @return          true if the LRC is valid, false otherwise
     */
    public static boolean validateLrc(byte[] frame) {
        
        if (frame == null || frame.length < 3) return false;
        
        byte calc = (byte) 0x7F;
        
        // // XOR from STX (inclusive) to ETX (inclusive); the last byte is the LRC
        for (int i = 0; i < frame.length - 1; i++) {
            calc ^= frame[i];
        }
        return calc == frame[frame.length - 1];
        
    } // public static boolean validateLrc(byte[] frame)

    /**
     * 
     * Extracts the transaction result from the payload provided as a parameter.
     * 
     * @param payload
     * 
     * @return 
     * 
     */
    public static String getTransactionResult(String payload) {
        
        int len = payload.length();
         
        if (len <= 0 && len < 12) {
            
            return TRANSACTION_RESULT_UNKNOUN;
            
        }
        
        // [11-12] Transaction result: pos 11..12 → substring(10, 12)
        return payload.substring(10, 12);
        
    }
     
     /**
     * 
     * Check if the frame relates to an 'R' message
     * for reissuing the last receipt.
     * 
     * If affirmative, it returns the receipt destination:
     *  '1' : the receipt must be sent to the ECR
     *  '0' : the receipt must be printed
     * 
     * @param frame
     * 
     * @return 
     * '1': The receipt must be sent to the ECR
     * '0': The receipt must be printed
     * 'x': The flag value could not be detected
     *
     */
    public static char getFlagFromReprintTicketMessage(byte[] frame) {
        
        char flagReprintTicket = 'x';
        
        // Extraction of the ASCII payload, excluding STX (byte 0), ETX, and the final LRC.
        // frame: [STX][payload...][ETX][LRC]
        // payload starts at index=1 and has a length of frame.length - 3
        String payload = new String(frame, 1, frame.length - 3, StandardCharsets.US_ASCII);
        int len = payload.length();
        
        // Check if the terminal ID starts with the string 'TEST'.
        if (payload.startsWith("TEST")) {
            
            // The string 'TEST' is deleted
            String tmpPayload = new String(frame, 1, frame.length - 3, StandardCharsets.US_ASCII);
            payload = tmpPayload.replace("TEST", "");
            
            // Recalculate the payload length
            len = payload.length();
          
        }
       
        // [10] Message code: pos 10 → index 9
        char msgCode = len >= 10 ? payload.charAt(9) : '?';
        
        if (msgCode == ECR_COMMAND_REPRINT_TICKET) {
            
            // [11] Flag di stampa ('0' / '1')
            flagReprintTicket = len >= 10 ? payload.charAt(10) : 'x';
        }
        
        return flagReprintTicket;
        
    } // private char getFlagFromReprintTicketMessage(byte[] frame) {
    
  /**
     * 
    * Normalizes the frame received or read from the log.
    * 
    * If it detects the string "TEST" (54 45 53 54) immediately after the STX, 
    * it creates a new array by removing the prefix and realigning the offsets.
    *
    * @param frame the raw byte array (including STX)
    * 
    * @return the byte array normalized according to the ECR17 standard
    * 
    */
    public static byte[] normalize(byte[] frame) {
        
        // Security check: must contain at least STX + "TEST"
        if (frame == null || frame.length < 5) {
            return frame;
        }

        // Checks if it starts with STX (0x02) followed by "TEST"
        if (frame[0] == STX && 
            frame[1] == 'T' && 
            frame[2] == 'E' && 
            frame[3] == 'S' && 
            frame[4] == 'T') {

            // Create a new array reduced by 4 bytes
            byte[] cleanFrame = new byte[frame.length - 4];

            // Inserts STX at position 0
            cleanFrame[0] = STX; 

            // Copy everything else, skipping bytes 1, 2, 3, 4 (T, E, S, T)
            System.arraycopy(frame, 5, cleanFrame, 1, frame.length - 5);

            return cleanFrame;
            
        }

        // Frame without STX, ETX, LRC
        if (frame[0] != STX && frame[0] != ACK && frame[0] != NAK) {
            
            System.out.println(" <<< WARNING! STRING TO BE NORMALIZED (STX/ETX/LRC ARE NOT PRESENT IN THE MESSAGE))");
            System.out.println(" >>> REQ HEX : ");
            System.out.println(toHex(frame));
            
            System.out.println(" >>> REQ ASCII : ");
            System.out.println(getRawAscii(frame));
            
            // If the first byte is NOT STX/ACK/NAK,
            // insert STX at the beginning
            byte[] rebuilt = new byte[frame.length + 3];
            rebuilt[0] = STX; 
            
            // Copy from byte 0 of the frame to byte 1 of rebuilt
            System.arraycopy(frame, 0, rebuilt, 1, frame.length); 
            
            // Add ETX
            rebuilt[rebuilt.length - 2] = ETX; 
            
            // Calculates the LRC and appends it
            rebuilt[rebuilt.length - 1] = lrcWithBase0x7F(rebuilt, 0, rebuilt.length - 1); 
            
            return rebuilt;
        }
        
        return frame;
        
    } // public static byte[] normalize(byte[] frame) 
    
    public static String generateRandomString(int length) {
        
        String uuid = UUID.randomUUID().toString().replace("-", "");

        if (length > 32) {
            throw new IllegalArgumentException("The maximum length of a UUID is 32.");
        }

        return uuid.substring(0, length);

    } // public static String generateRandomString(int length)
    
     /**
     * 
     * Checks if the Message Code belongs to the allowed list of POS responses.
     * List of codes: 'C', 'c', 'E', 'e', ​​'i', 'S', 's', 'T', 'U', 'K'
     * 
     * @param msgCode the message code character (position 10)
     * 
     * @return true if valid, false otherwise
     * 
     */
    public static boolean isValidPosResponse(char msgCode) {
        
        final String VALID_RESPONSE_CODES = "CcEeSsTiUK";
        
        return VALID_RESPONSE_CODES.indexOf(msgCode) != -1;
        
    } // public static boolean isValidPosResponse(char msgCode)
    
} // end of class
