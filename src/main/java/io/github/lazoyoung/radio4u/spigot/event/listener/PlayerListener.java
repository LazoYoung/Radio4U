package io.github.lazoyoung.radio4u.spigot.event.listener;

import io.github.lazoyoung.radio4u.spigot.radio.RadioListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        RadioListener listener = RadioListener.get(event.getPlayer());
        listener.leaveChannel();
        listener.purge();
    }

}
