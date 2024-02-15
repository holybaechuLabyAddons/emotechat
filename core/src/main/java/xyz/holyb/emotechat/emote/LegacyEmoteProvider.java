package xyz.holyb.emotechat.emote;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.labymod.api.util.io.web.request.Request;
import net.labymod.api.util.io.web.request.Request.Method;
import xyz.holyb.emotechat.EmoteChatAddon;
import xyz.holyb.emotechat.bttv.BTTVEmote;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LegacyEmoteProvider {
  private final EmoteChatAddon addon;

  private static final String BACKEND_URL = "https://api.emotechat.de/";

  private static final String ID_SPLITTER_ROUTE = "emote/globalIds/splitter";
  private static final String EMOTE_ADD_ROUTE = "emote/add";
  private static final String EMOTE_INFO_ROUTE = "emote/get/%s";

  private final Map<LegacyGlobalId, LegacyServerEmote> cachedServerEmotes = new HashMap<>();

  public String idSplitter = "";

  public LegacyEmoteProvider(EmoteChatAddon addon) {
    this.addon = addon;

    loadIdSplitter();
  }

  private void loadIdSplitter() {
    Request.ofString()
        .addHeader("User-Agent", "EmoteChat addon for LabyMod 4")
        .addHeader("Content-Type", "application/json")
        .url(BACKEND_URL + ID_SPLITTER_ROUTE)
        .async()
        .execute(res -> {
          this.idSplitter = res.get();
        });
  }

  public LegacyServerEmote retrieveEmoteByGlobalId(LegacyGlobalId globalId) {
    LegacyServerEmote cachedServerEmote = cachedServerEmotes.get(globalId);
    if (Objects.nonNull(cachedServerEmote)) {
      return cachedServerEmote;
    }

    cachedServerEmotes.put(globalId, Request.ofGson(LegacyServerEmote.class)
        .addHeader("User-Agent", "EmoteChat addon for LabyMod 4")
        .addHeader("Content-Type", "application/json")
        .url(BACKEND_URL + String.format(EMOTE_INFO_ROUTE, globalId.toString(this.idSplitter)))
        .executeSync().get()
    );

    return retrieveEmoteByGlobalId(globalId);
  }

  public LegacyServerEmote addEmote(String bttvId) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("bttvId", bttvId);

    return Request.ofGson(LegacyServerEmote.class)
        .addHeader("User-Agent", "EmoteChat addon for LabyMod 4")
        .addHeader("Content-Type", "application/json")
        .method(Method.POST)
        .json(jsonObject)
        .url(BACKEND_URL + EMOTE_ADD_ROUTE)
        .executeSync().get();
  }
}
