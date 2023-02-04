package lollipop.commands;

import lollipop.CommandType;
import lollipop.Constant;
import lollipop.Command;
import lollipop.Tools;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class Avatar implements Command {

    @Override
    public String[] getAliases() {
        return new String[] {"avatar"};
    }

    @Override
    public CommandType getCategory() {
        return CommandType.MISCELLANEOUS;
    }

    @Override
    public String getHelp() {
        return "Display the profile picture of a discord account sized properly.\n" +
                "`/avatar` can be used to access another discord member's avatar link or to save it\n" +
                "The picture will be perfectly sized in a 512x512 resolution size\n" +
                "Usage: `" + Constant.PREFIX + getAliases()[0] + " [member]`";
    }

    @Override
    public CommandData getSlashCmd() {
        return Tools.defaultSlashCmd(this)
                .addOption(OptionType.USER, "member", "Mention a member to access their discord avatar.", false);
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        final List<OptionMapping> options = event.getOptions();
        if(event.isFromGuild()) {
            if(options.isEmpty()) {
                Member target = event.getMember();
                if (target == null) {
                    event.replyEmbeds(new EmbedBuilder().setDescription("Could not find the specified member!").setColor(Color.red).build()).queue();
                    return;
                }
                event.replyEmbeds(new EmbedBuilder()
                        .setImage(target.getEffectiveAvatarUrl() + "?size=512")
                        .setAuthor(target.getEffectiveName(), target.getAvatarUrl())
                        .build()
                ).queue();
            } else {
                Member target = options.get(0).getAsMember();
                if (target == null) {
                    event.replyEmbeds(new EmbedBuilder().setDescription("Could not find the specified member!").setColor(Color.red).build()).queue();
                    return;
                }
                event.replyEmbeds(new EmbedBuilder()
                        .setImage(target.getEffectiveAvatarUrl() + "?size=512")
                        .setAuthor(target.getEffectiveName(), target.getAvatarUrl())
                        .build()
                ).queue();
            }
        } else {
            if(options.isEmpty()) {
                User target = event.getUser();
                event.replyEmbeds(new EmbedBuilder()
                        .setImage(target.getEffectiveAvatarUrl() + "?size=512")
                        .setAuthor(target.getName(), target.getAvatarUrl())
                        .build()
                ).queue();
            } else {
                User target = event.getOptions().get(0).getAsUser();
                event.replyEmbeds(new EmbedBuilder()
                        .setImage(target.getEffectiveAvatarUrl() + "?size=512")
                        .setAuthor(target.getName(), target.getAvatarUrl())
                        .build()
                ).queue();
            }
        }
    }

    @Override
    public int cooldownInSeconds() {
        return 0;
    }

}
