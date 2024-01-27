package xyz.holyb.emotechat.listener;

import net.labymod.api.client.chat.ChatMessage;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.util.logging.Logging;
import xyz.holyb.emotechat.EmoteChatAddon;
import xyz.holyb.emotechat.bttv.BTTVEmote;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatReceiveListener {
  private final EmoteChatAddon addon;

  private final Pattern matchRegex = Pattern.compile("<:([a-zA-Z0-9]+):([a-z0-9]+)@(BTTV)>"); // ex: <:pepoG:5d63e543375afb1da9a68a5a@BTTV>

  public ChatReceiveListener(EmoteChatAddon addon) {
    this.addon = addon;
  }

  private Component replaceEmote(String message){
    Component newComponent = Component.empty();
    List<Component> componentChildren = new ArrayList<>();

    for (String word : message.split(" ")){
      Matcher matcher = this.matchRegex.matcher(word);

      if (!matcher.matches()) {
        componentChildren.add(Component.text(word + " "));
        continue;
      }

      Component emoteComponent = Component.empty();
      emoteComponent.setChildren(Arrays.asList(
          Component.icon(
                  Icon.url(String.format(BTTVEmote.EMOTE_IMAGE_ENDPOINT, matcher.group(2), addon.configuration().emoteQuality().get())))
              .setSize(addon.configuration().emoteSize().get()),
          Component.text(" ")));

      componentChildren.add(emoteComponent);
    }

    newComponent.setChildren(componentChildren);

    return newComponent;
  }

  private List<Component> replaceEmoteFromComponents(List<Component> components){
    List<Component> newComponents = new ArrayList<>();
    boolean containsEmote = false;

    for (int i = 0, componentsSize = components.size(); i < componentsSize; i++) {
      Component component = components.get(i);

      if (!component.getChildren().isEmpty()) replaceEmoteFromComponents(component.getChildren());

      if (component instanceof TextComponent textComponent) {
        boolean isEmote = false;

        for (String word : textComponent.getText().split(" ")) {
          Matcher matcher = this.matchRegex.matcher(word);

          if (matcher.matches()) {
            isEmote = true;
            containsEmote = true;
            newComponents.add(replaceEmote(textComponent.getText()));

            break;
          }
        }

        if (!isEmote) newComponents.add(component);
      }
    }

    return containsEmote ? newComponents : components;
  }

  @Subscribe
  public void onChatReceive(ChatReceiveEvent event) {
    if (!addon.configuration().enabled().get()) return;

    ChatMessage message = event.chatMessage();

    if (message.component().getChildren().isEmpty()){
      event.setMessage(replaceEmote(message.getFormattedText()));
    }else {
      message.component().setChildren(replaceEmoteFromComponents(message.component().getChildren()));
      event.setMessage(message.component());
    }
  }
}
