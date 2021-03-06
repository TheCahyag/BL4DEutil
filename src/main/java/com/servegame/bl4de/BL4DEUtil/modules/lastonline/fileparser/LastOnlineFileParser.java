package com.servegame.bl4de.BL4DEUtil.modules.lastonline.fileparser;

import com.servegame.bl4de.BL4DEUtil.BL4DEUtil;
import org.slf4j.Logger;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * File: LastOnlineFileParser.java
 * @author Brandon Bires-Navel (brandonnavel@outlook.com)
 */
public class LastOnlineFileParser {

    private static Logger logger;
    private static BL4DEUtil util;

    public final static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");

    /**
     * LastOnlineFileParser constructor
     * @param plugin - BL4DEUtil - to reference logger and such
     */
    public LastOnlineFileParser(BL4DEUtil plugin){
        logger = plugin.getLogger();
        util = plugin;
    }

    /**
     * getRecentPlayerLogins parses the designated data file and will return a map filled
     * with Entry<Date, String>
     * @return Optional\<Map<Date, String>> map containing all players and their last join date
     */
    public static Optional<Map<Date, String>> getRecentPlayerLogins(){
        File file = new File(util.getRecentPlayersDataDir());
        // The map is <Date, String> so the tree map will sort the map automatically by date
        Map<Date, String> players = new TreeMap<>();
        synchronized (LastOnlineFileParser.class) {
            try (Scanner in = new Scanner(file)) {
                while (in.hasNextLine()) {
                    // Line format: PLAYER_NAME|DATE
                    String[] tokens = in.nextLine().split("\\|");
                    players.put(parseDateIn(tokens[1]), tokens[0]);
                }
                in.close();
            } catch (FileNotFoundException e) {
                logger.info("Encountered an FileNotFoundException while parsing recent_player_logins.dat (most likely).");
                e.printStackTrace();
                return Optional.empty();
            }
            return Optional.of(players);
        }
    }

    /**
     * logPlayerJoin will check the current map and see if the player joining has an entry. If they are currently in the
     * map their date will be updated, if not a new entry will be created for them
     * @param name - String - the name of the player
     */
    public static void logPlayerJoin(String name){
        Optional<Map<Date, String>> mapOptional = getRecentPlayerLogins();
        Map<Date, String> data;
        if (!mapOptional.isPresent()){
            logger.info("No player entries were found. (It probably couldn't access the file)");
            new Throwable().printStackTrace();
            return;
        }
        data = mapOptional.get();
        OUTER:
        if (data.containsValue(name)){
            for (Map.Entry<Date, String> entry :
                    data.entrySet()) {
                if (entry.getValue().equals(name)){
                    data.remove(entry.getKey());
                    data.put(new Date(), name);
                    break OUTER;
                }
            }
        } else {
            data.put(new Date(), name);
        }
        writeChanges(data);
    }

    /**
     * writeChanges will open the given player data file and will re-write with the new data
     * @param map - Optional\<Map<Date, String>> map containing all players and their last join date
     */
    private static void writeChanges(Map<Date, String> map) {
        synchronized (LastOnlineFileParser.class) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(util.getRecentPlayersDataDir()))) {
                for (Map.Entry<Date, String> entry :
                        map.entrySet()) {
                    String line = entry.getValue() + "|" + dateFormat.format(entry.getKey()) + "\n";
                    bw.write(line);
                }
                bw.close();
            } catch (IOException e) {
                logger.info("Ran into a problem when trying to write to the data file.");
                e.printStackTrace();
            }
        }
    }

    /**
     * parseDateIn will take a string and turn it into a date
     * @param date - date to parse
     * @return - Date object that reflects the string
     */
    private static Date parseDateIn(String date){
        Date dateToParse = null;
        try {
            dateToParse = dateFormat.parse(date);
        } catch (ParseException e){
            logger.info("Failed to parse date. (This shouldn't happen)");
            e.printStackTrace();
        }
        return dateToParse == null ? new Date() : dateToParse; // Last ditch effort if it can't parse the date (shouldn't happen tho)
    }
}
