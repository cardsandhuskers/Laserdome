package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.Laserdome;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class ArrowDestroyListener implements Listener {
    private Laserdome plugin;
    private Location teamASpawn, teamBSpawn;
    private char centerLineAxis;

    public ArrowDestroyListener(Laserdome plugin) {
        this.plugin = plugin;

        teamASpawn = plugin.getConfig().getLocation("TeamASpawn");
        teamBSpawn = plugin.getConfig().getLocation("TeamBSpawn");
        int xDiff = Math.abs(teamASpawn.getBlockX() - teamBSpawn.getBlockX());
        int zDiff = Math.abs(teamASpawn.getBlockZ() - teamBSpawn.getBlockZ());
        if(xDiff > zDiff) {
            centerLineAxis = 'z';
        } else {
            centerLineAxis = 'x';
        }

    }
    @EventHandler
    public void onArrowFall(EntityDamageEvent e) {
        if(e.getEntity().getType() != EntityType.DROPPED_ITEM) {
            return;
        }
        Item item = (Item) e.getEntity();
        if(item.getItemStack().getType() != Material.ARROW) {
            return;
        }

        Location l = e.getEntity().getLocation();
        Location sideOfArrow;
        if(centerLineAxis == 'x') {
            int centerLine = (teamASpawn.getBlockZ() + teamBSpawn.getBlockZ()) / 2;

            //compare current location to center line based on side
            if (teamASpawn.getZ() < centerLine) {
                if (l.getZ() < centerLine) {
                    sideOfArrow = teamASpawn;
                } else {
                    sideOfArrow = teamBSpawn;
                }
            } else {
                if (l.getZ() < centerLine) {
                    sideOfArrow = teamBSpawn;
                } else {
                    sideOfArrow = teamASpawn;
                }
            }
        } else {
            int centerLine = (teamASpawn.getBlockX() + teamBSpawn.getBlockX()) / 2;

            //compare current location to center line based on side
            if (teamASpawn.getX() < centerLine) {
                if (l.getX() < centerLine) {
                    sideOfArrow = teamASpawn;
                } else {
                    sideOfArrow = teamBSpawn;
                }
            } else {
                if (l.getX() < centerLine) {
                    sideOfArrow = teamBSpawn;
                } else {
                    sideOfArrow = teamASpawn;
                }
            }
        }
        System.out.println("DROPPING ARROW");
        e.getEntity().remove();
        Location dropLoc = new Location(sideOfArrow.getWorld(), sideOfArrow.getX(),sideOfArrow.getY() + 3, sideOfArrow.getZ());
        dropLoc.getWorld().dropItemNaturally(dropLoc, new ItemStack(Material.ARROW));
        dropLoc.getWorld().spawnParticle(Particle.CLOUD, dropLoc, 40);

    }
}
