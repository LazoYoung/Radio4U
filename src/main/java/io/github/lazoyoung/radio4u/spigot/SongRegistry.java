package io.github.lazoyoung.radio4u.spigot;

import javax.annotation.Nullable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SongRegistry {
    
    private Plugin plugin;
    private FileConfiguration config;
    private File file;
    
    
    public SongRegistry(Plugin plugin, File file, FileConfiguration config) {
        this.plugin = plugin;
        this.file = file;
        this.config = config;
    }
    
    
    public boolean importSong(int id, String file, @Nullable String name, @Nullable String desc) throws IOException {
        
        if(config.getConfigurationSection(String.valueOf(id)) != null) {
            return false;
        }
        
        config.set(id + ".file", file);
        
        if(name != null) {
            config.set(id + ".name", name);
        }
        
        if(desc != null) {
            config.set(id + ".description", desc);
        }
        
        config.save(this.file); // Save to disk
        Playlist.getGlobalPlaylist().addSong(getSongFromDisk(id));
        return true;
    }
    
    public void discardSong(int id) throws IOException {
        config.set(String.valueOf(id), null);
        config.save(file);
        Playlist.getGlobalPlaylist().removeSong(id);
    }
    
    public Song getSongFromDisk(int id) {
        String fileName = config.getString(id + ".file");
        
        if(fileName == null) {
            return null;
        }
        
        File file = new File(plugin.getDataFolder(), fileName);
        String name = config.getString(id + ".name");
        String desc = config.getString(id + ".description");
        return new Song(id, file, name, desc);
    }
    
    /**
     * @apiNote It's recommended to call this method async from bukkit threads.
     * @return Registered songs' id list sorted in ascending order.
     */
    public List<Integer> getIdList() {
        List<Integer> list = new ArrayList<>();
        
        for(String s : config.getKeys(false)) {
            list.add(Integer.parseInt(s));
        }
        
        Collections.sort(list);
        return list;
    }
    
    public List<String> getFileList() {
        List<String> list = new ArrayList<>();
        
        for(String key : config.getKeys(true)) {
            if(key.equals("file")) {
                list.add((String) config.get(key));
            }
        }
        
        return list;
    }
    
    /**
     * @apiNote It's recommended to call this method async from bukkit threads.
     * @param space Minimum gap between the returned id and the next one (which may be absent)
     */
    public int getEmptyId(int space) {
        Set<String> set = config.getKeys(false);
        String[] arr = new String[set.size()];
        arr = set.toArray(arr);
        
        if(arr.length < 1) {
            return 1;
        }
        else if(space < 1) {
            return getEmptyId();
        }
        
        List<String> keys = Arrays.asList(arr);
        Collections.sort(keys);
        
        int id = -1;
        
        for(int c = 0; c < keys.size(); c ++) {
            int i = keys.size() - (c + 1);
            int now = Integer.parseInt(keys.get(i));
            
            if(i > 0) {
                int pre = Integer.parseInt(keys.get(i - 1));
                
                if(now - pre > space) {
                    continue;
                }
            }
            id = now + 1;
        }
        
        return id;
    }
    
    /**
     * Returns the value obtained from getEmptyId(1)
     * @apiNote It's recommended to call this method async from bukkit threads.
     */
    public int getEmptyId() {
        return getEmptyId(1);
    }
    
    public int importNewSongs() {
        File folder = plugin.getDataFolder();
        
        if(!folder.isDirectory()) {
            folder.mkdirs();
        }
        
        final List<String> exists = getFileList();
        File[] newFiles = folder.listFiles((dir, name) -> name.endsWith(".nbs") && !exists.contains(name));
        
        if(newFiles != null && newFiles.length > 0) {
            int id = getEmptyId(newFiles.length);
            int cnt = 0;
    
            for (File file : newFiles) {
                String fileName = file.getName();
                try {
                    plugin.getLogger().info("Importing " + fileName);
                    if(importSong(id++, fileName, fileName.split("\\.") [0], null)) {
                        cnt++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            return cnt;
        }
        
        return 0;
    }
    
}

