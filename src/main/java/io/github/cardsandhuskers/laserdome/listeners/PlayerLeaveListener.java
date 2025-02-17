package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.handlers.GameStageHandler;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {

    private final GameStageHandler gameStageHandler;
    private final Team teamA;
    private final Team teamB;

    public PlayerLeaveListener(GameStageHandler gameStageHandler, Team teamA, Team teamB) {
        this.gameStageHandler = gameStageHandler;
        this.teamA = teamA;
        this.teamB = teamB;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        if(e.getPlayer().getGameMode() != GameMode.ADVENTURE) return;
        Team team = TeamHandler.getInstance().getPlayerTeam(e.getPlayer());
        if (team == null) return;

        if(team.equals(teamA)) {
            gameStageHandler.onValidShot(teamA);
        }

        if(team.equals(teamB)) {
            gameStageHandler.onValidShot(teamB);
        }
    }
}
