package lollipop.commands.duel.models;

import lollipop.Constant;
import lollipop.commands.duel.Duel;
import lollipop.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DGame {

    private final DPlayer homePlayer;
    private final DPlayer guestPlayer;

    private DPlayer turnPlayer;
    private DPlayer idlePlayer;

    private ScheduledFuture<?> acceptTimeout;
    private ScheduledFuture<?> gameTimeout;

    private InteractionHook mentionMessage;
    private Message requestMessage;
    private Message displayMessage;


    private final String[] victoryMsg = {"You are too strong...", "That kind of power should be illegal!", "You are a god amongst men!", "How did you get so much power?", "Nobody dares to duel with you!"};
    private final String cpuAvatar = "https://www.pngkey.com/png/full/0-8970_open-my-computer-icon-circle.png";
    private final String lollipopAvatar = "https://www.dictionary.com/e/wp-content/uploads/2018/11/lollipop-emoji.png";
    private final String cpuLink = "https://github.com/lollipop-bot/lollipop-discord/blob/main/src/main/java/lollipop/commands/duel/models/DCPUAI.java";
    private final String startGifLink = "https://c.tenor.com/2C1HHrXGzbMAAAAC/anime-fight.gif";
    private final String fleeGifLink = "https://media.tenor.com/Ws_QII5jXlQAAAAC/anime-jjba.gif";
    private final String endGifLink = "https://c.tenor.com/1WSdEj1xWfAAAAAd/one-punch-man-anime.gif";
    private final int moveCount = 3;

    public DGame(DPlayer homePlayer, DPlayer guestPlayer) {
        this.homePlayer = homePlayer;
        this.guestPlayer = guestPlayer;

        this.turnPlayer = homePlayer;
        this.idlePlayer = guestPlayer;
    }

    public DPlayer getHomePlayer() {
        return this.homePlayer;
    }

    public DPlayer getGuestPlayer() {
        return this.guestPlayer;
    }

    public DPlayer getTurnPlayer() {
        return this.turnPlayer;
    }

    public DPlayer getIdlePlayer() {
        return this.idlePlayer;
    }

    public ScheduledFuture<?> getAcceptTimeout() {
        return acceptTimeout;
    }

    public ScheduledFuture<?> getGameTimeout() {
        return gameTimeout;
    }

    public Message getDisplayMessage() {
        return this.displayMessage;
    }

    public InteractionHook getMentionMessage() {
        return this.mentionMessage;
    }

    public void setMentionMessage(InteractionHook mentionMessage) {
        this.mentionMessage = mentionMessage;
    }

    public Message getRequestMessage() {
        return this.requestMessage;
    }

    public void setRequestMessage(Message requestMessage) {
        this.requestMessage = requestMessage;
    }

    public void sendDuelRequest(SlashCommandInteractionEvent event) {
        event.reply(guestPlayer.getMember().getAsMention()).queue(this::setMentionMessage);
        event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                .setDescription(homePlayer.getMember().getAsMention() + " requested to duel you! Do you accept their duel request?")
                .setFooter("Quick! You have 30 seconds to accept!")
                .build()
        ).setActionRow(
                Button.primary("accept", "accept"),
                Button.danger("deny", "deny")
        ).queue(this::setRequestMessage);

        this.acceptTimeout = event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                .setDescription(guestPlayer.getMember().getAsMention() + " didn't arrive in time! The duel request expired...")
                .setColor(Color.red)
                .build()
        ).queueAfter(30, TimeUnit.SECONDS, m -> {
            denyDuelRequest(event);
        });
    }

    public void denyDuelRequest(GenericInteractionCreateEvent event)
    {
        this.getMentionMessage().deleteOriginal().queue();
        this.getRequestMessage().delete().queue();
        Duel.memberToGame.remove(homePlayer.getMember().getIdLong());
        Duel.memberToGame.remove(guestPlayer.getMember().getIdLong());
        Duel.occupiedShards[event.getJDA().getShardInfo().getShardId()]--;
    }
    public void initiateGame(TextChannel channel) {
        if(this.getMentionMessage() != null) this.getMentionMessage().deleteOriginal().queue();
        if(this.getRequestMessage() != null) this.getRequestMessage().delete().queue();

        DMove[] options = new DMove[moveCount+1];
        System.arraycopy(DMFactory.getMoves(), 0, options, 0, moveCount);
        options[moveCount] = DMFactory.getSurrender();

        this.displayMessage = channel.sendMessageEmbeds(
                new EmbedBuilder()
                        .setAuthor(turnPlayer.getName() + "'s turn", Constant.WEBSITE, this.turnPlayer.getMember().getEffectiveAvatarUrl())
                        .setTitle(homePlayer.getName() + " vs " + guestPlayer.getName())
                        .setDescription("`" + guestPlayer.getName() + "` accepted the duel request!\nLet the duel begin! ⚔️")
                        .addField(
                                homePlayer.getName(),
                                "> Health: `" + this.homePlayer.getHP() + " HP`\n> Strength: `" + this.homePlayer.getSP() + " SP`",
                                true
                        ).addField(
                                guestPlayer.getName(),
                                "> Health: `" + this.guestPlayer.getHP() + " HP`\n> Strength: `" + this.guestPlayer.getSP() + " SP`",
                                true
                        )
                        .setImage(startGifLink)
                        .setFooter("Choose what you want to play... You have 30 seconds to react!")
                        .build()
        ).setActionRow(Arrays.stream(options).map(DMove::getButton).toArray(Button[]::new)).complete();

        this.setupGameTimeout(channel);
    }

    public void playTurn(DMove move) {
        TextChannel channel = this.displayMessage.getTextChannel();

        String turnDescription = performMove(move);
        String moveGif = move.getGif();
        if(turnDescription == null) return;

        if(this.checkWin(channel)) return;

        boolean isTimedOut = this.idlePlayer.isTimedOut();
        if (!isTimedOut) this.switchPlayerTurns();

        if(turnPlayer.isCPU()) {
            turnDescription += "\n\n";

            DMove[] options = DMFactory.generateRandomMoves(moveCount);
            DMove cpuMove = DCPUAI.minimax(this, options);

            turnDescription += performMove(cpuMove);
            moveGif = cpuMove.getGif();

            if(this.checkWin(channel)) return;

            boolean isPlayerTimedOut = this.idlePlayer.isTimedOut();
            if (!isPlayerTimedOut) this.switchPlayerTurns();
            else {
                this.turnPlayer.setTimeoutDuration(0);
                int turns = (int) (2 + Math.random()*2);

                for(int i=0; i<turns; i++) {
                    turnDescription += "\n\n";

                    options = DMFactory.generateRandomMoves(moveCount);
                    cpuMove = DCPUAI.minimax(this, options);
                    while(cpuMove.getName().equals("za warudo")) {
                        options = DMFactory.generateRandomMoves(moveCount);
                        cpuMove = DCPUAI.minimax(this, options);
                    }

                    turnDescription += performMove(cpuMove);

                    if(this.checkWin(channel)) return;
                }
            }
        }

        // in case of some errors with switching for message sending
        if(turnPlayer.isCPU()) this.switchPlayerTurns();

        DMove[] options = new DMove[moveCount + 1];
        System.arraycopy(DMFactory.generateRandomMoves(moveCount), 0, options, 0, moveCount);
        options[moveCount] = DMFactory.getSurrender();

        MessageEmbed messageEmbed = new EmbedBuilder()
                .setAuthor(turnPlayer.getName() + "'s turn", Constant.WEBSITE, this.turnPlayer.getMember().getEffectiveAvatarUrl())
                .setTitle(homePlayer.getName() + " vs " + guestPlayer.getName())
                .setDescription(turnDescription)
                .addField(
                        homePlayer.getName(),
                        "> Health: `" + this.homePlayer.getHP() + " HP`\n> Strength: `" + this.homePlayer.getSP() + " SP`",
                        true
                ).addField(
                        guestPlayer.getName(),
                        "> Health: `" + this.guestPlayer.getHP() + " HP`\n> Strength: `" + this.guestPlayer.getSP() + " SP`",
                        true
                )
                .setImage(moveGif)
                .setFooter("Choose what you want to play... You have 30 seconds to react!")
                .build();

        if (!isTimedOut) {
            this.displayMessage.editMessageEmbeds(messageEmbed)
                    .setActionRow(
                            Arrays.stream(options)
                                    .map(DMove::getButton)
                                    .map(Button::asDisabled)
                                    .toArray(Button[]::new)
                    ).queue();
            this.displayMessage.editMessageComponents()
                    .setActionRow(
                            Arrays.stream(options)
                                    .map(DMove::getButton)
                                    .toArray(Button[]::new)
                    ).queueAfter(5, TimeUnit.SECONDS);
        } else {
            this.displayMessage.editMessageEmbeds(messageEmbed)
                    .setActionRow(
                            Arrays.stream(options)
                                    .map(DMove::getButton)
                                    .toArray(Button[]::new)
                    ).queue();
        }

        this.setupGameTimeout(channel);
    }

    private String performMove(DMove move) {
        String turnDescription = "vote for lollipop or gae";

        switch (move.getName()) {
            case "surrender" -> {
                TextChannel channel = this.displayMessage.getTextChannel();

                if (this.gameTimeout != null) this.gameTimeout.cancel(false);
                this.displayMessage.delete().queue();
                Duel.memberToGame.remove(turnPlayer.getMember().getIdLong());
                Duel.occupiedShards[channel.getJDA().getShardInfo().getShardId()]--;

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setColor(Color.green)
                        .setImage(move.getGif())
                        .setDescription(String.format(move.getPhrase(), turnPlayer.getName()))
                        .setFooter("Type " + Constant.PREFIX + "duel to start another duel with me!");

                int lxp = homePlayer.hasMultiplier() ? (int)(((int)(Math.random()*11)-80)/Constant.MULTIPLIER) : (int)(Math.random()*11)-80;
                Database.addToUserBalance(homePlayer.getMember().getId(), lxp);

                if (idlePlayer.isCPU()) {
                    int lastRating = DCPUAI.getDuelsRating(channel.getJDA().getShardInfo().getShardId());
                    DCPUAI.updateRating(this.displayMessage.getJDA(), true, homePlayer.getHP(), guestPlayer.getHP());
                    int currentRating = DCPUAI.getDuelsRating(channel.getJDA().getShardInfo().getShardId());
                    char ratingChange = ' ';
                    if(lastRating>=currentRating)
                        ratingChange = '-';
                    else
                        ratingChange = '+';
                    embedBuilder.setAuthor(idlePlayer.getName() + " won the duel!", cpuLink, cpuAvatar);
                    embedBuilder.setFooter("New AI rating: " + currentRating + " ("+ ratingChange + Math.abs(currentRating - lastRating) +")"+ "\n"+ turnPlayer.getName() + " lost " + -lxp + " lollipops", lollipopAvatar);
                } else {
                    Duel.memberToGame.remove(idlePlayer.getMember().getIdLong());

                    int xp = guestPlayer.hasMultiplier() ? (int)((int)(Math.random()*31)+70*Constant.MULTIPLIER) : (int)(Math.random()*31)+70;
                    Database.addToUserBalance(guestPlayer.getMember().getId(), xp);

                    embedBuilder.setAuthor(idlePlayer.getName() + " won the duel!", Constant.WEBSITE, idlePlayer.getMember().getEffectiveAvatarUrl());
                    embedBuilder.setFooter(idlePlayer.getName() + " gained " + xp + " lollipops / " + turnPlayer.getName() + " lost " + -lxp + " lollipops", lollipopAvatar);
                }

                channel.sendMessageEmbeds(embedBuilder.build()).queue();

                return null;
            }
            case "punch", "kick", "headbutt", "chop" -> {
                if (this.idlePlayer.isDefending()) {
                    turnDescription = String.format(move.getBlockPhrase(), idlePlayer.getName(), turnPlayer.getName());
                    this.idlePlayer.setDefending(false);
                } else {
                    int damage = (int) (Math.random() * 6) + 5 + this.turnPlayer.getSP();
                    this.idlePlayer.updateHP(-damage);
                    turnDescription = String.format(move.getPhrase(), turnPlayer.getName(), damage);
                }
            }
            case "eat" -> {
                int health = (int) (Math.random() * 11) + 20;
                this.turnPlayer.updateHP(health);
                turnDescription = String.format(move.getPhrase(), turnPlayer.getName(), health);
            }
            case "breathe" -> {
                int strength = (int) (Math.random() * 3) + 3;
                this.turnPlayer.updateSP(strength);
                turnDescription = String.format(move.getPhrase(), turnPlayer.getName(), strength);
            }
            case "block", "shield" -> {
                turnDescription = String.format(move.getPhrase(), turnPlayer.getName());
                this.turnPlayer.setDefending(true);
            }
            case "4th gear", "hinokami kagura" -> {
                if (this.idlePlayer.isDefending()) {
                    turnDescription = String.format(move.getBlockPhrase(), idlePlayer.getName(), turnPlayer.getName());
                    this.idlePlayer.setDefending(false);
                } else {
                    int damage = (int) (Math.random() * 6) + 13 + this.turnPlayer.getSP();
                    this.idlePlayer.updateHP(-damage);
                    turnDescription = String.format(move.getPhrase(), turnPlayer.getName(), damage);
                }
            }
            case "rasengan" -> {
                if (this.idlePlayer.isDefending()) {
                    turnDescription = String.format(move.getBlockPhrase(), idlePlayer.getName(), turnPlayer.getName());
                    this.idlePlayer.setDefending(false);
                } else {
                    int damage = (int) (Math.random() * 6) + 14 + this.turnPlayer.getSP();
                    this.idlePlayer.updateHP(-damage);
                    turnDescription = String.format(move.getPhrase(), turnPlayer.getName(), damage);
                }
            }
            case "ora" -> {
                if (this.idlePlayer.isDefending()) {
                    turnDescription = String.format(move.getBlockPhrase(), idlePlayer.getName(), turnPlayer.getName());
                    this.idlePlayer.setDefending(false);
                } else {
                    int damage = (int) (Math.random() * 6) + 15 + this.turnPlayer.getSP();
                    this.idlePlayer.updateHP(-damage);
                    turnDescription = String.format(move.getPhrase(), turnPlayer.getName(), damage);
                }
            }
            case "us smash" -> {
                if (this.idlePlayer.isDefending()) {
                    turnDescription = String.format(move.getBlockPhrase(), idlePlayer.getName(), turnPlayer.getName());
                    this.idlePlayer.setDefending(false);
                } else {
                    int damage = (int) (Math.random() * 6) + 16 + this.turnPlayer.getSP();
                    this.idlePlayer.updateHP(-damage);
                    turnDescription = String.format(move.getPhrase(), turnPlayer.getName(), damage);
                }
            }
            case "100%" -> {
                int strength = (int) (Math.random() * 5) + 6;
                this.turnPlayer.updateSP(strength);
                turnDescription = String.format(move.getPhrase(), turnPlayer.getName(), strength);
            }
            case "serious punch" -> {
                if (this.idlePlayer.isDefending()) {
                    int damage = (int) (Math.random() * 11) + 20 + this.turnPlayer.getSP();
                    this.idlePlayer.updateHP(-damage);
                    turnDescription = String.format(move.getPhrase(), turnPlayer.getName(), damage);
                    this.idlePlayer.setDefending(false);
                } else {
                    int damage = (int) (Math.random() * 11) + 40 + this.turnPlayer.getSP();
                    this.idlePlayer.updateHP(-damage);
                    turnDescription = String.format(move.getPhrase(), turnPlayer.getName(), damage);
                }
            }
            case "za warudo" -> {
                this.idlePlayer.setTimeoutStart(System.currentTimeMillis());
                this.idlePlayer.setTimeoutDuration(3);
                turnDescription = String.format(move.getPhrase(), turnPlayer.getName());
            }
            case "yare yare daze" -> {
                int damage = (int) (Math.random() * 6) + 15 + this.turnPlayer.getSP();
                int strength = (int) (Math.random() * 11) + 5;
                this.idlePlayer.updateSP(-strength);
                this.idlePlayer.updateHP(-damage);
                turnDescription = String.format(move.getPhrase(), turnPlayer.getName(), strength, damage);
                this.idlePlayer.setDefending(false);
            }
        }

        homePlayer.setHP(Math.max(0, homePlayer.getHP()));
        homePlayer.setSP(Math.max(-5, homePlayer.getSP()));
        guestPlayer.setHP(Math.max(0, guestPlayer.getHP()));
        guestPlayer.setSP(Math.max(-5, guestPlayer.getSP()));

        return turnDescription;
    }

    private void setupGameTimeout(TextChannel channel) {
        if(this.gameTimeout != null && !this.gameTimeout.isCancelled()) this.gameTimeout.cancel(false);

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.green)
                .setImage(fleeGifLink)
                .setAuthor(idlePlayer.getName() + " won the duel!", cpuLink, cpuAvatar)
                .setTitle(victoryMsg[(int)(Math.random()*victoryMsg.length)])
                .setDescription(turnPlayer.getName() + " fled and abandoned the duel game...");

        int lxp = turnPlayer.hasMultiplier() ? (int)((int)(Math.random()*11)-80/Constant.MULTIPLIER) : (int)(Math.random()*11)-80;

        if(idlePlayer.isCPU()) {
            embedBuilder.setFooter(turnPlayer.getName() + " lost " + -lxp + " lollipops", lollipopAvatar);
            this.gameTimeout = channel.sendMessageEmbeds(embedBuilder.build()).queueAfter(30, TimeUnit.SECONDS, me -> {
                DCPUAI.updateRating(this.displayMessage.getJDA(), true, homePlayer.getHP(), guestPlayer.getHP());
                Database.addToUserBalance(turnPlayer.getMember().getId(), lxp);
                Duel.memberToGame.remove(turnPlayer.getMember().getIdLong());
                Duel.occupiedShards[channel.getJDA().getShardInfo().getShardId()]--;
                this.displayMessage.delete().queue();
            });
        } else {
            int xp = idlePlayer.hasMultiplier() ? (int)((int)(Math.random()*31)+70*Constant.MULTIPLIER) : (int)(Math.random()*31)+70;
            embedBuilder.setFooter(idlePlayer.getName() + " gained " + xp + " lollipops / " + turnPlayer.getName() + " lost " + -lxp + " lollipops", lollipopAvatar);
            this.gameTimeout = channel.sendMessageEmbeds(embedBuilder.build()).queueAfter(30, TimeUnit.SECONDS, me -> {
                Database.addToUserBalance(idlePlayer.getMember().getId(), xp);
                Database.addToUserBalance(turnPlayer.getMember().getId(), lxp);
                Duel.memberToGame.remove(turnPlayer.getMember().getIdLong());
                Duel.memberToGame.remove(idlePlayer.getMember().getIdLong());
                Duel.occupiedShards[channel.getJDA().getShardInfo().getShardId()]--;
                this.displayMessage.delete().queue();
            });
        }
    }

    private void switchPlayerTurns() {
        DPlayer temp = this.turnPlayer;
        this.turnPlayer = this.idlePlayer;
        this.idlePlayer = temp;
    }

    public boolean checkWin(TextChannel channel) {
        if(homePlayer.getHP() <= 0) {
            this.gameTimeout.cancel(false);
            this.displayMessage.delete().queue();
            Duel.occupiedShards[channel.getJDA().getShardInfo().getShardId()]--;
            Duel.memberToGame.remove(homePlayer.getMember().getIdLong());

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setColor(Color.green)
                    .setImage(endGifLink)
                    .setAuthor(guestPlayer.getName() + " won the duel!", cpuLink, cpuAvatar)
                    .setTitle(victoryMsg[(int)(Math.random()*victoryMsg.length)])
                    .setDescription(
                            "> " + homePlayer.getName() + "'s Health: `0 HP`\n" +
                            "> " + guestPlayer.getName() + "'s Health: `" + guestPlayer.getHP() + " HP`"
                    )
                    .setFooter("Type " + Constant.PREFIX + "duel to start another duel with me!");

            int lxp = homePlayer.hasMultiplier() ? (int)(((int)(Math.random()*11)-80)/Constant.MULTIPLIER) : (int)(Math.random()*11)-80;
            Database.addToUserBalance(homePlayer.getMember().getId(), lxp);

            if(guestPlayer.isCPU()) {
                int lastRating = DCPUAI.getDuelsRating(channel.getJDA().getShardInfo().getShardId());
                DCPUAI.updateRating(this.displayMessage.getJDA(), true, homePlayer.getHP(), guestPlayer.getHP());
                int currentRating = DCPUAI.getDuelsRating(channel.getJDA().getShardInfo().getShardId());
                char ratingChange = ' ';
                if(lastRating>=currentRating)
                    ratingChange = '-';
                else
                    ratingChange = '+';
                embedBuilder.setFooter("New AI rating: " + currentRating + " ("+ ratingChange + Math.abs(currentRating - lastRating) +")"+ "\n"+ homePlayer.getName() + " lost " + -lxp + " lollipops", lollipopAvatar);
            } else {
                Duel.memberToGame.remove(guestPlayer.getMember().getIdLong());

                int xp = guestPlayer.hasMultiplier() ? (int)((int)(Math.random()*31)+70*Constant.MULTIPLIER) : (int)(Math.random()*31)+70;
                Database.addToUserBalance(guestPlayer.getMember().getId(), xp);
                embedBuilder.setFooter(guestPlayer.getName() + " gained " + xp + " lollipops / " + homePlayer.getName() + " lost " + -lxp + " lollipops", lollipopAvatar);
            }

            channel.sendMessageEmbeds(embedBuilder.build()).queue();

            return true;
        } else if(guestPlayer.getHP() <= 0) {
            this.gameTimeout.cancel(false);
            this.displayMessage.delete().queue();
            Duel.occupiedShards[channel.getJDA().getShardInfo().getShardId()]--;
            Duel.memberToGame.remove(homePlayer.getMember().getIdLong());
            int lastRating = -1;
            if(!guestPlayer.isCPU()) Duel.memberToGame.remove(guestPlayer.getMember().getIdLong());
            else {
                lastRating = DCPUAI.getDuelsRating(channel.getJDA().getShardInfo().getShardId());
                DCPUAI.updateRating(this.displayMessage.getJDA(), false, homePlayer.getHP(), guestPlayer.getHP());
            }

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setColor(Color.green)
                    .setAuthor(homePlayer.getName() + " won the duel!", Constant.WEBSITE, homePlayer.getMember().getEffectiveAvatarUrl())
                    .setTitle(victoryMsg[(int)(Math.random()*victoryMsg.length)])
                    .setImage(endGifLink)
                    .setDescription(
                            "> " + homePlayer.getName() + "'s Health: `" + homePlayer.getHP() + " HP`\n" +
                            "> " + guestPlayer.getName() + "'s Health: `0 HP`"
                    );

            int xp = homePlayer.hasMultiplier() ? (int)((int)(Math.random()*31)+70*Constant.MULTIPLIER) : (int)(Math.random()*31)+70;
            Database.addToUserBalance(homePlayer.getMember().getId(), xp);
            if(guestPlayer.isCPU()) {
                int currentRating = DCPUAI.getDuelsRating(channel.getJDA().getShardInfo().getShardId());
                char ratingChange = ' ';
                if(lastRating>=currentRating)
                    ratingChange = '-';
                else
                    ratingChange = '+';
                embedBuilder.setFooter("New AI rating: " + currentRating + " ("+ ratingChange + Math.abs(currentRating - lastRating) +")"+ "\n"+ homePlayer.getName() + " gained " + xp + " lollipops", lollipopAvatar);
            } else {
                int lxp = guestPlayer.hasMultiplier() ? (int) ((int) (Math.random() * 11) - 80 / Constant.MULTIPLIER) : (int) (Math.random() * 11) - 80;
                Database.addToUserBalance(guestPlayer.getMember().getId(), lxp);
                embedBuilder.setFooter(homePlayer.getName() + " gained " + xp + " lollipops / " + guestPlayer.getName() + " lost " + -lxp + " lollipops", lollipopAvatar);
            }
            channel.sendMessageEmbeds(embedBuilder.build()).queue();
            return true;
        }
        return false;
    }


}
