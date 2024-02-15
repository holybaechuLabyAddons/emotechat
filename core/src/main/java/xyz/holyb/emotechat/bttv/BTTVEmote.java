package xyz.holyb.emotechat.bttv;

import net.labymod.api.util.io.web.request.Request;
import xyz.holyb.emotechat.emote.LegacyGlobalId;

import java.util.Objects;

public class BTTVEmote {
  public static final String EMOTE_IMAGE_ENDPOINT = "https://cdn.betterttv.net/emote/%s/%dx";
  private static final String EMOTE_ENDPOINT = "https://api.betterttv.net/3/emotes/%s";

  public BTTVEmote(LegacyGlobalId globalId, String id, String code, String imageType){
    this.legacyGlobalId = globalId;
    this.id = id;
    this.code = code;
    this.imageType = imageType;
    this.animated = Objects.equals(imageType, "gif");
  }

  public static BTTVEmote id(String id){
    return Request.ofGson(BTTVEmote.class).url(String.format(EMOTE_ENDPOINT, id)).executeSync().get();
  }

  public LegacyGlobalId legacyGlobalId;

  public String id;
  public String code;
  public String imageType;
  public boolean animated;
  public BTTVUser user;

  public String getImageURL(int size){
    return String.format(EMOTE_IMAGE_ENDPOINT, this.id, size);
  }

  public String toString(){
    return this.code + " by " + this.user.displayName;
  }
}
