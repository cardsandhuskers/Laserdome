package io.github.cardsandhuskers.laserdome.objects;

import io.github.cardsandhuskers.laserdome.Laserdome;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class StatCalculator {
    Laserdome plugin;
    private ArrayList<PlayerStatsHolder> playerStatsHolders;
    private ArrayList<SingleGameKillsHolder> sgKillsHolders;
    public StatCalculator(Laserdome plugin) {
        this.plugin = plugin;
    }

    public void calculateStats() throws Exception{
        HashMap<String, PlayerStatsHolder> playerStatsMap = new HashMap<>();
        playerStatsHolders = new ArrayList<>();
        sgKillsHolders = new ArrayList<>();

        FileReader reader = null;
        try {
            reader = new FileReader(plugin.getDataFolder() + "/stats.csv");
        } catch (IOException e) {
            plugin.getLogger().warning("Stats file not found!");
            return;
        }

        String[] headers = {"Event", "Team", "Name", "Kills"};
        CSVFormat.Builder builder = CSVFormat.Builder.create();
        builder.setHeader(headers);
        CSVFormat format = builder.build();

        CSVParser parser;
        try {
            parser = new CSVParser(reader, format);
        } catch (IOException e) {
            throw new Exception(e);
        }
        List<CSVRecord> recordList = parser.getRecords();

        try {
            reader.close();
        } catch (IOException e) {
            throw new Exception(e);
        }

        for(CSVRecord r:recordList) {
            if (r.getRecordNumber() == 1) continue;
            //System.out.println(r);
            String name = r.get(2);
            if(playerStatsMap.containsKey(name)) {
                PlayerStatsHolder h = playerStatsMap.get(name);
                h.kills += Integer.parseInt(r.get(3));
            } else {
                PlayerStatsHolder h = new PlayerStatsHolder(name);
                h.kills += Integer.parseInt(r.get(3));
                playerStatsMap.put(name, h);
            }
            SingleGameKillsHolder kh = new SingleGameKillsHolder();
            kh.name = name;
            kh.kills = Integer.parseInt(r.get(3));
            kh.eventNum = Integer.parseInt(r.get(0));
            sgKillsHolders.add(kh);
        }

        playerStatsHolders = new ArrayList<>(playerStatsMap.values());
        //System.out.println(playerStatsHolders);
        //System.out.println(playerStatsMap);
    }

    public ArrayList<PlayerStatsHolder> getStatsHolders() {
        ArrayList<PlayerStatsHolder> psh = new ArrayList<>(playerStatsHolders);

        Comparator PlayerStatsCompare = new PlayerStatsComparator();
        psh.sort(PlayerStatsCompare);
        Collections.reverse(psh);
        return psh;
    }

    public ArrayList<SingleGameKillsHolder> getSGKillsHolders() {
        ArrayList<SingleGameKillsHolder> sgkh = new ArrayList<>(sgKillsHolders);

        Comparator SGKHComparator = new SGKHComparator();
        sgkh.sort(SGKHComparator);
        Collections.reverse(sgkh);
        return sgkh;
    }


    public class PlayerStatsHolder{
        int kills = 0;
        String name;
        public PlayerStatsHolder(String name) {
            this.name = name;
        }
    }

    class PlayerStatsComparator implements Comparator<PlayerStatsHolder> {
        public int compare(PlayerStatsHolder h1, PlayerStatsHolder h2) {
            int compare = Integer.compare(h1.kills, h2.kills);
            if(compare == 0) h1.name.compareTo(h2.name);
            if(compare == 0) compare = 1;
            return  compare;
        }
    }

    public class SingleGameKillsHolder {
        String name;
        int eventNum;
        int kills;
    }
    class SGKHComparator implements Comparator<SingleGameKillsHolder> {
        public int compare(SingleGameKillsHolder kh1, SingleGameKillsHolder kh2) {
            int compare = Integer.compare(kh1.kills, kh2.kills);
            if(compare == 0) compare = kh1.name.compareTo(kh2.name);
            if(compare == 0) compare = 1;
            return compare;
        }
    }
}
