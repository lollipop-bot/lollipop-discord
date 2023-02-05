package lollipop.commands.duel;

import lollipop.*;
import lollipop.commands.duel.models.DCPUAI;
import lollipop.commands.duel.models.DGame;
import lollipop.commands.duel.models.DPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;


import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class Duel implements Command {

    @Override
    public String[] getAliases() {
        return new String[]{"duel"};
    }

    @Override
    public CommandType getCategory() {
        return CommandType.FUN;
    }

    @Override
    public String getHelp() {
        return "Challenge a player to a duel in a fun competitive battle role-playing game!\n" +
                "In a duel, you can play signature anime moves turn-by-turn against an opponent in a battle to the death\n" +
                "Winners will earn 70-100 lollipops but if you lose against a CPU you will lose 30-50 lollipops\n" +
                "The goal is to bring your opponent down to `0 HP`\n" +
                "You can learn about all the available moves by using the `/move` command\n" +
                "Usage: `" + Constant.PREFIX + getAliases()[0] + " [member*]`";
    }

    @Override
    public CommandData getSlashCmd() {
        return Tools.defaultSlashCmd(this)
                .addOption(OptionType.USER, "member", "Mention the player you want to challenge. Leave this field empty if you want to duel against a CPU.", false);
    }

    // Game Settings and Occupancy
    public static HashMap<Long, DGame> memberToGame = new HashMap<>();
    public static int[] occupiedShards;

    @Override
    public void run(SlashCommandInteractionEvent event) {
        if(occupiedShards == null) occupiedShards = new int[event.getJDA().getShardManager().getShardsTotal()];
        if(!event.getInteraction().isFromGuild()) {
            event.replyEmbeds(
                    new EmbedBuilder()
                            .setColor(Color.red)
                            .setDescription("This command can only be used in guilds!")
                            .build()
            ).setEphemeral(true).queue();
            return;
        }
        if(memberToGame.containsKey(event.getMember().getIdLong())) {
            event.replyEmbeds(new EmbedBuilder()
                    .setDescription("You are already in a duel! Finish your current duel to be able to start a new one...")
                    .setColor(Color.red)
                    .build()
            ).setEphemeral(true).queue();
            return;
        }
        int shardId = event.getJDA().getShardInfo().getShardId();
        if(occupiedShards[shardId] >= 3) {
            event.replyEmbeds(new EmbedBuilder()
                            .setDescription("This shard has hit the maximum limit of duel games! Please wait until the current duels end...")
                            .setFooter("There is a limit of 3 duels per shard to prevent the players from having a laggy experience. Please be patient!")
                            .setColor(Color.red)
                            .build()
            ).setEphemeral(true).queue();
            return;
        }
        else occupiedShards[shardId]++;

        final List<OptionMapping> options = event.getOptions();
        if(options.isEmpty()) {
            event.reply("Duels CPU " + event.getJDA().getShardInfo().getShardId() + " - `AI Rating = " + DCPUAI.calculateRating(event.getJDA()) + "`").queue();

            DPlayer homePlayer = new DPlayer(event.getMember());
            DPlayer guestPlayer = new DPlayer(null);
            DGame game = new DGame(homePlayer, guestPlayer);

            Duel.memberToGame.put(event.getMember().getIdLong(), game);

            game.initiateGame(event.getTextChannel());
        } else if(options.size() == 1) {
            DPlayer homePlayer = new DPlayer(event.getMember());
            BotStatistics.sendMultiplier(homePlayer.getMember().getId(), () -> homePlayer.setMultiplierStatus(true), () -> homePlayer.setMultiplierStatus(false));

            if(options.get(0).getAsMember().getIdLong() == Constant.BOT_ID) {
                event.reply("Duels CPU " + event.getJDA().getShardInfo().getShardId() + " - `AI Rating = " + DCPUAI.calculateRating(event.getJDA()) + "`").queue();

                DPlayer guestPlayer = new DPlayer(null);
                guestPlayer.setMultiplierStatus(false);
                DGame game = new DGame(homePlayer, guestPlayer);

                Duel.memberToGame.put(event.getMember().getIdLong(), game);

                game.initiateGame(event.getTextChannel());
            } else {
                DPlayer guestPlayer = new DPlayer(options.get(0).getAsMember());
                BotStatistics.sendMultiplier(homePlayer.getMember().getId(), () -> guestPlayer.setMultiplierStatus(true), () -> guestPlayer.setMultiplierStatus(false));
                DGame game = new DGame(homePlayer, guestPlayer);

                if(guestPlayer.getMember().getUser().isBot()) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setDescription("You can't request a duel with other bots!\nTry using `" + Constant.PREFIX + "help duel` for usage examples!")
                            .setColor(Color.red)
                            .build()
                    ).setEphemeral(true).queue();
                    return;
                }
                if(guestPlayer.getMember() == event.getMember()) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setDescription("You can't request a duel with yourself!\nTry using `" + Constant.PREFIX + "help duel` for usage examples!")
                            .setColor(Color.red)
                            .build()
                    ).setEphemeral(true).queue();
                    return;
                }

                Duel.memberToGame.put(homePlayer.getMember().getIdLong(), game);
                Duel.memberToGame.put(guestPlayer.getMember().getIdLong(), game);

                game.sendDuelRequest(event);
            }
        }
    }

    @Override
    public int cooldownInSeconds() {
        return 0;
    }

}
