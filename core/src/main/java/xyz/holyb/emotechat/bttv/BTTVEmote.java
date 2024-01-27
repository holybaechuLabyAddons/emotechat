package xyz.holyb.emotechat.bttv;

import net.labymod.api.util.io.web.request.Request;

public class BTTVEmote {
  public static final String EMOTE_IMAGE_ENDPOINT = "https://cdn.betterttv.net/emote/%s/%dx";

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
