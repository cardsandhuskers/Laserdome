package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.Laserdome;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Player Attack listener, used to cancel attacks that are not the arrow shots.
 * The arrow shots themselves are handled in ArrowHitListener
 */
public class PlayerAttackListener implements Listener {

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