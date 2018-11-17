package io.github.lazoyoung.radio4u.spigot;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Radio4Spigot extends JavaPlugin {
    
    public SongRegistry songRegistry;
    
    
    @Override
    public void onEnable() {
        boolean regLoaded = loadSongRegistry();
        
        if(regLoaded) {
            loadSongs();
            loadAllPlaylist();
        }
        else {
            getLogger().severe("Failed to init song registry.");
            getPluginLoader().disablePlugin(this);
        }
        
        getCommand("playlist").setExecutor(new PlaylistCommand(this));
        getCommand("radio").setExecutor(new RadioCommand(this));
    }
    
    private boolean loadSongRegistry() {
        File file = new File(getDataFolder(), "SongRegistry.yml");
        File songDir = new File(getDataFolder(), "songs");
        FileConfiguration config = null;
        
        try {
            if(!getDataFolder().isDirectory() && !getDataFolder().mkdirs()) {
                return false;
            }
            
            if (!file.exists() && !file.createNewFile()) {
                return false;
            }
            
            if(!songDir.isDirectory() && !songDir.mkdirs()) {
                return false;
            }
    
            config = new YamlConfiguration();
            config.load(file);
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
        
        songRegistry = new SongRegistry(this, file, config);
        return true;
    }
    
    private void loadAllPlaylist() {
        loadGlobalPlaylist();
        
        File dir = new File(getDataFolder(), "playlists");
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".yml"));
        
        if(files == null) {
            getLogger().severe("Error occurred while loading playlists from disk!");
            return;
        }
        
        for(File file : files) {
            String name = file.getName().split("\\.") [0];
            Playlist.createPlaylist(this, name);
        }
    }
    
    private void loadGlobalPlaylist() {
        Playlist pl = Playlist.createPlaylist(this, "global");
        
        try {
            Objects.requireNonNull(pl).clearSongs();
            
            for (int id : songRegistry.getIdList()) {
                pl.addSongIntoDisk(id);
                pl.add(Objects.requireNonNull(songRegistry.getSong(id)));
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
    
    private void loadSongs() {
        int cnt = songRegistry.loadSongs();
        
        if(cnt > 0) {
            getLogger().info("Found " + cnt + " songs from disk.");
        }
    }
    
}
