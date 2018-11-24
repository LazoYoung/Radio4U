package io.github.lazoyoung.radio4u.spigot.command;

import io.github.lazoyoung.radio4u.spigot.Radio4Spigot;
import io.github.lazoyoung.radio4u.spigot.Util;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Command;

@Command(name = "radio4u", desc = "Main command for Radio4U.", aliases = {"radioplugin", "musicplugin"}, permission = "radio4u.plugin")
public class Radio4UCommand implements CommandExecutor {

    private Radio4Spigot plugin;

    public Radio4UCommand(Radio4Spigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
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
        if(sender.hasPermission("radio4u.plugin.reload")) {
            plugin.loadSongs(sender);
        }
        else {
            sender.sendMessage(Util.PERMISSION_DENIED);
        }
        return true;
    }
}
