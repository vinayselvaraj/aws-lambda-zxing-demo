package zxingdemo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LambdaHandler implements RequestHandler<Map<String,String>, String> {

    private static final Logger logger = LoggerFactory.getLogger(LambdaHandler.class);


    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final String PDF_TYPE = "application/pdf";
    private static final String TIFF_TYPE = "image/tiff";

    private static final int blurCount = 5;
    private static final int threadPoolSize = 32;

    /**
     * Lambda handler
     *
     * @param event
     * @param context
     * @return
     */
    @Override
    public String handleRequest(Map<String, String> event, Context context) {

        // Detect file type (PNG, JPEG, TIFF, PDF, etc..)

        return null;
    }

    /**
     * Processes an input file
     *
     * @param f File to process
     * @throws IOException
     */
    private void processInputFile(File f, QRParserResponse response) throws IOException, InterruptedException {


        // Get all pages in the document as images
        List<BufferedImage> pages = extractPages(f);
        logger.debug(String.format("# of pages: %d", pages.size()));

        // Init Rekognition client
        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();

        // Init executor services
        ExecutorService rekognitionExecutor = Executors.newFixedThreadPool(1);
        ExecutorService zxingExecutor = Executors.newFixedThreadPool(threadPoolSize);

        int pageCounter = 1;

        for(BufferedImage page : pages) {

            // Send image to Amazon Rekognition
            rekognitionExecutor.execute(new RekognitionDetector(rekognitionClient, deepCopyImage(page), response, pageCounter));

            // Parse QR codes
            for(int k=0; k<blurCount; k++) {

                zxingExecutor.execute(new ZxingDecoder(deepCopyImage(page), response, pageCounter));

                float[] blurKernel = {1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f};
                BufferedImageOp blur = new ConvolveOp(new Kernel(3, 3, blurKernel));
                page = blur.filter(page, null);
            }

            pageCounter += 1;
        }

        zxingExecutor.shutdown();
        rekognitionExecutor.shutdown();

        zxingExecutor.awaitTermination(15, TimeUnit.MINUTES);
        rekognitionExecutor.awaitTermination(15, TimeUnit.MINUTES);

    }

    /**
     * Creates a copy of a BufferedImage
     * Taken from https://stackoverflow.com/a/26894825
     * @param bi BufferedImage to copy
     * @return
     */
    private BufferedImage deepCopyImage(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    /**
     * Reads the input file and returns an List of BufferedImage for each frame/page
     *
     * @param f Input File (PDF or TIFF)
     * @return List of BufferedImages
     */
    private List<BufferedImage> extractPages(File f) throws IOException {
        List<BufferedImage> images = new ArrayList();

        // Detect file type
        String contentType = Files.probeContentType(f.toPath());
        logger.debug(String.format("Detect file type: %s", contentType));

        // TIFF Image
        if(contentType.equals(TIFF_TYPE)) {
            ImageInputStream is = ImageIO.createImageInputStream(f);
            Iterator<ImageReader> iterator = ImageIO.getImageReaders(is);
            ImageReader reader = iterator.next();
            reader.setInput(is);

            for(int i = 0; i<reader.getNumImages(true); i++) {
                BufferedImage img = reader.read(i);
                images.add(img);
            }
        }

        // PDF Document
        if(contentType.equals(PDF_TYPE)) {
            PDDocument document = PDDocument.load(f);
            PDFRenderer renderer = new PDFRenderer(document);

            PDPageTree pageTree = document.getDocumentCatalog().getPages();
            for(int i=0; i < pageTree.getCount(); i++) {
                BufferedImage img = renderer.renderImageWithDPI(i, 1200, ImageType.GRAY);
                images.add(img);
            }
            document.close();
        }

        return images;
    }



    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 0) {
            System.err.println("SYNTAX: LambdaHandler <input file>");
            System.exit(1);
        }

        QRParserResponse response = new QRParserResponse();

        LambdaHandler handler = new LambdaHandler();
        handler.processInputFile(new File(args[0]), response);

        System.out.println(response);
    }
}
