package io.github.cardsandhuskers.laserdome.commands;

import io.github.cardsandhuskers.laserdome.Laserdome;
import io.github.cardsandhuskers.laserdome.handlers.GameStageHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static io.github.cardsandhuskers.teams.Teams.handler;

public class StartGameCommand implements CommandExecutor {
    private Laserdome plugin;
    public StartGameCommand(Laserdome plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player p && p.isOp()) {
            if (handler.getTeams().size() >= 2) {
                GameStageHandler gameStageHandler = new GameStageHandler(plugin);
                gameStageHandler.startGame();
                return true;
            }
            p.sendMessage(ChatColor.RED + "ERROR: There must be at least 2 teams");
            return true;
        }
        return false;
    }
}
