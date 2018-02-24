package io.github.lazoyoung.radio4u.spigot;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class SongCommand implements CommandExecutor {
    
    private SongRegistry registry;
    
    
    public SongCommand(SongRegistry registry) {
        this.registry = registry;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if(args.length == 0) {
            sender.sendMessage(new String[] {
                    "You can manage nbs song registration with this command.\n\n",
                    "- /song import <file-name> [id] [name] [description]\n",
                    "-- Import a .nbs file into this plugin.\n\n",
                    "- /song autoimport\n",
                    "-- Import every .nbs files which ain't registered.\n\n",
                    "- /song discard <id>\n",
                    "-- Remove the song with given id from plugin.\n\n",
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
            sender.sendMessage("Please input the file-name.");
            return false;
        }
        
        String fileName = args[1];
        int id;
        String name = args[3];
        String desc = args[4];
        
        try {
            id = Integer.parseInt(args[2]);
        } catch(NumberFormatException e) {
            if(e.getMessage().equals("null")) {
                id = registry.getEmptyId();
            }
            else {
                sender.sendMessage("Please input a valid number for [id].");
                return false;
            }
        }
    
        try {
            registry.importSong(id, fileName, name, desc);
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage("Error occurred while writing configuration.");
        }
        return true;
    }
    
    private boolean autoImportSong(CommandSender sender) {
        int count = registry.importNewSongs();
        sender.sendMessage("Imported " + count + " nbs files.");
        return true;
    }
    
    private boolean discardSong(CommandSender sender, String[] args) {
        if(args.length < 2) {
            sender.sendMessage("Please input the id for the song to be removed.");
            return false;
        }
        
        try {
            registry.discardSong(Integer.parseInt(args[1]));
        } catch(NumberFormatException e) {
            sender.sendMessage("Please input a valid number for <id>");
            return false;
        }
        
        return true;
    }
    
}
