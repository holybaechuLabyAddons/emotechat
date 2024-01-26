package xyz.holyb.emotechat.bttv;

public class BTTVEmote {
  private static final String EMOTE_IMAGE_ENDPOINT = "https://cdn.betterttv.net/emote/%s/%dx";

  public static BTTVEmote createDefault() {
    BTTVEmote emote = new BTTVEmote();
    emote.id = "56e9f494fff3cc5c35e5287e";
    emote.code = "monkaS";
    emote.imageType = "png";
    emote.animated = false;

    BTTVUser user = new BTTVUser();
    user.displayName = "Monkasen";
    user.name = "monkasen";
    user.id = "55bfba180baa41467919aabf";
    user.providerId = "97950061";

    emote.user = user;

    return emote;
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
