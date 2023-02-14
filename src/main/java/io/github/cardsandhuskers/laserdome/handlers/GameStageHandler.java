package io.github.cardsandhuskers.laserdome.handlers;

import io.github.cardsandhuskers.laserdome.Laserdome;
import io.github.cardsandhuskers.laserdome.listeners.*;
import io.github.cardsandhuskers.laserdome.objects.Countdown;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static io.github.cardsandhuskers.laserdome.Laserdome.*;
import static io.github.cardsandhuskers.teams.Teams.handler;

public class GameStageHandler {
    private Laserdome plugin;
    private int teamAPlayers, teamBPlayers;
    private Team teamA, teamB, lastWinner;
    private ArenaColorHandler arenaColorHandler;
    private int numShrinks;
    private Countdown shrinkScheduler;
    private boolean gameActive = false;

    public GameStageHandler(Laserdome plugin) {
        ArrayList<Team> teamList = handler.getPointsSortedList();
        teamA = teamList.get(0);
        teamB = teamList.get(1);
        this.plugin = plugin;
    }

    public void startGame() {
        arenaColorHandler = new ArenaColorHandler(plugin, teamA, teamB);

        plugin.getServer().getPluginManager().registerEvents(new PlayerAttackListener(teamA, teamB, this, plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerMoveListener(plugin, teamA, teamB, this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ArrowHitListener(plugin, teamA, teamB, arenaColorHandler, this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ArrowDestroyListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, teamA, teamB), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerLeaveListener(this, teamA, teamB), plugin);

        arenaColorHandler.setColorBlocks();

        pregameTimer();
    }
    public boolean isGameActive() {
        return gameActive;
    }

    public void pregameTimer() {
        Countdown pregameTimer = new Countdown(plugin,
                plugin.getConfig().getInt("PregameTime"),
                //Timer Start
                () -> {
                    teamAWins = 0;
                    teamBWins = 0;
                    gameState = Laserdome.GameState.GAME_STARTING;
                    for(Player p: Bukkit.getOnlinePlayers()) {
                        p.teleport(plugin.getConfig().getLocation("SpectatorSpawn"));//TODO add spectatorSpawn loc
                        Inventory inv = p.getInventory();
                        inv.clear();
                        p.setGameMode(GameMode.ADVENTURE);
                        //inv.addItem(new ItemStack(Material.AIR));
                        p.setInvisible(false);
                    }
                },

                //Timer End
                () -> {
                    preroundTimer();
                },

                //Each Second
                (t) -> {
                    timeVar = t.getSecondsLeft();
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 1) {
                        Bukkit.broadcastMessage(teamA.color + ChatColor.STRIKETHROUGH + "----------------------------------------");
                        Bukkit.broadcastMessage(ChatColor.BOLD + "          Final Game:" + "\n        The Laserdome!");
                        Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "How To Play:");
                        Bukkit.broadcastMessage("There are always two arrows in play." +
                                "\nThey will spawn above the black targets on the arena. " +
                                "\nOn round 1, one arrow will spawn on each platform. On other rounds, both arrows spawn on the side of the team that lost." +
                                "\nDuring the rounds, each time a team shoots an arrow, one will spawn for the other team." +
                                "\nOver time, the arena size will shrink, forcing players inward. Falling off the arena counts as a death.");
                        Bukkit.broadcastMessage(teamB.color + ChatColor.STRIKETHROUGH + "----------------------------------------");
                    }
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 11) {
                        Bukkit.broadcastMessage(teamA.color + ChatColor.STRIKETHROUGH + "----------------------------------------");
                        Bukkit.broadcastMessage("This game will be a best of 5 rounds. First to 3 wins." +
                                "\nThe first team to do that will be crowned the winner of the event." +
                                "\nGood luck and may the best team win!");
                        Bukkit.broadcastMessage(teamB.color + ChatColor.STRIKETHROUGH + "----------------------------------------");
                    }
                }
        );
        pregameTimer.scheduleTimer();
    }

    public void resetPlayers() {
        for(Player p:teamA.getOnlinePlayers()) {
            p.teleport(plugin.getConfig().getLocation("TeamASpawn"));//TODO add teamASpawn loc
            p.setInvisible(false);

            Inventory inv = p.getInventory();
            inv.clear();

            ItemStack bow = new ItemStack(Material.BOW);
            ItemMeta bowMeta = bow.getItemMeta();
            bowMeta.setUnbreakable(true);
            bow.setItemMeta(bowMeta);
            inv.setItem(0, bow);

            p.setGameMode(GameMode.ADVENTURE);
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setSaturation(20);
        }
        for(Player p:teamB.getOnlinePlayers()) {
            p.teleport(plugin.getConfig().getLocation("TeamBSpawn"));//TODO add teamBSpawn loc
            p.setInvisible(false);

            Inventory inv = p.getInventory();
            inv.clear();
            ItemStack bow = new ItemStack(Material.BOW);

            ItemMeta bowMeta = bow.getItemMeta();
            bowMeta.setUnbreakable(true);
            bow.setItemMeta(bowMeta);

            inv.setItem(0, bow);

            p.setGameMode(GameMode.ADVENTURE);
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setSaturation(20);
        }
    }
    public void preroundTimer() {
        Countdown preroundTimer = new Countdown(plugin,
                plugin.getConfig().getInt("PreRoundTime"),
                //Timer Start
                () -> {
                    gameState = GameState.ROUND_STARTING;
                    resetPlayers();
                    Collection<Entity> entities = plugin.getConfig().getLocation("TeamASpawn").getWorld().getEntities();
                    for(Entity e:entities) {
                        if(e.getType() == EntityType.DROPPED_ITEM) {
                            e.remove();
                        }
                    }
                },

                //Timer End
                () -> {
                    timeVar = 0;
                    teamAPlayers = teamA.getOnlinePlayers().size();
                    teamBPlayers = teamB.getOnlinePlayers().size();
                    roundActive();
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.sendTitle(">GO!<", "",5,10,5);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,2);
                    }
                    if(teamAWins + teamBWins == 0) {
                        Location test = plugin.getConfig().getLocation("TeamASpawn");
                        Location l = new Location(test.getWorld(), test.getX(), test.getY() + 3, test.getZ());
                        l.getWorld().dropItemNaturally(l, new ItemStack(Material.ARROW));
                        l.getWorld().spawnParticle(Particle.CLOUD, l, 40);

                        test = plugin.getConfig().getLocation("TeamBSpawn");
                        l = new Location(test.getWorld(), test.getX(), test.getY() + 3, test.getZ());
                        l.getWorld().dropItemNaturally(l, new ItemStack(Material.ARROW));
                        l.getWorld().spawnParticle(Particle.CLOUD, l, 40);

                    } else if(lastWinner.equals(teamA)) {
                        Location test = plugin.getConfig().getLocation("TeamBSpawn");
                        Location l = new Location(test.getWorld(), test.getX(), test.getY() + 3, test.getZ());
                        l.getWorld().dropItemNaturally(l, new ItemStack(Material.ARROW));
                        l.getWorld().dropItemNaturally(l, new ItemStack(Material.ARROW));
                        l.getWorld().spawnParticle(Particle.CLOUD, l, 40);
                    } else {
                        Location test = plugin.getConfig().getLocation("TeamASpawn");
                        Location l = new Location(test.getWorld(), test.getX(), test.getY() + 3, test.getZ());
                        l.getWorld().dropItemNaturally(l, new ItemStack(Material.ARROW));
                        l.getWorld().dropItemNaturally(l, new ItemStack(Material.ARROW));
                        l.getWorld().spawnParticle(Particle.CLOUD, l, 40);
                    }
                },

                //Each Second
                (t) -> {
                    timeVar = t.getSecondsLeft();
                    if(t.getSecondsLeft() <= 4) {
                        for(Player p:teamA.getOnlinePlayers()) {
                            p.sendTitle(">" + t.getSecondsLeft() + "<", "",5,10,5);
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,1);
                        }
                        for(Player p:teamB.getOnlinePlayers()) {
                            p.sendTitle(">" + t.getSecondsLeft() + "<", "",5,10,5);
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,1);
                        }
                        Bukkit.broadcastMessage(ChatColor.GREEN + "Round " + (teamAWins + teamBWins + 1) + " starts in " + ChatColor.YELLOW + t.getSecondsLeft() + ChatColor.GREEN + " seconds!");
                    }
                    if(t.getSecondsLeft() == 2) {
                        arenaColorHandler.rebuildFloor();
                    }
                }
        );
        preroundTimer.scheduleTimer();
    }

    public void roundActive() {
        gameState = Laserdome.GameState.ROUND_ACTIVE;
        numShrinks = 0;
        gameActive = true;

        shrinkScheduler = new Countdown(plugin,
                60,
                //Timer Start
                () -> {
                    //arrowCountdownHandler.startOperation();
                },

                //Timer End
                () -> {
                },

                //Each Second
                (t) -> {
                    //System.out.println(t.getSecondsLeft());
                    if((t.getSecondsLeft() % 15 == 0 && t.getSecondsLeft() != t.getTotalSeconds()) || t.getSecondsLeft() == 1) {


/*
                        final int shrinks = numShrinks;
                        for(int i = 1; i <=6; i++) {
                            int finalI = i;
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                                if(finalI %2 == 0) {
                                    arenaColorHandler.shrinkArena(Material.RED_STAINED_GLASS, shrinks);
                                } else {
                                    arenaColorHandler.shrinkArena(Material.YELLOW_STAINED_GLASS, shrinks);
                                    for(Player p: Bukkit.getOnlinePlayers()) {
                                        p.playSound(p.getLocation(), Sound.ENTITY_IRON_GOLEM_DAMAGE, 1, .5F);
                                    }
                                }
                            },10L * i);
                        }
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                            for(Player p: Bukkit.getOnlinePlayers()) {
                                p.playSound(p.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1, .5F);
                            }
                            arenaColorHandler.shrinkArena(Material.AIR, shrinks);
                        },70L);
                        numShrinks++;
                        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Arena is about to shrink. Watch out!");

*/



                    }




                }
        );
        shrinkScheduler.scheduleTimer();
    }

    public void postroundTimer() {
        Countdown postroundTimer = new Countdown(plugin,
                plugin.getConfig().getInt("PostRoundTime"),
                //Timer Start
                () -> {
                    gameState = GameState.ROUND_OVER;
                },

                //Timer End
                () -> {
                    preroundTimer();
                    arenaColorHandler.rebuildFloor();

                },

                //Each Second
                (t) -> {
                    timeVar = t.getSecondsLeft();
                }
        );
        postroundTimer.scheduleTimer();
    }

    public void roundOver() {
        gameActive = false;
        if(shrinkScheduler != null) {
            shrinkScheduler.cancelTimer();
        }
        //arrowCountdownHandler.cancelOperation();
        arenaColorHandler.numShrinks = 0;
        arenaColorHandler.numShots = 0;
        gameState = GameState.ROUND_OVER;
        if(teamAWins == 3) {
            gameOver(teamA);
        } else if(teamBWins == 3) {
            gameOver(teamB);
        } else {
            postroundTimer();
        }
    }

    /**
     *
     * @param shotTeam
     */
    public void onValidShot(Team shotTeam) {
        if(shotTeam.equals(teamA)) {
            teamAPlayers--;
            if(teamAPlayers == 0) {
                lastWinner = teamB;
                teamBWins++;
                roundOver();

                for(Player p:Bukkit.getOnlinePlayers()) {
                    p.sendTitle(teamB.color + "Round Over", teamB.color + teamB.getTeamName() + " Wins!",5,10,5);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,2);
                }
                Bukkit.broadcastMessage(teamB.color + teamB.getTeamName() + ChatColor.RESET + " Won Round " + ChatColor.YELLOW + (teamAWins + teamBWins));
            }
        }
        if(shotTeam.equals(teamB)) {
            teamBPlayers--;
            if(teamBPlayers == 0) {
                lastWinner = teamA;
                teamAWins++;
                roundOver();

                for(Player p:Bukkit.getOnlinePlayers()) {
                    p.sendTitle(teamA.color + "Round Over", teamA.color + teamA.getTeamName() + " Wins!",5,10,5);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,2);
                }
                Bukkit.broadcastMessage(teamA.color + teamA.getTeamName() + ChatColor.RESET + " Won Round " + ChatColor.YELLOW + (teamAWins + teamBWins));
            }
        }
    }
    public void gameOver(Team winner) {

        //FIREWORKS
        for(int i = 0; i < 10; i++) {
            Location spawnA = plugin.getConfig().getLocation("TeamASpawn");
            Location spawnB = plugin.getConfig().getLocation("TeamBSpawn");
            int centerX = (spawnA.getBlockX() + spawnB.getBlockX()) / 2;
            int centerZ = (spawnA.getBlockZ() + spawnB.getBlockZ()) / 2;
            int y = spawnA.getBlockY();


            Location l1 = new Location(spawnA.getWorld(), centerX + 10, y, centerZ + 10);
            Location l2 = new Location(spawnA.getWorld(), centerX + 10, y, centerZ - 10);
            Location l3 = new Location(spawnA.getWorld(), centerX -10, y, centerZ + 10);
            Location l4 = new Location(spawnA.getWorld(), centerX - 10, y, centerZ - 10);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                Firework firework1 = (Firework) l1.getWorld().spawnEntity(l1, EntityType.FIREWORK);
                FireworkMeta fireworkMeta = firework1.getFireworkMeta();
                fireworkMeta.addEffect(FireworkEffect.builder().withColor(winner.translateColor()).flicker(true).build());
                firework1.setFireworkMeta(fireworkMeta);

                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    firework1.detonate();
                }, 30L);


                Firework firework2 = (Firework) l2.getWorld().spawnEntity(l2, EntityType.FIREWORK);
                firework2.setFireworkMeta(fireworkMeta);
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    firework2.detonate();
                }, 30L);

                Firework firework3 = (Firework) l3.getWorld().spawnEntity(l3, EntityType.FIREWORK);
                firework3.setFireworkMeta(fireworkMeta);
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    firework3.detonate();
                }, 30L);

                Firework firework4 = (Firework) l4.getWorld().spawnEntity(l4, EntityType.FIREWORK);
                firework4.setFireworkMeta(fireworkMeta);
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    firework4.detonate();
                }, 30L);

            }, 20L * i);
        }//end fireworks

        Countdown gameOverTimer = new Countdown(plugin,
                plugin.getConfig().getInt("GameOverTime"),
                //Timer Start
                () -> {

                },

                //Timer End
                () -> {
                    HandlerList.unregisterAll(plugin);
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.teleport(plugin.getConfig().getLocation("Lobby"));
                        //Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                        //    p.setGameMode(GameMode.ADVENTURE);
                        //},2);
                    }
                },

                //Each Second
                (t) -> {
                    timeVar = t.getSecondsLeft();
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 1) {
                        for(Player p:Bukkit.getOnlinePlayers()) {
                            p.sendTitle(winner.color + winner.getTeamName(), "HAS WON THE EVENT!",10,60,10);
                            p.playSound(p.getLocation(), Sound.MUSIC_DISC_OTHERSIDE, 1, 1);

                        }
                        Bukkit.broadcastMessage(winner.color + ChatColor.STRIKETHROUGH + "------------------------------");
                        Bukkit.broadcastMessage(winner.color + ChatColor.BOLD + winner.getTeamName() + ChatColor.RESET + " HAS WON THE MINECRAFT TOURNAMENT!");
                        Bukkit.broadcastMessage(ChatColor.BOLD + "MEMBERS:");
                        for(Player p:winner.getOnlinePlayers()) {
                            Bukkit.broadcastMessage(winner.color + ChatColor.BOLD + p.getName());
                        }
                        Bukkit.broadcastMessage(winner.color + ChatColor.STRIKETHROUGH + "------------------------------");
                    }
                    if(timeVar == t.getSecondsLeft() - 6) {
                        arenaColorHandler.rebuildFloor();
                    }
                }
        );
        gameOverTimer.scheduleTimer();
    }
}
