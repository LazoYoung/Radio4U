package io.github.lazoyoung.radio4u.spigot;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class SongCommand implements CommandExecutor {
    
    private Radio4Spigot plugin;
    
    
    public SongCommand(Radio4Spigot plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if(args.length == 0) {
            sender.sendMessage(new String[] {
                    "Song : register noteblock songs\n",
                    " \n",
                    "/song import <file-name.nbs> [id]\n",
                    "└ Import a .nbs file into this plugin.\n",
                    "/song autoimport\n",
                    "└ Import every .nbs files which ain't registered.\n",
                    "/song discard <id>\n",
                    "└ Remove the song with given id from plugin.\n"
            });
            return true;
        }
        
        String sub = args[0].toLowerCase();
        
        switch(sub) {
            case "import":
                return importSong(sender, args);
                
            case "autoimport":
                return autoImportSong(sender);
                
            case "discard":
                return discardSong(sender, args);
                
            default:
                return false;
        }
    }
    
    private boolean importSong(CommandSender sender, String[] args) {
        if(args.length < 2) {
            sender.sendMessage("Please input file name.");
            return false;
        }
        
        StringBuilder fileName = new StringBuilder();
        int id = -1;
        
        for(int i=2; i<args.length; i++) {
            if(args[i].endsWith(".nbs")) {
                if(i + 1 < args.length) {
                    try {
                        id = Integer.parseInt(args[i + 1]);
                    } catch(NumberFormatException e) {
                        sender.sendMessage("Please input a valid number for [id].");
                        return false;
                    }
                } else {
                    id = plugin.songRegistry.getEmptyId();
                }
                break;
            }
            fileName.append(args[i]);
        }
        
        if(id < 0) {
            sender.sendMessage("Please do not omit filename extension! (.nbs)");
            return false;
        }
    
        try {
            plugin.songRegistry.importSong(id, fileName.toString());
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage("Error occurred while writing configuration.");
        }
        return true;
    }
    
    private boolean autoImportSong(CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int count = plugin.songRegistry.importNewSongs();
            
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                sender.sendMessage("Imported " + count + " nbs files.");
            });
        });
        
        return true;
    }
    
    private boolean discardSong(CommandSender sender, String[] args) {
        if(args.length < 2) {
            sender.sendMessage("Please input the id for the song to be removed.");
            return false;
        }
        
        try {
            plugin.songRegistry.discardSong(Integer.parseInt(args[1]));
        } catch(NumberFormatException e) {
            sender.sendMessage("Please input a valid number for <id>");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage("Error occurred while modifying playlist file!");
        }
    
        return true;
    }
    
}
