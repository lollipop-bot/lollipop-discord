package lollipop.commands;

import lollipop.CommandType;
import lollipop.Constant;
import lollipop.Command;
import lollipop.Tools;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class BitesTheDust implements Command {

    @Override
    public String[] getAliases() {
        return new String[] {"biteszadust", "btd"};
    }

    @Override
    public CommandType getCategory() {
        return CommandType.FUN;
    }

    @Override
    public String getHelp() {
        return "Go back in time and delete your most recent messages!\n" +
                "`/biteszadust` will work only delete the user's messages in the most recent 30 messages in the same channel\n" +
                "It will not work across multiple channels and can be used to clean your most recent messages only\n" +
                "If you haven't been active in the past 30 messages, nothing will happen\n" +
                "Usage: `" + Constant.PREFIX + getAliases()[0] + "`";
    }

    @Override
    public CommandData getSlashCmd() {
        return Tools.defaultSlashCmd(this);
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

        if (event.isFromGuild() && !event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.replyEmbeds(
                    new EmbedBuilder()
                            .setColor(Color.red)
                            .setDescription("This command can't be used because I don't have the `MESSAGE_MANAGE` permission in this server!")
                            .build()
            ).queue();
            return;
        }
        List<Message> msgList = event.getChannel().getHistory().retrievePast(31).complete()
                .stream().filter(m -> m.getAuthor().getIdLong() == event.getUser().getIdLong()).collect(Collectors.toList());

        if(msgList.isEmpty()) {
            event.reply("You haven't done anything recently to travel back in time!").setEphemeral(true).queue();
            return;
        }
        event.getChannel().purgeMessages(msgList);
        event.reply("Successfully travelled back 30 messages in time without leaving any traces behind!").setEphemeral(true).queue();
    }

}
