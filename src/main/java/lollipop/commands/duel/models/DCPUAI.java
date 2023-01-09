package lollipop.commands.duel.models;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Duels Game AI built using Minimax Algorithm with optimal scenario utilization <br>
 * Scenario Array = {attack, defense, heal, strength}
 */
public class DCPUAI {

    private static int totalGames = 0;
    private static int gamesWon = 0;

    private static final HashMap<String, int[]> moveInfluence = new HashMap<>() {{
        put("punch", new int[]{2, 0, 0, 0});
        put("kick", new int[]{2, 0, 0, 0});
        put("headbutt", new int[]{2, 0, 0, 0});
        put("chop", new int[]{2, 0, 0, 0});
        put("eat", new int[]{0, 1, 2, 0});
        put("breathe", new int[]{1, 0, 0, 2});
        put("shield", new int[]{0, 2, 0, 0});
        put("block", new int[]{1, 2, 0, 0});
        put("4th gear", new int[]{3, 0, 0, 0});
        put("hinokami kagura", new int[]{3, 0, 0, 0});
        put("rasengan", new int[]{3, 0, 0, 0});
        put("ora", new int[]{3, 0, 0, 0});
        put("us smash", new int[]{3, 0, 0, 0});
        put("100%", new int[]{0, 0, 0, 4});
        put("serious punch", new int[]{6, 0, 0, 0});
        put("za warudo", new int[]{2, 2, 2, 2});
        put("yare yare daze", new int[]{3, 0, 0, 0});
    }};

    /**
     * Generates a scenario array based on the current status of the game <br>
     * scenario[0] = attack importance <br>
     * scenario[1] = defense importance <br>
     * scenario[2] = healing importance <br>
     * scenario[3] = strength importance
     * @param game {@link DGame} instance of the current game being played
     * @return scenario array with move type importance levels
     */
    private static int[] generateScenario(DGame game) {
        int[] scenario = new int[4];
        DPlayer cpu = game.getGuestPlayer();
        DPlayer player = game.getHomePlayer();

        if(player.getHP() < 25) scenario[0]++;
        if(cpu.getHP() > player.getHP()+50) scenario[0]++;
        if(cpu.getHP() > player.getHP()+30) scenario[0]++;
        if(cpu.getHP() < 30) { scenario[1]+=3; scenario[2]++; }
        if(cpu.getHP() < player.getHP()-50) { scenario[1]++; scenario[2]+=3; }
        else scenario[0]++;
        if(cpu.getHP() < player.getHP()-30) scenario[2]++;
        else scenario[0]++;
        if(cpu.getSP() < player.getSP()-3) scenario[3]++;
        if(cpu.getSP() < player.getSP()-7) scenario[3]+=3;
        if(Math.abs(cpu.getHP()-player.getHP()) <= 15) scenario[3]++;

        return scenario;
    }

    /**
     * Calculate a move's score based on the current game scenario.
     * @param scenario scenario array
     * @param influence move influence array
     * @return score of the given move influence array
     */
    private static int calculateScore(int[] scenario, int[] influence) {
        int score = 0;
        for(int i=0; i<3; i++)
            score += scenario[i] * influence[i];

        return score;
    }

    /**
     * Calculate for the best possible move of the provided options.
     * @param game current game status to calculate scenario array
     * @param options move options for the AI to choose from
     * @return Best {@link DMove} to play for the AI from the options given
     */
    public static DMove minimax(DGame game, DMove[] options) {
        int[] scenario = generateScenario(game);

        DMove bestMove = null;
        int maxScore = -1;
        for(DMove move : options) {
            int score = calculateScore(scenario, moveInfluence.get(move.getName()));
            if(score >= maxScore) {
                maxScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    public static String calculateRating() {
        if(totalGames == 0) return "Unknown";
        int percentage = gamesWon*100/totalGames;
        if(percentage < 20) return "Trivial; Win Rate = " + percentage + "%";
        else if(percentage < 40) return "Noob; Win Rate = " + percentage + "%";
        else if(percentage < 60) return "Amateur; Win Rate = " + percentage + "%";
        else if(percentage < 80) return "Master; Win Rate = " + percentage + "%";
        return "Unbeatable; Win Rate = " + percentage + "%";
    }

    public static void incrementGamesCount(boolean won) {
        totalGames++;
        if(won) gamesWon++;
    }

}
