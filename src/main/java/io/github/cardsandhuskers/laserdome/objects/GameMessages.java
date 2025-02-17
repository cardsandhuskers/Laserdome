package io.github.cardsandhuskers.laserdome.objects;

import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GameMessages {

    public static String gameDescription(String aColor, String bColor, int shootTime) {
        String GAME_DESCRIPTION =
                aColor + ChatColor.STRIKETHROUGH + "----------------------------------------\n" + ChatColor.RESET +
                ChatColor.BOLD + "          Final Game:" + "\n        The Laserdome!" +
                ChatColor.BLUE + ChatColor.BOLD + "\nHow To Play:" + ChatColor.RESET +
                "\nThere are always two arrows in play." +
                "\nThey will spawn above the black targets on the arena. " +
                "\nOn round 1, one arrow will spawn on each platform. On other rounds, both arrows spawn on the side of the team that lost." +
                "\nDuring the rounds, each time a team shoots an arrow, one will spawn for the other team." +
                "\nWhen an arrow spawns on your half, you will have " + ChatColor.GOLD + ChatColor.BOLD + shootTime + ChatColor.RESET + " seconds to shoot the arrow." +
                "\nFor every 2 arrows shot, the arena will shrink by 2 blocks. Falling off the arena counts as a death.\n" +
                bColor + ChatColor.STRIKETHROUGH + "----------------------------------------";
        return GAME_DESCRIPTION;
    }

    public static String winDescription(String aColor, String bColor) {
        String WIN_DESCRIPTION =
                aColor + ChatColor.STRIKETHROUGH + "----------------------------------------" + ChatColor.RESET +
                "\nYou win the round when all opponents have been eliminated." +
                "\nThis game will be a best of 5 rounds. The first team to 3 wins will be crowned the winner of the event." +
                "\nSpectators: show support for your favorite team by clicking on the banner of your choice!" +
                "\nGood luck and may the best team win!" +
                bColor + ChatColor.STRIKETHROUGH + "\n----------------------------------------";


        return WIN_DESCRIPTION;
    }


    public static String announceWinner(Team winner) {
        String WINNER_MESSAGE =
                winner.color + ChatColor.STRIKETHROUGH + "------------------------------\n" + ChatColor.RESET +
                winner.color + ChatColor.BOLD + winner.getTeamName() + ChatColor.RESET + " HAS WON THE MINECRAFT TOURNAMENT!" +
                ChatColor.BOLD + ChatColor.UNDERLINE + "\n\nMEMBERS:";
        for(Player p:winner.getOnlinePlayers()) {
            WINNER_MESSAGE += "\n" + winner.color + ChatColor.BOLD + p.getName();
        }
        WINNER_MESSAGE += winner.color + ChatColor.STRIKETHROUGH + "\n------------------------------";
        return WINNER_MESSAGE;
    }

}
