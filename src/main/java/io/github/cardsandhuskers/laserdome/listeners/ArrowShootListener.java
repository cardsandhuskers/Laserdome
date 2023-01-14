package io.github.cardsandhuskers.laserdome.listeners;

import io.github.cardsandhuskers.laserdome.Laserdome;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

import static io.github.cardsandhuskers.teams.Teams.handler;

public class ArrowShootListener implements Listener {
    Laserdome plugin;
    Team teamA, teamB;
    public ArrowShootListener(Laserdome plugin, Team teamA, Team teamB) {
        this.plugin = plugin;
        this.teamA = teamA;
        this.teamB = teamB;
    }
    @EventHandler
    public void onArrowHit(ProjectileHitEvent e) {
        if(e.getEntity().getType() != EntityType.ARROW) {
            return;
        }
        if(e.getEntity().getShooter() instanceof Player p) {
            Team team = handler.getPlayerTeam(p);
            if(team == null) {
                return;
            }
            Location arrowSpawn;
            Location temp;
            if(team.equals(teamA)) {
                temp = plugin.getConfig().getLocation("TeamBSpawn");
            } else if(team.equals(teamB)) {
                temp = plugin.getConfig().getLocation("TeamASpawn");
            } else {
                return;
            }
            arrowSpawn = new Location(temp.getWorld(), temp.getX(), temp.getY() + 3, temp.getZ());
            arrowSpawn.getWorld().dropItemNaturally(arrowSpawn, new ItemStack(Material.ARROW));
            arrowSpawn.getWorld().spawnParticle(Particle.CLOUD, arrowSpawn, 40);
            e.getEntity().remove();
        }
    }
}
