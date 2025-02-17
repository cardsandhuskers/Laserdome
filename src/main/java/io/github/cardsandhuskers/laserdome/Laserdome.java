package io.github.cardsandhuskers.laserdome;

import io.github.cardsandhuskers.laserdome.commands.*;
import io.github.cardsandhuskers.laserdome.objects.Placeholder;
import io.github.cardsandhuskers.laserdome.objects.StatCalculator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Laserdome extends JavaPlugin {
    public static int teamAWins = 0, teamBWins = 0, timeVar = 0;
    public static GameState gameState = GameState.GAME_STARTING;
    public StatCalculator statCalculator;
    @Override
    public void onEnable() {
        // Plugin startup logic
        //Placeholder API validation
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            /*
             * We register the EventListener here, when PlaceholderAPI is installed.
             * Since all events are in the main class (this class), we simply use "this"
             */
            new Placeholder(this).register();

        } else {
            /*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             */
            System.out.println("Could not find PlaceholderAPI!");
        }

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        getCommand("startLaserdome").setExecutor(new StartGameCommand(this));
        getCommand("setLaserdomeLobby").setExecutor(new SetLobbyCommand(this));
        getCommand("setLaserdomeTeamSpawn").setExecutor(new SetTeamSpawnCommand(this));
        getCommand("setLaserdomeSpecSpawn").setExecutor(new SetSpectatorSpawnCommand(this));
        getCommand("setLaserdomePos1").setExecutor(new SetPos1Command(this));
        getCommand("setLaserdomePos2").setExecutor(new SetPos2Command(this));
        getCommand("setLaserdomeArenaLoc1").setExecutor(new SetArenaCorner1Command(this));
        getCommand("setLaserdomeArenaLoc2").setExecutor(new SetArenaCorner2Command(this));

        statCalculator = new StatCalculator(this);
        try {
            statCalculator.calculateStats();
        } catch (Exception e) {
            StackTraceElement[] trace = e.getStackTrace();
            String str = "";
            for(StackTraceElement element:trace) str += element.toString() + "\n";
            this.getLogger().severe("ERROR Calculating Stats!\n" + str);
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public enum GameState {
        GAME_STARTING,
        ROUND_STARTING,
        ROUND_ACTIVE,
        ROUND_OVER,
        GAME_OVER
    }
}
