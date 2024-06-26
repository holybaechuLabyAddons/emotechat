package xyz.holyb.emotechat.bttv;

import com.google.gson.reflect.TypeToken;
import net.labymod.api.util.io.web.request.Callback;
import net.labymod.api.util.io.web.request.Request;
import java.util.List;

public class BTTVSearch {
    private static final String SEARCH_BACKEND = "https://api.betterttv.net/3/emotes/shared/search?query=%s";

    public static void search(String query, Callback<List<BTTVEmote>> callback){
      Request.ofGson(new TypeToken<List<BTTVEmote>>(){}).url(String.format(SEARCH_BACKEND, query)).async().execute(callback);
    }
}
