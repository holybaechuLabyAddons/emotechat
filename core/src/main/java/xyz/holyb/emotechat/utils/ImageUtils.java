package xyz.holyb.emotechat.utils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ImageUtils {
  public String getBase64FromImage(BufferedImage image) throws IOException {
      final ByteArrayOutputStream os = new ByteArrayOutputStream();

      ImageIO.write(image, "png", os);
      return Base64.getEncoder().encodeToString(os.toByteArray());
  }

  public List<BufferedImage> getBufferedImagesFromGIF(String url) throws IOException {
    URL urlObject = new URL(url);

    ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
    ImageInputStream inputStream = ImageIO.createImageInputStream(urlObject.openStream());
    reader.setInput(inputStream, false);

    int num = reader.getNumImages(true);

    List<BufferedImage> images = new ArrayList<>();

    for (int i = 0; i < num; i++) {
      BufferedImage image = reader.read(i);
      if (image != null) {
        images.add(image);
      }
    }

    return images;
  }
}