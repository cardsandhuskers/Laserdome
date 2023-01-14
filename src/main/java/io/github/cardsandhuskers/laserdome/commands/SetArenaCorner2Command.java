package io.github.cardsandhuskers.laserdome.commands;

import io.github.cardsandhuskers.laserdome.Laserdome;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetArenaCorner2Command implements CommandExecutor {
    private final Laserdome plugin;
    public SetArenaCorner2Command(Laserdome plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player p && p.isOp()) {
            Location location = p.getLocation();
            plugin.getConfig().set("arenaCorner2", location);
            plugin.saveConfig();
            p.sendMessage("Corner 1 Set at: " + location.toString());
        } else if(sender instanceof Player p) {
            p.sendMessage(ChatColor.RED + "ERROR: You do not have sufficient permission to do this");
        } else {
            System.out.println(ChatColor.RED + "ERROR: Cannot run from console");
        }

        return true;
    }
}
