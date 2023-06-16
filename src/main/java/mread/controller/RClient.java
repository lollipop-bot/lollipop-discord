package mread.controller;

import java.util.Set;

import mread.model.Chapter;
import mread.model.Manga;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import threading.ThreadManagement;

public class RClient {

	private final RListener listener;

	public RClient(RListener listener) {
		this.listener = listener;
	}

	// genre can be null
	public void browseManga(int page, String genre, InteractionHook message) {
        ThreadManagement.execute(() -> listener.sendMangas(RLoader.browseManga(page, genre), message));
	}

	// search for manga by keyword
	public void searchManga(String query, InteractionHook message) {
		ThreadManagement.execute(() -> listener.sendMangas(RLoader.searchManga(query), message));
	}

    // get popular mangas
    public void getPopularManga(InteractionHook message) {
        ThreadManagement.execute(() -> listener.sendPopularManga(RLoader.getPopularManga(), message));
    }

    // get top rated mangas
    public void getTopManga(InteractionHook message) {
        ThreadManagement.execute(() -> listener.sendTopManga(RLoader.getTopManga(), message));
    }

    // get latest mangas
    public void getLatestManga(InteractionHook message) {
        ThreadManagement.execute(() -> listener.sendLatestManga(RLoader.getLatestManga(), message));
    }

    public void randomManga(boolean nsfw, InteractionHook message) {
        ThreadManagement.execute(() -> listener.sendRandomManga(RLoader.getRandomManga(nsfw), message));
    }

	// get chapters
	public void chapters(Manga manga, SelectMenuInteractionEvent event) {
		ThreadManagement.execute(() -> listener.sendChapters(RLoader.getChapters(manga), event));
	}

	// get pages
	public void pages(Chapter chapter, SelectMenuInteractionEvent event) {
		ThreadManagement.execute(() -> listener.sendPages(RLoader.getPages(chapter), event));
	}

	// get all genres
	public Set<String> genres() {
		return RConstants.getGenres().keySet();
	}

}
