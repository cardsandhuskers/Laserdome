package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.handlers.SpecBannerHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerClickListener implements Listener {

    SpecBannerHandler specBannerHandler;

    public PlayerClickListener(SpecBannerHandler specBannerHandler) {
        this.specBannerHandler = specBannerHandler;
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent e) {
        if(e.getItem() != null) {
            specBannerHandler.updateChoice(e.getItem().getItemMeta().getDisplayName(), e.getPlayer());
        }
    }
}
