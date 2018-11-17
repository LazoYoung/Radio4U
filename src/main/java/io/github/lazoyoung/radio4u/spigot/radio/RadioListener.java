package io.github.lazoyoung.radio4u.spigot.radio;

import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class RadioListener {
    
    private static HashMap<UUID, RadioListener> listener = new HashMap<>();
    public float volume;
    private Radio channel;
    private Player player;
    
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
        this.player = player;
        this.volume = 1F;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    @Nullable
    public Radio getChannel() {
        return channel;
    }
    
    public void joinChannel(@Nonnull Radio channel) {
        if(this.channel != null) {
            channel.quit(player);
        }
        
        channel.join(player);
        this.channel = channel;
    }
    
    public void leaveChannel() {
        if(this.channel == null) {
            return;
        }
        
        this.channel.quit(player);
        this.channel = null;
    }
    
}
