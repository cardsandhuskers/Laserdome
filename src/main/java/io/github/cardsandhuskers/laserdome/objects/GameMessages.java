package io.github.cardsandhuskers.laserdome.objects;

import io.github.cardsandhuskers.teams.objects.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static io.github.cardsandhuskers.laserdome.Laserdome.teamAWins;
import static io.github.cardsandhuskers.laserdome.Laserdome.teamBWins;

public class GameMessages {

    private static final Map<String, NamedTextColor> COLOR_MAP = new HashMap<>();

    static {
        COLOR_MAP.put("&0", NamedTextColor.BLACK);
        COLOR_MAP.put("&1", NamedTextColor.DARK_BLUE);
        COLOR_MAP.put("&2", NamedTextColor.DARK_GREEN);
        COLOR_MAP.put("&3", NamedTextColor.DARK_AQUA);
        COLOR_MAP.put("&4", NamedTextColor.DARK_RED);
        COLOR_MAP.put("&5", NamedTextColor.DARK_PURPLE);
        COLOR_MAP.put("&6", NamedTextColor.GOLD);
        COLOR_MAP.put("&7", NamedTextColor.GRAY);
        COLOR_MAP.put("&8", NamedTextColor.DARK_GRAY);
        COLOR_MAP.put("&9", NamedTextColor.BLUE);
        COLOR_MAP.put("&a", NamedTextColor.GREEN);
        COLOR_MAP.put("&b", NamedTextColor.AQUA);
        COLOR_MAP.put("&c", NamedTextColor.RED);
        COLOR_MAP.put("&d", NamedTextColor.LIGHT_PURPLE);
        COLOR_MAP.put("&e", NamedTextColor.YELLOW);
        COLOR_MAP.put("&f", NamedTextColor.WHITE);
    }

    public static Component getGameDesc(String aColor, String bColor, int shootTime) {
        return Component.text()
                .append(getBorder(aColor))
                .append(Component.text("\n          Final Game:" + "\n        The Laserdome!", NamedTextColor.WHITE, TextDecoration.BOLD))
                .append(Component.text("\nHow To Play:", NamedTextColor.BLUE, TextDecoration.BOLD))
                .append(Component.text("""
                                \nThere are always two arrows in play.
                                They will spawn above the black targets on the arena.
                                On round 1, one arrow will spawn on each platform.
                                After that, both arrows spawn for the team that lost last.
                                After a team shoots an arrow, the other team will get it.
                                When an arrow spawns on your half, you will have\s""",
                        NamedTextColor.WHITE))
                .append(Component.text(shootTime, NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" seconds to shoot the arrow.", NamedTextColor.WHITE))
                .append(Component.text("""
                                For every 2 arrows shot, the arena will shrink by 2 blocks.
                                Falling off the arena counts as a death.""",
                        NamedTextColor.WHITE))
                .append(getBorder(bColor))
                .build();
    }

    public static Component getGameWinDesc(String aColor, String bColor) {
        return Component.text()
                .append(getBorder(aColor))
                .append(Component.text("""
                                \nYou win the round when all opponents have been eliminated.
                                This game will be a best of 5 rounds. The first team to 3 wins will be crowned the winner of the event.
                                Spectators: show support for your favorite team by clicking on the banner of your choice!
                                Good luck and may the best team win!
                                """).decoration(TextDecoration.BOLD, false))
                .append(getBorder(bColor)).build();
    }

    public static Component getWinnerMessage(Team winner) {
        TextComponent.Builder componentBuilder = Component.text()
                .append(getBorder(winner.getConfigColor()))
                .append(Component.text("\nWinner:\n"))
                .append(Component.text(winner.getTeamName(), COLOR_MAP.get(winner.getConfigColor()), TextDecoration.BOLD))
                .append(Component.text("\n\nMembers:"));

        for(Player p:winner.getOnlinePlayers()) {
            componentBuilder.append(Component.text("\n" + p.getName(), COLOR_MAP.get(winner.getConfigColor()), TextDecoration.BOLD));
        }
        componentBuilder.append(Component.text("\n"));
        componentBuilder.append(getBorder(winner.getConfigColor()));

        return componentBuilder.build();
    }

    public static Title getWinnerTitle(Team winner) {
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(6000), Duration.ofMillis(1000));

        return Title.title(Component.text(winner.getColor() + winner.getTeamName() + "HAS WON THE EVENT!"), Component.empty(), times);
    }

    public static Title getRoundEndTitle(Team winner) {
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(6000), Duration.ofMillis(1000));

        return Title.title(Component.text(winner.getColor() + "Round Over"), Component.text(winner.getColor() + winner.getTeamName() + " Wins!"), times);
    }

    public static Component getRoundWinMessage(Team winner) {
        return Component.text()
                .append(Component.text(winner.getTeamName(), COLOR_MAP.get(winner.getConfigColor()), TextDecoration.BOLD))
                .append(Component.text(" Won Round "))
                .append(Component.text(teamAWins + teamBWins, NamedTextColor.YELLOW, TextDecoration.BOLD))
                .build();
    }

    public static Component getBorder(String colorStr) {
        return Component.text("----------------------------------------").color(COLOR_MAP.get(colorStr)).decorate(TextDecoration.BOLD).decorate(TextDecoration.STRIKETHROUGH);
    }

}
