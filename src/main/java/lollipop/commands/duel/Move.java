package lollipop.commands.duel;

import lollipop.CommandType;
import lollipop.Constant;
import lollipop.Command;
import lollipop.Tools;
import lollipop.commands.duel.models.DMFactory;
import lollipop.commands.duel.models.DMove;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class Move implements Command {
    @Override
    public String[] getAliases() {
        return new String[]{"move"};
    }

    @Override
    public CommandType getCategory() {
        return CommandType.FUN;
    }

    @Override
    public String getHelp() {
        return "Gives detail about a specific move that is available in a duel!\nUsage: `" + Constant.PREFIX + getAliases()[0] + " [move*]`";
    }

    @Override
    public CommandData getSlashCmd() {
        OptionData option = new OptionData(OptionType.STRING, "move", "available move name", true);
        option.addChoice("all", "all");
        for(DMove move : DMFactory.getMoves()) option.addChoice(move.getName(), move.getName());
        return Tools.defaultSlashCmd(this)
                .addOptions(option);
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        final List<OptionMapping> options = event.getOptions();
        String moveName = options.get(0).getAsString();

        if(moveName.equalsIgnoreCase("all")) {
            event.replyEmbeds(
                    new EmbedBuilder()
                            .setTitle("Available Duel Moves")
                            .setDescription(String.join(", ", Arrays.stream(DMFactory.getMoves()).map(DMove::getName).toArray(String[]::new)))
                            .setFooter("Try /move [moveName] to get details on a specific move")
                            .setColor(Color.GREEN)
                            .build()
            ).queue();
            return;
        }
        DMove move = DMFactory.getMove(moveName);
        if(move == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setDescription("I could not find an available move under that name! Please try again with a different input or do `" + Constant.PREFIX + "move all` to get a list of all the available moves!")
                    .setColor(Color.red)
                    .build()
            ).queue();
        } else {
            event.replyEmbeds(new EmbedBuilder()
                            .setTitle(move.getName())
                            .setDescription(move.getDescription())
                            .setFooter("Try /move all to see all available duel moves!")
                            .setColor(Color.GREEN)
                            .build()
            ).queue();
        }
    }
}
