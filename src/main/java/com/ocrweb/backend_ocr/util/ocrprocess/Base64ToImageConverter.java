package com.ocrweb.backend_ocr.util.ocrprocess;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import io.github.cdimascio.dotenv.Dotenv;

public class Base64ToImageConverter {

    private static final Dotenv dotenv = Dotenv.load();

    public static File convertBase64ToImage(String base64String, String format, String outputFileName) throws IOException {
        if (format == null || format.isEmpty()) {
            format = "png";
        }

        if (outputFileName == null || outputFileName.isEmpty()) {
            outputFileName = "output_image";
        }

        byte[] imageBytes = Base64.getDecoder().decode(base64String);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        BufferedImage image = ImageIO.read(bis);
        bis.close();

        String outputDirectory = dotenv.get("IMAGE_OUTPUT_DIRECTORY");
        File outputFile = new File(outputDirectory + outputFileName + "." + format);

        outputFile.getParentFile().mkdirs();
        ImageIO.write(image, format, outputFile);

        return outputFile;
    }
}

