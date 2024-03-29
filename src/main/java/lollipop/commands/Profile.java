package lollipop.commands;

import lollipop.*;
import lollipop.Database;
import lollipop.commands.leaderboard.models.LBMember;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Profile implements Command {

    @Override
    public String[] getAliases() {
        return new String[]{"profile"};
    }

    @Override
    public CommandType getCategory() {
        return CommandType.FUN;
    }

    @Override
    public String getHelp() {
        return "Display a guild member's lollipop currency profile.\n" +
                "`/profile` shows a member's title, rank, lollipop count, and general profile attributes\n" +
                "To view a list of the richest lollipop users, use the `/leaderboard` command\n" +
                "Usage: `" + Constant.PREFIX + getAliases()[0] + " [member*]`";
    }

    @Override
    public CommandData getSlashCmd() {
        return Tools.defaultSlashCmd(this)
                .addOption(OptionType.USER, "member", "Mention user to show their profile.", false);
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

        EmbedBuilder builder = new EmbedBuilder();
        DateTimeFormatter dateFormat = DateTimeFormatter.RFC_1123_DATE_TIME;
        Member target;

        if(event.getOptions().size() == 1) {
            target = event.getOptions().get(0).getAsMember();
            if(target == null) {
                event.replyEmbeds(
                        new EmbedBuilder()
                                .setColor(Color.red)
                                .setDescription("Could not find that user! Please try again with a valid member in the options!")
                                .build()
                ).setEphemeral(true).queue();
                return;
            }
            if(target.getUser().isBot()) {
                event.replyEmbeds(
                        new EmbedBuilder()
                                .setColor(Color.red)
                                .setDescription("You can't look at the profile of a bot account!")
                                .build()
                ).setEphemeral(true).queue();
                return;
            }
        }
        else target = event.getMember();

        int lollipops = Database.getUserBalance(target.getId());
        int[] ranks = Database.getUserRank(target.getId(), event.getGuild());
        int guildSize = event.getGuild().getMemberCount();
        int globalSize = Database.getCurrencyUserCount();
        double level = lollipopsToLevel(lollipops);
        ArrayList<String> title = new ArrayList<>();
        if(target.getUser().getIdLong()==815017361215979541L || target.getUser().getIdLong()==525126007330570259L) title.add("Developer");
        if(target.isOwner()) title.add("Owner");
        if(target.isBoosting()) title.add("Booster");
        //leaderboard titles
        int rank = Database.getUserGlobalRank(target.getId());
        if(rank <= 3)
            title.add("#"+rank + " global");
        else if(rank <= 5)
            title.add("Top 5");
        else if(rank <= 10)
            title.add("Top 10");
        else if(rank <= 25)
            title.add("Top 25");
        else if(rank <= 50)
            title.add("Top 50");
        builder.setAuthor(target.getEffectiveName() + "'s profile", "https://top.gg/bot/919061572649910292");
        builder.setThumbnail(target.getEffectiveAvatarUrl() + "?size=512");
        builder.setColor(target.getColor());
        String banner = target.getUser().retrieveProfile().complete().getBannerUrl();
        if(banner != null) {
            banner += "?size=512";
            builder.setImage(banner);
        }
        else builder.setImage("https://user-images.githubusercontent.com/47650058/147891305-58aa09b6-2053-4180-9a9a-8c09826567f1.png");
        StringBuilder titleStringFormatted = new StringBuilder("");
        for (String s:title) {
            titleStringFormatted.append(s);
            if(title.indexOf(s)!= title.size()-1)
               titleStringFormatted.append(", ");
        }
        builder.setDescription("**Title:** `" + titleStringFormatted + "`\n");
        builder.addField("Level", level + " \uD83E\uDE99", true);
        builder.addField("Rank", "**Guild:** " + ranks[0] + "/" + guildSize + "\n**Global:** " + ranks[1] + "/" + globalSize, true);
        builder.addField("Lollipops", lollipops + " \uD83C\uDF6D", true);
        builder.addField("Misc",
                target.getEffectiveName() + " | " + target.getAsMention() + " | " + target.getUser().getAsTag() + "\n" +
                        "**Account Creation:** " + target.getUser().getTimeCreated().format(dateFormat) + "\n" +
                        "**Member Join:** " + target.getTimeJoined().format(dateFormat), false);
        builder.setFooter("Vote for lollipop on top.gg to increase your multiplier to " + Constant.MULTIPLIER + "x");
        Runnable success = () -> {
            builder.appendDescription("**Multiplier:** `" + Constant.MULTIPLIER + "x`");
            event.replyEmbeds(builder.build()).queue();
        };
        Runnable failure = () -> {
            builder.appendDescription("**Multiplier:** `1x`");
            event.replyEmbeds(builder.build()).queue();
        };
        BotStatistics.sendMultiplier(target.getId(), success, failure);
    }

    @Override
    public int cooldownDuration() {
        return 5;
    }

    /**
     * Lollipops to Level relationship
     * @param lollipops number of lollipops
     * @return current level based on lollipops in double
     */
    private double lollipopsToLevel(int lollipops) {
        return Math.round( ( (Math.sqrt(lollipops+10) - 3.162d) / 4d ) *100 ) / 100d;
    }

}
