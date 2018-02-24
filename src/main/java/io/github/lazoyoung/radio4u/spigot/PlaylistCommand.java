package io.github.lazoyoung.radio4u.spigot;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlaylistCommand implements CommandExecutor {
    
    private Radio4Spigot plugin;
    private HashMap<UUID, String> selection = new HashMap<>();
    private final UUID consoleId = UUID.randomUUID();
    
    
    public PlaylistCommand(Radio4Spigot plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(new String[] {
                    "Manage your noteblock music playlist!\n\n",
                    "- /playlist select <name>\n",
                    "-- Select a playlist to play or modify.\n\n",
                    "- /playlist show\n",
                    "-- Print the list of songs.\n\n",
                    "- /playlist <create/remove> <name>\n",
                    "-- Make or delete a playlist.\n\n",
                    "- /playlist song <add/remove/clearall> <id>[,id, ...]\n",
                    "-- Add or remove songs from the playlist.\n\n"
            });
        }
        
        String sub = args[0].toLowerCase();
        
        switch(sub) {
            case "select":
                return select(sender, args);
                
            case "show":
            case "list":
            case "showlist":
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
    
    private Playlist getSelection(CommandSender sender, boolean warn) {
        Playlist pl;
        
        if(sender instanceof ConsoleCommandSender) {
            pl = Playlist.getPlaylist(selection.get(consoleId));
        }
        else if(sender instanceof Player) {
            pl = Playlist.getPlaylist(selection.get(((Player) sender).getUniqueId()));
        }
        else {
            return null;
        }
        
        if(pl == null && warn) {
            sender.sendMessage("Select a playlist first: /playlist select <id>");
        }
        
        return pl;
    }
    
    private boolean select(CommandSender sender, String[] args) {
        String name = args[1];
        
        if(name == null) {
            sender.sendMessage("Please input the name of playlist.");
            return false;
        }
        
        UUID id = consoleId;
        
        if(sender instanceof Player) {
            id = ((Player) sender).getUniqueId();
        }
        else if(!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("You are not allowed to use this command.");
            return false;
        }
        
        Playlist pl = Playlist.getPlaylist(name);
        
        if(pl != null) {
            selection.put(id, pl.getName());
            sender.sendMessage("Selected playlist: " + name);
        }
        else {
            sender.sendMessage("That playlist does not exist.");
        }
        
        return true;
    }
    
    /*
    private boolean play(CommandSender sender, boolean pause) {
        Playlist pl = getSelection(sender, true);
        
        if(sender instanceof ConsoleCommandSender) {
            sender.sendMessage("You are not allowed to use this command!");
        }
        else if(pl != null) {
            if(pause) {
                pl.pause((Player) sender);
            } else {
                pl.play((Player) sender);
            }
        }
        return true;
    }
    */
    
    private boolean show(final CommandSender sender) {
        Playlist pl = getSelection(sender, true);
        
        if(pl != null) {
            List<Integer> songList = pl.getSongs();
            
            if(songList.isEmpty()) {
                sender.sendMessage("This playlist is empty.");
            }
            else {
                final SongRegistry registry = plugin.songRegistry;
                
                songList.forEach(integer -> {
                    Song s = registry.getSongInfo(integer);
                    sender.sendMessage(s.id + " - " + s.name);
                });
            }
        }
        return true;
    }
    
    private boolean create(CommandSender sender, String[] args) {
        String name = args[1];
        
        if(name == null) {
            sender.sendMessage("Please input the name of playlist.");
            return false;
        }
        
        boolean success = Playlist.createPlaylist(plugin, name);
        
        if(success) {
            sender.sendMessage("Playlist " + name.toLowerCase() + " has been created.");
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
        final Playlist pl = getSelection(sender, true);
        
        if(pl == null) {
            return true;
        }
        
        if(operate == null) {
            sender.sendMessage("Operation is missing: add / remove / clearall");
            return false;
        }
        
        operate = operate.toLowerCase();
        
        if(args.length < 3) {
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
                list.forEach(id -> {
                    try {
                        pl.addSong(id);
                    } catch (IOException e) {
                        e.printStackTrace();
                        sender.sendMessage("Error occurred while the operation with: " + id);
                    }
                });
                sender.sendMessage("Added " + list.size() + " songs into playlist " + pl.getName() + ".");
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
                sender.sendMessage("Removed " + list.size() + " songs from playlist " + pl.getName() + ".");
                break;
            case "clearall":
            case "clear":
                int cnt = pl.getSongs().size();
                try {
                    pl.clearSongs();
                } catch (IOException e) {
                    e.printStackTrace();
                    sender.sendMessage("Failed to write changes to playlist file.");
                }
                sender.sendMessage("Cleared " + cnt + " songs.");
                break;
            default:
                sender.sendMessage("Unknown operation: " + operate);
                return false;
        }
        
        return true;
    }
}
