package io.github.lazoyoung.radio4u.spigot.command;

import io.github.lazoyoung.radio4u.spigot.Radio4Spigot;
import io.github.lazoyoung.radio4u.spigot.Text;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Command;

@Command(name = "radio4u", desc = "Main command for Radio4U.", aliases = {"radioplugin", "musicplugin"}, permission = "radio4u.plugin")
public class Radio4UCommand implements CommandExecutor {

    private Radio4Spigot plugin;
    private Text text;

    public Radio4UCommand(Radio4Spigot plugin) {
        this.plugin = plugin;
        this.text = plugin.text;
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
                "/radio4u reload : " + text.get("command.radio4u.reload") + "\n",
                "/radio : " + text.get("command.radio") + "\n",
                "/playlist : " + text.get("command.playlist") + "\n"
        });
        return true;
    }

    private boolean reload(CommandSender sender) {
        if(sender.hasPermission("radio4u.plugin.reload")) {
            plugin.loadSongs(sender);
        }
        else {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
        }
        return true;
    }
}
