package lollipop.commands.duel;

import lollipop.*;
import lollipop.commands.duel.models.DGame;
import lollipop.commands.duel.models.DGamePT;
import lollipop.commands.duel.models.DPlayer;
import lollipop.commands.duel.models.DPlayerPT;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;


import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
        return "Duel somebody (or an AI) in a small fun and competitive battle game!\nUsage: `" + Constant.PREFIX + getAliases()[0] + " [user*]`";
    }

    @Override
    public CommandData getSlashCmd() {
        return Tools.defaultSlashCmd(this)
                .addOption(OptionType.USER, "user", "mention a user", false);
    }

    // Game Settings and Occupancy
    public static HashMap<Long, DGamePT> memberToGame = new HashMap<>();
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
            DPlayerPT homePlayer = new DPlayerPT(event.getMember());
            DPlayerPT guestPlayer = new DPlayerPT(null);
            DGamePT game = new DGamePT(homePlayer, guestPlayer);

            Duel.memberToGame.put(event.getMember().getIdLong(), game);

            game.initiateGame(event.getTextChannel());
        } else if(options.size() == 1) {
            DPlayerPT homePlayer = new DPlayerPT(event.getMember());
            BotStatistics.sendMultiplier(homePlayer.getMember().getId(), () -> homePlayer.setMultiplierStatus(true), () -> homePlayer.setMultiplierStatus(false));

            if(options.get(0).getAsMember().getIdLong() == Constant.BOT_ID) {
                DPlayerPT guestPlayer = new DPlayerPT(null);
                guestPlayer.setMultiplierStatus(false);
                DGamePT game = new DGamePT(homePlayer, guestPlayer);

                Duel.memberToGame.put(event.getMember().getIdLong(), game);

                game.initiateGame(event.getTextChannel());
            } else {
                DPlayerPT guestPlayer = new DPlayerPT(options.get(0).getAsMember());
                BotStatistics.sendMultiplier(homePlayer.getMember().getId(), () -> guestPlayer.setMultiplierStatus(true), () -> guestPlayer.setMultiplierStatus(false));
                DGamePT game = new DGamePT(homePlayer, guestPlayer);

                if(guestPlayer.getMember().getUser().isBot()) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setDescription("You can't request a duel with other bots!\nTry using `/help duel` for usage examples!")
                            .setColor(Color.red)
                            .build()
                    ).setEphemeral(true).queue();
                    return;
                }
                if(guestPlayer.getMember() == event.getMember()) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setDescription("You can't request a duel with yourself!\nTry using `/help duel` for usage examples!")
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

}
