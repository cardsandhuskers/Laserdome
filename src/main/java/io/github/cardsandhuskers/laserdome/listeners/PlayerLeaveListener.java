package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.handlers.GameStageHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static io.github.cardsandhuskers.teams.Teams.handler;

public class PlayerLeaveListener implements Listener {

    private GameStageHandler gameStageHandler;
    private Team teamA, teamB;

    public PlayerLeaveListener(GameStageHandler gameStageHandler, Team teamA, Team teamB) {
        this.gameStageHandler = gameStageHandler;
        this.teamA = teamA;
        this.teamB = teamB;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        if(e.getPlayer().getGameMode() != GameMode.ADVENTURE) return;
        Team team = handler.getPlayerTeam(e.getPlayer());
        if (team == null) return;

        if(team.equals(teamA)) {
            gameStageHandler.onValidShot(teamA);
        }

        if(team.equals(teamB)) {
            gameStageHandler.onValidShot(teamB);
        }
    }
}
