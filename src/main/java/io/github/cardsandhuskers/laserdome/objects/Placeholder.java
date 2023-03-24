package io.github.cardsandhuskers.laserdome.objects;


import io.github.cardsandhuskers.laserdome.Laserdome;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;

import static io.github.cardsandhuskers.laserdome.Laserdome.*;
import static io.github.cardsandhuskers.teams.Teams.handler;


public class Placeholder extends PlaceholderExpansion {
    private final Laserdome plugin;

    public Placeholder(Laserdome plugin) {
        this.plugin = plugin;
    }


    @Override
    public String getIdentifier() {
        return "Laserdome";
    }
    @Override
    public String getAuthor() {
        return "cardsandhuskers";
    }
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    @Override
    public boolean persist() {
        return true;
    }


    @Override
    public String onRequest(OfflinePlayer p, String s) {
        if(s.equalsIgnoreCase("teamAWins")) {
            String str = "___";
            for(int i = 0; i < teamAWins; i++) {
                str = str.substring(0, i) + '█' + str.substring(i + 1);
            }
            return str;
        }
        if(s.equalsIgnoreCase("teamBWins")) {
            String str = "___";
            for(int i = 0; i < teamBWins; i++) {
                str = str.substring(0, i) + '█' + str.substring(i + 1);
            }
            return str;
        }
        if(s.equalsIgnoreCase("round")) {
            return teamAWins + teamBWins + 1 + "";
        }
        if (s.equalsIgnoreCase("timer")) {
            if(gameState == GameState.ROUND_ACTIVE) return "";
            int time = timeVar;
            if(time == 0) return "";
            int mins = time / 60;
            String seconds = String.format("%02d", time - (mins * 60));
            return mins + ":" + seconds;
        }
        if(s.equalsIgnoreCase("timerStage")) {
            switch (gameState) {
                case GAME_STARTING: return "Game Starts";
                case ROUND_ACTIVE: return "Round Active";
                case ROUND_STARTING: return "Round Starts";
                case ROUND_OVER: return "Round Over";
                case GAME_OVER: return "Return to Lobby";
            }
        }

        String[] values = s.split("_");
        //playerKills, totalKills, wins
        // lb pos
        //playerKills_1
        try {
            if (values[0].equalsIgnoreCase("playerKills")) {
                ArrayList<StatCalculator.SingleGameKillsHolder> killsHolders = plugin.statCalculator.getSGKillsHolders();
                //System.out.println(killsHolders);
                if(Integer.parseInt(values[1]) > killsHolders.size()) return  "";
                StatCalculator.SingleGameKillsHolder holder = killsHolders.get(Integer.parseInt(values[1]) - 1);

                String color = "";
                if (handler.getPlayerTeam(Bukkit.getPlayer(holder.name)) != null)
                    color = handler.getPlayerTeam(Bukkit.getPlayer(holder.name)).color;
                return color + holder.name + ChatColor.RESET + " Event " + holder.eventNum + ": " + holder.kills;


            }
            if (values[0].equalsIgnoreCase("totalKills")) {
                ArrayList<StatCalculator.PlayerStatsHolder> killsHolders = plugin.statCalculator.getStatsHolders();
                //System.out.println(killsHolders);
                if(Integer.parseInt(values[1]) > killsHolders.size()) return  "";
                StatCalculator.PlayerStatsHolder holder = killsHolders.get(Integer.parseInt(values[1]) - 1);
                String color = "";
                if (handler.getPlayerTeam(Bukkit.getPlayer(holder.name)) != null)
                    color = handler.getPlayerTeam(Bukkit.getPlayer(holder.name)).color;
                return color + holder.name + ChatColor.RESET + ": " + holder.kills;
            }
        } catch (Exception e) {
            StackTraceElement[] trace = e.getStackTrace();
            String str = "";
            for(StackTraceElement element:trace) str += element.toString() + "\n";
            plugin.getLogger().severe("Error with Placeholder!\n" + str);
        }



        return null;
    }
}
