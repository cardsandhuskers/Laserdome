package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.handlers.GameStageHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static io.github.cardsandhuskers.teams.Teams.handler;

public class PlayerJoinListener implements Listener {

    private GameStageHandler gameStageHandler;
    private Team teamA, teamB;

    public PlayerJoinListener(GameStageHandler gameStageHandler, Team teamA, Team teamB) {
        this.gameStageHandler = gameStageHandler;
        this.teamA = teamA;
        this.teamB = teamB;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Team team = handler.getPlayerTeam(e.getPlayer());
        if(team == null) return;
        if(team.equals(teamA) || team.equals(teamB)) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("Laserdome"),
                    ()-> e.getPlayer().setGameMode(GameMode.SPECTATOR), 5);

        }
    }

}
