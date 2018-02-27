package io.github.lazoyoung.radio4u.spigot;

import com.xxmicloxx.NoteBlockAPI.Song;
import io.github.lazoyoung.radio4u.spigot.exception.UnsupportedSenderException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlaylistCommand implements CommandExecutor {
    
    private Radio4Spigot plugin;
    
    
    public PlaylistCommand(Radio4Spigot plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(new String[] {
                    "Playlist : manage noteblock song playlist!\n",
                    " \n",
                    "/playlist select <name>\n",
                    "└ Select a playlist to play or modify.\n",
                    "/playlist play\n",
                    "└ Play the selected playlits.\n",
                    "/playlist list\n",
                    "└ Print the list of playlists.\n",
                    "/playlist info\n",
                    "└ Print the list of songs.\n",
                    "/playlist <create/remove> <name>\n",
                    "└ Make or delete a playlist.\n",
                    "/playlist song <add/remove/clearall> <id>[,id, ...]\n",
                    "└ Add or remove songs from the playlist.\n"
            });
            return true;
        }
        
        String sub = args[0].toLowerCase();
        
        try {
            switch (sub) {
                case "select":
                    return select(sender, args);
        
                case "play":
                    return play(sender);
        
                case "list":
                    return list(sender);
        
                case "info":
                case "show":
                case "track":
                case "tracks":
                case "tracklist":
                    return tracklist(sender, args);
        
                case "create":
                case "add":
                case "make":
                    return create(sender, args);
        
                case "remove":
                case "delete":
                    return remove(sender, args);
        
                case "song":
                    return song(sender, args);
        
                default:
                    return false;
            }
        } catch(UnsupportedSenderException e) {
            e.informSender();
            return true;
        }
    }
    
    private Playlist getSelection(CommandSender sender) {
        Playlist playlist;
        
        if(sender instanceof ConsoleCommandSender) {
            playlist = Playlist.getSelection(Playlist.consoleId);
        }
        else if(sender instanceof Player) {
            playlist = Playlist.getSelection(((Player) sender).getUniqueId());
        }
        else {
            return null;
        }
        
        if(playlist == null) {
            sender.sendMessage("Select a playlist first: /playlist select <name>");
        }
        
        return playlist;
    }
    
    private boolean select(CommandSender sender, String[] args) {
        String name = args[1];
        
        if(name == null) {
            sender.sendMessage("Please input the name of playlist.");
            return false;
        }
        
        try {
            if (Playlist.selectPlaylist(sender, Playlist.getPlaylist(name))) {
                sender.sendMessage("Selected playlist: " + name);
            } else {
                sender.sendMessage("That playlist does not exist.");
            }
        } catch(UnsupportedSenderException e) {
            e.informSender();
        }
        
        return true;
    }
    
    private boolean play(CommandSender sender) throws UnsupportedSenderException {
        Playlist pl = getSelection(sender);
        
        if(pl != null) {
            if(sender instanceof ConsoleCommandSender)
                throw new UnsupportedSenderException(sender);
            
            Player player = (Player) sender;
            String name = "#" + player.getName().toLowerCase();
            
            if(Radio.createChannel(plugin, name, true, false)) {
                Radio channel = Radio.getChannel(name);
                channel.setPlaylist(pl);
                channel.join((Player) sender);
    
                try {
                    channel.playNext(false);
                    sender.sendMessage("You can pause the music with /radio pause");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    sender.sendMessage("Failed to play song: file is missing");
                }
            }
        }
        return true;
    }
    
    private boolean list(CommandSender sender) {
        for(Playlist playlist : Playlist.getAllPlaylists()) {
            String name = playlist.getName();
            int count = playlist.getSongs(false).size();
            
            if(name.equals("global")) {
                continue;
            }
            
            if(count > 0) {
                sender.sendMessage(name + " - " + count + " songs");
            }
            else {
                sender.sendMessage(name + " - empty playlist");
            }
        }
        
        Playlist global = Playlist.getGlobalPlaylist();
        if(global == null) {
            sender.sendMessage("! Global playlist is unavailable now.");
        }
        else {
            int count = global.getSongs(false).size();
            sender.sendMessage("global - " + count + " songs");
        }
        return true;
    }
    
    private boolean tracklist(final CommandSender sender, String[] args) {
        Playlist pl = getSelection(sender);
        
        if(pl != null) {
            final List<Integer> songList = pl.getSongs(true);
            
            if(songList.isEmpty()) {
                sender.sendMessage("This playlist is empty.");
                return true;
            }
    
            int lastPage = (int) Math.ceil(songList.size() / 10);
            int page;
            
            if(args.length > 1) {
                page = Integer.parseInt(args[1]);
                
                if(page > lastPage) {
                    sender.sendMessage("Page " + page + " exceeds the maximum of " + lastPage + ".");
                    return true;
                }
            } else {
                page = 1;
            }
            
            try {
                List<Integer> results = songList.subList((page - 1) * 10, page * 10 - 1);
        
                final SongRegistry registry = plugin.songRegistry;
                results.forEach(id -> {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        showList(sender, registry.getSong(id), id);
                    });
                });
                sender.sendMessage("$ Viewing list of songs: " + page + "/" + lastPage + " page.");
            } catch(NumberFormatException e) {
                sender.sendMessage("Please input valid number in [page]");
                return false;
            }
        }
        return true;
    }
    
    // TODO Improve UI
    private void showList(CommandSender sender, Song song, int id) {
        if(song != null) {
            sender.sendMessage(song.getTitle() + " (" + id + ")");
        } else {
            sender.sendMessage("An unknown song (" + id + ")");
        }
    }
    
    private boolean create(CommandSender sender, String[] args) {
        String name = args[1];
        boolean success;
        
        if(name == null) {
            sender.sendMessage("Please input the name of playlist.");
            return false;
        }
        
        try {
            success = Playlist.createPlaylist(plugin, name);
        } catch(IllegalArgumentException ignored) {
            sender.sendMessage("Alphanumeric names are only accepted. (A-Z, 0-9, dashes)");
            return true;
        }
        
        if(success) {
            sender.sendMessage("Playlist " + name.toLowerCase() + " has been created.");
            try {
                Playlist.selectPlaylist(sender, Playlist.getPlaylist(name));
            }
            catch (UnsupportedSenderException ignored) {}
        }
        else {
            sender.sendMessage("That playlist already exists.");
        }
        
        return true;
    }
    
    private boolean remove(CommandSender sender, String[] args) {
        String name = args[1];
        
        if(name == null) {
            sender.sendMessage("Please input the name of playlist.");
            return false;
        }
        
        boolean success = Playlist.removePlaylist(name);
        
        if(success) {
            sender.sendMessage("Playlist " + name.toLowerCase() + " has been removed.");
        }
        else {
            sender.sendMessage("That playlist can't be removed or is absent.");
        }
    
        return true;
    }
    
    private boolean song(final CommandSender sender, String[] args) {
        String operate = args[1];
        List<Integer> list = new ArrayList<>();
        final Playlist pl = getSelection(sender);
        
        if(pl == null) {
            return true;
        }
        
        if(operate == null) {
            sender.sendMessage("Operation is missing: add / remove / clearall");
            return false;
        }
        
        operate = operate.toLowerCase();
        
        if(args.length < 3) {
            if(operate.equals("clear") || operate.equals("clearall")) {
                int cnt = pl.getSongs(false).size();
                try {
                    pl.clearSongs();
                } catch (IOException e) {
                    e.printStackTrace();
                    sender.sendMessage("Failed to write changes to playlist file.");
                }
                sender.sendMessage("Cleared " + cnt + " songs.");
                return true;
            }
            
            sender.sendMessage("Please provide the song id. (Multiple inputs are allowed)");
            return false;
        }
        
        if(args.length < 4) {
            try {
                String input = args[2];
                
                if(input.contains(",")) {
                    for(String id : input.split(",")) {
                        list.add(Integer.parseInt(id));
                    }
                }
                else {
                    list.add(Integer.parseInt(input));
                }
            } catch(NumberFormatException e) {
                sender.sendMessage("Please input the valid number for <id>.");
                return false;
            }
        }
    
        switch (operate) {
            case "add":
                int count = 0;
                int fails = 0;
                for (int id : list) {
                    try {
                        if (plugin.songRegistry.getSong(id) != null) {
                            if(pl.addSong(id)) {
                                count++;
                            } else {
                                fails++;
                            }
                        } else {
                            fails++;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        sender.sendMessage("Error occurred while the operation with: " + id);
                    }
                }
                if(fails > 0) {
                    sender.sendMessage(fails + " song(s) could not be added.");
                }
                sender.sendMessage("Added " + count + " song(s) into \'" + pl.getName() + "\'.");
                break;
            case "remove":
                for(int id : list) {
                    try {
                        pl.removeSong(id);
                    } catch (IOException e) {
                        e.printStackTrace();
                        sender.sendMessage("Error occurred while the operation with: " + id);
                    }
                }
                sender.sendMessage("Removed " + list.size() + " song(s) from \'" + pl.getName() + "\'.");
                break;
            default:
                sender.sendMessage("Unknown operation: " + operate);
                return false;
        }
        
        return true;
    }
}
