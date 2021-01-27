package hpaip;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;


public class RekognitionDetector implements Runnable {
    private BufferedImage image;
    private QRParserResponse response;
    private int page;
    private AmazonRekognition rekognitionClient;

    public RekognitionDetector(AmazonRekognition rekognitionClient, BufferedImage image, QRParserResponse response, int page) {
        this.image = image;
        this.response = response;
        this.page = page;
        this.rekognitionClient = rekognitionClient;
    }


    @Override
    public void run() {

        System.out.println("START RekognitionDetector page: " + page);


        try {
            DetectLabelsRequest request = new DetectLabelsRequest()
                    .withImage(getRekognitionImage())
                    .withMaxLabels(1000).withMinConfidence(50f);

            DetectLabelsResult result = rekognitionClient.detectLabels(request);
            List<Label> labels = result.getLabels();

            for (Label label : labels) {

                // Skip labels that are not QR codes
                if(!label.getName().equals("QR Code")) {
                    continue;
                }

                System.out.println("Page: " + page + " Label: " + label.getName());
                System.out.println("Confidence: " + label.getConfidence().toString() + "\n");


                List<Instance> instances = label.getInstances();
                System.out.println("Instances of " + label.getName());
                if (instances.isEmpty()) {
                    System.out.println("  " + "None");
                } else {
                    for (Instance instance : instances) {
                        System.out.println("  Confidence: " + instance.getConfidence().toString());
                        System.out.println("  Bounding box: " + instance.getBoundingBox().toString());
                    }
                }
                System.out.println("Parent labels for " + label.getName() + ":");
                List<Parent> parents = label.getParents();
                if (parents.isEmpty()) {
                    System.out.println("  None");
                } else {
                    for (Parent parent : parents) {
                        System.out.println("  " + parent.getName());
                    }
                }
                System.out.println("--------------------");
                System.out.println();

            }
        } catch (AmazonRekognitionException e) {
            e.printStackTrace();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }

        System.out.println("END RekognitionDetector page: " + page);
    }

    private Image getRekognitionImage() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        ByteBuffer byteBuffer = ByteBuffer.wrap(baos.toByteArray());
        return new Image().withBytes(byteBuffer);
    }
}
