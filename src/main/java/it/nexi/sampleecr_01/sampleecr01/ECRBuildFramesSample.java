package it.nexi.sampleecr_01.sampleecr01;

import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.ETX;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.GT_TAG_INDEX;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.STX;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.lrcWithBase0x7F;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.padDigits;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.padLeftSpacesToWidth;
import static it.nexi.sampleecr_01.sampleecr01.UtilsSample.sanitizeDigit;
import java.nio.charset.StandardCharsets;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.GT_ISO_FIELD_RESPONSE;
import static it.nexi.sampleecr_01.sampleecr01.ConstantSample.GT_TAG_RESPONSE;
import java.util.Comparator;
import java.util.List;

/**
 * 
 * Nexi Payments
 * 
 * In this class, requests are constructed according to 
 * ECR 17 specifications. 
 * 
 * The requests are sent to the POS as an array of bytes.
 * 
 */
public class ECRBuildFramesSample {
    
    /**
      * 
     * Build Payment request frame (message code 'P')
     * in accordance with the Nexi documentation.

        Pos	Length	Type	Content
        1	8	N	Terminal ID (00000000-99999999)

        9	1	A	Reserved – fixed to ‘0’ (0x30)

        10	1	A	Message code: ‘P’ (0x50)

        11	8	N	Cash register ID

        19	1	N	Presence of message with additional data for the GT
				‘0’ = additional data message not present
				‘1’ = additional data message present
					
        20	2	N	Reserved – fixed to ‘0’ (0x30)

        22	1	N	Start transaction when card already present:
				‘0’ = start with card not yet inserted in the terminal
				‘1’ = start with card already inserted in the terminal
					
        23	1	N	Payment type:
				‘0’ = automatic card recognition on terminal with customer 
                                      selection in case of cobranded cards
				‘1’ = only debit cards
				‘2’ = only credit cards
				‘3’ = other cards
					
        24	8	N	Transaction amount, right aligned, 
				filled with ‘0’ (0x30) on the left.Amount is always considered to be expressed
				in cents (e.g.650 = 6,50)

        32	128	AN	Text to be print (Code contract) at the end of the payment receipt.
                                The field is always 128 chars long, it’s right aligned,
				filled with ‘ ‘ (0x20) on the left.The terminal shall only print valid chars, 
                                skipping the blanks.160	8	N	Reserved – fixed to ‘0’ (0x30)

        Final Frame :
        [STX] + BODY(167 byte ASCII) + [ETX] + [LRC]
     * 
     *
     * @param terminalId
     * @param messageCode
     * @param cashRegisterId
     * @param additionalDataFlag
     * @param startWithCardPresent
     * @param paymentType
     * @param amountCents
     * @param textInput
     * 
     * @return 
     */
    public static byte[] buildPaymentFrame(String terminalId,
                                           char messageCode,
                                           String cashRegisterId,
                                           char additionalDataFlag,
                                           char startWithCardPresent,
                                           char paymentType,
                                           String amountCents,
                                           String textInput
                                          ) {

        // Build the BODY as an ASCII string.
        StringBuilder app = new StringBuilder(167);

        // 1..8: Numeric TID, padded to 8 digits
        app.append(padDigits(terminalId, 8));

        // 9: character '0' (reserved)
        app.append('0');

        // 10: message code 
        app.append(messageCode);

        // 11..18: ECR numeric ID, 8 digits
        app.append(padDigits(cashRegisterId, 8));

        // 19: additional data flag ('0'/'1'), forced to numeric
        app.append(sanitizeDigit(additionalDataFlag));

        // 20..21: "00" (reserved)
        app.append("00");

        // 22: flag "start with card already present" ('0'/'1')
        app.append(sanitizeDigit(startWithCardPresent));

        // 23: Payment Type (0..3)
        app.append(sanitizeDigit(paymentType));

        // 24..31: amount in cents (8 digits)
        app.append(padDigits(amountCents, 8));

        // 32..159: text to be printed, right-aligned, padded with spaces on the left
        app.append(padLeftSpacesToWidth(textInput, 128));

        // 160..167: "00000000" (reserved)
        app.append("00000000");

        // Converts the BODY into an array of ASCII bytes.
        byte[] body = app.toString().getBytes(StandardCharsets.US_ASCII);

        /**
         * Build frame:
         *  [0]           = STX
         *  [1..body.len] = BODY ASCII
         *  [body.len+1]  = ETX
         *  [body.len+2]  = LRC (calculated using lrcWithBase0x7F)
         */
        byte[] out = new byte[1 + body.length + 1 + 1];
        int i = 0;

        // STX
        out[i++] = STX;

        // Coppy the BODY
        System.arraycopy(body, 0, out, i, body.length);
        i += body.length;

        // ETX
        out[i++] = ETX;

        // LRC calculated from STX through ETX, inclusive.
        out[i] = lrcWithBase0x7F(out, 0, i);

        return out;
        
    }  //public static byte[] buildPaymentFrame
    
     /**
      * 
      * Build the extended payment request frame (message code 'X')
      * in accordance with the Nexi "Payment with extended result" documentation.
      *
        Pos	Length	Type	Content
        1	8	N	Terminal ID (00000000-99999999)

        9	1	A	Reserved – fixed to ‘0’ (0x30)

        10	1	A	Message code: ‘X’ (0x58)

        11	8	N	Cash register ID

        19	1	N	Presence of message with additional data for the GT:
					‘0’ = additional data message not present
					‘1’ = additional data message present
					
        20	2	N	Reserved – fixed to ‘0’ (0x30)

        22	1	N	Start transaction when card already present:
				‘0’ = start with card not yet inserted in the terminal
				‘1’ = start with card already inserted in the terminal
					
        23	1	N	Payment type:
				‘0’ = automatic card recognition on terminal with customer 
				      selection in case of cobranded cards
				‘1’ = only debit cards
				‘2’ = only credit cards
				‘3’ = other cards
					
        24	8	N	Transaction amount, right aligned, filled with ‘0’ (0x30) on the left. 
				Amount is always considered to be expressed in cents (e.g. 650 = 6,50)
					
        32	128	AN	Text to be print (Code contract) at the end of the payment receipt. 
				The field is always 128 chars long, it’s right aligned, 
				filled with ‘ ‘ (0x20) on the left. 
				The terminal shall only print valid chars, skipping the blanks.
					
        160	8	N	Reserved – fixed to ‘0’ (0x30)
     * 
     * Final Frame :
     *   [STX] + BODY(167 byte ASCII) + [ETX] + [LRC]
     * 
     * @param terminalId
     * @param messageCode
     * @param cashRegisterId
     * @param additionalDataFlag
     * @param startWithCardPresent
     * @param paymentType
     * @param amountCents
     * @param textInput
     * 
     * @return 
     * 
     */
    
    public static byte[] buildPaymentExtendedFrame(String terminalId,
                                             char messageCode,
                                             String cashRegisterId,
                                             char additionalDataFlag,
                                             char startWithCardPresent,
                                             char paymentType,
                                             String amountCents,
                                             String textInput
                                            ) {

        // Build BODY 
        StringBuilder app = new StringBuilder(167);

        // 1..8: Numeric TID, padded to 8 digits
        app.append(padDigits(terminalId, 8));

        // 9: character '0' (reserved)
        app.append('0');

        // 10: message code 
        app.append(messageCode);

        // 11..18: ECR numeric ID, 8 digits
        app.append(padDigits(cashRegisterId, 8));

        // 19: additional data flag ('0'/'1'), forced to numeric
        app.append(sanitizeDigit(additionalDataFlag));

        // 20..21: "00" (reserved)
        app.append("00");

        // 22: flag "start with card already present" ('0'/'1')
        app.append(sanitizeDigit(startWithCardPresent));

        // 23: payment type (0..3)
        app.append(sanitizeDigit(paymentType));

        // 24..31: amount in cents (8 digits)
        app.append(padDigits(amountCents, 8));

        // 32..159: text to print, right aligned, padded with spaces, to left
        app.append(padLeftSpacesToWidth(textInput, 128));

        // 160..167: "00000000" (reserved)
        app.append("00000000");

        // Convert BODY to ASCII byte array
        byte[] body = app.toString().getBytes(StandardCharsets.US_ASCII);

        /**
         *  Build frame:
         *  [0]           = STX
         *  [1..body.len] = BODY ASCII
         *  [body.len+1]  = ETX
         *  [body.len+2]  = LRC (calculated using lrcWithBase0x7F)
         */
        byte[] out = new byte[1 + body.length + 1 + 1];
        int i = 0;

        // STX
        out[i++] = STX;

        // BODY copy
        System.arraycopy(body, 0, out, i, body.length);
        i += body.length;

        // ETX
        out[i++] = ETX;

        // LRC calculated from STX to ETX included
        out[i] = lrcWithBase0x7F(out, 0, i);
        
        return out;
        
    }  //public static byte[] buildPaymentExtendedFrame
    
     /**
      * 
     * Build the Reversal Frame (message code 'S')
     * in accordance with the Nexi "Reversal" documentation.
     *
        Pos	Length	Type	Content
        1	8	N	Terminal ID (00000000-99999999)

        9	1	A	Reserved – fixed to ‘0’ (0x30)

        10	1	A	Message code: ‘S’ (0x53)

        11	8	N	Cash register ID

        19	6	N	STAN of the transaction to be reversed.
				If the field is filled with ‘0’ (0x30) no check is performed.
				If the value is different from “000000”, 
				the terminal performs the reversal only if the value 
				is equal to the last payment transaction STAN.
					
        25	1	N	Presence of message with additional data for the GT:
				‘0’ = additional data message not present
				‘1’ = additional data message present
					
        26	1	N	Reserved – fixed to ‘0’ (0x30)
				(Terminal requires the same card to perform the reversal)
     
     * Final Fame:
     *   [STX] + BODY(167 byte ASCII) + [ETX] + [LRC]
     * 
     * 
     * 
     * @param terminalId
     * @param messageCode
     * @param cashRegisterId
     * @param stan
     * @param additionalDataFlag
     * 
     * @return 
     * 
     */
    public static byte[] buildReversalFrame(String terminalId,
                                            char messageCode,
                                            String cashRegisterId,
                                            String stan,
                                            char additionalDataFlag
                                           ) {

        // Build BODY 
        StringBuilder app = new StringBuilder(167);

        // 1..8: Numeric TID, padded to 8 digits
        app.append(padDigits(terminalId, 8));

        // 9: character '0' (reserved)
        app.append('0');

        // 10: message code (es. 'S' for reversal request)
        app.append(messageCode);

        // 11..18: ECR numeric ID, 8 digits
        app.append(padDigits(cashRegisterId, 8));

        if (stan.isEmpty()) {
            stan = "000000";
        }
        
        // 19..25: STAN
        app.append(stan);
        
        // 25: additional data flag ('0'/'1'), forced to numeric
        app.append(sanitizeDigit(additionalDataFlag));

        // 26: "0" (reserved)
        app.append("0");

        // Convert BODY to ASCII byte array
        byte[] body = app.toString().getBytes(StandardCharsets.US_ASCII);

        /**
         * Buiild frame:
         *  [0]           = STX
         *  [1..body.len] = BODY ASCII
         *  [body.len+1]  = ETX
         *  [body.len+2]  = LRC (calculated using lrcWithBase0x7F)
         */
        byte[] out = new byte[1 + body.length + 1 + 1];
        int i = 0;

        // STX
        out[i++] = STX;

        // copy theBODY
        System.arraycopy(body, 0, out, i, body.length);
        i += body.length;

        // ETX
        out[i++] = ETX;

        // LRC calculated from STX to ETX included
        out[i] = lrcWithBase0x7F(out, 0, i);

        return out;
        
    }  //public static byte[] buildReversalFrame
    
    /**
     * 
     * Build the Terminal Status frame request (message code 's')
     * in accordance with the Nexi "Payment with extended result" documentation.
     
        Pos	Length	Type	Content
        1	8	N	Terminal ID (00000000-99999999)

        9	1	A	Reserved – fixed to ‘0’ (0x30)

        10	1	A	Message code: ‘s’ (0x73)
    
     * Frame finale:
     *   [STX] + BODY(167 byte ASCII) + [ETX] + [LRC]
     * 
     * 
     * @param terminalId
     * @param messageCode
     * 
     * @return 
     * 
     */
    public static byte[] buildTerminalStatusFrame(String terminalId,
                                                  char messageCode
                                                 ) {

        // Build Body
        StringBuilder app = new StringBuilder(167);

        // 1..8: Numeric TID, padded to 8 digits
        app.append(padDigits(terminalId, 8));

        // 9: character '0' (reserved)
        app.append('0');

        // 10: message code (es. 'X' for extended payment request)
        app.append(messageCode);
        
        // Convert BODY to ASCII byte array
        byte[] body = app.toString().getBytes(StandardCharsets.US_ASCII);

        /**
         * Build frame:
         *  [0]           = STX
         *  [1..body.len] = BODY ASCII
         *  [body.len+1]  = ETX
         *  [body.len+2]  = LRC (calculated using lrcWithBase0x7F)
         */
        byte[] out = new byte[1 + body.length + 1 + 1];
        int i = 0;

        // STX
        out[i++] = STX;

        // copy the BODY
        System.arraycopy(body, 0, out, i, body.length);
        i += body.length;

        // ETX
        out[i++] = ETX;

        // LRC calculaed from STX to ETX included
        out[i] = lrcWithBase0x7F(out, 0, i);

        return out;
        
    }  //public static byte[] buildTerminalStatusFrame
    
     /**
      * 
     * Build the Send Last Result request frame (message code 'G')
     * in accordance with Nexi documentation.
     *
        Pos	Length	Type	Content
        1	8	N	Terminal ID (00000000-99999999)

        9	1	A	Reserved – fixed to ‘0’ (0x30)

        10	1	A	Message code: ‘G’ (0x47)

        11	8	N	Cash register ID

        19	1	N	Presence of message with additional data for the GT:
				‘0’ = additional data message not present
				‘1’ = additional data message present

        20	3	N	Reserved (fixed at “0” 30 hex)
     
     * Final Frame :
     *   [STX] + BODY(167 byte ASCII) + [ETX] + [LRC]
     * 
     * 
     * 
     * @param terminalId
     * @param messageCode
     * @param cashRegisterId
     * @param additionalDataFlag
     * 
     * @return 
     * 
     */
    public static byte[] buildSendLastResultFrame(String terminalId,
                                            char messageCode,
                                            String cashRegisterId,
                                            char additionalDataFlag
                                           ) {

        // Build BODY
        StringBuilder app = new StringBuilder(167);

        // 1..8: Numeric TID, padded to 8 digits
        app.append(padDigits(terminalId, 8));

        // 9: character '0' (reserved)
        app.append('0');

        // 10: message code 
        app.append(messageCode);

        // 11..18: ECR numeric ID, 8 digits
        app.append(padDigits(cashRegisterId, 8));
        
        // 19: additional from flag ('0'/'1'), forced to numeric
        app.append(sanitizeDigit(additionalDataFlag));

        // 20: "000" (reserved)
        app.append("000");

        // Convert BODY to ASCII byte array
        byte[] body = app.toString().getBytes(StandardCharsets.US_ASCII);

        /**
         * 
         * Build frame:
         *  [0]           = STX
         *  [1..body.len] = BODY ASCII
         *  [body.len+1]  = ETX
         *  [body.len+2]  = LRC (calculated using lrcWithBase0x7F)
         */
        byte[] out = new byte[1 + body.length + 1 + 1];
        int i = 0;

        // STX
        out[i++] = STX;

        // Copy the BODY
        System.arraycopy(body, 0, out, i, body.length);
        i += body.length;

        // ETX
        out[i++] = ETX;

        // LRC calculated from STX to a ETX included
        out[i] = lrcWithBase0x7F(out, 0, i);

        return out;
        
    }  //public static byte[] buildSendLastResultFrame
    
    
     /**
     * 
     * Build the Card Verification request frame (message code 'H')
     * in accordance with Nexi documentation.
     * 
     *
     * BODY (167 caratteri ASCII) con posizioni 1-based:
     * 
     *   Pos	Length          Type            Content
     *   1	8		N		Terminal ID (00000000-99999999)
     *
     *   9	1		A		Reserved – fixed to ‘0’ (0x30)
     *
     *   10	1		A		Message code: ‘H’ (0x48)
     *
     *   11	8		N		Cash register ID
     * 
     *   19	1		N		Presence of message with additional data for the GT:
        * 					‘0’ = additional data message not present
        *					‘1’ = additional data message present
     * 
     *   20	2		N		Reserved – fixed to ‘0’ (0x30)
     * 
     *   22	1		N		‘0’ = standard card verification
     * 
     *   23	1		N		Payment type:
     *                                          ‘0’ = automatic card recognition on terminal with 
     *                                                customer selection in case of cobranded cards
     *                                          ‘1’ = only debit cards
     *                                          ‘2’ = only credit cards
     *                                          ‘3’ = other cards
     * 
     *   24	16	N	Reserved – fixed to ‘0’ (0x30)
     *
     * 
     * Frame finale:
     *   [STX] + BODY(167 byte ASCII) + [ETX] + [LRC]
     * 
     * 
     * 
     * @param terminalId
     * @param messageCode
     * @param cashRegisterId
     * @param additionalDataFlag
     * @param cardVerificationType
     * @param paymentType
     * 
     * @return 
     */
    public static byte[] buildCardVerificationFrame(String terminalId,
                                                    char messageCode,
                                                    String cashRegisterId,
                                                    char additionalDataFlag,
                                                    char cardVerificationType,
                                                    char paymentType
                                                   ) {

        // Build BODY
        StringBuilder app = new StringBuilder(167);

        // 1..8: Numeric TID, padded to 8 digits
        app.append(padDigits(terminalId, 8));

        // 9: character '0' (reserved)
        app.append('0');

        // 10: message code 
        app.append(messageCode);

        // 11..18: ECR numeric ID, 8 digits
        app.append(padDigits(cashRegisterId, 8));

        // 19: additional data flag ('0'/'1'), forced to numeric
        app.append(sanitizeDigit(additionalDataFlag));

        // 20: fixed "0" (reserved)
        app.append("0");

        // 21: flag "card verification type" ('0'/'1')
        app.append(sanitizeDigit(cardVerificationType));
        
        // 22: fixed "0"
        app.append("0");
        
        // 23: payment type (0..3)
        app.append(sanitizeDigit(paymentType));

        // 24..31: fixed "0"
        app.append(padDigits("0", 16));

        // Convert BODY to ASCII byte array
        byte[] body = app.toString().getBytes(StandardCharsets.US_ASCII);

        /**
         *  Build frame:
         *  [0]           = STX
         *  [1..body.len] = BODY ASCII
         *  [body.len+1]  = ETX
         *  [body.len+2]  = LRC (calculated using lrcWithBase0x7F)
         */
        byte[] out = new byte[1 + body.length + 1 + 1];
        int i = 0;

        // STX
        out[i++] = STX;

        // Copy the BODY
        System.arraycopy(body, 0, out, i, body.length);
        i += body.length;

        // ETX
        out[i++] = ETX;

        // LRC calculated from STX to a ETX included
        out[i] = lrcWithBase0x7F(out, 0, i);

        return out;
        
    }  //public static byte[] buildCardVerificationFrame
    
    /**
     * 
     * Pos	Length	Type	Content
        1	8	N	Terminal ID (00000000-99999999)

        9	1	A	Reserved – fixed to ‘0’ (0x30)

        10	1	A	Message code: ‘U’ (0x55)

        11	6	N	Payment Type:
                                    “0” => Standard payment
					
        17	2	N	ISO field number.
				Indicates the ISO-8583 field where data from GT that 
				must be sent to ECR is found. This field is “00” by default and 
				indicates that there is not data from GT to return to ECR. 
				A value other than “00” alerts the terminal of:
                                    - data from GT retrieval coordinates
                                    - obligation to send additional data from GT message
						
				CURRENTLY THE FIELD IS SET AT “62”
					
        19	8	A	TAG number.
				Indicates the TAG where data from GT that must be sent to ECR is found. 
				This field is only significant if the previous field is not “00”. 
				The field is flush left, blank justified. 
				TAG from GT length is fixed at 255.
					
				CURRENTLY THE FIELD IS FIXED AT ‘DF8D01’
					
        27	1	N	Reserved (fixed at “0” 30 hex)

        28	4	N	Exclusive TAG index with additional data to be sent to GT.
				The field is considered as a byte map for which each byte 
				corresponds to a single index. “0” corresponds 
				to no TAG to be sent to GT.
					
        32	5	N	Reserved (fixed at “0” 30 hex)

        37	100-V	A	Privative TAG content. Max 100 characters - Min 1 character. 
				The field is always closed by an end-of-field character 01B hex

        <"The “Exclusive TAG content” field can be repeated for a maximum of 4 times.">
     * 
     * 
     * @param terminalId
     * @param messageCode
     * @param listAdditionalData
     * 
     * @return 
     * 
     */
    public static byte[] buildAdditionalDataFrame(String terminalId,
                                                  char messageCode,
                                                  List<MapAdditionalData> listAdditionalData
                                                 ) {
        
        String strAdditionalData = "";
        
        // Set the default map of expected indices 
        // for additional data (0000)
        String strTagIndex = GT_TAG_INDEX;
        
        StringBuilder sbAdditionalData = new StringBuilder();
        
        // Build the BODY
        StringBuilder app = new StringBuilder(167);

        // 1..8: Numeric TID, padded to 8 digits
        app.append(padDigits(terminalId, 8));

        // 9: character '0' (reserved)
        app.append('0');

        // 10: message code (es. 'U' for extended payment request)
        app.append(messageCode);

        // 11..16 : Payment Type: fixed to 0 
        app.append("000000");

        // 17..18 : ISO Field: "62" Instructs the POS to retrieve the return data from the GT.
        app.append(GT_ISO_FIELD_RESPONSE);
        
        // 19..26 : TAG Number: identifier Nexi/GT (Fixed DF8D01 + 2 space).
        app.append(GT_TAG_RESPONSE);
        // Adds spaces to complete the TAG Number field.
        app.append("  ");
        
        // 27 : Reserved (Fixed to '0')
        app.append("0");
        
        // Prepare the additional data to be added to the message.
        if (listAdditionalData != null && !listAdditionalData.isEmpty()) {
            
            // Since additional data is present, the index map
            // must be rebuilt using the actual indices specified
            // by the user
            strTagIndex = "";
            
            // Arranges the list in ascending order based on the index value
            listAdditionalData.sort(Comparator.comparingInt(MapAdditionalData::getIndex));
            
            for (MapAdditionalData mapAdditionalData : listAdditionalData){
            
                if (!mapAdditionalData.getValue().trim().isEmpty()) {
                    
                    sbAdditionalData.append(mapAdditionalData.getValue());
                    
                    // After the variable-length value,
                    // comes the ESC character separator (end of field)
                    sbAdditionalData.append((char) 0x1B);
                    
                    // Adds the index to the map
                    strTagIndex+= mapAdditionalData.getIndex();
                    
                }
                
            } // for (MapAdditionalData mapAdditionalData : listAdditionalData)
            
            // Security check.
            // Only 4 indices can be entered.
            if (strTagIndex.length() > 4) {
                
                strTagIndex = strTagIndex.substring(0, 4);
                
            }
            
            // If fewer than 4 indices have been entered,
            // the map must be completed by adding 0s.
            while (strTagIndex.length() < 4) {
                
                strTagIndex+="0";
                
            }
            
            // Create the string containing the additional data
            strAdditionalData = sbAdditionalData.toString();

        } // if (listAdditionalData != null && !listAdditionalData.isEmpty()) {
             
        // 28..31 : TAG Index (es. 1000, 1500)
        app.append(strTagIndex);

        // 32..36 : Reserved (Fixed to '0').
        app.append("00000");
        
        // 37.. variable length : TAG Content: The value to associate with the tag
        app.append(strAdditionalData);
        
        // Convert the BODY to ASCII byte array
        byte[] body = app.toString().getBytes(StandardCharsets.US_ASCII);

        /**
         * 
         *  Build frame:
         *  [0]           = STX
         *  [1..body.len] = BODY ASCII
         *  [body.len+1]  = ETX
         *  [body.len+2]  = LRC (calculated using lrcWithBase0x7F)
         */
        byte[] out = new byte[1 + body.length + 1 + 1];
        int i = 0;

        // STX
        out[i++] = STX;

        // Copy the BODY
        System.arraycopy(body, 0, out, i, body.length);
        i += body.length;

        // ETX
        out[i++] = ETX;

        // LRC calculated from STX to ETX included
        out[i] = lrcWithBase0x7F(out, 0, i);

        return out;
        
    }  //public byte[] buildAdditionalDataFrame
    
    
     /**
      * 
     * Build the Reprint ticket request frame (message code 'R')
     * in accordance with the Nexi documentation.
     *
        Pos	Length	Type	Content
        1	8	N	Terminal ID (00000000-99999999)

        9	1	A	Reserved – fixed to ‘0’ (0x30)

        10	1	A	Message code: ‘G’ (0x47)

        11	8	N	Cash register ID

        19	1	N	Presence of message with additional data for the GT:
				‘0’ = additional data message not present
				‘1’ = additional data message present

        20	3	N	Reserved (fixed at “0” 30 hex)
        
     * Final Frame:
     *   [STX] + BODY(167 byte ASCII) + [ETX] + [LRC]
     * 
     * 
     * 
     * @param terminalId
     * @param messageCode
     * @param enableDisablePrinttingOnECR
     * @param typeTicket
     * 
     * @return 
     * 
     */
    public static byte[] buildReprintTicketFrame(String terminalId,
                                                 char messageCode,
                                                 char enableDisablePrinttingOnECR,   
                                                 char typeTicket   
                                                ) {

        // Build BODY 
        StringBuilder app = new StringBuilder(167);

        // 1..8: Numeric TID, padded to 8 digits
        app.append(padDigits(terminalId, 8));

        // 9: caharacter '0' (reserved)
        app.append('0');

        // 10: message code (es. 'R' for reprint ticket request)
        app.append(messageCode);
        
        // 11: enable/disable ticket on ECR (e.g. '0' disable printing on ECR, '1' enable printing on ECR)
        app.append(enableDisablePrinttingOnECR);
        
        // 12: type ticket (e.g. '0' last financial ticket, '1' last service ticket)
        app.append(typeTicket);
        
        // 13..22: character '0' (reserved)
        app.append(padDigits("0",10));

        // Convert BODY to ASCII byte array
        byte[] body = app.toString().getBytes(StandardCharsets.US_ASCII);

        /**
         * 
         * Build frame:
         *  [0]           = STX
         *  [1..body.len] = BODY ASCII
         *  [body.len+1]  = ETX
         *  [body.len+2]  = LRC (calculated using lrcWithBase0x7F)
         */
        byte[] out = new byte[1 + body.length + 1 + 1];
        int i = 0;

        // STX
        out[i++] = STX;

        // Copy BODY
        System.arraycopy(body, 0, out, i, body.length);
        i += body.length;

        // ETX
        out[i++] = ETX;

        // LRC calculated from STX to ETX included
        out[i] = lrcWithBase0x7F(out, 0, i);
        
        return out;
        
    }  //public static byte[] buildReprintTicketFrame
    
     /**
     * 
     * Build the Enable Disable Printing Receipt request frame (message code 'E')
     * in accordance with Nexi documentation.
     *
     *   Pos	Length	Type	Content
     *   1	8	N	Terminal ID (00000000-99999999)
     *
     *   9	1	A	Reserved – fixed to ‘0’ (0x30)
     *
     *   10	1	A	Message code: ‘E’ (0x45)
     *
     *   11	1	N	Enable disable printing receipt on ECR flag:
     *                           ‘0’ - Disable printing receipt on ECR 
     *                                 (any receipt will be printed by the terminal).
     *                           ‘1’ - Enable printing receipt on ECR (any receipt 
     *                                 will be sent to the ECR by using the "S" command).
     *                                 * 
     * Final frame :
     *   [STX] + BODY(167 byte ASCII) + [ETX] + [LRC]
     * 
     * 
     * 
     * @param terminalId
     * @param messageCode
     * @param enableDisable
     * 
     * @return 
     * 
     */
    public static byte[] buildEnableDisablePrintingReceiptFrame(String terminalId,
                                                                char messageCode,
                                                                char enableDisable   
                                                               ) {

        // Build BODY 
        StringBuilder app = new StringBuilder(167);

        // 1..8:Numeric TID, padded to 8 digits
        app.append(padDigits(terminalId, 8));

        // 9: character '0' (reserved)
        app.append('0');

        // 10: message code 'E'
        app.append(messageCode);
        
        // 11: '0' print ECR disabled; '1' print ECR Enabled
        app.append(enableDisable);
        
        // Convert BODY to ASCII byte array
        byte[] body = app.toString().getBytes(StandardCharsets.US_ASCII);

        /**
         * 
         * Build frame:
         *  [0]           = STX
         *  [1..body.len] = BODY ASCII
         *  [body.len+1]  = ETX
         *  [body.len+2]  = LRC (calculated using lrcWithBase0x7F)
         */
        byte[] out = new byte[1 + body.length + 1 + 1];
        int i = 0;

        // STX
        out[i++] = STX;

        // Copy BODY
        System.arraycopy(body, 0, out, i, body.length);
        i += body.length;

        // ETX
        out[i++] = ETX;

        // LRC calculated from STX to ETX included
        out[i] = lrcWithBase0x7F(out, 0, i);
        
        return out;
        
    }  //public  byte[] buildEnableDisablePrintingReceiptFrame
    
    
    /**
     * 
     * Build the Pre-authorization request frame (message code 'p')
     * in accordance with the Nexi documentation.
     *
        Pos	Length	Type	Content
        1	8	N	Terminal ID (00000000-99999999)

        9	1	A	Reserved – fixed to ‘0’ (0x30)

        10	1	A	Message code: ‘p’ (0x70)

        11	8	N	Cash register ID
        
        19	1	N	Presence of message with additional data for the GT:
				‘0’ = additional data message not present
				‘1’ = additional data message present
					
        20	2	N	Reserved – fixed to ‘0’ (0x30)

        22	1	N	Start transaction when card already present:
				‘0’ = start with card not yet inserted in the terminal
				‘1’ = start with card already inserted in the terminal
					
        23	1	N	Payment type:
				‘0’ = automatic card recognition on terminal with customer 
                                      selection in case of cobranded cards
				‘1’ = only debit cards
				‘2’ = only credit cards
				‘3’ = other cards
					
        24	8	N	Pre-authorization transaction amount, right aligned, 
				filled with ‘0’ (0x30) on the left. 
				Amount is always considered to be expressed in cents (e.g. 650 = 6,50)
					
        32	128	AN	Text to be print (Code contract)at the end of the payment receipt. 
				The field is always 128 chars long, it’s right aligned, 
				filled with ‘ ‘ (0x20) on the left. 
				The terminal shall only print valid chars, skipping the blanks.
					
        160	8	N	Reserved – fixed to ‘0’ (0x30)
        
     * Final Frame :
     *   [STX] + BODY(167 byte ASCII) + [ETX] + [LRC]
     * 
     * 
     * @param terminalId
     * @param messageCode
     * @param cashRegisterId
     * @param additionalDataFlag
     * @param startWithCardPresent
     * @param paymentType
     * @param amountCents
     * @param textInput
     * 
     * @return 
     */
    public static byte[] buildPreauthorizationFrame(String terminalId,
                                                   char messageCode,
                                                   String cashRegisterId,
                                                   char additionalDataFlag,
                                                   char startWithCardPresent,
                                                   char paymentType,
                                                   String amountCents,
                                                   String textInput
                                                  ) {

        // Build BODY 
        StringBuilder app = new StringBuilder(167);

        // 1..8: Numeric TID, padded to 8 digits
        app.append(padDigits(terminalId, 8));

        // 9: character '0' (reserved)
        app.append('0');

        // 10: message code 
        app.append(messageCode);

        // 11..18: ECR numeric ID, 8 digits
        app.append(padDigits(cashRegisterId, 8));

        // 19: additional data flag ('0'/'1'), forced to numeric
        app.append(sanitizeDigit(additionalDataFlag));

        // 20..21: "00" (reserved)
        app.append("00");

        // 22: flag "start with card already present" ('0'/'1')
        app.append(sanitizeDigit(startWithCardPresent));

        // 23: payment type (0..3)
        app.append(sanitizeDigit(paymentType));

        // 24..31: amount in cents (8 digits)
        app.append(padDigits(amountCents, 8));

        // 32..159: text to print, right aligned, padded with spaces, to left
        app.append(padLeftSpacesToWidth(textInput, 128));

        // 160..167: "00000000" (reserved)
        app.append("00000000");

        // Convert BODY to ASCII byte array
        byte[] body = app.toString().getBytes(StandardCharsets.US_ASCII);

        /**
         * Build Frame :
         *  [0]           = STX
         *  [1..body.len] = BODY ASCII
         *  [body.len+1]  = ETX
         *  [body.len+2]  = LRC (calculated using lrcWithBase0x7F)
         */
        byte[] out = new byte[1 + body.length + 1 + 1];
        int i = 0;

        // STX
        out[i++] = STX;

        // BODY copy
        System.arraycopy(body, 0, out, i, body.length);
        i += body.length;

        // ETX
        out[i++] = ETX;

        // LRC calculated from STX to ETX included
        out[i] = lrcWithBase0x7F(out, 0, i);

        return out;
        
    }  //public static byte[] buildPreauthoritizaionFrame
    
    
      /**
       * 
     * 
     * Build the Pre-authorization incremental request frame (message code 'i')
     * in accordance with the Nexi documentation.
     *
        Pos	Length	Type	Content
        1	8	N	Terminal ID (00000000-99999999)

        9	1	A	Reserved – fixed to ‘0’ (0x30)

        10	1	A	Message code: ‘i’ (0x69)

        11	8	N	Cash register ID

        19	1	N	Presence of message with additional data for the GT:
				‘0’ = additional data message not present
				‘1’ = additional data message present
					
        20	4	N	Reserved – fixed to ‘0’ (0x30)

        24	8	N	Authorization transaction amount, right aligned, 
				filled with ‘0’ (0x30) on the left. Amount 
				is always considered to be expressed in cents (e.g. 650 = 6,50)
					
        32	128	AN	Text to be print (Code contract) at the end of the payment receipt. 
				The field is always 128 chars long, it’s right aligned, 
				filled with ‘ ‘ (0x20) on the left. 
				The terminal shall only print valid chars, skipping the blanks.
					
        160	9	N	Unique identifier of the pre-authorization received from Host 
				and printed on the pre-authorization receipt issued by 
				the terminal (Original Preathorization Code)
					
        169	8	N	Reserved – fixed to ‘0’ (0x30)
    
     * Fianl frame:
     *   [STX] + BODY(167 byte ASCII) + [ETX] + [LRC]
     * 
     * 
     * 
     * @param terminalId
     * @param messageCode
     * @param cashRegisterId
     * @param additionalDataFlag
     * @param amountCents
     * @param preAuthorizationCode
     * @param textInput
     * 
     * @return 
     * 
     */
    public static byte[] buildPreauthorizationIncrementalFrame(String terminalId,
                                                               char messageCode,
                                                               String cashRegisterId,
                                                               char additionalDataFlag,
                                                               String amountCents,
                                                               String preAuthorizationCode,
                                                               String textInput
                                                              ) {

        // Build BODY 
        StringBuilder app = new StringBuilder(167);

        // 1..8: Numeric TID, padded to 8 digits
        app.append(padDigits(terminalId, 8));

        // 9: character '0' (reserved)
        app.append('0');

        // 10: message code 
        app.append(messageCode);

        // 11..18: ECR numeric ID, 8 digits
        app.append(padDigits(cashRegisterId, 8));

        // 19: additional data flag ('0'/'1'), forced to numeric
        app.append(sanitizeDigit(additionalDataFlag));

        // 20..23: "0" (reserved)
        app.append(padDigits("0", 4));

        // 24..31: amount in cents (8 cifre)
        app.append(padDigits(amountCents, 8));

        // 32..159: text to print, right aligned, padded with spaces, to left
        app.append(padLeftSpacesToWidth(textInput, 128));

        // 160..168: pre-authorization code
        app.append(padLeftSpacesToWidth(preAuthorizationCode,9));
        
         // 169..177: reserved
        app.append(padDigits("0", 8));

        // Convert BODY to ASCII byte array
        byte[] body = app.toString().getBytes(StandardCharsets.US_ASCII);

        /**
         * 
         * Build frame:
         *  [0]           = STX
         *  [1..body.len] = BODY ASCII
         *  [body.len+1]  = ETX
         *  [body.len+2]  = LRC (calculated using lrcWithBase0x7F)
         */
        byte[] out = new byte[1 + body.length + 1 + 1];
        int i = 0;

        // STX
        out[i++] = STX;

        // BODY copy
        System.arraycopy(body, 0, out, i, body.length);
        i += body.length;

        // ETX
        out[i++] = ETX;

        // LRC calculated from STX to ETX included
        out[i] = lrcWithBase0x7F(out, 0, i);

        return out;
        
    }  //public static byte[] buildPreauthorizationIncrementalFrame
    
    
      /**
     * Build the Pre-authorization Closure request frame (message code 'c')
     * in accordance with the Nexi documentation.
        Pos	Length	Type	Content
        1	8	N	Terminal ID (00000000-99999999)

        9	1	A	Reserved – fixed to ‘0’ (0x30)

        10	1	A	Message code: ‘i’ (0x63)

        11	8	N	Cash register ID

        19	1	N	Presence of message with additional data for the GT:
				‘0’ = additional data message not present
				‘1’ = additional data message present
					
        20	4	N	Reserved – fixed to ‘0’ (0x30)

        24	8	N	Authorization transaction amount, right aligned, 
				filled with ‘0’ (0x30) on the left. Amount is always considered 
				to be expressed in cents (e.g. 650 = 6,50)
					
        32	128	AN	Text to be print (Code contract) at the end of the payment receipt. 
				The field is always 128 chars long, it’s right aligned, 
				filled with ‘ ‘ (0x20) on the left. 
				The terminal shall only print valid chars, skipping the blanks.
					
        160	9	N	Unique identifier of the pre-authorization received 
				from Host and printed on the pre-authorization receipt 
				issued by the terminal (Original Preathorization Code)
					
        169	8	N	Reserved – fixed to ‘0’ (0x30)
        
     * Frame finale:
     *   [STX] + BODY(167 byte ASCII) + [ETX] + [LRC]
     * 
     * 
     * 
     * @param terminalId
     * @param messageCode
     * @param cashRegisterId
     * @param additionalDataFlag
     * @param amountCents
     * @param codePreauthorization
     * @param textInput
     * 
     * @return 
     */
    public static byte[] buildPreauthorizationClosureFrame(String terminalId,
                                                           char messageCode,
                                                           String cashRegisterId,
                                                           char additionalDataFlag,
                                                           String amountCents,
                                                           String codePreauthorization,
                                                           String textInput
                                                          ) {

        // Build BODY 
        StringBuilder app = new StringBuilder(167);

        // 1..8: Numeric TID, padded to 8 digits
        app.append(padDigits(terminalId, 8));

        // 9: character '0' (reserved)
        app.append('0');

        // 10: message code 
        app.append(messageCode);

        // 11..18: ECR numeric ID, 8 digits
        app.append(padDigits(cashRegisterId, 8));

        // 19: additional data flag ('0'/'1'), forced to numeric
        app.append(sanitizeDigit(additionalDataFlag));

        // 20..23: "0" (reserved)
        app.append(padDigits("0", 4));

        // 24..31: amount in cents (8 digits)
        app.append(padDigits(amountCents, 8));

        // 32..159: text to print, right aligned, padded with spaces, to left
        app.append(padLeftSpacesToWidth(textInput, 128));

        // 160..168: pre-authorization code
        app.append(padLeftSpacesToWidth(codePreauthorization,9));
        
         // 169..177: reserved
        app.append(padDigits("0", 8));

        // Convert BODY to ASCII byte array
        byte[] body = app.toString().getBytes(StandardCharsets.US_ASCII);

        /**
         * 
         * Build frame:
         *  [0]           = STX
         *  [1..body.len] = BODY ASCII
         *  [body.len+1]  = ETX
         *  [body.len+2]  = LRC (calculated using lrcWithBase0x7F)
         */
        byte[] out = new byte[1 + body.length + 1 + 1];
        int i = 0;

        // STX
        out[i++] = STX;

        // BODY copy
        System.arraycopy(body, 0, out, i, body.length);
        i += body.length;

        // ETX
        out[i++] = ETX;

        // LRC calculated from STX to ETX included
        out[i] = lrcWithBase0x7F(out, 0, i);

        return out;
        
    }  //public static byte[] buildPreauthorizationClosureFrame
    
     /**
     * 
     * Build the Close Session request frame (message code 'C')
     * in accordance with the Nexi documentation.
     *
     *  Pos	Length          Type            Content
     *  1	8		N		Terminal ID (00000000-99999999)
     *
     *  9	1		A		Reserved – fixed to ‘0’ (0x30)
     *
     *  10	1		A		Message code: ‘C’ (0x43)
     *
     *  11	8		N		Cash register ID
     *
     *  19	1		N		Presence of message with additional data for the GT:
     *                                          ‘0’ = additional data message not present
     *                                          ‘1’ = additional data message present
     *					
     *  20	7		N		Reserved – fixed to ‘0’ (0x30)
     *
     * Final Frame:
     *   [STX] + BODY(167 byte ASCII) + [ETX] + [LRC]
     * 
     * 
     * 
     * @param terminalId
     * @param messageCode
     * @param cashRegisterId
     * @param additionalDataFlag
     * @return 
     * 
     */
    public static byte[] buildCloseSessionFrame(String terminalId,
                                                char messageCode,
                                                String cashRegisterId,
                                                char additionalDataFlag
                                               ) {

        // Build BODY 
        StringBuilder app = new StringBuilder(167);

        // 1..8: Numeric TID, padded to 8 digits
        app.append(padDigits(terminalId, 8));

        // 9: character '0' (reserved)
        app.append('0');

        // 10: message code 
        app.append(messageCode);

        // 11..18: ECR numeric ID, 8 digits
        app.append(padDigits(cashRegisterId, 8));

        // 19 : additional data flag
        app.append(additionalDataFlag);
        
        // 20..26: '0' reserved
        app.append(padDigits("0",7));

        // Convert BODY to ASCII byte array
        byte[] body = app.toString().getBytes(StandardCharsets.US_ASCII);

        /**
         * Build frame:
         *  [0]           = STX
         *  [1..body.len] = BODY ASCII
         *  [body.len+1]  = ETX
         *  [body.len+2]  = LRC (calculated using lrcWithBase0x7F)
         */
        byte[] out = new byte[1 + body.length + 1 + 1];
        int i = 0;

        // STX
        out[i++] = STX;

        // BODY copy
        System.arraycopy(body, 0, out, i, body.length);
        i += body.length;

        // ETX
        out[i++] = ETX;

        // LRC calculated from STX to ETX included
        out[i] = lrcWithBase0x7F(out, 0, i);

        return out;
        
    }  //public static byte[] buildCloseSessionFrame
    
    /**
     * 
     * Build the terminal Total request frame (message code 'T')
     * in accordance with the Nexi documentation.
     *
        Pos	Length	Type	Content
        1	8	N	Terminal ID (00000000-99999999)

        9	1	A	Reserved – fixed to ‘0’ (0x30)

        10	1	A	Message code: ‘T’ (0x54)

        11	8	N	Cash register ID

        19	1	N	Presence of message with additional data for the GT:
				‘0’ = additional data message not present
				‘1’ = additional data message present
    
     * Final Frmae :
     *   [STX] + BODY(167 byte ASCII) + [ETX] + [LRC]
     * 
     * 
     * 
     * @param terminalId
     * @param messageCode
     * @param cashRegisterId
     * @param additionalDataFlag
     * 
     * @return 
     * 
     */
    public static byte[] buildTerminalTotalsFrame(String terminalId,
                                                  char messageCode,
                                                  String cashRegisterId,
                                                  char additionalDataFlag
                                                 ) {

        // Build BODY
        StringBuilder app = new StringBuilder(167);

        // 1..8: Numeric TID, padded to 8 digits
        app.append(padDigits(terminalId, 8));

        // 9: character '0' (reserved)
        app.append('0');

        // 10: message code
        app.append(messageCode);

        // 11..18: ECR numeric ID, 8 digits
        app.append(padDigits(cashRegisterId, 8));

        // 19 : additional data flag
        app.append(additionalDataFlag);
        
        // 20..26: '0' reserved
        app.append(padDigits("0",7));

        // Convert BODY to ASCII byte array
        byte[] body = app.toString().getBytes(StandardCharsets.US_ASCII);

        /**
         * Build frame:
         *  [0]           = STX
         *  [1..body.len] = BODY ASCII
         *  [body.len+1]  = ETX
         *  [body.len+2]  = LRC (calculated using lrcWithBase0x7F)
         */
        byte[] out = new byte[1 + body.length + 1 + 1];
        int i = 0;

        // STX
        out[i++] = STX;

        // BODY copy
        System.arraycopy(body, 0, out, i, body.length);
        i += body.length;

        // ETX
        out[i++] = ETX;

        // LRC calculated from STX to ETX included
        out[i] = lrcWithBase0x7F(out, 0, i);

        return out;
        
    }  //public static byte[] buildTerminalTotalsFrame
    
} // end of Class
