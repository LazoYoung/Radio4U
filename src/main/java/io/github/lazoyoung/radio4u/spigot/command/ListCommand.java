package io.github.lazoyoung.radio4u.spigot.command;

import io.github.lazoyoung.radio4u.spigot.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListCommand {
    
    private String cmd;
    
    
    public ListCommand(String pageCommand) {
        this.cmd = pageCommand + " ";
    }
    
    public void displayListHeader(String title, int page, int lastPage, CommandSender sender) {
        String side = "-------";
        String centre = " " + title + " (" + page + "/" + lastPage + ") ";
        
        if(sender instanceof Player && Util.isSpigot()) {
            String left = " [" + ChatColor.BOLD + ChatColor.YELLOW + "<" + ChatColor.RESET + "]";
            String right = "[" + ChatColor.BOLD + ChatColor.YELLOW + ">" + ChatColor.RESET + "] ";
            TextComponent body = new TextComponent();
            TextComponent leftBtn = new TextComponent(left);
            TextComponent rightBtn = new TextComponent(right);
    
            body.addExtra(side);
            
            if(page > 1) {
                leftBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("< Previous Page").create()));
                leftBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd + (page - 1)));
                body.addExtra(leftBtn);
            }
            
            body.addExtra(centre);
            
            if(page < lastPage) {
                rightBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Next Page >").create()));
                rightBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd + (page + 1)));
                body.addExtra(rightBtn);
            }
            
            body.addExtra(side);
            
            sender.spigot().sendMessage(body);
            return;
        }
    
        sender.sendMessage(side + centre + side);
    }
    
}
