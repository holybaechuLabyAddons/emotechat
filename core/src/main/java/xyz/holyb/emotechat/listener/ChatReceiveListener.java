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

import java.util.*;
import java.util.Map.Entry;

public class ChatReceiveListener {
  // TODO: integrate api to use emotes without using long string (once i buy server for that)
  // ^ reference: https://github.com/EmoteChat/EmoteChat/blob/master/emotechat-core/src/main/java/de/emotechat/addon/bttv/EmoteProvider.java

  private final EmoteChatAddon addon;

  public ChatReceiveListener(EmoteChatAddon addon) {
    this.addon = addon;
  }

  private String isEmote(String word) {
    // ex: <:pepoG:5d63e543375afb1da9a68a5a@BTTV>

    if (!(
        word.startsWith("<") &&
            word.endsWith(">")
    )) return null;

    String[] parts = word.substring(2, word.length()-1).split("[:@]");

    if (parts.length != 3) return null;
    if (
        parts[0].matches("^[A-Za-z0-9_.]+$") && // Emote slug/name
        parts[1].matches("^[A-Za-z0-9_.]+$") && // Emote ID from provider
        Objects.equals(parts[2], "BTTV") // Emote provider (only BTTV for now)
    ) return parts[1];

    return null;
  }

  private Map<String, Integer> containsEmote(String string) {
    Map<String, Integer> emotes = new HashMap<>();

    String[] words = string.split(" ");
    for (int i = 0; i < words.length; i++) {
      String word = words[i];

      String emoteID = isEmote(word);
      if (Objects.nonNull(emoteID)) emotes.put(emoteID, i);
    }

    return emotes;
  }

  private TextComponent replaceEmote(TextComponent component, Map<String, Integer> emotes) {
    // ! Needs to be called when component contains emote

    // TODO: add animated emote support

    Collection<Component> children = new ArrayList<>();

    String[] words = component.getText().split(" ");
    int lastPos = 0;
    for (Entry<String, Integer> entry : emotes.entrySet()) {
      String emoteID = entry.getKey();
      Integer emotePos = entry.getValue();

      children.add(component.copy().text(String.join(" ", Arrays.copyOfRange(words, lastPos, emotePos))+" "));

      // Emote
      children.add(
        Component.empty().setChildren(List.of(
          Component.icon(Icon.url(BTTVEmote.id(emoteID).getImageURL(addon.configuration().emoteQuality().get()))),
          Component.text(" ")
        )
      ));

      lastPos = emotePos;
    }

    children.add(component.copy().text(String.join(" ", Arrays.copyOfRange(words, lastPos+1, words.length))));

    return Component.empty().setChildren(children);
  }

  private List<Component> replaceEmoteFromComponents(List<Component> components) {
    List<Component> newComponents = new ArrayList<>();

    for (Component component : components) {
      if (!component.getChildren().isEmpty()) {
        component = Component.empty().setChildren(replaceEmoteFromComponents(component.getChildren()));
      }

      if (component instanceof TextComponent textComponent) {
        Map<String, Integer> emotes = containsEmote(textComponent.getText());

        if (!emotes.isEmpty()) {
          component = replaceEmote(textComponent, emotes);
        }
      }

      newComponents.add(component);
    }

    return newComponents;
  }

  @Subscribe
  public void onChatReceive(ChatReceiveEvent event) {
    ChatMessage message = event.chatMessage();

    if (message.component().getChildren().isEmpty()){
      Map<String, Integer> emotes = containsEmote(message.getFormattedText());

      if (emotes.isEmpty()) return;

      Task.builder(() -> {
        TextComponent component = replaceEmote(Component.text(message.getFormattedText()), emotes);
        Task.builder(() -> {
          // Execute on render thread to prevent emote not being rendered for a long time
          message.edit(component);
        }).build().executeOnRenderThread();
      }).build().execute();
    } else {
      Task.builder(() -> {
        List<Component> newChildren = replaceEmoteFromComponents(message.component().getChildren());

        if (newChildren.equals(message.component().getChildren())) return;

        // Execute on render thread to prevent emote not being rendered for a long time
        Task.builder(() -> message.edit(message.component().setChildren(newChildren))).build().executeOnRenderThread();
      }).build().execute();
    }
  }
}