package io.github.lazoyoung.radio4u.spigot.command;

import io.github.lazoyoung.radio4u.spigot.Radio4Spigot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Radio4UCommand implements CommandExecutor {

    private Radio4Spigot plugin;

    public Radio4UCommand(Radio4Spigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!cmd.getName().equals("radio4u")) {
            return true;
        }

        if(args.length > 0) {
            String arg = args[0].toLowerCase();

            switch(arg) {
                case "reload":
                    return reload(sender);

                default:
                    return false;
            }
        }

        sender.sendMessage(new String[] {
                "/radio4u reload : Reload song files from disk.\n",
                "/radio : Broadcast music through a radio channel.\n",
                "/playlist : Manage your playlist.\n"
        });
        return true;
    }

    private boolean reload(CommandSender sender) {
        plugin.loadSongs(sender);
        return true;
    }
}
