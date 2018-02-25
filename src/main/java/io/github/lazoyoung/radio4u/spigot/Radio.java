package io.github.lazoyoung.radio4u.spigot;

import com.xxmicloxx.NoteBlockAPI.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Radio implements Listener {
    
    public boolean repeat = false;
    public boolean autoSleep = true;
    
    private static HashMap<String, Radio> registry = new HashMap<>();
    private static HashMap<String, String> listener = new HashMap<>();
    private Radio4Spigot plugin;
    private Playlist playlist;
    private SongPlayer songPlayer;
    private List<Integer> songs = new ArrayList<>();
    private String name;
    private boolean local; // TODO Implement local channel (destroy when no one hears)
    private int index = 0;
    
    
    private Radio(Radio4Spigot plugin, String name, boolean local) {
        this.plugin = plugin;
        this.name = name;
        this.local = local;
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    
    public static boolean createChannel(Radio4Spigot plugin, String name, boolean local, boolean strictName) throws IllegalArgumentException {
        name = name.toLowerCase();
    
        if(registry.containsKey(name))
            return false;
        
        if(strictName && !Util.isAlphaNumeric(name))
            throw new IllegalArgumentException();
        
        registry.put(name, new Radio(plugin, name, local));
        return true;
    }
    
    public static Radio getChannelByPlayer(Player player) {
        return getChannel(listener.get(player.getName()));
    }
    
    public static Radio getChannel(String name) {
        return registry.get(name);
    }
    
    public static List<Radio> getChannels() {
        List<Radio> list = new ArrayList<>();
        registry.forEach((name, radio) -> list.add(radio));
        
        return list;
    }
    
    public void removeChannel() {
        if(songPlayer != null) {
            songPlayer.getPlayerList().forEach(p -> listener.remove(p));
            songPlayer.destroy();
        }
        registry.remove(this.getName());
    }
    
    public void join(Player player) {
        if(songPlayer != null) {
            songPlayer.addPlayer(player);
        }
        listener.put(player.getName(), name);
    }
    
    public void quit(Player player) {
        if(songPlayer != null) {
            songPlayer.removePlayer(player);
        }
        listener.remove(player.getName());
    }
    
    public String getName() {
        return name;
    }
    
    public com.xxmicloxx.NoteBlockAPI.Song getSongPlaying() {
        if(songPlayer != null) {
            return songPlayer.getSong();
        }
        return null;
    }
    
    public Playlist getPlaylist() {
        return playlist;
    }
    
    public boolean isPlaying() {
        return songPlayer != null && songPlayer.isPlaying();
    }
    
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        updateSongs(true);
    }
    
    public boolean pause() {
        if(isPlaying()) {
            songPlayer.setPlaying(false);
            return true;
        }
        
        return false;
    }
    
    public boolean resume() throws FileNotFoundException {
        if(songPlayer != null) {
            songPlayer.setPlaying(true);
            return true;
        }
        
        return playNext(false);
    }
    
    /**
     * Play the next song in the playlist for this radio.
     * @param skip Whether to skip the current song.
     * @return False if this radio failed to play.
     * @throws FileNotFoundException thrown if the song file is missing
     * @throws IndexOutOfBoundsException thrown if radio has reached the last song.
     */
    public boolean playNext(boolean skip) throws FileNotFoundException, IndexOutOfBoundsException {
        if(isPlaying()) {
            if(!skip) {
                return false;
            }
            
            songPlayer.destroy();
        }
        else if(playlist == null) {
            return false;
        }
        
        Song song = plugin.songRegistry.getSong(songs.get(index++));
        
        if(song == null) {
            throw new FileNotFoundException();
        }
        
        playSongFile(song.getPath());
        return true;
    }
    
    /**
     * Play the specific song in the playlist for this radio.
     * @param id ID of the song to play
     * @return False if playlist is not defined.
     * @throws FileNotFoundException thrown if song file is missing
     */
    public boolean play(int id) throws FileNotFoundException {
        if(playlist == null) {
            return false;
        }
        
        if(isPlaying()) {
            songPlayer.destroy();
        }
        
        playSongFile(plugin.songRegistry.getSong(id).getPath());
        return true;
    }
    
    public void updateSongs(boolean shuffle) {
        songs.addAll(playlist.getSongs());
        index = 0;
        
        if(shuffle) {
            Collections.shuffle(songs);
        }
    }
    
    @EventHandler
    public void onSongEnd(SongEndEvent event) {
        File endSong = event.getSongPlayer().getSong().getPath();
        
        if(songPlayer == null || !endSong.equals(songPlayer.getSong().getPath())) {
            return;
        }
        
        try {
            if(!songPlayer.isPlaying() && !(autoSleep && songPlayer.getPlayerList().size() < 1)) {
                playNext(false);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException ignored) {
            repeatRadio();
        }
    }
    
    private void repeatRadio() {
        if(repeat) {
            updateSongs(true);
            try {
                playNext(false);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void playSongFile(File file) throws FileNotFoundException {
        if(file.exists()) {
            songPlayer = new RadioSongPlayer(NBSDecoder.parse(file), SoundCategory.RECORDS);
            songPlayer.setPlaying(true);
        }
        else {
            throw new FileNotFoundException("Song file is missing: " + file.getName());
        }
    
        for(String playerName : listener.keySet()) {
            if(listener.get(playerName).equals(this.getName())) {
                Player player = Bukkit.getPlayer(playerName);
    
                if(player != null) {
                    player.sendMessage("Now playing: " + getSongPlaying().getTitle());
                    songPlayer.addPlayer(player);
                }
            }
        }
    }
    
}
