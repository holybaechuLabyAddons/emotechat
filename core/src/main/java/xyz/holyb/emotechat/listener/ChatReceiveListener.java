package xyz.holyb.emotechat.listener;

import net.labymod.api.client.chat.ChatMessage;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.component.TranslatableComponent;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.event.Priority;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatReceiveEvent;
import net.labymod.api.util.concurrent.task.Task;
import xyz.holyb.emotechat.EmoteChatAddon;
import xyz.holyb.emotechat.emote.Emote;
import xyz.holyb.emotechat.emote.EmoteProvider;
import xyz.holyb.emotechat.gui.AnimatedEmote;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static xyz.holyb.emotechat.gui.AnimatedEmote.renderEmotes;

public class ChatReceiveListener {
  private final EmoteChatAddon addon;

  public ChatReceiveListener(EmoteChatAddon addon) {
    this.addon = addon;
  }

  private Component createEmoteComponent(Emote serverEmote, ChatMessage message) throws IOException, URISyntaxException {
    if (addon.configuration().incompatWarn().get()) {
      if (!addon.labyAPI().config().ingame().advancedChat().enabled().get()) {
        addon.labyAPI().minecraft().chatExecutor().displayClientMessage(
            Component.translatable("emotechat.notifications.incompatWarn.advancedchat").color(NamedTextColor.RED)
        );
      }
    }

    if (serverEmote.animated && addon.configuration().animatedEmotes().get()) {
      renderEmotes();
      return Component.empty().setChildren(List.of(
          AnimatedEmote.url(
              serverEmote.getImageURL(addon.configuration().emoteQuality().get()),
              message,
              addon.configuration().emoteSize().get()
          ).iconComponent,
          Component.text(" ")
      ));
    } else {
      return Component.empty().setChildren(List.of(
          Component.icon(
              Icon.url(serverEmote.getImageURL(addon.configuration().emoteQuality().get())),
              Style.builder().color(NamedTextColor.WHITE).build(),
              addon.configuration().emoteSize().get()
          ),
          Component.text(" ")
      ));
    }
  }


  private Component replaceEmote(Component component, ChatMessage message) {
    Component result = Component.empty();
    boolean replaced = false;

    List<Component> children = new ArrayList<>(component.getChildren());
    for (int i = 0; i < children.size(); i++) {
      Component replacement = replaceEmote(children.get(i), message);
      if (Objects.nonNull(replacement)) {
        children.set(i, replacement);
        replaced = true;
      }
    }


    if (component instanceof TranslatableComponent translatableComponent) {
      List<Component> arguments = new ArrayList<>(translatableComponent.getArguments());
      for (int i = 0; i < arguments.size(); i++) {
        Component replacement = replaceEmote(arguments.get(i), message);
        if (Objects.nonNull(replacement)) {
          arguments.set(i, replacement);
          replaced = true;
        }
      }

      result = Component.translatable(translatableComponent.getKey()).arguments(arguments);
    }

    List<Component> replacement = new ArrayList<>();
    if (component instanceof TextComponent textComponent) {
      int lastReplacement = 0;
      String[] words = textComponent.getText().split(" ");
      for (int i = 0; i < words.length; i++) {
        String word = words[i];

        Emote emote = Emote.parse(word);
        if (Objects.isNull(emote)) continue;

        Emote serverEmote = EmoteProvider.get(emote.id);
        if (Objects.isNull(serverEmote)) continue;

        if (!replacement.isEmpty()) replacement.removeLast();
        try {
          replacement.addAll(List.of(
              Component.text(String.join(" ", Arrays.copyOfRange(words, lastReplacement, i)) + " "),
              createEmoteComponent(serverEmote, message)
          ));
        } catch(Exception e) {
          addon.labyAPI().minecraft().chatExecutor().displayClientMessage(
              Component.translatable("emotechat.notifications.error.render").color(NamedTextColor.RED)
          );
          addon.logger().debug("Failed to render an emote: ", e);
        }

        lastReplacement = i + 1;
        replaced = true;
      }

      replacement.add(Component.text(String.join(" ", Arrays.copyOfRange(words, lastReplacement, words.length))));
    }
    replacement.addAll(children);

    return replaced ? result.setChildren(replacement) : null;
  }

  @Subscribe
  public void onChatReceive(ChatReceiveEvent event) {
    Task.builder(() -> {
      ChatMessage message = event.chatMessage();
      Component replacement = replaceEmote(message.component(), message);
      if (Objects.isNull(replacement)) return;

      Task.builder(() -> message.edit(replacement)).build().executeOnRenderThread();
    }).delay((int) (addon.configuration().renderDelay().get() * 1000), TimeUnit.MILLISECONDS).build().execute();
  }
}