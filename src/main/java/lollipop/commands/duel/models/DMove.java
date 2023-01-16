package lollipop.commands.duel.models;

import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class DMove {

    private final String name;
    private final String phrase;
    private String blockPhrase;
    private final String description;
    private final String gif;
    private final DMType type;
    private final boolean blockable;
    private final int[] influence;
    private Button button;

    public DMove(String name, String id, String phrase, String descriptionPhrase, String gif, String detail, DMType type, boolean blockable, int[] influence) {
        this.name = name;
        this.phrase = phrase;
        this.description = "**" + descriptionPhrase + "**\n" + detail + "\n" + "> Type: " + type.toString() + "\n> Blockable: " + blockable;
        this.gif = gif;
        this.type = type;
        this.blockable = blockable;
        if(this.blockable) this.blockPhrase = "`%s` blocked `%s`'s " + this.name;
        switch (type) {
            case HEAL -> this.button = Button.success(id, name);
            case ATTACK -> this.button = Button.primary(id, name);
            case ULTIMATE, FORFEIT -> this.button = Button.danger(id, name);
            case STRENGTH, DEFENSE -> this.button = Button.secondary(id, name);
        }
        this.influence = influence;
    }

    public String getName() {
        return name;
    }

    public String getPhrase() {
        return phrase;
    }

    public String getBlockPhrase() {
        return blockPhrase;
    }

    public String getDescription() {
        return description;
    }

    public DMType getType() {
        return type;
    }

    public boolean isBlockable() {
        return blockable;
    }

    public Button getButton() {
        return button;
    }

    public String getGif() {
        return gif;
    }

    public String toString() {
        return this.name + ": " + this.phrase;
    }

    public int[] getInfluence() {
        return influence;
    }

}