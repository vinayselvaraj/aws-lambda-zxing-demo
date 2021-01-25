
package zxingdemo;


import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.util.*;
import java.util.List;



public class App {
    public static void main( String[] args ) throws Exception  {

        Map<DecodeHintType, ?> hints = buildHints();

        System.out.println(args[0]);

        ImageInputStream is = ImageIO.createImageInputStream(new File(args[0]));
        Iterator<ImageReader> iterator = ImageIO.getImageReaders(is);
        ImageReader reader = iterator.next();
        reader.setInput(is);

//        JFrame frame = new JFrame();
//        frame.getContentPane().setLayout(new FlowLayout());
//        frame.setVisible(true);

//        JLabel imageLabel = new JLabel();
//        frame.getContentPane().add(imageLabel);



        for(int i = 0; i<reader.getNumImages(true); i++) {
            System.out.println("Reading page: " + (i + 1));

            BufferedImage img = reader.read(i);

//            float[] blurKernel = { 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f };
//            float[] sharpenKernel = { 0.f, -1.f, 0.f, -1.f, 5.0f, -1.f, 0.f, -1.f, 0.f};


            BufferedImage editedImage = img;

            for(int k=0; k<5; k++) {
                try {

                    if(k>0) {
                        for(int j = 0; j<k; j++) {
                            float[] blurKernel = {1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f};
                            BufferedImageOp blur = new ConvolveOp(new Kernel(3, 3, blurKernel));
                            editedImage = blur.filter(editedImage, null);
                        }
                    }


//                    imageLabel.setIcon(new ImageIcon(editedImage));
//                    frame.pack();
//                    frame.repaint();

                    BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                            new BufferedImageLuminanceSource(editedImage)));
                    QRCodeMultiReader barcodeReader = new QRCodeMultiReader();
                    Result[] qrCodeResults = barcodeReader.decodeMultiple(binaryBitmap, hints);

                    for (Result qrCodeResult : qrCodeResults) {
                        System.out.println(String.format("[#%d] Applied filter: blur=%d ", (i + 1), k));
                        System.out.println(qrCodeResult.getText());
                        System.out.println(qrCodeResult.getBarcodeFormat());
                        ResultPoint[] points = qrCodeResult.getResultPoints();
                        for (ResultPoint point : points) {
                            System.out.println(String.format("X:%f, Y:%f", point.getX(), point.getY()));
                        }
                    }
                } catch(NotFoundException nfe) {
                }


            }

            //float[] sharpenKernel = { 0.f, -1.f, 0.f, -1.f, 5.0f, -1.f, 0.f, -1.f, 0.f};

            //BufferedImageOp blur = new ConvolveOp(new Kernel(3, 3, blurKernel));
            //BufferedImageOp sharpen = new ConvolveOp(new Kernel(3,3,sharpenKernel), ConvolveOp.EDGE_NO_OP, null);



//            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
//                    new BufferedImageLuminanceSource(img)));
//            QRCodeMultiReader barcodeReader = new QRCodeMultiReader();
//            Result[] qrCodeResults = barcodeReader.decodeMultiple(binaryBitmap, hints);
//
//            for (Result qrCodeResult : qrCodeResults) {
//                System.out.println(qrCodeResult.getText());
//                System.out.println(qrCodeResult.getBarcodeFormat());
//                ResultPoint[] points = qrCodeResult.getResultPoints();
//                for (ResultPoint point : points) {
//                    System.out.println(String.format("X:%f, Y:%f", point.getX(), point.getY()));
//                }
//            }

//            BufferedImage filteredImg = reader.read(i);
//
//            for(int k=-25; k<=25; k++) {
//
//                    try {
//
//                        imageLabel.setIcon(new ImageIcon(filteredImg));
//                        frame.pack();
//                        frame.repaint();
//
//                        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
//                                new BufferedImageLuminanceSource(filteredImg)));
//
//                        QRCodeMultiReader barcodeReader = new QRCodeMultiReader();
//
//                        Result[] qrCodeResults = barcodeReader.decodeMultiple(binaryBitmap, hints);
//                        for (Result qrCodeResult : qrCodeResults) {
//                            System.out.println(qrCodeResult.getText());
//                            System.out.println(qrCodeResult.getBarcodeFormat());
//                            ResultPoint[] points = qrCodeResult.getResultPoints();
//                            for (ResultPoint point : points) {
//                                System.out.println(String.format("X:%f, Y:%f", point.getX(), point.getY()));
//                            }
//                        }
//                    } catch (NotFoundException nfe) {
//                        System.out.println("Nothing found..");
//                    }
//
//
//
//                        if(k<0) {
//                            filteredImg = blur.filter(filteredImg, null);
//                        }
//
//                        if(k==0) {
//                            filteredImg = img;
//                        }
//
//                    if(k>0) {
//                        filteredImg = sharpen.filter(img, null);
//                    }
//
//                    System.out.println("===================================================");
//                    System.out.println(String.format("[#%d] Applied filter: blur=%d sharpen=%d", (i+1), k, k));
//
//            }


        }

    }


    private static Map<DecodeHintType,?> buildHints() {
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

