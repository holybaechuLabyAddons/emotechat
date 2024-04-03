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
import xyz.holyb.emotechat.emote.LegacyGlobalId;
import xyz.holyb.emotechat.emote.LegacyServerEmote;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class ChatReceiveListener {
  private final EmoteChatAddon addon;

  public ChatReceiveListener(EmoteChatAddon addon) {
    this.addon = addon;
  }

  private Map<LegacyGlobalId, Integer> containsEmote(String string) {
    Map<LegacyGlobalId, Integer> emotes = new HashMap<>();

    String[] words = string.split(" ");
    for (int i = 0; i < words.length; i++) {
      String word = words[i];

      LegacyGlobalId emoteID = LegacyGlobalId.parse(addon.legacyEmoteProvider.idSplitter, word);
      if (Objects.nonNull(emoteID)) emotes.put(emoteID, i);
    }

    return emotes;
  }

  private TextComponent replaceEmote(TextComponent component, Map<LegacyGlobalId, Integer> emotes, ChatMessage message)
      throws IOException {
    // ! Needs to be called when component contains emote

    Collection<Component> children = new ArrayList<>();

    String[] words = component.getText().split(" ");
    int lastPos = 0;
    for (Entry<LegacyGlobalId, Integer> entry : emotes.entrySet()) {
      LegacyGlobalId emoteID = entry.getKey();
      Integer emotePos = entry.getValue();

      children.add(component.copy().text(String.join(" ", Arrays.copyOfRange(words, lastPos, emotePos))+" "));

      // Emote
      LegacyServerEmote serverEmote = addon.legacyEmoteProvider.retrieveEmoteByGlobalId(emoteID);
      BTTVEmote bttvEmote = new BTTVEmote(serverEmote.globalId, serverEmote.bttvId, serverEmote.name, serverEmote.imageType);

      if (bttvEmote.animated && addon.configuration().animatedEmotes().get()) {
        children.add(
            Component.empty().setChildren(List.of(
                addon.gameTickListener.addAnimatedEmote(bttvEmote, message),
                Component.text(" ")
            ))
        );
      } else {
        children.add(
          Component.empty().setChildren(List.of(
            Component.icon(Icon.url(bttvEmote.getImageURL(addon.configuration().emoteQuality().get()))).setSize(addon.configuration().emoteSize().get()),
            Component.text(" ")
          ))
        );
      }

      lastPos = emotePos;
    }

    children.add(component.copy().text(String.join(" ", Arrays.copyOfRange(words, lastPos+1, words.length))));

    return Component.empty().setChildren(children);
  }

  private List<Component> replaceEmoteFromComponents(List<Component> components, ChatMessage message)
      throws IOException {
    List<Component> newComponents = new ArrayList<>();

    for (Component component : components) {
      if (!component.getChildren().isEmpty()) {
        component = component.copy().setChildren(replaceEmoteFromComponents(component.getChildren(), message));
      }

      if (component instanceof TextComponent textComponent) {
        Map<LegacyGlobalId, Integer> emotes = containsEmote(textComponent.getText());

        if (!emotes.isEmpty()) {
          component = replaceEmote(textComponent, emotes, message);
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
      Task.builder(() -> {
        Map<LegacyGlobalId, Integer> emotes = containsEmote(message.getFormattedText());

        if (emotes.isEmpty()) return;

        try {
          TextComponent component = replaceEmote(Component.text(message.getFormattedText()), emotes,
              message);
          Task.builder(() -> {
            // Execute on render thread to prevent emote not being rendered for a long time
            message.edit(component);
          }).build().executeOnRenderThread();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }).delay((int) (addon.configuration().renderDelay().get() * 1000), TimeUnit.MILLISECONDS).build().execute();
    } else {
      Task.builder(() -> {
        try {
          List<Component> newChildren = replaceEmoteFromComponents(message.component().getChildren(), message);

          if (newChildren.equals(message.component().getChildren())) return;

          // Execute on render thread to prevent emote not being rendered for a long time
          Task.builder(() -> message.edit(message.component().setChildren(newChildren))).build().executeOnRenderThread();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }).delay((int) (addon.configuration().renderDelay().get() * 1000), TimeUnit.MILLISECONDS).build().execute();
    }
  }
}