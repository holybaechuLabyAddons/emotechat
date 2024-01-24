package xyz.holyb.emotechat.listener;

import net.labymod.api.client.chat.ChatMessage;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import xyz.holyb.emotechat.EmoteChatAddon;
import xyz.holyb.emotechat.utils.ImageUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatReceiveListener {
  private final EmoteChatAddon addon;
  private final ImageUtils imageUtils = new ImageUtils();

  public ChatReceiveListener(EmoteChatAddon addon) {
    this.addon = addon;
  }

  @Subscribe
  public void onChatReceive(ChatReceiveEvent event) throws IOException {
    ChatMessage chatMessage = event.chatMessage();

    if (chatMessage.component().getChildren().isEmpty()){
      Component newComponent = Component.empty();
      List<Component> newChildren = new ArrayList<>();

      for (String word : chatMessage.getFormattedText().split(" ")) {
        if (!(word.startsWith(":") && word.endsWith(":"))) {
          newChildren.add(Component.text(word + " "));
          continue;
        }

        Component emojiComponent = Component.empty();
        Component iconComponent = Component.icon(Icon.url("data:image/png;base64,"+imageUtils.getBase64FromImage(imageUtils.getBufferedImagesFromGIF("https://cdn.betterttv.net/emote/602a55ca82b7c45eb1c94a9a/1x.gif").get(20))));

        emojiComponent.setChildren(Arrays.asList(iconComponent, Component.text(" ")));

        newChildren.add(emojiComponent);
      }

      newComponent.setChildren(newChildren);

      chatMessage.edit(newComponent);
    }
  }
}
