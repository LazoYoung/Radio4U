package io.github.lazoyoung.radio4u.spigot.radio;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class RadioListener {
    
    private static HashMap<UUID, RadioListener> listener = new HashMap<>();
    public float volume;
    private Radio channel;
    private UUID playerId;
    
    public static RadioListener get(Player player) {
        RadioListener instance =  listener.get(player.getUniqueId());
        if(instance == null) {
            instance = new RadioListener(player);
            listener.put(player.getUniqueId(), instance);
        }
        return instance;
    }
    
    public RadioListener(Player player) {
        this.channel = null;
        this.playerId = player.getUniqueId();
        this.volume = 1F;
    }
    
    public UUID getPlayerUUID() {
        return this.playerId;
    }
    
    @Nullable
    public Radio getChannel() {
        return channel;
    }
    
    public void joinChannel(@Nonnull Radio channel) {
        Player player = Bukkit.getPlayer(this.playerId);
        if(player != null) {
            if(this.channel != null) {
                channel.quit(player);
            }
            this.channel = channel;
            this.channel.join(player);
        }
    }
    
    public void leaveChannel() {
        Player player = Bukkit.getPlayer(this.playerId);
        if(this.channel != null) {
            this.channel.quit(player);
            this.channel = null;
        }
    }

    public void purge() {
        RadioListener.listener.remove(this.playerId);
    }
    
}
