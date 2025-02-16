package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.Laserdome;
import io.github.cardsandhuskers.laserdome.objects.ArrowHolder;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class ArrowDestroyListener implements Listener {
    private final Laserdome plugin;
    ArrowHolder arrow1, arrow2;

    public ArrowDestroyListener(Laserdome plugin, ArrowHolder arrow1, ArrowHolder arrow2) {
        this.plugin = plugin;
        this.arrow1 = arrow1;
        this.arrow2 = arrow2;

    }
    @EventHandler
    public void onArrowFall(EntityDamageEvent e) {
        if(e.getEntity().getType() != EntityType.ITEM) {
            return;
        }
        Item item = (Item) e.getEntity();
        if(item.getItemStack().getType() != Material.ARROW) {
            return;
        }

        System.out.println("DROPPING ARROW");
        e.getEntity().remove();

        ItemStack arrow = ((Item) e.getEntity()).getItemStack();
        NamespacedKey key = new NamespacedKey(plugin, "ID");
        ItemMeta meta = arrow.getItemMeta();
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

            if (arrow1.getId().equals(UUID.fromString(id))) {
                arrow1.resetArrow();
            } else if (arrow2.getId().equals(UUID.fromString(id))) {
                arrow2.resetArrow();
            }
        }
    }
}
