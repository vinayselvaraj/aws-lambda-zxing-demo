package zxingdemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QRParserResponse {
    private List<QRCode> detectedQRCodes;
    private List<QRCode> parsedQRCodes;

    public QRParserResponse() {
        detectedQRCodes = Collections.synchronizedList(new ArrayList<QRCode>());
        parsedQRCodes = Collections.synchronizedList(new ArrayList<QRCode>());
    }

    public List<QRCode> getDetectedQRCodes() {
        return detectedQRCodes;
    }

    public void setDetectedQRCodes(List<QRCode> detectedQRCodes) {
        this.detectedQRCodes = detectedQRCodes;
    }

    public List<QRCode> getParsedQRCodes() {
        return parsedQRCodes;
    }

    public void setParsedQRCodes(List<QRCode> parsedQRCodes) {
        this.parsedQRCodes = parsedQRCodes;
    }

    public void addDetectedQRCode(QRCode code) {
        detectedQRCodes.add(code);
    }

    /**
     * Adds the QR code if it doesn't already exist
     *
     * @param code
     */
    public void addParseQRCode(QRCode code) {
        boolean found = false;
        for(QRCode c : parsedQRCodes) {
            if(c.getData().equals(code.getData()) && c.getPage() == code.getPage()) {
                found = true;
                return;
            }
        }
        parsedQRCodes.add(code);
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(String.format("DetectedQRCodes=%s,", detectedQRCodes.toString()));
        buff.append(String.format("ParsedQRCodes=%s,", parsedQRCodes.toString()));
        buff.append(String.format("DetectedCount=%d,", detectedQRCodes.size()));
        buff.append(String.format("ParsedCount=%d", parsedQRCodes.size()));
        return buff.toString();
    }
}
