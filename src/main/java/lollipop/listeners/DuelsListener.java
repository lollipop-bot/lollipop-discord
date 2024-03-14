package lollipop.listeners;

import lollipop.Constant;
import lollipop.commands.duel.Duel;
import lollipop.commands.duel.models.DGame;
import lollipop.commands.duel.models.DMFactory;
import lollipop.commands.duel.models.DMove;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

/**
 * Responds to player actions for lollipop duels
 */
public class DuelsListener extends ListenerAdapter {

    /**
     * Triggered when a button is pressed
     * @param event button interaction event
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if(!event.isFromGuild()) return;
        if(!Duel.memberToGame.containsKey(event.getMember().getIdLong())) {
            event.deferReply().queue();
            return;
        }

        DGame game = Duel.memberToGame.get(event.getMember().getIdLong());

        if(event.getMember().getIdLong() == game.getGuestPlayer().getMember().getIdLong()) {
            event.replyEmbeds(new EmbedBuilder()
                    .setDescription("It is not your turn yet! Please wait for your turn.")
                    .setColor(Color.red)
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        if(Objects.equals(event.getButton().getId(), "accept")) {
            event.deferEdit().queue();

            game.getAcceptTimeout().cancel(false);
            game.initiateGame(event.getMessageChannel());
        }
        else if (Objects.equals(event.getButton().getId(), "decline")) {
            game.getAcceptTimeout().cancel(false);
            event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                    .setDescription(game.getGuestPlayer().getMember().getAsMention() + " did not accept youre duel request...")
                    .setColor(Color.red)
                    .build()).queue();
            game.denyDuelRequest(event);
        }
        else {
            if(event.getMessage().getIdLong() != game.getDisplayMessage().getIdLong()) return;

            if(event.getMember() != game.getTurnPlayer().getMember() && !event.getButton().getLabel().equals("surrender")) {
                event.replyEmbeds(new EmbedBuilder()
                        .setDescription("**It is not your turn! Please wait for the other player to finish his turn!**")
                        .setColor(Color.red)
                        .setFooter("If you have not started a duel yet, you can do so by typing " + Constant.PREFIX + "duel")
                        .build()
                ).setEphemeral(true).queue();
                return;
            }

            game.getGameTimeout().cancel(false);
            event.deferEdit().queue();

            String moveName = event.getButton().getLabel();
            DMove move = DMFactory.getMove(moveName);

            assert move != null;
            game.playTurn(move);
        }
    }

}
