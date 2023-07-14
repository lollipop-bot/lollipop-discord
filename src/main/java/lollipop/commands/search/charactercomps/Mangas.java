package lollipop.commands.search.charactercomps;

import lollipop.API;
import lollipop.pages.CharacterPage;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public class Mangas {

    static API api = new API();

    public static void run(StringSelectInteractionEvent event, CharacterPage page) {
        api.getCharacterMangas(event, page);
    }

}
