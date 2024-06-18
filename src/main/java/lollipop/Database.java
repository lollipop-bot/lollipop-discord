package lollipop;
import discorddb.sqlitedb.*;
import lollipop.commands.leaderboard.models.LBMember;
import lollipop.commands.leaderboard.models.LeaderboardResult;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import javax.naming.LimitExceededException;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Static database class to manage all of lollipop's databases
 */
public class Database {

    private static DatabaseTable currency;

    /**
     * Setup and Initialize all the databases
     */
    public static void setupDatabases() {
        try {
            SQLDatabase.createTable("currency", "id bigint primary key", "ct int");
            System.out.println("init");
            currency = SQLDatabase.getTable("currency");
        } catch (/*LimitExceededException | FileAlreadyExistsException | FileNotFoundException |*/ SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get user's current balance
     * @param id user id
     * @return balance amount
     */
    public static int getUserBalance(String id) {
        try {
            if (currency.searchQuery("id", id).length == 0) {
                currency.insertQuery(id, "0");
                return 0;
            }
            return (int)currency.searchQuery("id", id)[0][1];
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Gets a users ranking in terms of lollipops in the specified guild
     * @param id user id
     * @param guild guild to rank
     * @return int array: 1st element = guild rank, 2nd element = global rank
     */
    public static int[] getUserRank(String id, Guild guild) {
        ArrayList<Integer> guildRank = new ArrayList<>();
        for (Member member : guild.getMembers()) {
            int lp = getUserBalance(member.getId());
            guildRank.add(lp);
        }
        guildRank.sort(Collections.reverseOrder());
        ArrayList<Integer> globalRank = Arrays.stream(getColumnInt("currency", "ct").toArray())
                .mapToInt(i -> (int) i)
                .boxed()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toCollection(ArrayList::new));
        int userLp = getUserBalance(id);
        return new int[]{Collections.binarySearch(guildRank, userLp, Collections.reverseOrder()) + 1, Collections.binarySearch(globalRank, userLp, Collections.reverseOrder()) + 1};
    }

    /**
     * Gets a users ranking in terms of lollipops in the specified guild
     * @param id user id
     * @param guild guild to rank
     * @return int array: 1st element = guild rank, 2nd element = global rank
     */
    public static int getUserGuildRank(String id, Guild guild) {
        ArrayList<Integer> guildRank = new ArrayList<>();
        for(Member member : guild.getMembers()) {
            int lp = getUserBalance(member.getId());
            guildRank.add(lp);
        }
        guildRank.sort(Collections.reverseOrder());
        int userLp = getUserBalance(id);
        return Collections.binarySearch(guildRank, userLp, Collections.reverseOrder())+1;
    }

    /**
     * Gets a users ranking in terms of lollipops in the specified guild
     * @param id user id
     * @return int array: 1st element = guild rank, 2nd element = global rank
     */
    public static int getUserGlobalRank(String id) {
        ArrayList<Integer> globalRank = Arrays.stream(getColumnInt("currency", "ct").toArray())
                .mapToInt(i -> (int) i)
                .boxed()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toCollection(ArrayList::new));
        int userLp = getUserBalance(id);
        return Collections.binarySearch(globalRank, userLp, Collections.reverseOrder()) + 1;
    }

    /**
     * Increment user's balance by specified amount
     * @param id user id
     * @param increment increment amount
     */
    public static void addToUserBalance(String id, int increment) {
        int balance = getUserBalance(id) + increment;
        try{
            currency.updateQuery(new String[]{"ct"}, new String[]{String.valueOf(Math.max(0, balance))}, "id", id);//updateValue(id, Math.max(0, balance));
            System.out.println("added " + increment + " new user bal is " + balance);
        }
        catch (SQLException e){}
    }

    /**
     * Gets the top ranked members in a guild for lollipops
     * @param guild mentioned guild
     * @return arraylist of {@link LBMember} for the leaderboard
     */
    public static List<List<LBMember>> getLeaderboard(Guild guild) {
        ArrayList<LBMember> result = new ArrayList<>();
        HashMap<String, Integer> userToLollipops = new HashMap<>();
        for(Member member : guild.getMembers()) userToLollipops.put(member.getId(), getUserBalance(member.getId()));
        userToLollipops = Tools.sortByValue(userToLollipops);
        int rank = 0;
        for(String id : userToLollipops.keySet()) {
            Member member = guild.getMemberById(id);
            if(member == null || member.getUser().isBot()) continue;
            result.add(new LBMember(++rank, member.getUser().getName(), userToLollipops.get(id)));
        }
        return IntStream.range(0, result.size())
                .boxed()
                .collect(Collectors.groupingBy(i -> i / 10))
                .values()
                .stream()
                .map(indices -> indices.stream().map(result::get).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public static Collection<Integer> getColumnInt(String table, String label)
    {
        Collection<Integer> str = new ArrayList<>();
        try {
            var tmp = SQLDatabase.executeQuery("SELECT * FROM " + table);
            while(tmp.next())
                str.add(tmp.getInt(label));
        }
        catch (SQLException e){}
        return str;
    }
    public static Collection<String> getColumnStr(String table, String label)
    {
        Collection<String> str = new ArrayList<>();
        try {
            var tmp = SQLDatabase.executeQuery("SELECT * FROM " + table);
            while(tmp.next())
                str.add(tmp.getString(label));
        }
        catch (SQLException e){}
        return str;
    }
    /**
     * Gets the top ranked members for lollipops globally
     * @param jda current jda instance
     * @return arraylist of {@link LBMember} for the leaderboard
     */
    public static List<List<LBMember>> getLeaderboard(JDA jda) {
        ArrayList<LBMember> result = new ArrayList<>();
        HashMap<String, Integer> userToLollipops = new HashMap<>();
        for (String id : getColumnStr("currency", "id"))
            userToLollipops.put(id, getUserBalance(id));
        userToLollipops = Tools.sortByValue(userToLollipops);
        int rank = 0;
        for(String id : userToLollipops.keySet()) {
            User user = jda.getShardManager().getUserById(id);
            if(user == null || user.isBot()) continue;
            result.add(new LBMember(++rank, user.getName(), userToLollipops.get(id)));
        }
        return IntStream.range(0, result.size())
                .boxed()
                .collect(Collectors.groupingBy(i -> i / 10))
                .values()
                .stream()
                .map(indices -> indices.stream().map(result::get).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    /**
     * Gets the amount of keys in the currency database
     * @return integer for number of users in currency database
     */
    public static int getCurrencyUserCount() {
        try {
            var tmp = SQLDatabase.executeQuery("SELECT * FROM currency");
            tmp.last();
            return tmp.getRow();
        }
        catch (SQLException e){}
            return -1;
    }

}