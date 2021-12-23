package lollipop.commands;

import lollipop.CONSTANT;
import lollipop.Command;
import lollipop.Tools;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.List;

public class Onigiri implements Command {
    @Override
    public String[] getAliases() {
        return new String[] {"onigiri"};
    }

    @Override
    public String getCategory() {
        return "Roleplay";
    }

    @Override
    public String getHelp() {
        return "Purgatory Onigiri!\nUsage: `" + CONSTANT.PREFIX + getAliases()[0] + " [user]`";
    }

    @Override
    public void run(List<String> args, MessageReceivedEvent event) {
        if(args.isEmpty()) { Tools.wrongUsage(event.getTextChannel(), this); return; }
        String[] gifs = {"https://tenor.com/view/roronoa-zoro-purgatory-onigiri-zoro-one-piece-gif-19973757", "https://tenor.com/view/onepiece-gif-18892769", "https://tenor.com/view/zorolangmalakasidolo-gif-22253350", "https://tenor.com/view/slt-gif-24007778", "https://tenor.com/view/one-piece-gif-24064405"};
        Member target = Tools.getEffectiveMember(event.getGuild(), String.join(" ", args));
        if(target == null) {
            event.getChannel().sendMessageEmbeds(new EmbedBuilder().setDescription("Could not find the specified member!").setColor(Color.red).build()).queue();
            return;
        }
        event.getChannel().sendMessage("**PURGATORY ONIGIRI!**\n" + target.getAsMention() + " was sliced up by " + event.getMember().getAsMention()).queue();
        event.getChannel().sendMessage(gifs[(int)(Math.random()*gifs.length)]).queue();
    }
}