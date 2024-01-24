package xyz.holyb.emotechat;

import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;
import xyz.holyb.emotechat.listener.ChatReceiveListener;

@AddonMain
public class EmoteChatAddon extends LabyAddon<EmoteChatConfiguration> {

  @Override
  protected void enable() {
    this.registerSettingCategory();

    this.registerListener(new ChatReceiveListener(this));

    this.logger().info("Enabled the Addon");
  }

  @Override
  protected Class<EmoteChatConfiguration> configurationClass() {
    return EmoteChatConfiguration.class;
  }
}
