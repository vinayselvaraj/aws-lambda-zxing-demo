package zxingdemo;

public class QRParserResponse {
    private QRCode[] detectedQRCodes;
    private QRCode[] parsedQRCodes;

    public QRCode[] getDetectedQRCodes() {
        return detectedQRCodes;
    }

    public void setDetectedQRCodes(QRCode[] detectedQRCodes) {
        this.detectedQRCodes = detectedQRCodes;
    }

    public QRCode[] getParsedQRCodes() {
        return parsedQRCodes;
    }

    public void setParsedQRCodes(QRCode[] parsedQRCodes) {
        this.parsedQRCodes = parsedQRCodes;
    }
}
