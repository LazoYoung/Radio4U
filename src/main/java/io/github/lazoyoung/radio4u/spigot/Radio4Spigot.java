package io.github.lazoyoung.radio4u.spigot;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Radio4Spigot extends JavaPlugin {
    
    public SongRegistry songRegistry;
    
    
    @Override
    public void onEnable() {
        boolean regLoaded = loadSongRegistry();
        
        if(regLoaded) {
            loadPlaylists();
            importSongs();
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
    
    private void loadPlaylists() {
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
        Playlist.createPlaylist(this, "global");
        Playlist pl = Playlist.getPlaylist("global");
        
        try {
            pl.clearSongs();
            
            for (int id : songRegistry.getIdList()) {
                pl.addSong(id);
            }
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Failed to write changes to global playlist.");
        }
    }
    
    private void importSongs() {
        int cnt = songRegistry.importNewSongs();
        
        if(cnt > 0) {
            getLogger().info("Imported " + cnt + " new songs.");
        }
    }
    
}
