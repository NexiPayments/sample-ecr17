
package it.nexi.sampleecr_01.sampleecr01;

/**
 *
 * Nexi Payments
 * 
 * Defines the constants used by the various commands.
 * 
 */
public class ConstantSample {
    
    public static final byte    STX                                                 = 0x02;  // Start of Text
    public static final byte    ETX                                                 = 0x03;  // End of Text
    public static final byte    ACK                                                 = 0x06;  // ACK
    public static final byte    NAK                                                 = 0x15;  // NAK
    
    public static final char    ECR_COMMAND_PAYMENT                                 = 'P';
    public static final char    ECR_COMMAND_PAYMENT_EXTENDED_REQUEST                = 'X';
    public static final char    ECR_COMMAND_ADDITIONAL_DATA                         = 'U';
    public static final char    ECR_COMMAND_ENABLE_DISABLE_PRINTING_RECEIPT         = 'E';
    public static final char    ECR_COMMAND_REPRINT_TICKET                          = 'R';
    public static final char    ECR_COMMAND_REVERSAL                                = 'S';
    public static final char    ECR_COMMAND_TERMINAL_STATUS                         = 's';
    public static final char    ECR_COMMAND_SEND_LAST_RESULT                        = 'G'; 
    public static final char    ECR_COMMAND_PREAUTHORIZATION                        = 'p';
    public static final char    ECR_COMMAND_PREAUTHORIZATION_INCREMENTAL            = 'i';
    public static final char    ECR_COMMAND_PREAUTHORIZATION_CLOSURE                = 'c';
    public static final char    ECR_COMMAND_CLOSE_SESSION                           = 'C';
    public static final char    ECR_COMMAND_CARD_VERIFICATION                       = 'H';
    public static final char    ECR_COMMAND_TERMINAL_TOTALS                         = 'T';
    public static final char    ECR_COMMAND_MESSAGE_CODE_UNKNOUN                    = '_';
    
    public static final String  TRANSACTION_RESULT_POSIVITE                         = "00";
    public static final String  TRANSACTION_RESULT_NEGATIVE                         = "01";
    public static final String  TRANSACTION_RESULT_UNKNOUN                          = "XX";
    
    public static final String  GT_ISO_FIELD_RESPONSE                               = "62";
    public static final String  GT_TAG_RESPONSE                                     = "DF8D01";
    public static final String  GT_TAG_INDEX                                        = "0000";
    
    public static final String  GT_TAG_INDEX_02                                     = "2000";
    public static final int     TIMEOUT_SHORT_DATA_FROM_POS                         = 2000;
    
    public static final int     TIMEOUT_CONNECTION_TO_POS                           = 5000;
    public static final int     TIMEOUT_LONG_DATA_FROM_POS                          = 30000;
    public static final int     DELAY_SEND_COMMAND_TO_POS                           = 500;
    
    public static final char    ECR_COMMAND_REPRINT_TICKET_FLAG_ENABLE_ON_ECR       = '1';
    public static final char    ECR_COMMAND_REPRINT_TICKET_FLAG_ENABLE_ON_PRINTER   = '0';
    public static final char    FLAG_ADDITIONAL_DATA_ENABLED                        = '1';
    
}
