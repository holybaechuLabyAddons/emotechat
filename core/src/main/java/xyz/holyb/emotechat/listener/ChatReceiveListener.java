package xyz.holyb.emotechat.listener;

import net.labymod.api.client.chat.ChatMessage;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.util.concurrent.task.Task;
import xyz.holyb.emotechat.EmoteChatAddon;
import xyz.holyb.emotechat.bttv.BTTVEmote;
import xyz.holyb.emotechat.utils.ImageUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatReceiveListener {
  private final EmoteChatAddon addon;
  private final ImageUtils imageUtils = new ImageUtils();

  private final Pattern matchRegex = Pattern.compile("<:([a-zA-Z0-9]+):([a-z0-9]+)@(BTTV)>"); // ex: <:pepoG:5d63e543375afb1da9a68a5a@BTTV>

  public ChatReceiveListener(EmoteChatAddon addon) {
    this.addon = addon;
  }

  private Component replaceEmote(String message, ChatReceiveEvent event) throws IOException {
    Component newComponent = Component.empty();
    List<Component> componentChildren = new ArrayList<>();

    for (String word : message.split(" ")){
      Matcher matcher = this.matchRegex.matcher(word);

      if (!matcher.matches()) {
        componentChildren.add(Component.text(word + " "));
        continue;
      }

      BTTVEmote emote = BTTVEmote.id(matcher.group(2));

      Component emoteComponent = Component.empty();

      if (emote.animated) {
        emoteComponent.setChildren(
            Arrays.asList(
                addon.gameTickListener.addAnimatedEmote(event, imageUtils.getBufferedImagesFromGIF(emote.getImageURL(addon.configuration().emoteQuality().get()))),
            Component.text(" ")));
      }else {
        emoteComponent.setChildren(Arrays.asList(
            Component.icon(Icon.url(emote.getImageURL(addon.configuration().emoteQuality().get())))
                .setSize(addon.configuration().emoteSize().get()),
            Component.text(" ")));
      }
      componentChildren.add(emoteComponent);
    }

    newComponent.setChildren(componentChildren);

    return newComponent;
  }

  private List<Component> replaceEmoteFromComponents(List<Component> components, ChatReceiveEvent event)
      throws IOException {
    List<Component> newComponents = new ArrayList<>();

    for (int i = 0, componentsSize = components.size(); i < componentsSize; i++) {
      Component component = components.get(i);

      if (!component.getChildren().isEmpty()) replaceEmoteFromComponents(component.getChildren(), event);

      if (component instanceof TextComponent textComponent) {
        boolean isEmote = false;

        for (String word : textComponent.getText().split(" ")) {
          Matcher matcher = this.matchRegex.matcher(word);

          if (matcher.matches()) {
            isEmote = true;
            newComponents.add(replaceEmote(textComponent.getText(), event));

            break;
          }
        }

        if (!isEmote) newComponents.add(component);
      }
    }

    return newComponents;
  }

  @Subscribe
  public void onChatReceive(ChatReceiveEvent event) {
    if (!addon.configuration().enabled().get()) return;

    ChatMessage message = event.chatMessage();

    Task.builder(() -> {
      try {
        if (message.component().getChildren().isEmpty()){
          Component component = replaceEmote(message.getFormattedText(), event);
          Task.builder(() -> message.edit(component)).build().executeOnRenderThread();
        }else {
          try {
            message.component().setChildren(replaceEmoteFromComponents(message.component().getChildren(), event));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          Task.builder(() -> message.edit(message.component())).build().executeOnRenderThread();
        }
      }catch (Exception e) {
        e.printStackTrace();
      }
    }).build().execute();
  }
}
