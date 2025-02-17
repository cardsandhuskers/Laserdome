package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.Laserdome;
import io.github.cardsandhuskers.laserdome.handlers.GameStageHandler;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import static io.github.cardsandhuskers.laserdome.Laserdome.gameState;

public class PlayerMoveListener implements Listener {
    private final char centerLineAxis;
    private final Laserdome plugin;
    private final Location teamASpawn;
    private final Location teamBSpawn;
    private final Team teamA;
    private final Team teamB;
    private final GameStageHandler gameStageHandler;
    public PlayerMoveListener(Laserdome plugin, Team teamA, Team teamB, GameStageHandler gameStageHandler) {
        this.plugin = plugin;
        this.teamA = teamA;
        this.teamB = teamB;
        this.gameStageHandler = gameStageHandler;
        teamASpawn = plugin.getConfig().getLocation("TeamASpawn");
        teamBSpawn = plugin.getConfig().getLocation("TeamBSpawn");
        int xDiff = Math.abs(teamASpawn.getBlockX() - teamBSpawn.getBlockX());
        int zDiff = Math.abs(teamASpawn.getBlockZ() - teamBSpawn.getBlockZ());
        if(xDiff > zDiff) {
            centerLineAxis = 'z';
        } else {
            centerLineAxis = 'x';
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location l = p.getLocation();
        Team playerTeam = TeamHandler.getInstance().getPlayerTeam(p);
        if(playerTeam == null || (!playerTeam.equals(teamA) && !playerTeam.equals(teamB))) {
            return;
        }
        if(gameState == Laserdome.GameState.GAME_STARTING || gameState == Laserdome.GameState.GAME_OVER) {
            return;
        }
        if(p.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        Location playerTeamSpawn;

        //figure out which spawn/side they belong on
        if(playerTeam.equals(teamA)) {
            playerTeamSpawn = teamASpawn;
        }
        else {
            playerTeamSpawn = teamBSpawn;
        }

        if(l.getY() < playerTeamSpawn.getY() - 5) {
            if(gameState != Laserdome.GameState.ROUND_ACTIVE) {
                p.teleport(playerTeamSpawn);
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> p.setHealth(20), 5L);

            } else {
                gameStageHandler.onValidShot(playerTeam);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1, 2);
                p.setGameMode(GameMode.SPECTATOR);

                ItemStack[] invContents = p.getInventory().getContents();
                p.getInventory().clear();

                for (ItemStack invContent : invContents) {
                    if (invContent != null && invContent.getType() == Material.ARROW) {
                        Location playerSpawn;
                        Team team = TeamHandler.getInstance().getPlayerTeam(p);
                        if (team.equals(teamA)) {
                            playerSpawn = plugin.getConfig().getLocation("TeamASpawn");
                        } else {
                            playerSpawn = plugin.getConfig().getLocation("TeamBSpawn");
                        }
                        Location arrowSpawn = new Location(playerSpawn.getWorld(), playerSpawn.getX(), playerSpawn.getY() + 3, playerSpawn.getZ());
                        arrowSpawn.getWorld().dropItemNaturally(arrowSpawn, invContent);
                        arrowSpawn.getWorld().spawnParticle(Particle.CLOUD, arrowSpawn, 40);
                    }
                }
                Bukkit.broadcastMessage(playerTeam.color + p.getName() + ChatColor.RESET + " fell in lava.");
            }
        }

        if(centerLineAxis == 'x') {
            int centerLine = (teamASpawn.getBlockZ() + teamBSpawn.getBlockZ())/2;
            //compare current location to center line based on side
            if(playerTeamSpawn.getZ() < centerLine) {
                if(l.getZ() >= centerLine) {
                    //e.setCancelled(true);
                    l.setZ(l.getZ() - 1);
                    p.teleport(l);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1,1);
                    p.sendMessage(ChatColor.RED + "You cannot cross the center line!");
                }
            }
            if(playerTeamSpawn.getZ() > centerLine) {
                if(l.getZ() <= centerLine + 1) {
                    //e.setCancelled(true);
                    l.setZ(l.getZ() + 1);
                    p.teleport(l);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1,1);
                    p.sendMessage(ChatColor.RED + "You cannot cross the center line!");
                }
            }
        }
        if(centerLineAxis == 'z') {
            int centerLine = (teamASpawn.getBlockX() + teamBSpawn.getBlockX())/2;
            //compare current location to center line based on side
            if(playerTeamSpawn.getX() < centerLine) {
                if(l.getX() >= centerLine) {
                    //e.setCancelled(true);
                    l.setX(l.getX() - 1);
                    p.teleport(l);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1,1);
                    p.sendMessage(ChatColor.RED + "You cannot cross the center line!");
                }
            }
            if(playerTeamSpawn.getX() > centerLine) {
                if(l.getX() <= centerLine + 1) {
                    //e.setCancelled(true);
                    l.setX(l.getX() + 1);
                    p.teleport(l);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1,1);
                    p.sendMessage(ChatColor.RED + "You cannot cross the center line!");
                }
            }
        }
    }
}
