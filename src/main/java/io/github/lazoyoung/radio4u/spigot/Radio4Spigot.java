package io.github.lazoyoung.radio4u.spigot;

import io.github.lazoyoung.radio4u.spigot.event.listener.PlayerListener;
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
            try {
                loadSongs();
                loadPlaylist();
            } catch (NoClassDefFoundError e) {
                e.printStackTrace();
                if(e.toString().contains("NoteBlockAPI")) {
                    getServer().getConsoleSender().sendMessage("\u00A7cThis version of NoteBlockAPI" +
                            " is incompatible with Radio4U. Updating is recommended.");
                }
            }
        }
        else {
            getLogger().severe("Failed to load song registry.");
            getPluginLoader().disablePlugin(this);
        }
        
        registerCommands();
        registerEventListeners();
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
    
    private void loadPlaylist() {
        File dir = new File(getDataFolder(), "playlists");
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".yml"));
        
        if(files != null) {
            for (File file : files) {
                String name = file.getName().split("\\.")[0];
                Playlist.create(this, name);
            }
        }
    }
    
    private void loadSongs() {
        int cnt = songRegistry.loadSongs();
        
        if(cnt > 0) {
            getLogger().info("Found " + cnt + " songs from disk.");
        }
    }

    private void registerCommands() {
        getCommand("playlist").setExecutor(new PlaylistCommand(this));
        getCommand("radio").setExecutor(new RadioCommand(this));
    }

    private void registerEventListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }
    
}
