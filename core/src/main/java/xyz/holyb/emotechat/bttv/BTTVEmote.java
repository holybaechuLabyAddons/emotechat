package xyz.holyb.emotechat.bttv;

import net.labymod.api.util.io.web.request.Request;

public class BTTVEmote {
  public static final String EMOTE_IMAGE_ENDPOINT = "https://cdn.betterttv.net/emote/%s/%dx";
  private static final String EMOTE_ENDPOINT = "https://api.betterttv.net/3/emotes/%s";

  public static BTTVEmote id(String id){
    return Request.ofGson(BTTVEmote.class).url(String.format(EMOTE_ENDPOINT, id)).executeSync().get();
  }

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
