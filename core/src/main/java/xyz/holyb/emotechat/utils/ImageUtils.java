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
  private Map<Integer, List<BufferedImage>> cachedGIFs = new HashMap<>();
  private Map<BufferedImage, String> cachedBase64s = new HashMap<>();


  public String getBase64FromImage(BufferedImage image) throws IOException {
    if (cachedBase64s.get(image) == null){
      final ByteArrayOutputStream os = new ByteArrayOutputStream();

      ImageIO.write(image, "png", os);
      cachedBase64s.put(image, Base64.getEncoder().encodeToString(os.toByteArray()));
    }

    return cachedBase64s.get(image);
  }

  private List<BufferedImage> java(URL url) throws IOException {
    ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
    ImageInputStream inputStream = ImageIO.createImageInputStream(url.openStream());
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

  public List<BufferedImage> getBufferedImagesFromGIF(String url) throws IOException {
    if (cachedGIFs.get(url.hashCode()) != null) {
      return cachedGIFs.get(url.hashCode());
    }

    URL urlObject = new URL(url);

    cachedGIFs.put(url.hashCode(), java(urlObject));

    return getBufferedImagesFromGIF(url);
  }
}