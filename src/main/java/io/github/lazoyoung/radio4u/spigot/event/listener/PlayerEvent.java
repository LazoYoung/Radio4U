package io.github.lazoyoung.radio4u.spigot.event.listener;

import io.github.lazoyoung.radio4u.spigot.Util;
import io.github.lazoyoung.radio4u.spigot.radio.Radio;
import io.github.lazoyoung.radio4u.spigot.radio.RadioListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class PlayerEvent implements Listener {

    private Plugin plugin;
    
    public PlayerEvent(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Util.debug("PlayerQuitEvent occurred!");
        RadioListener listener = RadioListener.get(event.getPlayer());
        Radio channel = listener.getChannel();
        if(channel != null) {
            if(channel.isLocal()) {
                channel.closeChannel();
            }
            if(channel.getListenerUUIDs().size() == 1) {
                channel.setPlaying(false);
            }
        }
        listener.leaveChannel();
        listener.purge();
    }
}