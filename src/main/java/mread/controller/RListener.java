package mread.controller;

import java.util.ArrayList;
import java.util.List;

import mread.model.Chapter;
import mread.model.Manga;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public interface RListener {

    /**
     * Sends a manga list to the listener
     * @param mangas list of mangas
     */
	void sendMangas(ArrayList<Manga> mangas, InteractionHook message);

    /**
     * Sends a popular manga list to the listener
     * @param popular list of mangas
     */
    void sendPopularManga(ArrayList<Manga> popular, InteractionHook message);

    /**
     * Sends a top manga list to the listener
     * @param top list of mangas
     */
    void sendTopManga(ArrayList<Manga> top, InteractionHook message);

    /**
     * Sends a latest manga list to the listener
     * @param latest list of mangas
     */
    void sendLatestManga(ArrayList<Manga> latest, InteractionHook message);

    /**
     * Sends a list of chapters
     * @param chapters list of chapters
     */
	void sendChapters(ArrayList<Chapter> chapters, StringSelectInteractionEvent event);

    /**
     * Sends a list of pages
     * @param pages list of pages
     */
	void sendPages(ArrayList<String> pages, StringSelectInteractionEvent event);

    /**
     * Edits a message with a list of pages
     * @param pages list of pages
     */
    void editPages(ArrayList<String> pages, Message message, ButtonInteractionEvent event);

    /**
     * Sends a random manga from MAL
     * @param manga random manga
     */
    void sendRandomManga(Manga manga, InteractionHook message);

}
