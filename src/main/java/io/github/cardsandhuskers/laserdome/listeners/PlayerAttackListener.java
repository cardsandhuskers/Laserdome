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

    public PlayerAttackListener() {

    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        if(Laserdome.gameState != Laserdome.GameState.ROUND_ACTIVE) {
            e.setCancelled(true);
            return;
        }
        if(e.getEntity() instanceof Player && e.getDamager() instanceof Arrow) {
            return;
        }
        e.setCancelled(true);
    }
}