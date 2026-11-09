package it.nexi.sampleecr_01.sampleecr01;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Nexi Payments
 * 
 * class that provides parsing of the receipt 
 * received from the POS
 * 
 */
public class ECRTicketParserSample {
    
    private static final byte STX = 0x02;
    private static final byte CMD_S = 0x53;                 // 'S' (Position 10)
    private static final byte NEW_LINE_RESET = 0x7D;        // '}' (Reset/New line)
    private static final byte DOUBLE_HEIGHT_BOLD = 0x7F;    // (Bold)
    private static final byte TICKET_END = 0x1B;            // ESC (End of receipt)

    public static class TicketLine {
        
        public String text;
        public boolean isBold;

        public TicketLine(String text, boolean isBold) {
            
            this.text = text;
            this.isBold = isBold;
            
        }

        @Override
        public String toString() {
            
            return (isBold ? "[BOLD] " : "[NORM] ") + "|" + text + "|";
            
        }
        
    } // public static class TicketLine

        /**
         * 
         * Processes the frame received from the POS.
         * 
         * 1. Removes "TEST" if present.
         * 2. Verifies Message Code 'S' at position 10.
         * 3. Reads the lines starting at position 11.
         * 
         * @param originalFrame
         * 
         * @return 
         * 
         */
        public List<TicketLine> processFrame(byte[] originalFrame) {
            
            // FRAME NORMALIZATION
            byte[] frame = normalizeFrame(originalFrame);

            List<TicketLine> lines = new ArrayList<>();

            // VALIDATION ACCORDING TO SPECIFICATIONS
            // The frame must contain at least: STX(1) + TID(8) + RES(1) + 'S'(1) = 11 bytes total
            // The Message Code 'S' MUST be at position 10
            if (frame.length <= 11 || frame[10] != CMD_S) {
                
                return lines; // It is not a valid 'S' message or it is empty
                
            }

            // LINE PARSING (STARTING AT POSITION 11)
            StringBuilder currentBuffer = new StringBuilder();
            boolean isBold = false;
            final int STANDARD_WIDTH = 24;

            for (int i = 11; i < frame.length; i++) {
                
                int b = frame[i] & 0xFF;

                // Exit if the end of the receipt is found (ESC)
                if (b == TICKET_END) {
                    
                    flushLine(lines, currentBuffer, isBold);
                    
                    break;
                }

                switch (b) {
                    
                    case NEW_LINE_RESET:
                        
                        // Character 0x7D: New line and format reset to standard
                        flushLine(lines, currentBuffer, isBold);
                        
                        isBold = false; 
                        
                        break;

                    case DOUBLE_HEIGHT_BOLD:
                        
                        // Character 0x7F: Start bold (double height)
                        // If there was text in the buffer, we write it before changing format
                        flushLine(lines, currentBuffer, isBold);
                        
                        isBold = true;
                        
                        break;

                    default:
                        
                        // Printable ASCII characters
                        if (b >= 0x20) {
                            
                            currentBuffer.append((char) b);

                            // Gestione AUTO-WRAP:
                            // Se raggiungiamo i 24 caratteri la riga è "completata"
                            if (currentBuffer.length() >= STANDARD_WIDTH) {
                                
                                flushLine(lines, currentBuffer, isBold);
                                
                            }
                            
                        }
                        
                        break;
                        
                } // switch (b) 
                
            } // for (int i = 11; i < frame.length; i++) 

            return lines;
            
        } // public List<TicketLine> processFrame(byte[] originalFrame) 

        /**
         * 
         * Rimuove la stringa "TEST" (4 byte) se presente subito dopo lo STX.
         * Questo riallinea i campi alle posizioni fisse previste (TID=8, RES=1, CMD=1).
         * 
         */
        private byte[] normalizeFrame(byte[] frame) {
            
            if (frame != null && frame.length > 5) {
                
                // Check if the frame starts with the word "TEST" (54 45 53 54) 
                // at positions 1, 2, 3, 4
                if (frame[1] == 'T' && 
                    frame[2] == 'E' && 
                    frame[3] == 'S' && 
                    frame[4] == 'T'
                   ) {
                    
                    byte[] cleanFrame = new byte[frame.length - 4];
                    
                    cleanFrame[0] = STX; // Mantiene STX
                    
                    // Copy everything else, skipping "TEST"
                    System.arraycopy(frame, 5, cleanFrame, 1, frame.length - 5);
                    
                    return cleanFrame;
                    
                } // if (frame....
                
            } // if (frame != null && frame.length > 5)
            
            return frame;
            
        } // private byte[] normalizeFrame(byte[] frame)

        private void flushLine(List<TicketLine> lines, StringBuilder buffer, boolean bold) {
            
            // Adds the line (even if empty, to handle consecutive line breaks)
            lines.add(new TicketLine(buffer.toString(), bold));
            
            buffer.setLength(0);
        }
        
} // class ECRTicketParser 
    
