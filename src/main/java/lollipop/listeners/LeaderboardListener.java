package lollipop.listeners;

import lollipop.commands.leaderboard.Leaderboard;
import lollipop.Database;
import lollipop.commands.leaderboard.models.LBMember;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings({"ConstantConditions", "OptionalGetWithoutIsPresent"})
public class LeaderboardListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if(!event.isFromGuild()) return;
        if(!Leaderboard.leaderboardMessages.containsKey(event.getMessageIdLong())) return;

        String[] id = event.getComponentId().split(":");

        if (!event.getMember().getId().equals(id[0])) {
            event.reply("You are not the one who requested the leaderboard. Use `/leaderboard` to create a new one.")
                    .setEphemeral(true).queue();
            return;
        }

        Message message = event.getMessage();
        String page = message.getButtons().stream()
                .filter(it -> it.getId().contains("done"))
                .findAny().get().getId().split(":")[2];

        User user = event.getMember().getUser();

        switch (id[1]) {
            case "delete" -> {
                message.delete().queue();
                Leaderboard.leaderboardMessages.get(event.getMessageIdLong()).cancel(false);
                Leaderboard.leaderboardMessages.remove(event.getMessageIdLong());
                event.deferEdit().queue();
            }
            case "done" -> {
                final Button[] disabledButton = message.getButtons().stream().map(Button::asDisabled).toArray(Button[]::new);
                Leaderboard.leaderboardMessages.get(event.getMessageIdLong()).cancel(false);
                Leaderboard.leaderboardMessages.remove(event.getMessageIdLong());
                event.deferEdit().setActionRow(disabledButton).queue();
            }
            case "previous" -> {
                if(message.getEmbeds().get(0).getTitle().startsWith("Global")) {
                    event.deferEdit().queue();
                    editMessage(event.getJDA(), message, false, user, Integer.parseInt(page));
                } else {
                    event.deferEdit().queue();
                    editMessage(event.getGuild(), message, false, user, Integer.parseInt(page));
                }
            }
            case "next" -> {
                if(message.getEmbeds().get(0).getTitle().startsWith("Global")) {
                    event.deferEdit().queue();
                    editMessage(event.getJDA(), message, true, user, Integer.parseInt(page));
                } else {
                    event.deferEdit().queue();
                    editMessage(event.getGuild(), message, true, user, Integer.parseInt(page));
                }
            }
        }
    }

    private void editMessage(Guild guild, Message msg, boolean next, User user, int page) {
        List<List<LBMember>> guildsList = Database.getLeaderboard(guild);

        if(next) {
            if(page == guildsList.size()) page = 1;
            else page++;
        } else {
            if(page == 1) page = guildsList.size();
            else page--;
        }

        String userId = user.getId();

        LBMember member = new LBMember(Database.getUserGlobalRank(userId), user.getAsTag(), Database.getUserBalance(userId));
        EmbedBuilder embed = new EmbedBuilder().setDescription("```" + Leaderboard.getTable(guildsList.get(page - 1)) + "```")
                .addField("Your Rank", "`" + member.getRank() + ". " + member.getName() + " : " + member.getLollipops() + "`", false);

        msg.editMessageEmbeds(embed.build()).queue();
        msg.editMessageComponents(ActionRow.of(
                Button.secondary(userId + ":previous", Emoji.fromUnicode("⬅️")),
                Button.success(userId + ":done:" + page, Emoji.fromUnicode("✅")),
                Button.danger(userId + ":delete:" + msg.getId(), Emoji.fromUnicode("\uD83D\uDDD1")),
                Button.secondary(userId + ":next", Emoji.fromUnicode("➡️"))
        )).queue();
    }

    private void editMessage(JDA jda, Message msg, boolean next, User user, int page) {
        List<List<LBMember>> guildsList = Database.getLeaderboard(jda);

        if(next) {
            if(page == guildsList.size()) page = 1;
            else page++;
        } else {
            if(page == 1) page = guildsList.size();
            else page--;
        }

        String userId = user.getId();

        LBMember member = new LBMember(Database.getUserGlobalRank(userId), user.getAsTag(), Database.getUserBalance(userId));
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Global Leaderboard")
                .setDescription("```" + Leaderboard.getTable(guildsList.get(page - 1)) + "```")
                .addField("Your Rank", "`" + member.getRank() + ". " + member.getName() + " : " + member.getLollipops() + "`", false)
                .setThumbnail("https://www.dictionary.com/e/wp-content/uploads/2018/11/lollipop-emoji.png")
                .setFooter("Vote for lollipop on top.gg to increase your multiplier!");

        msg.editMessageEmbeds(embed.build()).queue();
        msg.editMessageComponents(ActionRow.of(
                Button.secondary(userId + ":previous", Emoji.fromUnicode("⬅️")),
                Button.success(userId + ":done:" + page, Emoji.fromUnicode("✅")),
                Button.danger(userId + ":delete:" + msg.getId(), Emoji.fromUnicode("\uD83D\uDDD1")),
                Button.secondary(userId + ":next", Emoji.fromUnicode("➡️"))
        )).queue();
    }

}