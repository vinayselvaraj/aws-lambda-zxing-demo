package zxingdemo;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import java.awt.image.BufferedImage;
import java.util.*;

public class ZxingDecoder implements Runnable {

    private BufferedImage image;
    private QRParserResponse response;
    private int page;

    public ZxingDecoder(BufferedImage image, QRParserResponse response, int page) {
        this.image = image;
        this.response = response;
        this.page = page;
    }

    @Override
    public void run() {

        System.out.println("START Parsing page: " + page);

        try {
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                    new BufferedImageLuminanceSource(image)));
            QRCodeMultiReader barcodeReader = new QRCodeMultiReader();
            Result[] qrCodeResults = barcodeReader.decodeMultiple(binaryBitmap, buildHints());

            for (Result qrCodeResult : qrCodeResults) {

                QRCode code = new QRCode();
                code.setPage(page);
                code.setData(qrCodeResult.getText());

                List<Point> points = new ArrayList<>();
                ResultPoint[] resultPoints = qrCodeResult.getResultPoints();
                for (ResultPoint resultPoint : resultPoints) {
                    Point point = new Point();
                    point.setX(resultPoint.getX());
                    point.setY(resultPoint.getY());
                    points.add(point);
                }
                code.setGeometry(points);

                System.out.println("Detected QR code in page: " + page);
                response.addParseQRCode(code);
            }
        } catch(NotFoundException nfe) {}

        System.out.println("END Parsing page: " + page);
    }

    /**
     * Builds hints for Zxing
     * @return
     */
    private Map<DecodeHintType,?> buildHints() {
        List<BarcodeFormat> possibleFormats = new ArrayList<>(Arrays.asList(
                BarcodeFormat.QR_CODE,
                BarcodeFormat.UPC_A,
                BarcodeFormat.UPC_E,
                BarcodeFormat.EAN_13,
                BarcodeFormat.EAN_8,
                BarcodeFormat.RSS_14,
                BarcodeFormat.RSS_EXPANDED,
                BarcodeFormat.CODE_39,
                BarcodeFormat.CODE_93,
                BarcodeFormat.CODE_128,
                BarcodeFormat.ITF,
                BarcodeFormat.DATA_MATRIX,
                BarcodeFormat.AZTEC,
                BarcodeFormat.PDF_417,
                BarcodeFormat.CODABAR,
                BarcodeFormat.MAXICODE
        ));

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, possibleFormats);

        // Try harder
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        return Collections.unmodifiableMap(hints);
    }

}
