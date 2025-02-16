package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.Laserdome;
import io.github.cardsandhuskers.laserdome.objects.ArrowHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class PlayerPickUpItemListener implements Listener {
    Laserdome plugin;
    ArrowHolder arrow1, arrow2;

    public PlayerPickUpItemListener(Laserdome plugin, ArrowHolder arrow1, ArrowHolder arrow2) {
        this.plugin = plugin;
        this.arrow1 = arrow1;
        this.arrow2 = arrow2;
    }

    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent e) {
        if(e.getEntity() instanceof Player p){
            ItemStack stack = e.getItem().getItemStack();
            NamespacedKey key = new NamespacedKey(plugin, "ID");
            ItemMeta meta = stack.getItemMeta();
            if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

                if (arrow1.getId().equals(UUID.fromString(id))) {
                    arrow1.onArrowPickup(p);
                } else if (arrow2.getId().equals(UUID.fromString(id))) {
                    arrow2.onArrowPickup(p);
                }
                return;
            }

        }
        e.setCancelled(true);
    }
}
