package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.Laserdome;
import io.github.cardsandhuskers.laserdome.handlers.ArenaColorHandler;
import io.github.cardsandhuskers.laserdome.handlers.GameStageHandler;
import io.github.cardsandhuskers.laserdome.objects.ArrowHolder;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

import static io.github.cardsandhuskers.teams.Teams.handler;

public class ArrowHitListener implements Listener {
    private final Laserdome plugin;
    private final Team teamA;
    private final Team teamB;
    private final ArenaColorHandler arenaColorHandler;
    private final GameStageHandler gameStageHandler;
    ArrowHolder arrow1, arrow2;

    public ArrowHitListener(Laserdome plugin, Team teamA, Team teamB, ArenaColorHandler arenaColorHandler, GameStageHandler gameStageHandler, ArrowHolder arrow1, ArrowHolder arrow2) {
        this.arenaColorHandler = arenaColorHandler;
        this.plugin = plugin;
        this.teamA = teamA;
        this.teamB = teamB;
        this.gameStageHandler = gameStageHandler;
        this.arrow1 = arrow1;
        this.arrow2 = arrow2;
    }
    @EventHandler
    public void onArrowHit(ProjectileHitEvent e) {
        if(e.getEntity().getType() != EntityType.ARROW) {
            return;
        }
        if(e.getEntity().getShooter() instanceof Player attacker) {
            Team attackerTeam = handler.getPlayerTeam(attacker);
            Arrow arrow = (Arrow)e.getEntity();
            NamespacedKey key = new NamespacedKey(plugin, "ID");
            String id = arrow.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if(id == null) {
                System.out.println("ID IS NULL");
                return;
            }

            //hit wall
            if(e.getHitBlock() != null) {
                //miss!
                if (arrow1.getId().equals(UUID.fromString(id))) {
                    arrow1.onArrowHit();
                } else if (arrow2.getId().equals(UUID.fromString(id))) {
                    arrow2.onArrowHit();
                }
                e.getEntity().remove();

            } else if(e.getHitEntity() != null && e.getHitEntity() instanceof Player target) {
                System.out.println("HIT PLAYER");
                //hit!
                Team targetTeam = TeamHandler.getInstance().getPlayerTeam(target);
                if (attackerTeam != targetTeam) {

                    if (arrow1.getId().equals(UUID.fromString(id))) {
                        arrow1.onArrowHit();
                    } else if (arrow2.getId().equals(UUID.fromString(id))) {
                        arrow2.onArrowHit();
                    }

                    if (attackerTeam == teamA && targetTeam == teamB) {
                        //valid shot has hit target
                        gameStageHandler.onValidShot(teamB);
                        sendMessages(attacker, target, teamB);
                    }
                    if (attackerTeam == teamB && targetTeam == teamA) {
                        //valid shot has hit target
                        gameStageHandler.onValidShot(teamA);
                        sendMessages(attacker, target, teamA);
                    }

                    e.setCancelled(true);
                } else {
                    //friendly fire!
                    if (arrow1.getId().equals(UUID.fromString(id))) {
                        arrow1.onArrowHit();
                    } else if (arrow2.getId().equals(UUID.fromString(id))) {
                        arrow2.onArrowHit();
                    }
                    e.setCancelled(true);

                }
            } else {
                System.out.println("HIT SOMETHING ELSE");
            }
        }

        arenaColorHandler.numShots++;
        if(arenaColorHandler.numShots %2 == 0) shrinkArena();
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

    private void sendMessages(Player attacker, Player attacked, Team attackedTeam) {
        if(gameStageHandler.killsMap.containsKey(attacker)) gameStageHandler.killsMap.put(attacker, gameStageHandler.killsMap.get(attacker) + 1);
        else gameStageHandler.killsMap.put(attacker, 1);


        attacked.setGameMode(GameMode.SPECTATOR);

        //maybe: delay so that drawn bow on death won't double send arrow DOES NOT WORK
        //can still shoot after being put in spec, that's the issue
        ItemStack[] invContents = attacked.getInventory().getContents();
        attacked.getInventory().clear();

        for (ItemStack invContent : invContents) {
            if (invContent != null && invContent.getType() == Material.ARROW) {
                Location l;
                if (attackedTeam.equals(teamA)) {
                    l = plugin.getConfig().getLocation("TeamASpawn");
                } else {
                    l = plugin.getConfig().getLocation("TeamBSpawn");
                }
                Location arrowSpawn = new Location(l.getWorld(), l.getX(), l.getY() + 3, l.getZ());
                arrowSpawn.getWorld().dropItemNaturally(arrowSpawn, invContent);
                arrowSpawn.getWorld().spawnParticle(Particle.CLOUD, arrowSpawn, 40);
            }
        }

        attacker.playSound(attacker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
        attacked.playSound(attacked.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT,1,2);
        attacker.sendMessage("You shot " + handler.getPlayerTeam(attacked).color + attacked.getName() + ChatColor.RESET + "!");
        attacked.sendMessage("You were shot by " + handler.getPlayerTeam(attacker).color + attacker.getName() + ChatColor.RESET + ".");
        for(Player p: Bukkit.getOnlinePlayers()) {
            if(!p.equals(attacked) && !p.equals(attacker)) {
                p.sendMessage(handler.getPlayerTeam(attacker).color + attacker.getName() + ChatColor.RESET + " shot " +
                        handler.getPlayerTeam(attacked).color + attacked.getName() + ChatColor.RESET + ".");
            }
        }
    }
}
