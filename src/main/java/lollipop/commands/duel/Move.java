package lollipop.commands.duel;

import lollipop.CommandType;
import lollipop.Constant;
import lollipop.Command;
import lollipop.Tools;
import lollipop.commands.duel.models.DMFactory;
import lollipop.commands.duel.models.DMType;
import lollipop.commands.duel.models.DMove;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        return "Learn about the available moves and move types in the lollipop duel system.\n" +
                "In the move field, choose `all` to get a list of all available moves, or choose a specific move to get a detailed description\n" +
                "Usage: `" + Constant.PREFIX + getAliases()[0] + " [move]`";
    }

    @Override
    public CommandData getSlashCmd() {
        OptionData option = new OptionData(OptionType.STRING, "move", "Select the move you want to learn about.", true);
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
            ArrayList<DMove> moves = Arrays.stream(DMFactory.getMoves()).collect(Collectors.toCollection(ArrayList::new));
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Available Duel Moves")
                    .setDescription(
                            """
                            **ATTACK** moves deal minimum damage to the opponent or break opponent blocks.
                            **DEFENSE** moves defend from future blockable attacks from the opponent. (Blocks break after an attack)
                            **HEAL** moves heal the turn player and boosts their HP.
                            **STRENGTH** moves increase the turn player's SP and helps them deal more damage in their future attacks.
                            **ULTIMATE** moves are rare moves that can turn the game around with their unique functionality.
                            """
                    )
                    .addField("Attack Moves", "`" + String.join("`, `", moves.stream().filter(move -> move.getType()==DMType.ATTACK).map(DMove::getName).toArray(String[]::new)) + "`", false)
                    .addField("Defense Moves", "`" + String.join("`, `", moves.stream().filter(move -> move.getType()==DMType.DEFENSE).map(DMove::getName).toArray(String[]::new)) + "`", false)
                    .addField("Healing Moves", "`" + String.join("`, `", moves.stream().filter(move -> move.getType()==DMType.HEAL).map(DMove::getName).toArray(String[]::new)) + "`", false)
                    .addField("Strength Moves", "`" + String.join("`, `", moves.stream().filter(move -> move.getType()==DMType.STRENGTH).map(DMove::getName).toArray(String[]::new)) + "`", false)
                    .addField("Ultimate Moves", "`" + String.join("`, `", moves.stream().filter(move -> move.getType()==DMType.ULTIMATE).map(DMove::getName).toArray(String[]::new)) + "`", false)
                    .setFooter("Try `/move move:[move name]` to get details on a specific move")
                    .setColor(Color.GREEN);
            event.replyEmbeds(embedBuilder.build()).queue();
            return;
        }
        DMove move = DMFactory.getMove(moveName);
        event.replyEmbeds(new EmbedBuilder()
                .setTitle(move.getName())
                .setDescription(move.getDescription())
                .setFooter("Try `/move move:all` to see all available duel moves!")
                .setImage(move.getGif())
                .setColor(Color.GREEN)
                .build()
        ).queue();
    }

    @Override
    public int cooldownDuration() {
        return 5;
    }
}
