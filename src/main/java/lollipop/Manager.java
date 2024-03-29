package lollipop;

import lollipop.commands.Random;
import lollipop.commands.*;
import lollipop.commands.duel.Duel;
import lollipop.commands.duel.Move;
import lollipop.commands.Eval;
import lollipop.commands.leaderboard.Leaderboard;
import lollipop.commands.search.Search;
import lollipop.commands.trivia.Trivia;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Manager {

    private final Map<String, Command> commands = new HashMap<>();
    private HashMap<Long, HashMap<String, Long>> cmdRegTimePerUser = new HashMap<Long, HashMap<String, Long>>();

    public Manager() {
        setCommands();
    }

    /**
     * Reload all slash commands to all shards
     * @param jda current shard
     */
    public void reloadCommands(JDA jda) {
        //update all commands
        jda.updateCommands().addCommands(
                new Duel().getSlashCmd(),
                new Move().getSlashCmd(),
                new Attack().getSlashCmd(),
                new Avatar().getSlashCmd(),
                new Baka().getSlashCmd(),
                new BitesTheDust().getSlashCmd(),
                new BotInfo().getSlashCmd(),
                new Defend().getSlashCmd(),
                new Eat().getSlashCmd(),
                new Gif().getSlashCmd(),
                new Help(this).getSlashCmd(),
                new Janken().getSlashCmd(),
                new Latest().getSlashCmd(),
                new Leaderboard().getSlashCmd(),
                new Pat().getSlashCmd(),
                new Ping().getSlashCmd(),
                new Policy().getSlashCmd(),
                new Popular().getSlashCmd(),
                new Profile().getSlashCmd(),
                new Random().getSlashCmd(),
                new RandomQuote().getSlashCmd(),
                new Search().getSlashCmd(),
                new Support().getSlashCmd(),
                new Top().getSlashCmd(),
                new Trivia().getSlashCmd(),
                new Vote().getSlashCmd()
        ).queue();
    }

    /**
     * Reload all slash commands for a specific guild
     * @param g guild
     */
    public void reloadCommands(Guild g) {
        //update all commands
        CommandData dashCmd = new Dashboard().getSlashCmd().setDefaultPermissions(DefaultMemberPermissions.DISABLED);
        CommandData evalCmd = new Eval().getSlashCmd().setDefaultPermissions(DefaultMemberPermissions.DISABLED);
        g.updateCommands().addCommands(
                new Duel().getSlashCmd(),
                new Move().getSlashCmd(),
                new Attack().getSlashCmd(),
                new Avatar().getSlashCmd(),
                new Baka().getSlashCmd(),
                new BitesTheDust().getSlashCmd(),
                new BotInfo().getSlashCmd(),
                new Defend().getSlashCmd(),
                new Eat().getSlashCmd(),
                new Gif().getSlashCmd(),
                new Help(this).getSlashCmd(),
                new Janken().getSlashCmd(),
                new Latest().getSlashCmd(),
                new Leaderboard().getSlashCmd(),
                new Pat().getSlashCmd(),
                new Ping().getSlashCmd(),
                new Policy().getSlashCmd(),
                new Popular().getSlashCmd(),
                new Profile().getSlashCmd(),
                new Random().getSlashCmd(),
                new RandomQuote().getSlashCmd(),
                new Search().getSlashCmd(),
                new Support().getSlashCmd(),
                new Top().getSlashCmd(),
                new Trivia().getSlashCmd(),
                new Vote().getSlashCmd()
        ).queue();
        // DEPRECATED FOR NOW
//        g.updateCommands()
//                .addCommands(
//                        dashCmd.setDefaultPermissions(DefaultMemberPermissions.DISABLED),
//                        evalCmd.setDefaultPermissions(DefaultMemberPermissions.DISABLED)
//                )
//                .queue();
    }

    /**
     * Reload a singular command for all shards
     * @param jda current shard
     * @param c command
     */
    public void reloadCommand(JDA jda, Command c) {
        jda.upsertCommand(c.getSlashCmd()).queue();
    }

    /**
     * Reload a singular command to a singular guild
     * @param g guild
     * @param c command
     */
    public void reloadCommand(Guild g, Command c) {
        g.upsertCommand(c.getSlashCmd()).queue();
    }

    /**
     * Set command manager commands
     */
    private void setCommands() {
        addCommand(new Help(this));
        addCommand(new Gif());
        addCommand(new Ping());
        addCommand(new Search());
        addCommand(new BotInfo());
        addCommand(new Avatar());
        addCommand(new Eval());
        addCommand(new Dashboard());
        addCommand(new Janken());
        addCommand(new Latest());
        addCommand(new Baka());
        addCommand(new RandomQuote());
        addCommand(new BitesTheDust());
        addCommand(new Pat());
        addCommand(new Eat());
        addCommand(new Random());
        addCommand(new Top());
        addCommand(new Duel());
        addCommand(new Move());
        addCommand(new Trivia());
        addCommand(new Profile());
        addCommand(new Leaderboard());
        addCommand(new Support());
        addCommand(new Attack());
        addCommand(new Defend());
        addCommand(new Vote());
        addCommand(new Popular());
        addCommand(new Policy());
    }

    /**
     * Adds command to command manager
     * @param c command
     */
    private void addCommand(Command c) {
        if(!commands.containsKey(c.getAliases()[0])) for(String cmd : c.getAliases()) commands.put(cmd, c);
    }

    /**
     * Gets all the commands stored in the command manger and filters out for a specific command category
     * @param category command category
     * @return collection of commands
     */
    public Collection<Command> getCommands(CommandType category) {
        ArrayList<Command> commands = new ArrayList<>();
        List<Command> values = this.commands.values().stream().filter(c -> c.getCategory() == category).collect(Collectors.toList());
        for(Command c : values) if(!commands.contains(c)) commands.add(c);
        return commands;
    }
    public Map<String, Command> getCommandMap() {return commands;}
    public HashMap<Long, HashMap<String, Long>> getCooldownRegistrationMap(){return cmdRegTimePerUser;}

    /**
     * Gets a list of all commands from the command manager
     * @return list of commands
     */
    public Collection<Command> getCommands() {
        ArrayList<Command> commands = new ArrayList<>();
        List<Command> values = new ArrayList<>(this.commands.values());
        for(Command c : values) if(!commands.contains(c)) commands.add(c);
        return commands;
    }

    /**
     * Get slash command from the command name
     * @param commandName command name
     * @return Command corresponding to the
     */
    public Command getCommand(String commandName) {
        if (commandName == null) return null;
        if(!commands.containsKey(commandName)) return null;
        return commands.get(commandName);
    }

    /**
     * Run the corresponding code to the corresponding command stored in the database
     * @param event slash command interaction event
     */
    void run(SlashCommandInteractionEvent event) {
        if(event.getMember() == null) return;

        if(commands.containsKey(event.getName())) {
            if (cmdRegTimePerUser.containsKey(event.getUser().getIdLong())) {
                if (cmdRegTimePerUser.get(event.getUser().getIdLong()).containsKey(event.getName())) {
                    long cTMs = System.currentTimeMillis();
                    if (cTMs - cmdRegTimePerUser.get(event.getUser().getIdLong()).get(event.getName()) < (commands.get(event.getName()).cooldownDuration() * 1000L)) {
                        event.replyEmbeds(new EmbedBuilder().setDescription("Please wait `" +
                                (((cmdRegTimePerUser.get(event.getUser().getIdLong()).get(event.getName()) + (commands.get(event.getName()).cooldownDuration() * 1000L)) - cTMs) / 1000) +
                                " seconds` before you can use `" + Constant.PREFIX + event.getName() + "`").setColor(Color.RED).build()).setEphemeral(true).queue();
                        return;
                    }
                }
            }
        }
        if(event.getInteraction().isFromGuild()) {
            if(!event.getGuild().getSelfMember().hasPermission(event.getGuildChannel(), Permission.MESSAGE_SEND))
                return;
            final String command = event.getName();
            if (commands.containsKey(command)) {
                if(event.getMember().getUser().isBot()) {
                    event.reply("Nice try, you lowly peasant! Only my masters can command me!")
                            .queue(m -> m.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
                    return;
                }
                commands.get(command).run(event);
                int xp = (int)(Math.random()*6)+1;
                if(Math.random()<0.4) Database.addToUserBalance(event.getUser().getId(), xp);
            }
        } else {
            final String command = event.getName();
            if(commands.containsKey(command)) {
                commands.get(command).run(event);
                int xp = (int)(Math.random()*6)+1;
                if(Math.random()<0.4) Database.addToUserBalance(event.getUser().getId(), xp);
            }
        }
        if(commands.containsKey(event.getName()))
            if (!cmdRegTimePerUser.containsKey(event.getUser().getIdLong()))
            {
                HashMap<String, Long> cmdCooldownDurations = new HashMap<>();
                cmdCooldownDurations.put(event.getName(), System.currentTimeMillis());
                cmdRegTimePerUser.put(event.getUser().getIdLong(), cmdCooldownDurations);
            }
            else
            {
                cmdRegTimePerUser.get(event.getUser().getIdLong()).put(event.getName(), System.currentTimeMillis());
                cmdRegTimePerUser.put(event.getUser().getIdLong(), cmdRegTimePerUser.get(event.getUser().getIdLong()));
            }
    }

}
