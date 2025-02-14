package io.github.cardsandhuskers.laserdome.handlers;

import io.github.cardsandhuskers.laserdome.Laserdome;
import io.github.cardsandhuskers.laserdome.listeners.*;
import io.github.cardsandhuskers.laserdome.objects.Countdown;
import io.github.cardsandhuskers.laserdome.objects.GameMessages;
import io.github.cardsandhuskers.teams.objects.Team;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static io.github.cardsandhuskers.laserdome.Laserdome.*;
import static io.github.cardsandhuskers.teams.Teams.handler;

public class GameStageHandler {
    private final Laserdome plugin;
    private int teamAPlayers, teamBPlayers;
    private final Team teamA;
    private final Team teamB;
    private Team lastWinner;
    private ArenaColorHandler arenaColorHandler;
    private SpecBannerHandler specBannerHandler;
    private int numShrinks;
    private boolean gameActive = false;
    public HashMap<Player, Integer> killsMap;

    public GameStageHandler(Laserdome plugin) {
        ArrayList<Team> teamList = handler.getPointsSortedList();
        killsMap = new HashMap<>();
        teamA = teamList.get(0);
        teamB = teamList.get(1);
        this.plugin = plugin;
    }

    public void startGame() {
        arenaColorHandler = new ArenaColorHandler(plugin, teamA, teamB);
        specBannerHandler = new SpecBannerHandler(teamA, teamB);

        plugin.getServer().getPluginManager().registerEvents(new PlayerAttackListener(teamA, teamB, this, plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerMoveListener(plugin, teamA, teamB, this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ArrowHitListener(plugin, teamA, teamB, arenaColorHandler, this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ArrowDestroyListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, teamA, teamB), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerLeaveListener(this, teamA, teamB), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerClickListener(specBannerHandler), plugin);

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

                        specBannerHandler.giveBanners();
                    }

                },

                //Timer End
                () -> {
                    preroundTimer();
                },

                //Each Second
                (t) -> {
                    timeVar = t.getSecondsLeft();
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 2) Bukkit.broadcastMessage(GameMessages.gameDescription(teamA.color, teamB.color));
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 12) Bukkit.broadcastMessage(GameMessages.winDescription(teamA.color, teamB.color));

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
                        if(e.getType() == EntityType.ITEM) {
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
        for(Player p:teamA.getOnlinePlayers()) p.getInventory().clear();
        for(Player p:teamB.getOnlinePlayers()) p.getInventory().clear();


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
                Firework firework1 = (Firework) l1.getWorld().spawnEntity(l1, EntityType.FIREWORK_ROCKET);
                FireworkMeta fireworkMeta = firework1.getFireworkMeta();
                fireworkMeta.addEffect(FireworkEffect.builder().withColor(winner.translateColor()).flicker(true).build());
                firework1.setFireworkMeta(fireworkMeta);

                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    firework1.detonate();
                }, 30L);


                Firework firework2 = (Firework) l2.getWorld().spawnEntity(l2, EntityType.FIREWORK_ROCKET);
                firework2.setFireworkMeta(fireworkMeta);
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    firework2.detonate();
                }, 30L);

                Firework firework3 = (Firework) l3.getWorld().spawnEntity(l3, EntityType.FIREWORK_ROCKET);
                firework3.setFireworkMeta(fireworkMeta);
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    firework3.detonate();
                }, 30L);

                Firework firework4 = (Firework) l4.getWorld().spawnEntity(l4, EntityType.FIREWORK_ROCKET);
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
                    }
                    try {
                        saveRecords();
                    } catch (IOException e) {
                        StackTraceElement[] trace = e.getStackTrace();
                        String str = "";
                        for(StackTraceElement element:trace) str += element.toString() + "\n";
                        plugin.getLogger().severe("ERROR Calculating Stats!\n" + str);
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
                        Bukkit.broadcastMessage(GameMessages.announceWinner(winner));
                    }
                    if(timeVar == t.getSecondsLeft() - 6) {
                        arenaColorHandler.rebuildFloor();
                    }
                }
        );
        gameOverTimer.scheduleTimer();
    }

    public void saveRecords() throws IOException {
        //for(Player p:killsMap.keySet()) if(p != null) System.out.println(p.getDisplayName() + ": " + killsMap.get(p));
        //System.out.println("~~~~~~~~~~~~~~~");

        FileWriter writer = new FileWriter(plugin.getDataFolder() + "/stats.csv", true);
        FileReader reader = new FileReader(plugin.getDataFolder() + "/stats.csv");

        String[] headers = {"Event", "Team", "Name", "Kills"};

        CSVFormat.Builder builder = CSVFormat.Builder.create();
        builder.setHeader(headers);
        CSVFormat format = builder.build();

        CSVParser parser = new CSVParser(reader, format);

        if(!parser.getRecords().isEmpty()) {
            format = CSVFormat.DEFAULT;
        }

        CSVPrinter printer = new CSVPrinter(writer, format);

        int eventNum;
        try {eventNum = Bukkit.getPluginManager().getPlugin("LobbyPlugin").getConfig().getInt("eventNum");} catch (Exception e) {eventNum = 1;}
        //printer.printRecord(currentGame);
        for(Player p:killsMap.keySet()) {
            if(p == null) continue;
            if(handler.getPlayerTeam(p) == null) continue;
            printer.printRecord(eventNum, handler.getPlayerTeam(p).getTeamName(), p.getDisplayName(), killsMap.get(p));
        }
        writer.close();
        try {
            plugin.statCalculator.calculateStats();
        } catch (Exception e) {
            StackTraceElement[] trace = e.getStackTrace();
            String str = "";
            for(StackTraceElement element:trace) str += element.toString() + "\n";
            plugin.getLogger().severe("ERROR Calculating Stats!\n" + str);
        }

    }

}
