package xyz.holyb.emotechat;

import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;
import xyz.holyb.emotechat.listener.ChatMessageSendListener;
import xyz.holyb.emotechat.listener.GameTickListener;
import xyz.holyb.emotechat.listener.ChatReceiveListener;

@AddonMain
public class EmoteChatAddon extends LabyAddon<EmoteChatConfiguration> {
  private static EmoteChatAddon instance;

  public EmoteChatAddon(){
    instance = this;
  }

  public static EmoteChatAddon get(){
    return instance;
  }

  public GameTickListener gameTickListener = new GameTickListener();

  @Override
  protected void enable() {
    this.registerSettingCategory();

    this.registerListener(gameTickListener);
    this.registerListener(new ChatReceiveListener(this));
    this.registerListener(new ChatMessageSendListener(this));

    this.logger().info("Enabled the Addon");
  }

  @Override
  protected Class<EmoteChatConfiguration> configurationClass() {
    return EmoteChatConfiguration.class;
  }
}
