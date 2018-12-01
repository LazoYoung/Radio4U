package io.github.lazoyoung.radio4u.spigot;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Text {
    
    private Radio4Spigot plugin;
    private Logger log;
    private File file;
    private FileConfiguration config;
    
    public Text(Radio4Spigot plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        initFile();
    }
    
    public static Text initText(Radio4Spigot plugin) {
        return new Text(plugin);
    }
    
    public String get(String path) {
        String def = this.config.getDefaults().getString(path);
        String val = this.config.getString(path, def);
        if(val == null) {
            return this.config.getString("text.unknown", "Unknown text. Review text.yml");
        }
        return ChatColor.translateAlternateColorCodes('&', val);
    }
    
    private void initFile() {
        file = new File(this.plugin.getDataFolder(), "text.yml");
    
        try {
            if (this.file.createNewFile()) {
                log.info("Created file: text.yml");
            }
            this.config = YamlConfiguration.loadConfiguration(this.file);
            this.config.options().copyDefaults(true);
            setDefaults();
        } catch (IOException e) {
            e.printStackTrace();
            log.severe("Failed to create file: text.yml");
        }
    }
    
    private void setDefaults() throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("command.usage", "Invalid command. Type /<command> for help.");
        map.put("command.forbidden", "You need a permission to do that.");
        map.put("command.radio4u.reload", "Reload song files from disk.");
        map.put("command.radio", "Broadcast music through a radio channel.");
        map.put("command.playlist", "Manage your playlist.");
        map.put("command.playlist.help", "Playlist : manage noteblock song playlist!");
        map.put("command.playlist.select", "Select a playlist to play or modify.");
        map.put("command.playlist.select.succeed", "Selected playlist: ");
        map.put("command.playlist.select.absent", "That playlist does not exist.");
        map.put("command.playlist.absent", "Select a playlist in advance.");
        map.put("command.playlist.select.name", "Please input the name of playlist.");
        map.put("command.playlist.play", "Play the selected playlist.");
        map.put("command.playlist.list", "Print the list of playlists.");
        map.put("command.playlist.show", "Print the list of songs in this playlist.");
        map.put("command.playlist.create", "Add or remove a playlist.");
        map.put("command.playlist.add", "Add or remove songs from the playlist.");
        map.put("text.unknown", "Unknown text. Please review text.yml");
        
        this.config.addDefaults(map);
        this.config.save(this.file);
    }
    
}
