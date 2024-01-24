package xyz.holyb.emotechat.utils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ImageUtils {
  private Map<String, List<BufferedImage>> cachedGIFs = new HashMap<>();
  private Map<String, BufferedImage> cachedImages = new HashMap<>();
  private Map<BufferedImage, String> cachedBase64s = new HashMap<>();


  public String getBase64FromImage(BufferedImage image) throws IOException {
    if (cachedBase64s.get(image) == null){
      final ByteArrayOutputStream os = new ByteArrayOutputStream();

      ImageIO.write(image, "png", os);
      cachedBase64s.put(image, Base64.getEncoder().encodeToString(os.toByteArray()));
    }

    return cachedBase64s.get(image);
  }

  public BufferedImage getImageFromURL(String url) throws IOException {
    if (cachedImages.get(url) == null) {
      cachedImages.put(url, ImageIO.read(new URL(url)));
    }

    return cachedImages.get(url);
  }

  public List<BufferedImage> getBufferedImagesFromGIF(String url) throws IOException {
    if (cachedGIFs.get(url) != null) {
      return cachedGIFs.get(url);
    }

    List<BufferedImage> images = new ArrayList<>();
    URL urlObject = new URL(url);
    ImageInputStream imageInputStream = ImageIO.createImageInputStream(urlObject.openStream());

    Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);

    if (readers.hasNext()) {
      ImageReader reader = readers.next();
      reader.setInput(imageInputStream);

      int numFrames = reader.getNumImages(true);

      for (int i = 0; i < numFrames; i++) {
        images.add(reader.read(i));

      }

      reader.dispose();
      imageInputStream.close();
    }

    cachedGIFs.put(url, images);

    return getBufferedImagesFromGIF(url);
  }
}
