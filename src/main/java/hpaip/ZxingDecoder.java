package hpaip;

import com.google.gson.Gson;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.*;
import java.util.List;

public class ZxingDecoder implements Runnable {

    private BufferedImage image;
    private QRParserResponse response;
    private int page;
    private int blur;
    private int resize;

    public ZxingDecoder(BufferedImage image, QRParserResponse response, int page, int blur, int resize) {
        this.image = image;
        this.response = response;
        this.page = page;
        this.blur = blur;
        this.resize = resize;
    }

    @Override
    public void run() {

        // Exit if at least one QR code was parsed.
        if(response.getParsedQRCodes().size() > 0) {
            return;
        }

        System.out.println(String.format("START Decoding page=%d blur=%d resize=%d threadId=%s", page, blur, resize, Thread.currentThread().getId()));
        Gson gson = new Gson();

        try {

            if(blur > 0) {
                for(int i=0; i<blur; i++) {
                    float[] blurKernel = {1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f};
                    BufferedImageOp blur = new ConvolveOp(new Kernel(3, 3, blurKernel));
                    image = blur.filter(image, null);
                }
            }

            if(resize > 0) {
                for(int i=0; i<resize; i++) {
                    int newWidth = (int) (image.getWidth() * 0.9);
                    int newHeight = (int) (image.getHeight() * 0.9);

                    // Resize
                    image = resize(image, newWidth, newHeight);
                }
            }

            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                    new BufferedImageLuminanceSource(image)));
            QRCodeMultiReader barcodeReader = new QRCodeMultiReader();
            Result[] qrCodeResults = barcodeReader.decodeMultiple(binaryBitmap, buildHints());

            Base64.Decoder b64Decoder = Base64.getDecoder();

            // Search for GST Signed QR Codes
            for (Result qrCodeResult : qrCodeResults) {

                String data = qrCodeResult.getText();

                String[] splitData = data.split("\\.");
                if(splitData.length != 3) {
                    continue;
                }

                String decodedSigned = null;
                String content = null;

                try {
                    decodedSigned = new String(b64Decoder.decode(splitData[0]));
                    content = new String(b64Decoder.decode(splitData[1]));
                    if(!content.contains("Irn")) {
                        // Skipping since key field was not found
                        continue;
                    }
                } catch(IllegalArgumentException iae) {
                    iae.printStackTrace();
                    // Unable to do base64 decoding
                }

                GSTQRCode code = new GSTQRCode();
                code.setPage(page);
                code.setDecodedSigned(gson.fromJson(decodedSigned, Map.class));
                Map<String, String> contentMap = gson.fromJson(content, Map.class);
                code.setData(gson.fromJson(contentMap.get("data"), Map.class));

                List<Point> points = new ArrayList<>();
                ResultPoint[] resultPoints = qrCodeResult.getResultPoints();
                for (ResultPoint resultPoint : resultPoints) {
                    Point point = new Point();
                    point.setX(resultPoint.getX());
                    point.setY(resultPoint.getY());
                    points.add(point);
                }
                code.setGeometry(points);

                System.out.println(String.format("Detected QR code in page: %d (blur=%d, resize=%d)", page, blur, resize));
                response.addParseQRCode(code);
            }
        } catch(NotFoundException nfe) {}


        System.out.println(String.format("END Decoding page=%d blur=%d resize=%d threadId=%s", page, blur, resize, Thread.currentThread().getId()));
    }

    /**
     * Resize image
     *
     * @param img Image to resize
     * @param newW new width
     * @param newH new height
     * @return Resized image
     */
    private BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, img.getType());

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
