package lollipop.commands.duel.models;

import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class DMove {

    private final String name;
    private final String phrase;
    private final String description;
    private String blockDescription = null;
    private final String gif;
    private final DMType type;
    private final boolean blockable;
    private Button button;

    public DMove(String name, String id, String phrase, String descriptionPhrase, String gif, String detail, DMType type, boolean blockable) {
        this.name = name;
        this.phrase = phrase;
        this.description = "**" + descriptionPhrase + "**\n" + detail + "\n" + "> Type: " + type.toString() + "\n> Blockable: " + blockable;
        this.gif = gif;
        this.type = type;
        this.blockable = blockable;
        if(this.blockable) this.blockDescription = "`%s` blocked `%s`'s " + this.name;
        switch (type) {
            case HEAL -> this.button = Button.success(id, name);
            case ATTACK -> this.button = Button.primary(id, name);
            case ULTIMATE, FORFEIT -> this.button = Button.danger(id, name);
            case STRENGTH, DEFENSE -> this.button = Button.secondary(id, name);
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBlockDescription() {
        return blockDescription;
    }

    public String getPhrase() {
        return phrase;
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

}