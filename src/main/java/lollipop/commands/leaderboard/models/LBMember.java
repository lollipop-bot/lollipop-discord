package lollipop.commands.leaderboard.models;

/**
 * Static class for Leaderboard Member Statistics
 */
public class LBMember {

    private final int rank;
    private final String name;
    private final int lollipops;

    public LBMember(int rank, String name, int lollipops) {
        this.rank = rank;
        this.name = name;
        this.lollipops = lollipops;
    }

    public int getRank() {
        return rank;
    }

    public String getName() {
        return name;
    }

    public int getLollipops() {
        return lollipops;
    }

}
