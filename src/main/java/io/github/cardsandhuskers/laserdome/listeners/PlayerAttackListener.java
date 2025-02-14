package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.Laserdome;
import io.github.cardsandhuskers.laserdome.handlers.GameStageHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import static io.github.cardsandhuskers.teams.Teams.handler;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class PlayerAttackListener implements Listener {
    private final Team teamA;
    private final Team teamB;
    private final GameStageHandler gameStageHandler;
    private final Laserdome plugin;

    public PlayerAttackListener(Team teamA, Team teamB, GameStageHandler gameStageHandler, Laserdome plugin) {
        this.plugin = plugin;
        this.teamA = teamA;
        this.teamB = teamB;
        this.gameStageHandler = gameStageHandler;
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        if(Laserdome.gameState != Laserdome.GameState.ROUND_ACTIVE) {
            e.setCancelled(true);
            return;
        }
        if(e.getEntity() instanceof Player attacked && e.getDamager() instanceof Arrow arrow) {
            Player attacker = (Player) arrow.getShooter();
            Team attackedTeam = handler.getPlayerTeam(attacked);
            Team attackerTeam = handler.getPlayerTeam(attacker);
            if(attackerTeam != null && attackedTeam != null) {
                if(attackerTeam == teamA && attackedTeam == teamB) {
                    //valid shot has hit target
                    gameStageHandler.onValidShot(teamB);
                    sendMessages(attacker, attacked, teamB);
                }
                if(attackerTeam == teamB && attackedTeam == teamA) {
                    //valid shot has hit target
                    gameStageHandler.onValidShot(teamA);
                    sendMessages(attacker, attacked, teamA);


                }
            }
        }
        e.setCancelled(true);
    }
    public void sendMessages(Player attacker, Player attacked, Team attackedTeam) {
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