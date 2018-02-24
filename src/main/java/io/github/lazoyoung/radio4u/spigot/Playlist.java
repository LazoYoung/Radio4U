package io.github.lazoyoung.radio4u.spigot;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Playlist {
    
    private static HashMap<String, Playlist> registry = new HashMap<>();
    private String name;
    private File file;
    private FileConfiguration config;
    
    
    private Playlist(Plugin plugin, String name) {
        this.name = name;
        this.file = new File(plugin.getDataFolder() + File.separator + "playlists", name + ".yml");
        this.config = loadConfig();
    }
    
    public static boolean createPlaylist(Plugin plugin, String name) {
        name = name.toLowerCase();
        
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
    
    public static Playlist getPlaylist(String name) {
        return registry.get(name.toLowerCase());
    }
    
    public static Playlist getGlobalPlaylist() {
        return getPlaylist("global");
    }
    
    public String getName() {
        return name;
    }
    
    public boolean addSong(int id) throws IOException {
        List<Integer> list = config.getIntegerList("list");
        
        if(list.add(id)) {
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
