package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.Laserdome;
import io.github.cardsandhuskers.laserdome.handlers.ArenaColorHandler;
import io.github.cardsandhuskers.laserdome.handlers.GameStageHandler;
import io.github.cardsandhuskers.laserdome.objects.ArrowHolder;
import io.github.cardsandhuskers.laserdome.objects.stats.Stats;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

import static io.github.cardsandhuskers.laserdome.Laserdome.teamAWins;
import static io.github.cardsandhuskers.laserdome.Laserdome.teamBWins;

/**
 * Handles the case when an arrow hits something, either another player or the wall/floor
 */
public class ArrowHitListener implements Listener {
    private final Laserdome plugin;
    private final Team teamA;
    private final Team teamB;
    private final ArenaColorHandler arenaColorHandler;
    private final GameStageHandler gameStageHandler;
    private final ArrowHolder arrow1, arrow2;
    private final Stats stats;

    public ArrowHitListener(Laserdome plugin, Team teamA, Team teamB, ArenaColorHandler arenaColorHandler, GameStageHandler gameStageHandler, ArrowHolder arrow1, ArrowHolder arrow2, Stats stats) {
        this.arenaColorHandler = arenaColorHandler;
        this.plugin = plugin;
        this.teamA = teamA;
        this.teamB = teamB;
        this.gameStageHandler = gameStageHandler;
        this.arrow1 = arrow1;
        this.arrow2 = arrow2;
        this.stats = stats;
    }
    @EventHandler
    public void onArrowHit(ProjectileHitEvent e) {

        if(e.getEntity().getType() != EntityType.ARROW) {
            return;
        }
        if(e.getEntity().getShooter() instanceof Player attacker) {
            Team attackerTeam = TeamHandler.getInstance().getPlayerTeam(attacker);
            Arrow arrow = (Arrow)e.getEntity();
            NamespacedKey key = new NamespacedKey(plugin, "ID");
            String id = arrow.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if(id == null) {
                //System.out.println("ID IS NULL");
                return;
            }

            //shrink attempt before hit is confirmed
            arenaColorHandler.numShots++;
            if(arenaColorHandler.numShots %2 == 0) shrinkArena();

            //hit wall
            if(e.getHitBlock() != null) {
                //miss!
                if (arrow1.getId().equals(UUID.fromString(id))) {
                    arrow1.onArrowHit();
                } else if (arrow2.getId().equals(UUID.fromString(id))) {
                    arrow2.onArrowHit();
                }
                e.getEntity().remove();
                stats.addEntry( (teamAWins + teamBWins + 1) + "," + attacker.getName() + "," + attackerTeam.getTeamName() + ",MISS,NONE,NONE");

            } else if(e.getHitEntity() != null && e.getHitEntity() instanceof Player target) {
                //hit!
                Team targetTeam = TeamHandler.getInstance().getPlayerTeam(target);
                if (attackerTeam != targetTeam) {
                    //System.out.println("Shot Opponent");
                    stats.addEntry((teamAWins + teamBWins + 1) + "," + attacker.getName() + "," + attackerTeam.getTeamName() + ",HIT," + target.getName() + "," + targetTeam.getTeamName());

                    if (attackerTeam == teamA && targetTeam == teamB) {
                        //valid shot has hit target
                        gameStageHandler.onValidShot(teamB);
                        sendMessages(attacker, target);
                    }
                    else if (attackerTeam == teamB && targetTeam == teamA) {
                        //valid shot has hit target
                        gameStageHandler.onValidShot(teamA);
                        sendMessages(attacker, target);
                    }
                    checkedTargetForArrows(target);

                } else {
                    //System.out.println("Shot Teammate");
                    //friendly fire!

                    stats.addEntry((teamAWins + teamBWins + 1) + "," + attacker.getName() + "," + attackerTeam.getTeamName() + ",MISS,NONE,NONE");
                }

                if (arrow1.getId().equals(UUID.fromString(id))) {
                    arrow1.onArrowHit();
                } else if (arrow2.getId().equals(UUID.fromString(id))) {
                    arrow2.onArrowHit();
                }
                e.setCancelled(true);
                e.getEntity().remove();
            }
        }
    }

    /**
     * Shrinks the arena after 2 arrows have been shot
     */
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

    /**
     * Sends messages when someone is shot
     * @param attacker - person who did the shooting
     * @param attacked - person who was shot
     */
    private void sendMessages(Player attacker, Player attacked) {
        if(gameStageHandler.killsMap.containsKey(attacker)) gameStageHandler.killsMap.put(attacker, gameStageHandler.killsMap.get(attacker) + 1);
        else gameStageHandler.killsMap.put(attacker, 1);

        attacked.setGameMode(GameMode.SPECTATOR);

        attacker.playSound(attacker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
        attacked.playSound(attacked.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT,1,2);
        attacker.sendMessage("You shot " + TeamHandler.getInstance().getPlayerTeam(attacked).color + attacked.getName() + ChatColor.RESET + "!");
        attacked.sendMessage("You were shot by " + TeamHandler.getInstance().getPlayerTeam(attacker).color + attacker.getName() + ChatColor.RESET + ".");
        for(Player p: Bukkit.getOnlinePlayers()) {
            if(!p.equals(attacked) && !p.equals(attacker)) {
                p.sendMessage(TeamHandler.getInstance().getPlayerTeam(attacker).color + attacker.getName() + ChatColor.RESET + " shot " +
                        TeamHandler.getInstance().getPlayerTeam(attacked).color + attacked.getName() + ChatColor.RESET + ".");
            }
        }
    }

    private void checkedTargetForArrows(Player target) {

        //main inventory
        ItemStack[] contents = target.getInventory().getStorageContents();
        for(ItemStack item: contents) {
            if(item != null && item.getType() == Material.ARROW) {
                if(checkForMatch(item, arrow1.getId())) arrow1.resetArrowAddTime();
                else if (checkForMatch(item, arrow2.getId())) arrow2.resetArrowAddTime();
            }
        }
        //in cursor
        ItemStack cursorStack = target.getOpenInventory().getCursor();
        if(cursorStack.getType() == Material.ARROW) {
            if(checkForMatch(cursorStack, arrow1.getId())) arrow1.resetArrowAddTime();
            else if (checkForMatch(cursorStack, arrow2.getId())) arrow2.resetArrowAddTime();
        }
        //crafting grid
        Inventory topInventory = target.getOpenInventory().getTopInventory();
        if (topInventory instanceof CraftingInventory craftingInventory) {
            ItemStack[] craftingContents = craftingInventory.getMatrix();
            for (int i = 0; i < craftingContents.length; i++) {
                ItemStack item = craftingContents[i];
                if (item != null && item.getType() == Material.ARROW) {
                    if(checkForMatch(item, arrow1.getId())) arrow1.resetArrowAddTime();
                    else if (checkForMatch(item, arrow2.getId())) arrow2.resetArrowAddTime();
                }
            }
            craftingInventory.setMatrix(craftingContents);
        }
        //offhand
        ItemStack offHandItem = target.getInventory().getItemInOffHand();
        if(offHandItem.getType() == Material.ARROW) {
            if(checkForMatch(offHandItem, arrow1.getId())) arrow1.resetArrowAddTime();
            else if (checkForMatch(offHandItem, arrow2.getId())) arrow2.resetArrowAddTime();
        }
        target.getInventory().clear();
    }

    private boolean checkForMatch(ItemStack item, UUID id) {
        ItemMeta arrowMeta = item.getItemMeta();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "ID");
        String idString = arrowMeta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
        if(idString != null) {
            //this is the right arrow
            return UUID.fromString(idString).equals(id);
        }
        return false;
    }
}
