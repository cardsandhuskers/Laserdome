package io.github.cardsandhuskers.laserdome.handlers;

import io.github.cardsandhuskers.laserdome.Laserdome;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ArenaColorHandler {
    private String higherWholex, lowerWholex, higherWholey, lowerWholey, higherWholez, lowerWholez;
    String higherx, lowerx, highery, lowery, higherz, lowerz;
    private Laserdome plugin;
    private Team teamA, teamB;
    public int numShots = 0, numShrinks = 0;

    public ArenaColorHandler(Laserdome plugin, Team teamA, Team teamB) {
        this.plugin = plugin;
        this.teamA = teamA;
        this.teamB = teamB;
        getPositions();
    }

    private void getPositions() {
        if (getCoordinate("pos1", 'x') > getCoordinate("pos2", 'x')) {
            higherWholex = "pos1";
            lowerWholex = "pos2";
        } else {
            higherWholex = "pos2";
            lowerWholex = "pos1";
        }
        if (getCoordinate("pos1", 'y') > getCoordinate("pos2", 'y')) {
            higherWholey = "pos1";
            lowerWholey = "pos2";
        } else {
            higherWholey = "pos2";
            lowerWholey = "pos1";
        }
        if (getCoordinate("pos1", 'z') > getCoordinate("pos2", 'z')) {
            higherWholez = "pos1";
            lowerWholez = "pos2";
        } else {
            higherWholez = "pos2";
            lowerWholez = "pos1";
        }
    }

    public void setColorBlocks() {
        World world = plugin.getConfig().getLocation("pos1").getWorld();

        Location teamASpawn = plugin.getConfig().getLocation("TeamASpawn");
        Location teamBSpawn = plugin.getConfig().getLocation("TeamBSpawn");
        char centerLineAxis;
        int xDiff = Math.abs(teamASpawn.getBlockX() - teamBSpawn.getBlockX());
        int zDiff = Math.abs(teamASpawn.getBlockZ() - teamBSpawn.getBlockZ());
        if(xDiff > zDiff) {
            centerLineAxis = 'z';
        } else {
            centerLineAxis = 'x';
        }


        if(centerLineAxis == 'x') {
            int centerLine = (teamASpawn.getBlockZ() + teamBSpawn.getBlockZ())/2;
            Team firstHalfTeam;
            Team secondHalfTeam;
            if(teamASpawn.getZ() < teamBSpawn.getZ()) {
                firstHalfTeam = teamA;
                secondHalfTeam = teamB;
            } else {
                firstHalfTeam = teamB;
                secondHalfTeam = teamA;
            }

            for(int x = getCoordinate(lowerWholex, 'x'); x <= getCoordinate(higherWholex, 'x'); x++) {
                for(int y = getCoordinate(lowerWholey, 'y'); y <= getCoordinate(higherWholey, 'y'); y++) {
                    for(int z = getCoordinate(lowerWholez, 'z'); z < centerLine; z++) {
                        Location l = new Location(world, x, y, z);
                        Material mat = l.getBlock().getType();
                        if(isColoredConcrete(mat)) {
                            l.getBlock().setType(getConcrete(firstHalfTeam.color));
                        }
                        if(isColoredCarpet(mat)) {
                            l.getBlock().setType(getCarpet(firstHalfTeam.color));
                        }
                    }
                    for(int z = centerLine + 1; z <= getCoordinate(higherWholez, 'z'); z++) {
                        Location l = new Location(world, x, y, z);
                        Material mat = l.getBlock().getType();
                        if(isColoredConcrete(mat)) {
                            l.getBlock().setType(getConcrete(secondHalfTeam.color));
                        }
                        if(isColoredCarpet(mat)) {
                            l.getBlock().setType(getCarpet(secondHalfTeam.color));
                        }
                    }
                }
            }
        }

        if(centerLineAxis == 'z') {
            int centerLine = (teamASpawn.getBlockX() + teamBSpawn.getBlockX())/2;
            Team firstHalfTeam;
            Team secondHalfTeam;
            if(teamASpawn.getX() < teamBSpawn.getX()) {
                firstHalfTeam = teamA;
                secondHalfTeam = teamB;
            } else {
                firstHalfTeam = teamB;
                secondHalfTeam = teamA;
            }

            for(int z = getCoordinate(lowerWholez, 'z'); z <= getCoordinate(higherWholez, 'z'); z++) {
                for(int y = getCoordinate(lowerWholey, 'y'); y <= getCoordinate(higherWholey, 'y'); y++) {
                    for(int x = getCoordinate(lowerWholex, 'x'); x < centerLine; x++) {
                        Location l = new Location(world, x, y, z);
                        Material mat = l.getBlock().getType();
                        if(isColoredConcrete(mat)) {
                            l.getBlock().setType(getConcrete(firstHalfTeam.color));
                        }
                        if(isColoredCarpet(mat)) {
                            l.getBlock().setType(getCarpet(firstHalfTeam.color));
                        }
                    }
                    for(int x = centerLine + 1; x <= getCoordinate(higherWholex, 'x'); x++) {
                        Location l = new Location(world, x, y, z);
                        Material mat = l.getBlock().getType();
                        if(isColoredConcrete(mat)) {
                            l.getBlock().setType(getConcrete(secondHalfTeam.color));
                        }
                        if(isColoredCarpet(mat)) {
                            l.getBlock().setType(getCarpet(secondHalfTeam.color));
                        }
                    }
                }
            }
        }
        rebuildFloor();
    }

    public void rebuildFloor() {
        if (getCoordinate("arenaCorner1", 'x') > getCoordinate("arenaCorner2", 'x')) {
            higherx = "arenaCorner1";
            lowerx = "arenaCorner2";
        } else {
            higherx = "arenaCorner2";
            lowerx = "arenaCorner1";
        }
        if (getCoordinate("arenaCorner1", 'y') > getCoordinate("arenaCorner2", 'y')) {
            highery = "arenaCorner1";
            lowery = "arenaCorner2";
        } else {
            highery = "arenaCorner2";
            lowery = "arenaCorner1";
        }
        if (getCoordinate("arenaCorner1", 'z') > getCoordinate("arenaCorner2", 'z')) {
            higherz = "arenaCorner1";
            lowerz = "arenaCorner2";
        } else {
            higherz = "arenaCorner2";
            lowerz = "arenaCorner1";
        }
        int offset = plugin.getConfig().getInt("SpareArenaOffset");
        World world = plugin.getConfig().getLocation("arenaCorner1").getWorld();
        for(int x = getCoordinate(lowerx, 'x'); x <= getCoordinate(higherx, 'x'); x++) {
            for(int y = getCoordinate(lowery, 'y'); y <= getCoordinate(highery, 'y'); y++) {
                for(int z = getCoordinate(lowerz, 'z'); z <= getCoordinate(higherz, 'z'); z++) {
                    Location pasteLoc = new Location(world, x,y,z);
                    Location copyLoc = new Location(world,x,y - offset,z);
                    pasteLoc.getBlock().setType(copyLoc.getBlock().getType());
                }
            }
        }

    }
    public void shrinkArena(Material mat, int numShrinks) {
        int offset;
        int initx = getCoordinate(lowerx, 'x');
        int initz = getCoordinate(lowerz, 'z');
        int endx = getCoordinate(higherx, 'x');
        int endz = getCoordinate(higherz, 'z');

        offset = (numShrinks+1) * 2;
        initx = initx + (numShrinks) * 2;
        initz = initz + (numShrinks) * 2;

        endx = endx - (numShrinks) * 2;
        endz = endz - (numShrinks) * 2;

        for(int x = initx; x <= endx; x++) {
            for(int y = getCoordinate(highery, 'y'); y >= getCoordinate(lowery, 'y'); y--) {
                for(int z = initz; z <= endz; z++) {
                    if(x < getCoordinate(lowerx, 'x') + offset || x > getCoordinate(higherx, 'x') - offset ||
                       z < getCoordinate(lowerz, 'z') + offset || z > getCoordinate(higherz, 'z') - offset) {
                        Location l = new Location(plugin.getConfig().getLocation("pos1").getWorld(),x,y,z);
                        if(y == getCoordinate(highery, 'y')) l.getBlock().setType(Material.AIR);
                        else l.getBlock().setType(mat);
                    }
                }
            }
        }
    }

    private Material getConcrete(String color) {
        switch (color) {
            case "§2": return Material.GREEN_CONCRETE;
            case "§3": return Material.CYAN_CONCRETE;
            case "§5": return Material.PURPLE_CONCRETE;
            case "§6": return Material.ORANGE_CONCRETE;
            case "§7": return Material.LIGHT_GRAY_CONCRETE;
            case "§8": return Material.BLACK_CONCRETE;
            case "§9": return Material.BLUE_CONCRETE;
            case "§a": return Material.LIME_CONCRETE;
            case "§b": return Material.LIGHT_BLUE_CONCRETE;
            case "§c": return Material.RED_CONCRETE;
            case "§d": return Material.PINK_CONCRETE;
            case "§e": return Material.YELLOW_CONCRETE;
            default: return Material.WHITE_CONCRETE;
        }
    }

    private Material getCarpet(String color) {
        switch (color) {
            case "§2": return Material.GREEN_CARPET;
            case "§3": return Material.CYAN_CARPET;
            case "§5": return Material.PURPLE_CARPET;
            case "§6": return Material.ORANGE_CARPET;
            case "§7": return Material.LIGHT_GRAY_CARPET; //light gray
            case "§8": return Material.GRAY_CARPET;
            case "§9": return Material.BLUE_CARPET;
            case "§a": return Material.LIME_CARPET;
            case "§b": return Material.LIGHT_BLUE_CARPET;
            case "§c": return Material.RED_CARPET;
            case "§d": return Material.MAGENTA_CARPET; //magenta
            case "§e": return Material.YELLOW_CARPET;
            default: return Material.WHITE_CARPET;
        }
    }


    private boolean isColoredConcrete(Material mat) {
        switch(mat) {
            case RED_CONCRETE:
            case GREEN_CONCRETE:
            case CYAN_CONCRETE:
            case BLUE_CONCRETE:
            case BLACK_CONCRETE:
            case LIME_CONCRETE:
            case LIGHT_GRAY_CONCRETE:
            case MAGENTA_CONCRETE:
            case ORANGE_CONCRETE:
            case YELLOW_CONCRETE:
            case LIGHT_BLUE_CONCRETE:
            case PURPLE_CONCRETE:
                return true;
            default:
                return false;
        }
    }

    private boolean isColoredCarpet(Material mat) {
        switch(mat) {
            case RED_CARPET:
            case GREEN_CARPET:
            case CYAN_CARPET:
            case BLUE_CARPET:
            case GRAY_CARPET:
            case LIME_CARPET:
            case LIGHT_GRAY_CARPET:
            case MAGENTA_CARPET:
            case ORANGE_CARPET:
            case YELLOW_CARPET:
            case LIGHT_BLUE_CARPET:
            case PURPLE_CARPET:
                return true;
            default:
                return false;
        }
    }

    private int getCoordinate(String pos, char axis) {
        Location l = plugin.getConfig().getLocation(pos);
        switch(axis) {
            case 'x': return l.getBlockX();
            case 'y': return l.getBlockY();
            case 'z': return l.getBlockZ();
            default: return 0;
        }
    }

}
