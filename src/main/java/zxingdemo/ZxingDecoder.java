package zxingdemo;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class ZxingDecoder implements Runnable {

    private BufferedImage image;
    private QRParserResponse response;
    private int page;
    private int blur;

    private final int resizeCount = 5;

    public ZxingDecoder(BufferedImage image, QRParserResponse response, int page, int blur) {
        this.image = image;
        this.response = response;
        this.page = page;
        this.blur = blur;
    }

    @Override
    public void run() {

        System.out.println(String.format("START Parsing page %d.  blur=%d", page, blur));

        for(int i=0; i<resizeCount; i++) {
            try {

                if(i>0) {
                    int newWidth = (int) (image.getWidth() * 0.9);
                    int newHeight = (int) (image.getHeight() * 0.9);

                    // Resize
                    image = resize(image, newWidth, newHeight);
                }


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

                    System.out.println(String.format("Detected QR code in page: %d (blur=%d, resize=%d)", page, blur, i));
                    response.addParseQRCode(code);
                }
            } catch(NotFoundException nfe) {}

        }


        System.out.println(String.format("END Parsing page %d.  blur=%d", page, blur));
    }

    private BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
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
