package io.github.lazoyoung.radio4u.spigot;

import io.github.lazoyoung.radio4u.spigot.exception.UnsupportedSenderException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
                    "/playlist list\n",
                    "└ Print the list of playlists.\n",
                    "/playlist show\n",
                    "└ Print the list of songs.\n",
                    "/playlist <create/remove> <name>\n",
                    "└ Make or delete a playlist.\n",
                    "/playlist song <add/remove/clearall> <id>[,id, ...]\n",
                    "└ Add or remove songs from the playlist.\n"
            });
            return true;
        }
        
        String sub = args[0].toLowerCase();
        
        switch(sub) {
            case "select":
                return select(sender, args);
    
            case "list":
                return list(sender);
                
            case "show":
            case "info":
            case "listsong":
            case "listsongs":
            case "songlist":
                return show(sender);
                
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
        
        Playlist pl = Playlist.getPlaylist(name);
        
        try {
            if (pl != null) {
                Playlist.selectPlaylist(sender, pl);
                sender.sendMessage("Selected playlist: " + name);
            } else {
                sender.sendMessage("That playlist does not exist.");
            }
        } catch(UnsupportedSenderException e) {
            e.informSender();
        }
        
        return true;
    }
    
    private boolean list(CommandSender sender) {
        for(Playlist playlist : Playlist.getAllPlaylists()) {
            String name = playlist.getName();
            int count = playlist.getSongs().size();
            
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
            int count = global.getSongs().size();
            sender.sendMessage("global - " + count + " songs");
        }
        return true;
    }
    
    private boolean show(final CommandSender sender) {
        Playlist pl = getSelection(sender);
        
        if(pl != null) {
            List<Integer> songList = pl.getSongs();
            
            if(songList.isEmpty()) {
                sender.sendMessage("This playlist is empty.");
            }
            else {
                final SongRegistry registry = plugin.songRegistry;
                
                songList.forEach(integer -> {
                    Song s = registry.getSongFromDisk(integer);
                    if(s != null) {
                        sender.sendMessage(s.id + " - " + s.name);
                    } else {
                        sender.sendMessage(integer + " - Unknown song");
                    }
                });
            }
        }
        return true;
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
            sender.sendMessage("Alphanumeric names are only accepted.");
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
                int cnt = pl.getSongs().size();
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
                int unknown = 0;
                for (int id : list) {
                    Song song = plugin.songRegistry.getSongFromDisk(id);
                    try {
                        if (song != null) {
                            pl.addSong(song);
                            count++;
                        } else {
                            unknown++;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        sender.sendMessage("Error occurred while the operation with: " + id);
                    }
                }
                if(unknown > 0) {
                    sender.sendMessage("Found " + unknown + " unknown songs! Register those with: /song");
                }
                sender.sendMessage("Added " + count + " song(s) into \'" + pl.getName() + "\'.");
                break;
            case "remove":
                list.forEach(id -> {
                    try {
                        pl.removeSong(id);
                    } catch (IOException e) {
                        e.printStackTrace();
                        sender.sendMessage("Error occurred while the operation with: " + id);
                    }
                });
                sender.sendMessage("Removed " + list.size() + " song(s) from \'" + pl.getName() + "\'.");
                break;
            default:
                sender.sendMessage("Unknown operation: " + operate);
                return false;
        }
        
        return true;
    }
}