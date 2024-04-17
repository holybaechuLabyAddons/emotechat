package xyz.holyb.emotechat.emote;

import com.google.gson.JsonObject;
import net.labymod.api.util.io.web.request.Request;
import net.labymod.api.util.io.web.request.Response;
import net.labymod.api.util.io.web.request.types.GsonRequest;
import net.labymod.api.util.logging.Logging;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EmoteProvider {
  // Backend source code is available at: https://github.com/holybaechuLabyAddons/emotechat-backend
  public static final String BACKEND_URL = "https://emotechat.hdskins.de/v1/"; // EmoteChat backend running on HDSkins server
  public static final String BACKUP_BACKEND_URL = "https://neo.emotechat.de/v1/"; // EmoteChat backend running on RappyTV's server

  public static final String EMOTE_SPLITTER = "|";

  public static final Map<String, Emote> CACHED_EMOTES = new HashMap<>();

  public static Emote get(String id) {
    Emote emote = CACHED_EMOTES.get(id);
    if (Objects.nonNull(emote)) return emote;

    GsonRequest<Emote> request = Request.ofGson(Emote.class)
        .addHeader("User-Agent", "EmoteChat for LabyMod 4");

    Response<Emote> response;
    try {
      response = request.url(BACKEND_URL + "emote/" + id).executeSync();
    } catch (Exception e) {
      try {
        response = request.url(BACKUP_BACKEND_URL + "emote/" + id).executeSync();
      } catch(Exception e1) {
        e.printStackTrace();

        return null;
      }
    }
    if (!response.isPresent()) return null;

    emote = response.get();
    CACHED_EMOTES.put(id, emote);

    return emote;
  }

  public static Emote addBTTV(String id) {
    JsonObject body = new JsonObject();
    body.addProperty("provider", "BTTV");
    body.addProperty("id", id);

    GsonRequest<Emote> request = Request.ofGson(Emote.class)
        .addHeader("User-Agent", "EmoteChat for LabyMod 4")
        .json(body);

    try {
      return request.url(BACKEND_URL + "emote").executeSync().get();
    } catch (Exception e) {
      try {
        return request.url(BACKUP_BACKEND_URL + "emote").executeSync().get();
      } catch(Exception e1) {
        e.printStackTrace();

        return null;
      }
    }
  }
}
