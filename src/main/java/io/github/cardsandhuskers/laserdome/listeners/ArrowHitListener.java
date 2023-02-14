package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.Laserdome;
import io.github.cardsandhuskers.laserdome.handlers.ArenaColorHandler;
import io.github.cardsandhuskers.laserdome.handlers.GameStageHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import static io.github.cardsandhuskers.teams.Teams.handler;

public class ArrowHitListener implements Listener {
    private Laserdome plugin;
    private Team teamA, teamB;
    private ArenaColorHandler arenaColorHandler;
    private GameStageHandler gameStageHandler;

    public ArrowHitListener(Laserdome plugin, Team teamA, Team teamB, ArenaColorHandler arenaColorHandler, GameStageHandler gameStageHandler) {
        this.arenaColorHandler = arenaColorHandler;
        this.plugin = plugin;
        this.teamA = teamA;
        this.teamB = teamB;
        this.gameStageHandler = gameStageHandler;
    }
    @EventHandler
    public void onArrowHit(ProjectileHitEvent e) {
        if(e.getEntity().getType() != EntityType.ARROW) {
            return;
        }
        if(e.getEntity().getShooter() instanceof Player p) {
            Team team = handler.getPlayerTeam(p);
            if(team == null) {
                return;
            }
            Location arrowSpawn;
            Location temp;
            if(team.equals(teamA)) {
                temp = plugin.getConfig().getLocation("TeamBSpawn");
            } else if(team.equals(teamB)) {
                temp = plugin.getConfig().getLocation("TeamASpawn");
            } else {
                return;
            }
            arrowSpawn = new Location(temp.getWorld(), temp.getX(), temp.getY() + 3, temp.getZ());
            arrowSpawn.getWorld().dropItemNaturally(arrowSpawn, new ItemStack(Material.ARROW));
            arrowSpawn.getWorld().spawnParticle(Particle.CLOUD, arrowSpawn, 40);
            e.getEntity().remove();

            arenaColorHandler.numShots++;
            if(arenaColorHandler.numShots %2 == 0) shrinkArena();
        }
    }

    private void shrinkArena() {
        if(arenaColorHandler.numShrinks >= 4) return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
            if(!gameStageHandler.isGameActive()) return;
            final int shrinks = arenaColorHandler.numShrinks;
            for(int i = 1; i <=6; i++) {
                int finalI = i;
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                    if(finalI %2 == 0) {
                        arenaColorHandler.shrinkArena(Material.RED_STAINED_GLASS, shrinks);
                    } else {
                        arenaColorHandler.shrinkArena(Material.YELLOW_STAINED_GLASS, shrinks);
                        for(Player player: Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_DAMAGE, 1, .5F);
                        }
                    }
                },10L * i);
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                for(Player player: Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1, .5F);
                }
                arenaColorHandler.shrinkArena(Material.AIR, shrinks);
            },70L);
            Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Arena is about to shrink. Watch out!");
            arenaColorHandler.numShrinks++;
        }, 60L);
    }
}
