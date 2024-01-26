package xyz.holyb.emotechat;

import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.ActivitySettingWidget.ActivitySetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.annotation.Exclude;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.util.MethodOrder;
import xyz.holyb.emotechat.activity.EmotesActivity;
import xyz.holyb.emotechat.bttv.BTTVEmote;
import java.util.HashMap;
import java.util.Map;

@ConfigName("settings")
public class EmoteChatConfiguration extends AddonConfig {

  @SwitchSetting
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

  @Exclude
  private Map<String, BTTVEmote> emotes = new HashMap<>();

  @Override
  public ConfigProperty<Boolean> enabled() {
    return this.enabled;
  }

  public Map<String, BTTVEmote> getEmotes() { return this.emotes; }

  @MethodOrder(after = "enabled")
  @ActivitySetting
  public Activity openEmotes(){
    return new EmotesActivity();
  }
}
