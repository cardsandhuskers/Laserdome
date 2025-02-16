package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.Laserdome;
import io.github.cardsandhuskers.laserdome.objects.ArrowHolder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class ArrowShootListener implements Listener {
    Laserdome plugin;
    ArrowHolder arrow1, arrow2;

    public ArrowShootListener(Laserdome plugin, ArrowHolder arrow1, ArrowHolder arrow2) {
        this.plugin = plugin;
        this.arrow1 = arrow1;
        this.arrow2 = arrow2;
    }

    @EventHandler
    public void onArrowShoot(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player p) {

            ItemStack arrowStack = e.getConsumable();

            if(arrowStack != null) {
                System.out.println("FOUND STACK TO SHOOT");
                NamespacedKey key = new NamespacedKey(plugin, "ID");
                if (arrowStack.hasItemMeta() && arrowStack.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                    System.out.println("STACK HAS DATA CONTAINER");
                    String id = arrowStack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);

                    Arrow arrow = (Arrow) e.getProjectile();
                    arrow.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);

                    if(arrow1.getId().equals(UUID.fromString(id))) {
                        arrow1.onArrowShoot();
                    } else if (arrow2.getId().equals(UUID.fromString(id))) {
                        arrow2.onArrowShoot();
                    }
                }
            } else {
                System.out.println("Found no stack");
                e.setCancelled(true);
            }
        }
    }
}
