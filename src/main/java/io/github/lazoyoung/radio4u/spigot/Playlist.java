package io.github.lazoyoung.radio4u.spigot;

import io.github.lazoyoung.radio4u.spigot.exception.UnsupportedSenderException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Playlist {
    
    public static final UUID consoleId = UUID.randomUUID();
    private static HashMap<String, Playlist> registry = new HashMap<>();
    private static HashMap<UUID, String> selection = new HashMap<>();
    private String name;
    private File file;
    private FileConfiguration config;
    
    
    private Playlist(Plugin plugin, String name) {
        this.name = name;
        this.file = new File(plugin.getDataFolder() + File.separator + "playlists", name + ".yml");
        this.config = loadConfig();
    }
    
    public static boolean createPlaylist(Plugin plugin, String name) throws IllegalArgumentException {
        name = name.toLowerCase();
        
        if(name.contains("\\."))
            throw new IllegalArgumentException();
        
        if(registry.containsKey(name))
            return false;
        
        registry.put(name, new Playlist(plugin, name));
        return true;
    }
    
    public static boolean removePlaylist(String name) throws SecurityException {
        name = name.toLowerCase();
        
        if(!registry.containsKey(name))
            return false;
        
        Playlist pl = registry.get(name);
        
        if(pl.file.delete()) {
            registry.remove(name);
            return true;
        }
        return false;
    }
    
    public static Collection<Playlist> getAllPlaylists() {
        return registry.values();
    }
    
    public static Playlist getPlaylist(String name) {
        return registry.get(name.toLowerCase());
    }
    
    public static Playlist getGlobalPlaylist() {
        return getPlaylist("global");
    }
    
    public static Playlist getSelection(UUID uuid) {
        String name = selection.get(uuid);
        
        if(name != null) {
            return getPlaylist(name);
        }
        return null;
    }
    
    public static boolean selectPlaylist(CommandSender selector, @Nullable Playlist playlist) throws UnsupportedSenderException {
        UUID id = Playlist.consoleId;
    
        if(selector instanceof Player) {
            id = ((Player) selector).getUniqueId();
        }
        else if(!(selector instanceof ConsoleCommandSender)) {
            throw new UnsupportedSenderException(selector);
        }
    
        if(playlist != null) {
            selection.put(id, playlist.getName());
            return true;
        }
        return false;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean addSong(Song song) throws IOException {
        List<Integer> list = config.getIntegerList("list");
        
        if(list.add(song.id)) {
            config.set("list", list);
            config.save(file);
            return true;
        }
        return false;
    }
    
    public boolean removeSong(int id) throws IOException {
        List<Integer> list = config.getIntegerList("list");
        
        if(list.remove((Integer) id)) {
            config.set("list", list);
            config.save(file);
            return true;
        }
        return false;
    }
    
    /**
     * @return the list of song IDs in this playlist.
     */
    public List<Integer> getSongs() {
        return config.getIntegerList("list");
    }
    
    public void clearSongs() throws IOException {
        config.set("list", null);
        config.save(file);
    }
    
    private FileConfiguration loadConfig() {
        FileConfiguration config = new YamlConfiguration();
        
        try {
            if(!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        
        return config;
    }
}