package xyz.holyb.emotechat.listener;

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

  private final Map<ChatReceiveEvent, List<BufferedImage>> animatedEmotes = new HashMap<>();
  private final Map<ChatReceiveEvent, Iterator<BufferedImage>> iterators = new HashMap<>();

  public IconComponent addAnimatedEmote(ChatReceiveEvent event, List<BufferedImage> bufferedImages) throws IOException {
    animatedEmotes.put(event, bufferedImages);
    iterators.put(event, bufferedImages.iterator());

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
    animatedEmotes.forEach((event, bufferedImages) -> {
      Iterator<BufferedImage> iterator = iterators.get(event);

      if (iterator.hasNext()) {
          try {
            nextFrame(event.chatMessage().component().getChildren(), "data:image/png;base64,"+imageUtils.getBase64FromImage(iterator.next()));

            event.setMessage(event.chatMessage().component());
          } catch (IOException exception) {
              throw new RuntimeException(exception);
          }
      }else {
        iterators.replace(event, bufferedImages.iterator());
      }
    });
  }
}