package io.github.lazoyoung.radio4u.spigot;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import io.github.lazoyoung.radio4u.spigot.exception.UnsupportedSenderException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Playlist extends com.xxmicloxx.NoteBlockAPI.model.Playlist {
    
    public static final UUID consoleId = UUID.randomUUID();
    private static HashMap<String, Playlist> registry = new HashMap<>();
    private static HashMap<UUID, String> selection = new HashMap<>();
    private String name;
    private File file;
    private FileConfiguration config;
    
    
    private Playlist(Radio4Spigot plugin, String name, boolean save, Song... songs) {
        super(songs);
        this.name = name;
        this.file = new File(plugin.getDataFolder() + File.separator + "playlists", name + ".yml");

        if(save) {
            this.config = loadConfig();
        }
    }
    
    public static Playlist create(Radio4Spigot plugin, String name, boolean save, Song... songs) throws IllegalArgumentException {
        name = name.toLowerCase();
        
        if(!Util.isAlphaNumeric(name))
            throw new IllegalArgumentException();
        
        if(registry.containsKey(name))
            return null;
    
        Playlist instance = new Playlist(plugin, name, save, songs);
        registry.put(name, instance);
        return instance;
    }
    
    public static Playlist create(Radio4Spigot plugin, String name) {
        File file = new File(plugin.getDataFolder() + File.separator + "playlists", name + ".yml");
        FileConfiguration config = new YamlConfiguration();
        
        if(file.exists()) {
            try {
                config.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
    
            List<Integer> list = config.getIntegerList("list");
            Iterator<Integer> iter = list.iterator();
            
            if(list.size() < 1) {
                return null;
            }
            
            Song[] songs = new Song[list.size()];
            int i = 0;
            
            while(iter.hasNext()) {
                songs[i++] = plugin.songRegistry.getSong(iter.next());
            }
            
            return create(plugin, name, true, songs);
        }
        
        return null;
    }
    
    public static boolean remove(String name) throws SecurityException {
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
    
    public static HashMap<String, Playlist> getRegistry() {
        return registry;
    }
    
    public static Playlist get(String name) {
        return registry.get(name.toLowerCase());
    }
    
    @Nullable
    public static Playlist getGlobalPlaylist() {
        return get("global");
    }
    
    public static Playlist getSelection(UUID uuid) {
        String name = selection.get(uuid);
        
        if(name != null) {
            return get(name);
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
    
    public boolean addSongIntoDisk(int id) throws IOException {
        List<Integer> list = config.getIntegerList("list");
        
        if(list.add(id)) {
            config.set("list", list);
            config.save(file);
            return true;
        }
        return false;
    }
    
    public boolean removeSongFromDisk(int id) throws IOException {
        List<Integer> list = config.getIntegerList("list");
        
        if (list.remove((Integer) id)) {
            config.set("list", list);
            config.save(file);
            return true;
        }
        return false;
    }
    
    /**
     * @return the list of song IDs in this playlist.
     */
    @Deprecated
    public List<Integer> getSongs(boolean sort) {
        List<Integer> list = config.getIntegerList("list");
        
        if(sort) {
            Collections.sort(list);
        }
        
        return list;
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
