package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.Laserdome;
import io.github.cardsandhuskers.laserdome.objects.ArrowHolder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class PlayerThrowListener implements Listener {
    Laserdome plugin;
    ArrowHolder arrow1, arrow2;

    public PlayerThrowListener(Laserdome plugin, ArrowHolder arrow1, ArrowHolder arrow2) {
        this.plugin = plugin;
        this.arrow1 = arrow1;
        this.arrow2 = arrow2;
    }

    @EventHandler
    public void onPlayerThrow(PlayerDropItemEvent e) {
        ItemStack droppedItem = e.getItemDrop().getItemStack();

        if(droppedItem.getType() != Material.ARROW) {
            e.setCancelled(true);
        } else {
            NamespacedKey key = new NamespacedKey(plugin, "ID");
            ItemMeta meta = droppedItem.getItemMeta();
            if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

                if (arrow1.getId().equals(UUID.fromString(id))) {
                    arrow1.onArrowDrop();
                } else if (arrow2.getId().equals(UUID.fromString(id))) {
                    arrow2.onArrowDrop();
                }
            }
        }
    }
}
