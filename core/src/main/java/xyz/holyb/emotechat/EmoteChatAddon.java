package xyz.holyb.emotechat;

import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.component.Component;
import net.labymod.api.models.addon.annotation.AddonMain;
import net.labymod.api.notification.Notification;
import xyz.holyb.emotechat.emote.LegacyEmoteProvider;
import xyz.holyb.emotechat.listener.ChatMessageSendListener;
import xyz.holyb.emotechat.listener.AnimatedEmoteRenderer;
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

  public AnimatedEmoteRenderer gameTickListener = new AnimatedEmoteRenderer(this);

  public LegacyEmoteProvider legacyEmoteProvider = new LegacyEmoteProvider(this);

  @Override
  protected void enable() {
    this.registerSettingCategory();

    this.registerListener(gameTickListener);
    this.registerListener(new ChatReceiveListener(this));
    this.registerListener(new ChatMessageSendListener(this));

    this.logger().info("Enabled the Addon");

    if (this.labyAPI().addonService().getAddon("chatutilities").isPresent() && this.configuration().incompatWarn().get()) {
      Notification.builder()
          .title(Component.text("EmoteChat"))
          .text(Component.text("EmoteChat might not work with ChatUtilities addon's \"Copy Button\" feature enabled."))
          .buildAndPush();
    }
  }

  @Override
  protected Class<EmoteChatConfiguration> configurationClass() {
    return EmoteChatConfiguration.class;
  }
}
