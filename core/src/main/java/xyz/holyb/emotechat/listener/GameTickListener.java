package xyz.holyb.emotechat.listener;

import net.labymod.api.client.chat.ChatMessage;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.IconComponent;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import xyz.holyb.emotechat.utils.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GameTickListener {
  private final ImageUtils imageUtils = new ImageUtils();

  private final Map<ChatMessage, List<BufferedImage>> animatedEmotes = new HashMap<>();
  private final Map<ChatMessage, Iterator<BufferedImage>> iterators = new HashMap<>();

  public IconComponent addAnimatedEmote(ChatMessage message, List<BufferedImage> bufferedImages) throws IOException {
    animatedEmotes.put(message, bufferedImages);
    iterators.put(message, bufferedImages.iterator());

    return Component.icon(Icon.url("data:image/png;base64,"+imageUtils.getBase64FromImage(bufferedImages.get(0))));
  }

  private void nextFrame(List<Component> components, String url) {
    for (Component component : components) {
      if (!component.getChildren().isEmpty()) nextFrame(component.getChildren(), url);

      if (component instanceof IconComponent iconComponent){
        iconComponent.setIcon(Icon.url(url));
      }
    }
  }

  @Subscribe
  public void onGameTick(GameTickEvent e) {
    animatedEmotes.forEach((message, bufferedImages) -> {
      Iterator<BufferedImage> iterator = iterators.get(message);

      if (iterator.hasNext()) {
          try {
            nextFrame(message.component().getChildren(), "data:image/png;base64,"+imageUtils.getBase64FromImage(iterator.next()));

            message.edit(message.component());
          } catch (IOException exception) {
              throw new RuntimeException(exception);
          }
      }else {
        iterators.replace(message, bufferedImages.iterator());
      }
    });
  }
}