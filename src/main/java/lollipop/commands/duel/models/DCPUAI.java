package lollipop.commands.duel.models;

import lollipop.Tools;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Duels Game AI built using Minimax Algorithm with optimal scenario utilization <br>
 * Scenario Array = {attack, defense, heal, strength}
 */
public class DCPUAI {

    private static int[] duelsRating;

    // Default starts at 200 rating
    public static void setupRating(ShardManager shards) {
        duelsRating = new int[shards.getShardsTotal()];
        Arrays.fill(duelsRating, 200);
    }

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

        if(player.getHP() < 25) scenario[0]+=2;
        if(player.getHP() < 15) scenario[0]+=4;
        if(cpu.getHP() > player.getHP()+50) { scenario[0]++; scenario[3]+=2; }
        if(cpu.getHP() > player.getHP()+30) { scenario[0]++; scenario[3]++; }
        if(cpu.getHP() < 30) { scenario[1]+=3; scenario[2]++; }
        if(cpu.getHP() < player.getHP()-50) { scenario[1]++; scenario[2]+=3; }
        else if(cpu.getHP() < player.getHP()-30) scenario[2]++;
        else scenario[0]++;
        if(cpu.getSP() < player.getSP()-3) scenario[3]++;
        if(cpu.getSP() < player.getSP()-7) scenario[3]+=3;
        if(Math.abs(cpu.getHP()-player.getHP()) <= 15) scenario[3]++;
        if(cpu.getHP() > player.getHP()) scenario[3]+=2;
        if(cpu.isDefending()) scenario[1] = 0;

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
            int score = calculateScore(scenario, move.getInfluence());
            if(score >= maxScore) {
                maxScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    public static String calculateRating(JDA jda) {
        int shard = jda.getShardInfo().getShardId();
        if(duelsRating[shard] < 64) return duelsRating[shard] + " (Trivial)";
        else if(duelsRating[shard] < 128) return duelsRating[shard] + " (Noob)";
        else if(duelsRating[shard] < 256) return duelsRating[shard] + " (Amateur)";
        else if(duelsRating[shard] < 512) return duelsRating[shard] + " (Master)";
        return duelsRating[shard] + " (Unbeatable)";
    }

    public static void updateRating(JDA jda, boolean won, int playerHP, int cpuHP) {
        int shard = jda.getShardInfo().getShardId();
        double delta = won ? Tools.ratingCurve(cpuHP-playerHP) : -Tools.ratingCurve(playerHP-cpuHP);
        delta = 1+delta*Tools.factorCurve(duelsRating[shard]);
        System.out.println(duelsRating[shard]);
        System.out.println(Tools.ratingCurve(Math.abs(cpuHP-playerHP)) + " " + Tools.factorCurve(duelsRating[shard]) + " " + delta);
        duelsRating[shard] = Math.max(1, (int)(duelsRating[shard] * delta));
    }

}
