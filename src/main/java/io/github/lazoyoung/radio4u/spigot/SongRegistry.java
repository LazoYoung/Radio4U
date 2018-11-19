package io.github.lazoyoung.radio4u.spigot;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SongRegistry {
    
    private Radio4Spigot plugin;
    private FileConfiguration config;
    private File file;
    private HashMap<Integer, Song> reg;
    
    
    public SongRegistry(Radio4Spigot plugin, File file, FileConfiguration config) {
        this.plugin = plugin;
        this.file = file;
        this.config = config;
        this.reg = new HashMap<>();
    }
    
    
    public boolean loadSong(int id, String fileName) throws IOException {
        
        this.config.set(id + ".file", fileName);
        this.config.save(this.file);
        File file = new File(plugin.getDataFolder() + File.separator + "songs", fileName);
        
        if(file.isFile()) {
            Song song = NBSDecoder.parse(file);
            Playlist global = Playlist.getGlobalPlaylist();
            this.reg.put(id, song);
            
            if(global == null) {
                Playlist.create(plugin, "global", false, song);
            }
            else {
                global.add(song);
            }
            return true;
        }
        
        this.config.set(id + ".file", null);
        this.config.save(this.file);
        return false;
    }
    
    public int loadSongs() {
        File folder = new File(plugin.getDataFolder(), "songs");
        
        if(!folder.isDirectory() && !folder.mkdirs()) {
            return -1;
        }
        
        File[] newFiles = folder.listFiles((dir, name) -> name.endsWith(".nbs"));
        
        if(newFiles != null && newFiles.length > 0) {
            int id = 1;
            int cnt = 0;
            
            for (File file : newFiles) {
                String fileName = file.getName();
                try {
                    if(loadSong(id++, fileName)) {
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
    
    public Song getSong(int id) {
        return this.reg.get(id);
    }

    /**
     * @apiNote It's recommended to call this method async from bukkit threads.
     * @param song
     * @return The ID of the song. -1 if song does not exist in registry.
     */
    public int getSongID(Song song) {
        Set<Map.Entry<Integer, Song>> entrySet = this.reg.entrySet();
        for (Map.Entry<Integer, Song> entry : entrySet) {
            if(song.getPath().equals(entry.getValue().getPath())) {
                return entry.getKey();
            }
        }
        return -1;
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
    @Deprecated
    public int getNextEmptyID(int space) {
        Set<String> set = config.getKeys(false);
        String[] arr = new String[set.size()];
        arr = set.toArray(arr);
        
        if(arr.length < 1) {
            return 1;
        }
        else if(space < 1) {
            return -1;
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
    
}

