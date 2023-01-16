package lollipop.commands.leaderboard.models;

import java.util.List;

/**
 * Leaderboard Result Model for leaderbaord text
 */
public class LeaderboardResult {

    private final LBMember member;
    private final List<List<LBMember>> leaderboard;

    public LeaderboardResult(LBMember member, List<List<LBMember>> leaderboard) {
        this.member = member;
        this.leaderboard = leaderboard;
    }

    public LBMember getMember() {
        return this.member;
    }

    public List<List<LBMember>> getLeaderboard() {
        return this.leaderboard;
    }

    /**
     * Static class for Leaderboard Member Statistics
     */
    public static class LBMember {

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

}
