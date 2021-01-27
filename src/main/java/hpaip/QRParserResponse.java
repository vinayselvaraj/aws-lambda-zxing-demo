package hpaip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QRParserResponse {
    private List<GSTQRCode> parsedQRCodes;

    public QRParserResponse() {
        parsedQRCodes = Collections.synchronizedList(new ArrayList<GSTQRCode>());
    }


    public List<GSTQRCode> getParsedQRCodes() {
        return parsedQRCodes;
    }

    public void setParsedQRCodes(List<GSTQRCode> parsedQRCodes) {
        this.parsedQRCodes = parsedQRCodes;
    }

    /**
     * Adds the QR code
     *
     * @param code
     */
    public void addParseQRCode(GSTQRCode code) {
        parsedQRCodes.add(code);
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(String.format("ParsedQRCodes=%s,", parsedQRCodes.toString()));
        buff.append(String.format("ParsedCount=%d", parsedQRCodes.size()));
        return buff.toString();
    }
}
