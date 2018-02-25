package io.github.lazoyoung.radio4u.spigot.exception;

import org.bukkit.command.CommandSender;

public class UnsupportedSenderException extends Exception {
    
    private CommandSender sender;
    
    public UnsupportedSenderException(CommandSender sender) {
        this.sender = sender;
    }
    
    public CommandSender getSender() {
        return sender;
    }
    
    public void informSender() {
        sender.sendMessage("You are not allowed to do this.");
    }
}
