package io.github.lazoyoung.radio4u.spigot;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SongRegistry {
    
    private Plugin plugin;
    private FileConfiguration config;
    private File file;
    private HashMap<Integer, Song> reg;
    
    
    public SongRegistry(Plugin plugin, File file, FileConfiguration config) {
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
            this.reg.put(id, song);
            Playlist.getGlobalPlaylist().add(song);
            return true;
        }
        
        this.config.set(id + ".file", null);
        this.config.save(this.file);
        return false;
    }
    
    public Song getSong(int id) {
        return this.reg.get(id);
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
    
    public int loadSongs() {
        File folder = new File(plugin.getDataFolder(), "songs");
        
        if(!folder.isDirectory()) {
            folder.mkdirs();
        }
        
        File[] newFiles = folder.listFiles((dir, name) -> name.endsWith(".nbs"));
        
        if(newFiles != null && newFiles.length > 0) {
            int id = getNextEmptyID(newFiles.length);
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
    
}

