package lollipop.commands.duel.models;

import lollipop.Constant;
import lollipop.commands.duel.Duel;
import lollipop.database.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DGamePT {

    private final DPlayerPT homePlayer;
    private final DPlayerPT guestPlayer;

    private DPlayerPT turnPlayer;
    private DPlayerPT idlePlayer;

    private ScheduledFuture<?> acceptTimeout;
    private ScheduledFuture<?> gameTimeout;

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

    public DGamePT(DPlayerPT homePlayer, DPlayerPT guestPlayer) {
        this.homePlayer = homePlayer;
        this.guestPlayer = guestPlayer;

        this.turnPlayer = homePlayer;
        this.idlePlayer = guestPlayer;
    }

    public DPlayerPT getHomePlayer() {
        return this.homePlayer;
    }

    public DPlayerPT getGuestPlayer() {
        return this.guestPlayer;
    }

    public DPlayerPT getTurnPlayer() {
        return this.turnPlayer;
    }

    public DPlayerPT getIdlePlayer() {
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

    public Message getRequestMessage() {
        return this.requestMessage;
    }

    public void setRequestMessage(Message requestMessage) {
        this.requestMessage = requestMessage;
    }

    public void sendDuelRequest(SlashCommandInteractionEvent event) {
        event.reply(guestPlayer.getMember().getAsMention()).complete();
        event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                .setDescription(homePlayer.getMember().getAsMention() + " requested to duel you! Do you accept their duel request?")
                .setFooter("Quick! You have 30 seconds to accept!")
                .build()
        ).setActionRow(
                Button.primary("accept", "accept")
        ).queue(this::setRequestMessage);

        this.acceptTimeout = event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                .setDescription(guestPlayer.getMember().getAsMention() + " didn't arrive in time! The duel request expired...")
                .setColor(Color.red)
                .build()
        ).queueAfter(30, TimeUnit.SECONDS, m -> {
            this.getRequestMessage().delete().queue();
            Duel.memberToGame.remove(homePlayer.getMember().getIdLong());
            Duel.memberToGame.remove(guestPlayer.getMember().getIdLong());
            Duel.occupiedShards[event.getJDA().getShardInfo().getShardId()]--;
        });
    }

    public void initiateGame(TextChannel channel) {
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
    }

    public void playTurn(DMove move) {
        TextChannel channel = this.displayMessage.getTextChannel();

        DMove[] options = new DMove[moveCount + 1];
        System.arraycopy(DMFactory.generateRandomMoves(moveCount), 0, options, 0, moveCount);
        options[moveCount] = DMFactory.getSurrender();

        String turnDescription = "";

        switch (move.getName()) {
            case "surrender" -> {
                if (this.gameTimeout != null) this.gameTimeout.cancel(false);
                this.displayMessage.delete().queue();
                Duel.memberToGame.remove(turnPlayer.getMember().getIdLong());
                Duel.occupiedShards[channel.getJDA().getShardInfo().getShardId()]--;
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(Color.green)
                        .setImage(endGifLink)
                        .setFooter("Type " + Constant.PREFIX + "duel to start another duel with me!");
                if (idlePlayer.isCPU()) {
                    embed.setAuthor("Computer won the duel!", cpuLink, cpuAvatar);
                    embed.setDescription(String.format(move.getDescription(), turnPlayer.getName()));
                    if (turnPlayer.hasMultiplier()) {
                        int xp = (int) (Math.random() * 11) - 20;
                        xp = (int) (xp / Constant.MULTIPLIER);
                        Database.addToUserBalance(turnPlayer.getMember().getId(), xp);
                        embed.setFooter(turnPlayer.getMember().getEffectiveName() + " lost " + (-1 * xp) + " lollipops!", lollipopAvatar);
                        channel.sendMessageEmbeds(embed.build()).queue();
                    } else {
                        int xp = (int) (Math.random() * 11) - 20;
                        Database.addToUserBalance(turnPlayer.getMember().getId(), xp);
                        embed.setFooter(turnPlayer.getMember().getEffectiveName() + " lost " + (-1 * xp) + " lollipops!", lollipopAvatar);
                        channel.sendMessageEmbeds(embed.build()).queue();
                    }
                } else {
                    embed.setAuthor(idlePlayer.getName() + " won the duel!", Constant.WEBSITE, idlePlayer.getMember().getUser().getEffectiveAvatarUrl());
                    embed.setDescription(String.format(move.getDescription(), turnPlayer.getName()));
                    Duel.memberToGame.remove(idlePlayer.getMember().getIdLong());
                    if (idlePlayer.hasMultiplier()) {
                        int xp = (int) (Math.random() * 31) + 70;
                        xp = (int) (xp * Constant.MULTIPLIER);
                        Database.addToUserBalance(idlePlayer.getMember().getId(), xp);
                        embed.setFooter(idlePlayer.getName() + " gained " + xp + " lollipops!", lollipopAvatar);
                        channel.sendMessageEmbeds(embed.build()).queue();
                    } else {
                        int xp = (int) (Math.random() * 31) + 70;
                        Database.addToUserBalance(idlePlayer.getMember().getId(), xp);
                        embed.setFooter(idlePlayer.getName() + " gained " + xp + " lollipops!", lollipopAvatar);
                        channel.sendMessageEmbeds(embed.build()).queue();
                    }
                }
                return;
            }
            case "punch", "kick", "headbutt", "chop" -> {
                if (this.idlePlayer.isDefending()) {
                    turnDescription = "`" + idlePlayer.getName() + "` blocked `" + this.idlePlayer + "`'s attack!";
                    this.idlePlayer.setDefending(false);
                } else {
                    int damage = (int) (Math.random() * 6) + 5 + this.turnPlayer.getSP();
                    this.idlePlayer.updateHP(-damage);
                    turnDescription = String.format(move.getDescription(), turnPlayer.getName(), damage);
                }
            }
            case "eat" -> {
                int health = (int) (Math.random() * 11) + 20;
                this.turnPlayer.updateHP(health);
                turnDescription = String.format(move.getDescription(), turnPlayer.getName(), health);
            }
            case "breathe" -> {
                int strength = (int) (Math.random() * 3) + 3;
                this.turnPlayer.updateSP(strength);
                turnDescription = String.format(move.getDescription(), turnPlayer.getName(), strength);
            }
            case "block", "shield" -> {
                turnDescription = String.format(move.getDescription(), turnPlayer.getName());
                this.turnPlayer.setDefending(true);
            }
            case "4th gear", "hinokami kagura" -> {
                if (this.idlePlayer.isDefending()) {
                    turnDescription = String.format(move.getBlockDescription(), idlePlayer.getName(), turnPlayer.getName());
                    this.idlePlayer.setDefending(false);
                } else {
                    int damage = (int) (Math.random() * 6) + 13 + this.turnPlayer.getSP();
                    this.idlePlayer.updateHP(-damage);
                    turnDescription = String.format(move.getDescription(), turnPlayer.getName(), damage);
                }
            }
            case "rasengan" -> {
                if (this.idlePlayer.isDefending()) {
                    turnDescription = String.format(move.getBlockDescription(), idlePlayer.getName(), turnPlayer.getName());
                    this.idlePlayer.setDefending(false);
                } else {
                    int damage = (int) (Math.random() * 6) + 14 + this.turnPlayer.getSP();
                    this.idlePlayer.updateHP(-damage);
                    turnDescription = String.format(move.getDescription(), turnPlayer.getName(), damage);
                }
            }
            case "ora" -> {
                if (this.idlePlayer.isDefending()) {
                    turnDescription = String.format(move.getBlockDescription(), idlePlayer.getName(), turnPlayer.getName());
                    this.idlePlayer.setDefending(false);
                } else {
                    int damage = (int) (Math.random() * 6) + 15 + this.turnPlayer.getSP();
                    this.idlePlayer.updateHP(-damage);
                    turnDescription = String.format(move.getDescription(), turnPlayer.getName(), damage);
                }
            }
            case "us smash" -> {
                if (this.idlePlayer.isDefending()) {
                    turnDescription = String.format(move.getBlockDescription(), idlePlayer.getName(), turnPlayer.getName());
                    this.idlePlayer.setDefending(false);
                } else {
                    int damage = (int) (Math.random() * 6) + 16 + this.turnPlayer.getSP();
                    this.idlePlayer.updateHP(-damage);
                    turnDescription = String.format(move.getDescription(), turnPlayer.getName(), damage);
                }
            }
            case "100%" -> {
                int strength = (int) (Math.random() * 5) + 6;
                this.turnPlayer.updateSP(strength);
                turnDescription = String.format(move.getDescription(), turnPlayer.getName(), strength);
            }
            case "serious punch" -> {
                if (this.idlePlayer.isDefending()) {
                    int damage = (int) (Math.random() * 11) + 20 + this.turnPlayer.getSP();
                    this.idlePlayer.updateHP(-damage);
                    turnDescription = String.format(move.getDescription(), idlePlayer.getName(), turnPlayer.getName());
                    this.idlePlayer.setDefending(false);
                } else {
                    int damage = (int) (Math.random() * 11) + 40 + this.turnPlayer.getSP();
                    this.idlePlayer.updateHP(-damage);
                    turnDescription = String.format(move.getDescription(), turnPlayer.getName(), damage);
                }
            }
            case "za warudo" -> {
                this.idlePlayer.setTimeoutStart(System.currentTimeMillis());
                this.idlePlayer.setTimeoutDuration(6);
                turnDescription = String.format(move.getDescription(), turnPlayer.getName());
            }
            case "yare yare daze" -> {
                int damage = (int) (Math.random() * 6) + 15 + this.turnPlayer.getSP();
                int strength = (int) (Math.random() * 11) + 5;
                this.idlePlayer.updateSP(-strength);
                this.idlePlayer.updateHP(-damage);
                turnDescription = String.format(move.getDescription(), turnPlayer.getName(), strength, damage);
                this.idlePlayer.setDefending(false);
            }
        }

        boolean isTimedOut = this.idlePlayer.isTimedOut();
        if (!isTimedOut) this.switchPlayerTurns();

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
                .setImage(move.getGif())
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

    private void setupGameTimeout(TextChannel channel) {
        if(this.gameTimeout != null && !this.gameTimeout.isCancelled()) this.gameTimeout.cancel(false);

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.green)
                .setImage(fleeGifLink)
                .setAuthor(idlePlayer.getName() + " won the duel!", cpuLink, cpuAvatar)
                .setTitle(victoryMsg[(int)(Math.random()*victoryMsg.length)])
                .setDescription(turnPlayer.getName() + " fled and abandoned the duel game...");

        if(idlePlayer.isCPU()) {
            int xp = turnPlayer.hasMultiplier() ? (int)((int)(Math.random()*11)-40/Constant.MULTIPLIER) : (int)(Math.random()*11)-40;
            embedBuilder.setFooter(turnPlayer.getName() + " lost " + -xp + " lollipops!", lollipopAvatar);
            this.gameTimeout = channel.sendMessageEmbeds(embedBuilder.build()).queueAfter(30, TimeUnit.SECONDS, me -> {
                Database.addToUserBalance(turnPlayer.getMember().getId(), xp);
                Duel.memberToGame.remove(turnPlayer.getMember().getIdLong());
                Duel.occupiedShards[channel.getJDA().getShardInfo().getShardId()]--;
                this.displayMessage.delete().queue();
            });
        } else {
            int xp = idlePlayer.hasMultiplier() ? (int)((int)(Math.random()*31)+70*Constant.MULTIPLIER) : (int)(Math.random()*31)+70;
            embedBuilder.setFooter(idlePlayer.getName() + " won " + xp + " lollipops!", lollipopAvatar);
            this.gameTimeout = channel.sendMessageEmbeds(embedBuilder.build()).queueAfter(30, TimeUnit.SECONDS, me -> {
                Database.addToUserBalance(idlePlayer.getMember().getId(), xp);
                Duel.memberToGame.remove(turnPlayer.getMember().getIdLong());
                Duel.memberToGame.remove(idlePlayer.getMember().getIdLong());
                Duel.occupiedShards[channel.getJDA().getShardInfo().getShardId()]--;
                this.displayMessage.delete().queue();
            });
        }
    }

    private void switchPlayerTurns() {
        DPlayerPT temp = this.turnPlayer;
        this.turnPlayer = this.idlePlayer;
        this.idlePlayer = temp;
    }

    public void checkWin(TextChannel channel) {
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

            if(guestPlayer.isCPU()) {
                if(homePlayer.hasMultiplier()) {
                    int xp = (int)(Math.random()*11)-20;
                    xp = (int)(xp/Constant.MULTIPLIER);
                    Database.addToUserBalance(homePlayer.getMember().getId(), xp);
                    embedBuilder.setFooter(homePlayer.getName() + " lost " + (-xp) + " lollipops!", lollipopAvatar);
                    channel.sendMessageEmbeds(embedBuilder.build()).queue();
                } else {
                    int xp = (int)(Math.random()*11)-20;
                    Database.addToUserBalance(homePlayer.getMember().getId(), xp);
                    embedBuilder.setFooter(homePlayer.getName() + " lost " + (-xp) + " lollipops!", lollipopAvatar);
                    channel.sendMessageEmbeds(embedBuilder.build()).queue();
                }
            } else {
                Duel.memberToGame.remove(guestPlayer.getMember().getIdLong());

                if(guestPlayer.hasMultiplier()) {
                    int xp = (int)(Math.random()*31)+70;
                    xp = (int)(xp*Constant.MULTIPLIER);
                    Database.addToUserBalance(guestPlayer.getMember().getId(), xp);
                    embedBuilder.setFooter(guestPlayer.getName() + " gained " + xp + " lollipops!", lollipopAvatar);
                    channel.sendMessageEmbeds(embedBuilder.build()).queue();
                } else {
                    int xp = (int)(Math.random()*31)+70;
                    Database.addToUserBalance(guestPlayer.getMember().getId(), xp);
                    embedBuilder.setFooter(guestPlayer.getName() + " gained " + xp + " lollipops!", lollipopAvatar);
                    channel.sendMessageEmbeds(embedBuilder.build()).queue();
                }
            }
        } else if(guestPlayer.getHP() <= 0) {
            this.gameTimeout.cancel(false);
            this.displayMessage.delete().queue();
            Duel.occupiedShards[channel.getJDA().getShardInfo().getShardId()]--;
            Duel.memberToGame.remove(homePlayer.getMember().getIdLong());

            if(!guestPlayer.isCPU()) Duel.memberToGame.remove(guestPlayer.getMember().getIdLong());

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setColor(Color.green)
                    .setFooter("Type " + Constant.PREFIX + "duel to start another duel with me!")
                    .setAuthor(homePlayer.getName() + " won the duel!", Constant.WEBSITE, homePlayer.getMember().getEffectiveAvatarUrl())
                    .setTitle(victoryMsg[(int)(Math.random()*victoryMsg.length)])
                    .setImage(endGifLink)
                    .setDescription(
                            "> " + homePlayer.getName() + "'s Health: `" + homePlayer.getHP() + "`\n" +
                            "> " + guestPlayer.getName() + "'s Health: `0 HP`"
                    );

            if(homePlayer.hasMultiplier()) {
                int xp = (int)(Math.random()*31)+70;
                xp = (int)(xp*Constant.MULTIPLIER);
                Database.addToUserBalance(homePlayer.getMember().getId(), xp);
                embedBuilder.setFooter(homePlayer.getName() + " gained " + xp + " lollipops!", lollipopAvatar);
                channel.sendMessageEmbeds(embedBuilder.build()).queue();
            } else {
                int xp = (int)(Math.random()*31)+70;
                Database.addToUserBalance(homePlayer.getMember().getId(), xp);
                embedBuilder.setFooter(homePlayer.getName() + " gained " + xp + " lollipops!", lollipopAvatar);
                channel.sendMessageEmbeds(embedBuilder.build()).queue();
            }
        }
    }


}
