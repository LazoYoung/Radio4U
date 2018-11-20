package io.github.lazoyoung.radio4u.spigot.event.listener;

import com.xxmicloxx.NoteBlockAPI.event.SongNextEvent;
import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import io.github.lazoyoung.radio4u.spigot.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class RadioEvent implements Listener {
    
    @EventHandler
    public void onNextSong(SongNextEvent event) {
        SongPlayer splayer = event.getSongPlayer();
        for(UUID playerId : splayer.getPlayerUUIDs()) {
            Player player = Bukkit.getPlayer(playerId);
            if(player != null) {
                Util.actionMessage(player, "Now playing: " + splayer.getSong().getTitle());
            }
        }
    }
    
}