package io.github.cardsandhuskers.laserdome.commands;

import io.github.cardsandhuskers.laserdome.Laserdome;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetTeamSpawnCommand implements CommandExecutor {
    private Laserdome plugin;
    public SetTeamSpawnCommand(Laserdome plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof  Player p && p.isOp()) {
            if(args.length > 0) {
                Location l = p.getLocation();
                String team;
                try {
                    team = args[0];
                    if(!(team.equalsIgnoreCase("a") || team.equalsIgnoreCase("b"))) {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    p.sendMessage(ChatColor.RED + "ERROR: Argument must be A or B");
                    return false;
                }

                plugin.getConfig().set("Team" + team.toUpperCase() + "Spawn", l);
                plugin.saveConfig();
                p.sendMessage("Location set to " + l.toString());

            } else {
                p.sendMessage(ChatColor.RED + "ERROR: Must specify either team A or B");
            }
        } else if(sender instanceof Player p) {
            p.sendMessage(ChatColor.RED + "ERROR: You do not have sufficient permission to do this");
        } else {
            System.out.println(ChatColor.RED + "ERROR: Cannot run from console");
        }

        return true;
    }

}
