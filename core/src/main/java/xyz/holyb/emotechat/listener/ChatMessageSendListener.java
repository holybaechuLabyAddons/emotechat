package xyz.holyb.emotechat.listener;

import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import xyz.holyb.emotechat.EmoteChatAddon;

public class ChatMessageSendListener {
  private final EmoteChatAddon addon;

  public ChatMessageSendListener(EmoteChatAddon addon){
      this.addon = addon;
  }

  @Subscribe
  public void onChatMessageSend(ChatMessageSendEvent event){
    if (!addon.configuration().enabled().get()) return;

    String message = event.getMessage();

    String[] words = message.split(" ");
    for (int i = 0; i < words.length; i++) {
      String word = words[i].toLowerCase();
      if (!(word.startsWith(":") && word.endsWith(":"))) continue;

      int finalI = i;
      addon.configuration().getEmotes().forEach((name, emote) -> {
        if(name.toLowerCase().equals(word.substring(1, word.length()-1))){

          words[finalI] = String.format("%s%s%s", emote.legacyGlobalId.emoteName, addon.legacyEmoteProvider.idSplitter, emote.legacyGlobalId.emoteId); // ex: pepogAC
        }
      });
    }

    event.changeMessage(String.join(" ", words), message);
  }
}
