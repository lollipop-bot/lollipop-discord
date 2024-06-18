package lollipop.commands.leaderboard;

import lollipop.*;
import lollipop.Database;
import lollipop.commands.leaderboard.models.LBMember;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Leaderboard implements Command {

    public static HashMap<Long, ScheduledFuture<?>> leaderboardMessages = new HashMap<>();

    @Override
    public String[] getAliases() {
        return new String[]{"leaderboard"};
    }

    @Override
    public CommandType getCategory() {
        return CommandType.FUN;
    }

    @Override
    public String getHelp() {
        return "Display the current leaderboard of lollipop players ranked on terms of player currencies!\n" +
                "Lollipop currency can be earned by using commands and playing games such as `/duel` and `/trivia`\n" +
                "To find your own ranking and currency profile, you can use `/profile`\n" +
                "Usage: `" + Constant.PREFIX + getAliases()[0] + "`";
    }

    @Override
    public CommandData getSlashCmd() {
        return Tools.defaultSlashCmd(this).addOptions(
                new OptionData(OptionType.STRING, "scope", "Select a scope for the leaderboard to rank the desired players.", true)
                        .addChoice("guild", "guild")
                        .addChoice("global", "global")
        );
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        if(!event.getInteraction().isFromGuild()) {
            event.replyEmbeds(
                    new EmbedBuilder()
                            .setColor(Color.red)
                            .setDescription("This command can only be used in guilds!")
                            .build()
            ).queue();
            return;
        }

        final java.util.List<OptionMapping> options = event.getOptions();
        final List<String> args = options.stream().map(OptionMapping::getAsString).toList();

        if(args.get(0).equals("guild")) {
            User author = event.getUser();
            String userId = author.getId();

            List<LBMember> memberList = Database.getLeaderboard(event.getGuild()).get(0);
            LBMember cMember = new LBMember(Database.getUserGuildRank(userId, event.getGuild()), author.getName(), Database.getUserBalance(userId));

            if(memberList.isEmpty()) {
                event.reply("No members on the page").setEphemeral(true).queue();
                return;
            }
            String tmp = event.getGuild().getName() + "'s Leaderboard";
            StringBuilder sb = new StringBuilder(tmp);
            while(sb.length()<130)
                sb.append("\u200E ");
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(sb.toString())
                    .setDescription("```" + getTable(memberList) + "```")
                    .addField("Your Rank", "`" + cMember.getRank() + ". " + cMember.getName() + " : " + cMember.getLollipops() + "`", false)
                    .setThumbnail("https://www.dictionary.com/e/wp-content/uploads/2018/11/lollipop-emoji.png")
                    .setFooter("Vote for lollipop on top.gg to increase your multiplier!");

            InteractionHook message = event.replyEmbeds(embed.build())
                    .addActionRow(
                            Button.secondary(userId + ":previous", Emoji.fromUnicode("⬅️")),
                            Button.success(userId + ":done:1", Emoji.fromUnicode("✅")),
                            Button.danger(userId + ":delete", Emoji.fromUnicode("\uD83D\uDDD1")),
                            Button.secondary(userId + ":next", Emoji.fromUnicode("➡️"))
                    ).complete();
            long messageId = message.retrieveOriginal().complete().getIdLong();
            ScheduledFuture<?> dispose = message.editOriginalComponents().setActionRow(
                    Button.secondary(userId + ":previous", Emoji.fromUnicode("⬅️")).asDisabled(),
                    Button.success(userId + ":done:1", Emoji.fromUnicode("✅")).asDisabled(),
                    Button.danger(userId + ":delete", Emoji.fromUnicode("\uD83D\uDDD1")).asDisabled(),
                    Button.secondary(userId + ":next", Emoji.fromUnicode("➡️")).asDisabled()
            ).queueAfter(3, TimeUnit.MINUTES, msg -> {
                leaderboardMessages.remove(messageId);
            });

            leaderboardMessages.put(messageId, dispose);
        } else if(args.get(0).equals("global")) {
            User author = event.getUser();
            String userId = author.getId();

            List<LBMember> memberList = Database.getLeaderboard(event.getJDA()).get(0);
            LBMember cMember = new LBMember(Database.getUserGlobalRank(userId), author.getName(), Database.getUserBalance(userId));

            if(memberList.isEmpty()) {
                event.reply("No members on the page").setEphemeral(true).queue();
                return;
            }
            StringBuilder sb = new StringBuilder("Global Leaderboard");
            while(sb.length()<130)
                sb.append("\u200E ");
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(sb.toString())
                    .setDescription("```" + getTable(memberList) + "```")
                    .addField("Your Rank", "`" + cMember.getRank() + ". " + cMember.getName() + " : " + cMember.getLollipops() + "`", false)
                    .setThumbnail("https://www.dictionary.com/e/wp-content/uploads/2018/11/lollipop-emoji.png")
                    .setFooter("Vote for lollipop on top.gg to increase your multiplier!");

            InteractionHook message = event.replyEmbeds(embed.build())
                    .addActionRow(
                            Button.secondary(userId + ":previous", Emoji.fromUnicode("⬅️")),
                            Button.success(userId + ":done:1", Emoji.fromUnicode("✅")),
                            Button.danger(userId + ":delete", Emoji.fromUnicode("\uD83D\uDDD1")),
                            Button.secondary(userId + ":next", Emoji.fromUnicode("➡️"))
                    ).complete();
            long messageId = message.retrieveOriginal().complete().getIdLong();
            ScheduledFuture<?> dispose = message.editOriginalComponents().setActionRow(
                    Button.secondary(userId + ":previous", Emoji.fromUnicode("⬅️")).asDisabled(),
                    Button.success(userId + ":done:1", Emoji.fromUnicode("✅")).asDisabled(),
                    Button.danger(userId + ":delete", Emoji.fromUnicode("\uD83D\uDDD1")).asDisabled(),
                    Button.secondary(userId + ":next", Emoji.fromUnicode("➡️")).asDisabled()
            ).queueAfter(3, TimeUnit.MINUTES, msg -> {
                leaderboardMessages.remove(messageId);
            });

            leaderboardMessages.put(messageId, dispose);
        }
    }

    @Override
    public int cooldownDuration() {
        return 10;
    }

    public static String getTable(List<LBMember> memberList) {
        StringBuilder table = new StringBuilder();
        int nameSize = memberList.stream()
                .mapToInt(it -> Math.min(it.getName().length(), 22))
                .max()
                .orElse(0);
        int pointSize = memberList.stream()
                .mapToInt(it -> String.valueOf(it.getLollipops()).length())
                .max()
                .orElse(0);

        String rowFormat = "║%-" + (Math.max(4, String.valueOf(memberList.get(memberList.size() - 1).getRank()).length()) + 1) + "s" +
                "║%-" + (Math.max(nameSize, 5) + 1) + "s" +
                "║%-" + (Math.max(pointSize, 9) + 1) + "s║%n";
        String divider = String.format(rowFormat, "", "", "", "").replaceAll(" ", "═");

        table.append(String.format(rowFormat, "", "", "", "").replaceFirst("║", "╔")
                .replaceFirst("║", "╦").replaceFirst("║", "╦")
                .replaceFirst("║", "╗").replaceAll(" ", "═"));
        table.append(String.format(rowFormat, "Rank ", "Name", "Lollipops"));
        table.append(divider);

        for (LBMember member : memberList) {
            String name = member.getName();
            table.append(String.format(rowFormat, member.getRank() + ".", name.substring(0, Math.min(22, name.length())), member.getLollipops()));
        }

        table.append(String.format(rowFormat, "", "", "", "").replaceFirst("║", "╚")
                .replaceFirst("║", "╩").replaceFirst("║", "╩")
                .replaceFirst("║", "╝").replaceAll(" ", "═"));

        return table.toString();
    }

}
