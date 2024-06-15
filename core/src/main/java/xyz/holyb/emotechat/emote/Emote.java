package xyz.holyb.emotechat.emote;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class Emote {
  @SerializedName("_id")
  public String id;

  public ProviderData provider;

  @SerializedName("image_type")
  public String imageType;

  public boolean animated;
  public boolean banned;

  public String getImageURL(Integer size) {
    return switch (this.provider.provider) {
      case "BTTV" -> String.format("https://cdn.betterttv.net/emote/%s/%dx", this.provider.id, size);
      default -> "";
    };
  }

  public String name() {
    return id.split(String.format("\\%s", EmoteProvider.EMOTE_SPLITTER))[0];
  }

  public String id() {
    return id.split(String.format("\\%s", EmoteProvider.EMOTE_SPLITTER))[1];
  }

  public static Emote parseLegacyEmote(String id) {
    int splitter = -1;
    boolean foundLower = false;

    char[] chars = id.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      char currentChar = chars[i];

      if (Character.isLowerCase(currentChar)) {
        foundLower = true;
      }

      if (Character.isUpperCase(currentChar) && splitter == -1) {
        if (!foundLower) {
          return null;
        }
        splitter = i;
      }

      if ((Character.isLowerCase(currentChar) || !Character.isAlphabetic(currentChar)) && splitter != -1) {
        return null;
      }
    }

    if (splitter == -1) {
      return null;
    }

    return EmoteProvider.get(id); // New API fallbacks to legacy API if it detects legacy ID
  }

  public static Emote parse(String id) {
    if (!id.matches(String.format("^[^ ]{1,}?\\%s[a-zA-Z0-9]{1,}", EmoteProvider.EMOTE_SPLITTER))) {
      Emote legacyEmote = parseLegacyEmote(id);
      return Objects.nonNull(legacyEmote) ? legacyEmote : null;
    }

    Emote emote = new Emote();
    emote.id = id;

    return emote;
  }

  public String toString() {
    return String.format("%s from %s", this.id, this.provider.provider);
  }
}
