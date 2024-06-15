package xyz.holyb.emotechat.gui;

import net.labymod.api.Laby;
import net.labymod.api.client.chat.ChatMessage;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.IconComponent;
import net.labymod.api.client.component.TranslatableComponent;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.gui.icon.Icon;
import xyz.holyb.emotechat.utils.ImageUtils;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AnimatedEmote {
  private static final Map<String, AnimatedEmote> CACHE = new ConcurrentHashMap<>();
  private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(3);

  public static List<Component> updateComponents(List<Component> components, AnimatedEmote animatedEmote) {
    List<Component> result = new ArrayList<>(components);

    for (int i = 0; i < result.size(); i++) {
      Component component = result.get(i);
      if (component instanceof TranslatableComponent translatableComponent)
        result.set(i, Component.translatable(translatableComponent.getKey()).arguments(updateComponents(translatableComponent.getArguments(), animatedEmote)));
      else if (component instanceof IconComponent) {
        result.set(i, animatedEmote.iconComponent);
      }

      List<Component> children = component.getChildren();
      if (!children.isEmpty()) component.setChildren(updateComponents(children, animatedEmote));
    }

    return result;
  }

  public static void renderEmotes() {
    for (AnimatedEmote value : CACHE.values()) {
      if (value.images == null || value.images.length == 0) {
        value.imageIndex = 0;
        continue;
      }

      int imageDelay = value.imageDelays[value.imageIndex];

      if (value.imageDelay >= imageDelay) {
        value.imageDelay = 0;

        if (value.imageIndex >= value.images.length - 1) {
          value.imageIndex = 0;
        } else {
          value.imageIndex++;
        }
      } else {
        value.imageDelay++;
      }

      value.iconComponent.setIcon(Icon.url(value.images[value.imageIndex]));

      for (ChatMessage message : value.messages) {
        Laby.labyAPI().minecraft().executeOnRenderThread(() -> message.edit(updateComponents(List.of(message.component()), value).getFirst()));
      }
    }
  }

  static {
    EXECUTOR_SERVICE.scheduleAtFixedRate(AnimatedEmote::renderEmotes, 0, 10, TimeUnit.MILLISECONDS);
  }

  public String[] images; // URLs of base64 encoded png images decoded from GIF
  public int[] imageDelays;
  public int imageDelay = 0;
  public int imageIndex = 0;
  public List<ChatMessage> messages;
  public IconComponent iconComponent;

  public static AnimatedEmote url(String url, ChatMessage message, int size) throws IOException, URISyntaxException {
    if (CACHE.containsKey(url)) {
      AnimatedEmote cached = CACHE.get(url);
      cached.messages.add(message);
      
      return cached;
    }

    AnimatedEmote animatedEmote = new AnimatedEmote();

    ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
    ImageInputStream inputStream = ImageIO.createImageInputStream(new URI(url).toURL().openStream());
    reader.setInput(inputStream, false);

    int num = reader.getNumImages(true);

    animatedEmote.images = new String[num];
    animatedEmote.imageDelays = new int[num];
    Arrays.fill(animatedEmote.imageDelays, 5);

    for (int i = 0; i < num; i++) {
      BufferedImage image = reader.read(i);
      if (image == null) continue;

      animatedEmote.images[i] = "data:image/png;base64," + ImageUtils.getBase64FromImage(image);

      IIOMetadata meta = reader.getImageMetadata(i);
      IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(meta.getNativeMetadataFormatName());
      IIOMetadataNode gce = ImageUtils.getMetadataNode(root, "GraphicControlExtension");

      int delay = Integer.parseInt(gce.getAttribute("delayTime"));
      if (delay > 0) {
        animatedEmote.imageDelays[i] = delay;
      }
    }

    CACHE.put(url, animatedEmote);

    animatedEmote.messages = new ArrayList<>(List.of(message));
    animatedEmote.iconComponent = Component.icon(Icon.url(animatedEmote.images[animatedEmote.imageIndex]), Style.builder().color(NamedTextColor.WHITE).build(), size);

    return animatedEmote;
  }
}
