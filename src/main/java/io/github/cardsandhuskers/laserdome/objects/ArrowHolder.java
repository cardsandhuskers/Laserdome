package io.github.cardsandhuskers.laserdome.objects;

import io.github.cardsandhuskers.laserdome.Laserdome;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ArrowHolder implements Runnable{

    private Integer assignedTaskId;
    private Laserdome plugin;
    private int arrowTime;

    private int heldTime;

    private Team holdingTeam;
    private final Team teamA, teamB;

    private Player holdingPlayer;

    private ArrowState arrowState;

    private Location teamAArrowSpawn, teamBArrowSpawn;

    private final UUID id;

    private final String name;

    public ArrowHolder(Laserdome plugin, Team teamA, Team teamB, String name) {
        this.plugin = plugin;
        this.teamA = teamA;
        this.teamB = teamB;
        this.name = name;
        arrowTime = plugin.getConfig().getInt("ArrowTime");
        id = UUID.randomUUID();

        teamAArrowSpawn = plugin.getConfig().getLocation("TeamASpawn").clone().add(0,3,0);
        teamBArrowSpawn = plugin.getConfig().getLocation("TeamBSpawn").clone().add(0,3,0);
    }


    public void onArrowShoot() {
        arrowState = ArrowState.IN_AIR;
        holdingPlayer = null;
    }

    public void spawnArrow(Team team) {
        arrowState = ArrowState.ON_GROUND;
        heldTime = 0;
        holdingPlayer = null;
        holdingTeam = team;
        Location arrowSpawn;
        if(holdingTeam == teamA) arrowSpawn = teamAArrowSpawn;
        else arrowSpawn = teamBArrowSpawn;

        arrowSpawn.getWorld().dropItemNaturally(arrowSpawn, createArrowItem());
        arrowSpawn.getWorld().spawnParticle(Particle.CLOUD, arrowSpawn, 40);

    }

    public void resetArrow() {
        Location arrowSpawn;
        if(holdingTeam == teamA) arrowSpawn = teamAArrowSpawn;
        else arrowSpawn = teamBArrowSpawn;

        arrowSpawn.getWorld().dropItemNaturally(arrowSpawn, createArrowItem());
        arrowSpawn.getWorld().spawnParticle(Particle.CLOUD, arrowSpawn, 40);
    }

    public void onArrowDrop() {
        arrowState = ArrowState.ON_GROUND;
        holdingPlayer = null;
    }

    public void onArrowPickup(Player p) {
        arrowState = ArrowState.IN_INVENTORY;
        holdingPlayer = p;

        Team playerTeam = TeamHandler.getInstance().getPlayerTeam(p);

        if(holdingTeam != playerTeam) {
            holdingTeam = playerTeam;
            heldTime = 0;
        }
    }

    public void onArrowHit() {
        if(holdingTeam == teamA) holdingTeam = teamB;
        else if (holdingTeam == teamB) holdingTeam = teamA;
        spawnArrow(holdingTeam);
    }

    public void onRoundStart(Team arrowTeam) {
        if(arrowTeam == teamA) spawnArrow(arrowTeam);
        else if (arrowTeam == teamB) spawnArrow(arrowTeam);

        startOperation();
    }

    public void onRoundEnd() {
        cancelOperation();
    }

    /**
     * gets rid of the arrow
     */
    public void removeArrow() {
        if (arrowState == ArrowState.ON_GROUND) {
            //get the list of items on ground and find an arrow matching this one
            for(Entity e: teamAArrowSpawn.getWorld().getEntities()) {
                if(e instanceof Item i) {
                    if(i.getItemStack().getType() == Material.ARROW) {
                        ItemMeta arrowMeta = i.getItemStack().getItemMeta();
                        NamespacedKey namespacedKey = new NamespacedKey(plugin, "ID");
                        String idString = arrowMeta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
                        if(idString != null) {
                            if(UUID.fromString(idString).equals(id)) {
                                //this is the right arrow
                                i.remove();
                            }
                        }
                    }
                }
            }

        } else if (arrowState == ArrowState.IN_INVENTORY) {
            ItemStack[] contents = holdingPlayer.getInventory().getStorageContents();
            for(ItemStack item: contents) {
                if(item != null && item.getType() == Material.ARROW) {
                    ItemMeta arrowMeta = item.getItemMeta();
                    NamespacedKey namespacedKey = new NamespacedKey(plugin, "ID");
                    String idString = arrowMeta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
                    if(idString != null) {
                        if(UUID.fromString(idString).equals(id)) {
                            //this is the right arrow
                            holdingPlayer.getInventory().remove(item);
                        }
                    }
                }
            }
        }
    }

    public void run() {
        heldTime++;
        //System.out.println("Arrow:\n   Held time: " + heldTime + "\n   Holding Team: " + holdingTeam + "\n  HoldingPlayer: " + holdingPlayer + "\n  ArrowState: " + arrowState);

        if(heldTime == arrowTime / 2 || (heldTime >= arrowTime - 5 && heldTime != arrowTime)) {
            Component message = getCountdownMessage();

            for(Player p: holdingTeam.getOnlinePlayers()) {
                p.sendMessage(message);
                p.playSound(p, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1.5f);
            }
        }

        //if team has held arrow for more than max seconds
        if(heldTime >= arrowTime) {
            //if they've shot it, don't do anything, just needs to 'leave hand' on time, not land
            if(arrowState != ArrowState.IN_AIR) {
                removeArrow();
                //reset arrow to other team
                if(holdingTeam == teamA) spawnArrow(teamB);
                else if (holdingTeam == teamB) spawnArrow(teamA);

                Component message = Component.text("You held " + name + " for too long.");
                for(Player p: holdingTeam.getOnlinePlayers()) {
                    p.sendMessage(message);
                    p.playSound(p, Sound.ENTITY_WOLF_DEATH, 1, .5f);
                }
            }
        }
    }

    @NotNull
    private Component getCountdownMessage() {
        Component message = Component.text("You have ")
                .append(Component.text(arrowTime - heldTime).decorate(TextDecoration.BOLD).color(NamedTextColor.RED))
                .append(Component.text(" seconds to shoot "))
                .append(Component.text(name + ". "));
        if(holdingPlayer != null) {
            message = message.append(Component.text("\n   (held by: " + holdingPlayer.getName() + ")").color(NamedTextColor.GRAY));
        }
        else {
            message = message.append(Component.text("\n   (on ground)").color(NamedTextColor.GRAY));
        }
        return message;
    }

    /**
     * Create an arrow item with the embedded UUID
     * @return ItemStack
     */
    private ItemStack createArrowItem() {
        ItemStack arrowItem = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrowItem.getItemMeta();
        arrowMeta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "ID");
        PersistentDataContainer container = arrowMeta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.STRING, id.toString());
        arrowItem.setItemMeta(arrowMeta);

        return arrowItem;
    }

    /**
     * Transfers UUID to arrow when in entity form, works for shot arrow or dropped item
     * @param droppedItem - item to put UUID into
     */
    public void transferTagToEntity(Entity droppedItem) {
        PersistentDataContainer entityContainer = droppedItem.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "ID");
        entityContainer.set(namespacedKey, PersistentDataType.STRING, id.toString());
    }

    /**
     * Stop the repeating task
     */
    public void cancelOperation() {
        if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
    }

    /**
     * Schedules this instance to "run" every tick
     */
    public void startOperation() {
        heldTime = 0;
        // Initialize our assigned task's id, for later use so we can cancel
        this.assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0, 20L);
    }

    enum ArrowState {
        ON_GROUND,
        IN_INVENTORY,
        IN_AIR
    }

    public UUID getId() {
        return id;
    }

    public Team getHoldingTeam() {
        return holdingTeam;
    }
}
