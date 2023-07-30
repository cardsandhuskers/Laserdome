package io.github.cardsandhuskers.laserdome.handlers;

import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Collections;

import static io.github.cardsandhuskers.teams.Teams.handler;

public class SpecBannerHandler {
    Team teamA,teamB;


    public SpecBannerHandler(Team teamA, Team teamB) {
        this.teamA = teamA;
        this.teamB = teamB;
    }


    public void giveBanners() {
        ItemStack aBanner = new ItemStack(getBanner(teamA.color));
        ItemMeta aBannerMeta = aBanner.getItemMeta();
        aBannerMeta.setLore(Collections.singletonList("click this banner to root for " + teamA.getTeamName()));
        aBannerMeta.setDisplayName(teamA.getTeamName());
        aBanner.setItemMeta(aBannerMeta);


        ItemStack bBanner = new ItemStack(getBanner(teamB.color));
        ItemMeta bBannerMeta = bBanner.getItemMeta();
        bBannerMeta.setLore(Collections.singletonList("click this banner to root for " + teamA.getTeamName()));
        bBannerMeta.setDisplayName(teamB.getTeamName());
        bBanner.setItemMeta(bBannerMeta);

        for(Player p: Bukkit.getOnlinePlayers()) {
            Team t = handler.getPlayerTeam(p);
            if(!(t == teamA || t == teamB)) {
                p.getInventory().setItem(3, aBanner);
                p.getInventory().setItem(5, bBanner);
            }
        }
    }

    public void updatebanner(Player p, Team choice) {
        ItemStack banner = new ItemStack(getBanner(choice.color));
        ItemMeta bannerMeta = banner.getItemMeta();
        bannerMeta.setLore(Collections.singletonList("click this banner to root for " + choice.getTeamName()));
        bannerMeta.addEnchant(Enchantment.LURE, 1, true);
        bannerMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        bannerMeta.setDisplayName(choice.getTeamName());
        banner.setItemMeta(bannerMeta);

        int index;
        if(choice == teamA) {
            index = 3;
        } else if(choice == teamB) {
            index = 5;
        } else {
            return;
        }
        p.getInventory().setItem(index, banner);

    }

    public void updateChoice(String teamName, Player p) {
        Team team = handler.getTeam(teamName);
        if(team == null) return;

        setArmor(p, team);
        updatebanner(p, team);

    }

    public void setArmor(Player p, Team team) {
        ItemStack banner = new ItemStack(getBanner(team.color));
        p.getEquipment().setHelmet(banner);

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        chestplateMeta.setColor(team.translateColor());
        chestplate.setItemMeta(chestplateMeta);
        p.getEquipment().setChestplate(chestplate);

        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        leggingsMeta.setColor(team.translateColor());
        leggings.setItemMeta(leggingsMeta);
        p.getEquipment().setLeggings(leggings);

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootsMeta.setColor(team.translateColor());
        boots.setItemMeta(bootsMeta);
        p.getEquipment().setBoots(boots);
    }


    private Material getBanner(String color) {
        switch (color) {
            case "§2": return Material.GREEN_BANNER;
            case "§3": return Material.CYAN_BANNER;
            case "§5": return Material.PURPLE_BANNER;
            case "§6": return Material.ORANGE_BANNER;
            case "§7": return Material.LIGHT_GRAY_BANNER;
            case "§8": return Material.BLACK_BANNER;
            case "§9": return Material.BLUE_BANNER;
            case "§a": return Material.LIME_BANNER;
            case "§b": return Material.LIGHT_BLUE_BANNER;
            case "§c": return Material.RED_BANNER;
            case "§d": return Material.PINK_BANNER;
            case "§e": return Material.YELLOW_BANNER;
            default: return Material.WHITE_BANNER;
        }
    }
}
