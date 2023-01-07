package lollipop.commands.duel.models;

import java.util.HashMap;
import java.util.HashSet;

public class DMFactory {

    private static final DMove[] moves;
    private static final DMove surrender;
    private static final HashMap<String, DMove> moveMap;

    static {
        moveMap = new HashMap<>();

        surrender = new DMove(
                "surrender",
                "ff",
                "`%s` surrendered out of fear and quit the duel.",
                "surrender the duel challenge",
                "https://c.tenor.com/1WSdEj1xWfAAAAAd/one-punch-man-anime.gif",
                "Surrender your game and quit the duel",
                DMType.FORFEIT,
                false
        );

        moves = new DMove[] {
                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "punch",
                        "attack1",
                        "`%s` punched their opponent and caused `%d HP` damage!",
                        "Punch your opponents with tight fists!",
                        "https://c.tenor.com/6a42QlkVsCEAAAAd/anime-punch.gif",
                        "(`5-10 HP` damage on opponent)",
                        DMType.ATTACK,
                        true
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "kick",
                        "attack2",
                        "`%s` kicked their opponent and caused `%d HP` damage!",
                        "Kick your opponents fearlessly!",
                        "https://c.tenor.com/1sTe1w12WHwAAAAC/nezuko-kamado-tanjiro-kamado.gif",
                        "(`5-10 HP` damage on opponent)",
                        DMType.ATTACK,
                        true
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "headbutt",
                        "attack3",
                        "`%s` headbutted their opponent and caused `%d HP` damage!",
                        "Use that head of yours to headbutt your opponents!",
                        "https://c.tenor.com/4AvIBPKxbOwAAAAd/demonslayer-headbutt.gif",
                        "(`5-10 HP` damage on opponent)",
                        DMType.ATTACK,
                        true
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "chop",
                        "attack4",
                        "`%s` chopped their opponent and caused `%d HP` damage!",
                        "Chop your opponents down with that firm knife hand!",
                        "https://c.tenor.com/FMO5562dLt4AAAAd/one-punch-man2-saitama-v-garou.gif",
                        "(`5-10 HP` damage on opponent)",
                        DMType.ATTACK,
                        true
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "eat",
                        "heal1",
                        "`%s` healed himself and gained `%d HP`!",
                        "Eat and restore your health!",
                        "https://c.tenor.com/NUt8vwChgIcAAAAC/luffy-eating.gif",
                        "(`20-30 HP` health boost)",
                        DMType.HEAL,
                        false
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "breathe",
                        "strength1",
                        "`%s` took a deep breath and became `%d SP` stronger!",
                        "Take a deep breath and embrace your power!",
                        "https://c.tenor.com/WGGJBAiyhxQAAAAC/demon-slayer-kimetsu-no-yaiba.gif",
                        "(`3-5 SP` strength boost)",
                        DMType.STRENGTH,
                        false
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "shield",
                        "defense1",
                        "`%s` shielded themselves from the next attack!",
                        "Use a shield to defend yourself!",
                        "https://c.tenor.com/Z_0BQslObuIAAAAd/dragon-ball-barrier.gif",
                        "(Blocks your opponent's next blockable attack; wears off after 1 attack)",
                        DMType.DEFENSE,
                        false
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "block",
                        "defense2",
                        "`%s` is anticipating on blocking the next attack!",
                        "Dodge your opponents attacks by moving out of the way!",
                        "https://media.tenor.com/8H0ommaqSPIAAAAd/blue-lock-bachira.gif",
                        "(Blocks your opponent's next blockable attack; wears off after 1 attack)",
                        DMType.DEFENSE,
                        false
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "4th gear",
                        "ultimate1",
                        "**4th GEAR: BOUNDMAN!**\n`%s` switched into the 4th gear and bounced their opponent away causing `%d HP` damage!",
                        "Bounce your opponents back into hell in the 4th gear!",
                        "https://c.tenor.com/Z6xWNeyumJMAAAAC/one-piece-fight.gif",
                        "(`13-18 HP` damage on opponent)",
                        DMType.ULTIMATE,
                        true
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "hinokami kagura",
                        "ultimate2",
                        "**HINOKAMI KAGURA!**\n`%s` sliced their opponent swiftly and caused `%d HP` damage!",
                        "Slice your opponents with the swift hinokami kagura!",
                        "https://c.tenor.com/LUAKGZSLoD8AAAAd/demon-slayer-tanjiro.gif",
                        "(`13-18 HP` damage on opponent)",
                        DMType.ULTIMATE,
                        true
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "rasengan",
                        "ultimate3",
                        "**RASENGAN!**\n`%s` blasted their opponent away and caused `%d HP` damage!",
                        "Blast your opponents with some chakra!",
                        "https://c.tenor.com/_zEr-rdppKMAAAAC/minato-naruto.gif",
                        "(`14-19 HP` damage on opponent)",
                        DMType.ULTIMATE,
                        true
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "ora",
                        "ultimate4",
                        "**ORA ORA ORA ORA ORAAAA!**\n`%s` pounded their opponent and caused `%d HP` damage!",
                        "Pound your opponents with multiple powerful shots quickly!",
                        "https://c.tenor.com/LytxJSf81m4AAAAC/ora-beatdown-oraoraora.gif",
                        "(`15-20 HP` damage on opponent)",
                        DMType.ULTIMATE,
                        true
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "us smash",
                        "ultimate5",
                        "**United... States... SMASH!**\n`%s` smashed their opponent with more than 100% power!",
                        "Go beyond plus ultra and smash your opponents on a US scale!",
                        "https://media.tenor.com/ZRDPXCwLXcIAAAAd/all-might-my-hero-academia.gif",
                        "(`16-20 HP` damage on your opponent)",
                        DMType.ULTIMATE,
                        true
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "100%",
                        "ultimate6",
                        "**98.. 99.. 100%**\n`%s` lost control of their emotions and gained `%d SP` strength!",
                        "Lose control of your emotions and gain unfathomable strength!",
                        "https://media.tenor.com/8cNey0yg9xsAAAAC/mob-100.gif",
                        "(`6-10 SP` strength boost)",
                        DMType.ULTIMATE,
                        true
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "serious punch",
                        "ultimate7",
                        "**Anybody in my way... gets punched!**\n`%s` punched their opponent *seriosuly* and caused `%d HP` damage!",
                        "Punch your opponents *seriously*!",
                        "https://c.tenor.com/vlsvbgqYz5QAAAAd/carnage-kabuto-saitama.gif",
                        "(`40-50 HP` damage on opponent but `20-30 HP` when opponent is blocking)",
                        DMType.ULTIMATE,
                        false
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "za warudo",
                        "ultimate8",
                        "**ZA WARUDO!**\n`%s` stopped time. Their opponent is frozen for `6 seconds`!",
                        "Freeze time for your opponents and get moves ahead!",
                        "https://c.tenor.com/ETlOjJ8aU7EAAAAC/za-warudo-jojo-bizarre-adventure.gif",
                        "(Prevents the opponent from having a turn for 6 seconds)",
                        DMType.ULTIMATE,
                        false
                ),

                // --------------------------------------------------------------------------------------------------------

                new DMove(
                        "yare yare daze",
                        "ultimate9",
                        "**yare yare daze...**\n`%s` intimidated their opponent making them `%d SP` weaker and pounded them multiple times causing `%d HP` damage!",
                        "Intimidate your opponents to make them weaker and pound them several times in the same move!",
                        "https://c.tenor.com/Wtn31Gl1CpYAAAAC/jotaro-ora.gif",
                        "(Opponents become `5-15 SP` weaker and damaged `15-20 HP`)",
                        DMType.ULTIMATE,
                        false
                ),

                // --------------------------------------------------------------------------------------------------------
        };

        for(DMove move : moves)
            moveMap.put(move.getName(), move);
    }

    public static DMove[] getMoves() {
        return moves;
    }

    public static DMove getSurrender() {
        return surrender;
    }

    public static DMove[] generateRandomMoves(int count) {
        int size = moves.length;

        DMove[] result = new DMove[count];
        int range = size/count;

        int idx = -1;
        for(int i=0; i<count-1; i++) {
            idx += (int)(Math.random() * range)+1;
            result[i] = moves[idx];
        }
        idx += (int)(Math.random() * (size-range*(count-1)));
        result[count-1] = moves[idx];

        return result;
    }

    public static DMove getMove(String moveName) {
        if(!moveMap.containsKey(moveName)) return null;
        return moveMap.get(moveName);
    }

}
