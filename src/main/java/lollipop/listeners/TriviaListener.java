package lollipop.listeners;

import lollipop.BotStatistics;
import lollipop.Constant;
import lollipop.commands.trivia.Trivia;
import lollipop.commands.trivia.models.TGame;
import lollipop.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collections;
import java.util.Objects;

public class TriviaListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if(!event.isFromGuild()) return;
        if(Objects.requireNonNull(event.getUser()).isBot()) return;

        long id = event.getMessageIdLong();

        if(Trivia.openGames.containsKey(id)) {
            TGame game = Trivia.openGames.get(id);
            game.gameTimeout.cancel(false);
            if(event.getUser() != game.user) {
                event.reply("You can't use the buttons because you didn't use this command! Use the `top` command to be able to use buttons!").setEphemeral(true).queue();
                return;
            }
            if(Objects.equals(event.getButton().getId(), "right")) {
                Runnable success = () -> {
                    int xp = (int)(Math.random()*21)+40;
                    xp = (int)(xp* Constant.MULTIPLIER);
                    Database.addToUserBalance(event.getUser().getId(), xp);
                    event.editMessageEmbeds(
                            new EmbedBuilder()
                                    .setTitle("Correct Answer!")
                                    .setColor(Color.green)
                                    .setDescription("You guessed the correct anime!")
                                    .setThumbnail("https://cdn.discordapp.com/emojis/738541796174594108.webp?size=80&quality=lossless")
                                    .setFooter("You won " + xp + " lollipops!", "https://www.dictionary.com/e/wp-content/uploads/2018/11/lollipop-emoji.png")
                                    .build()
                    ).setActionRows(Collections.emptyList()).queue();
                };
                Runnable failure = () -> {
                    int xp = (int)(Math.random()*21)+40;
                    Database.addToUserBalance(event.getUser().getId(), xp);
                    event.editMessageEmbeds(
                            new EmbedBuilder()
                                    .setTitle("Correct Answer!")
                                    .setColor(Color.green)
                                    .setDescription("You guessed the correct anime!")
                                    .setThumbnail("https://cdn.discordapp.com/emojis/738541796174594108.webp?size=80&quality=lossless")
                                    .setFooter("You won " + xp + " lollipops!", "https://www.dictionary.com/e/wp-content/uploads/2018/11/lollipop-emoji.png")
                                    .build()
                    ).setActionRows(Collections.emptyList()).queue();
                };
                BotStatistics.sendMultiplier(game.user.getId(), success, failure);
            } else {
                Runnable success = () -> {
                    int xp = (int)(Math.random()*11)-20;
                    xp = (int)(xp/Constant.MULTIPLIER);
                    Database.addToUserBalance(event.getUser().getId(), xp);
                    event.editMessageEmbeds(
                            new EmbedBuilder()
                                    .setTitle("Wrong Answer!")
                                    .setColor(Color.red)
                                    .setDescription("You guessed the wrong anime!")
                                    .setThumbnail("https://cdn.discordapp.com/emojis/886080067195772970.webp?size=80&quality=lossless")
                                    .setFooter("You lost " + (-1*xp) + " lollipops!", "https://www.dictionary.com/e/wp-content/uploads/2018/11/lollipop-emoji.png")
                                    .build()
                    ).setActionRows(Collections.emptyList()).queue();
                };
                Runnable failure = () -> {
                    int xp = (int)(Math.random()*11)-20;
                    Database.addToUserBalance(event.getUser().getId(), xp);
                    event.editMessageEmbeds(
                            new EmbedBuilder()
                                    .setTitle("Wrong Answer!")
                                    .setColor(Color.red)
                                    .setDescription("You guessed the wrong anime!")
                                    .setThumbnail("https://cdn.discordapp.com/emojis/886080067195772970.webp?size=80&quality=lossless")
                                    .setFooter("You lost " + (-1*xp) + " lollipops!", "https://www.dictionary.com/e/wp-content/uploads/2018/11/lollipop-emoji.png")
                                    .build()
                    ).setActionRows(Collections.emptyList()).queue();
                };
                BotStatistics.sendMultiplier(game.user.getId(), success, failure);
            }
            Trivia.openGames.remove(id);
        }
    }
}
