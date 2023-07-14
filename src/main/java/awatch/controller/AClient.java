package awatch.controller;

import awatch.model.Character;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import threading.ThreadManagement;

import java.io.IOException;
import java.util.HashSet;

/**
 * Access the threading and ALoader which retrieves data from the APIs
 */
public class AClient {

    private final AListener listener;

    /**
     * Initialize AListener in constructor
     * @param listener AListener
     */
    public AClient(AListener listener) {
        this.listener = listener;
    }

    /**
     * Runs a thread to make a search anime call
     * @param query anime name
     * @param nsfw nsfw allowed
     */
    public void searchAnime(String query, boolean nsfw, InteractionHook message) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendSearchAnime(ALoader.loadAnime(query, nsfw), message);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to make a search character call
     * @param query character name
     */
    public void searchCharacter(String query, InteractionHook message) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendSearchCharacter(ALoader.loadCharacter(query), message);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to make a search user call
     * @param query username
     */
    public void searchUser(String query, InteractionHook message) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendSearchUser(ALoader.loadSearchUser(query), message);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to make a get character animes call
     * @param character character
     */
    public void getCharacterAnimes(Character character, StringSelectInteractionEvent event) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendCharacterAnimes(ALoader.loadCharacterInfo(character), event);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to make a get character mangas call
     * @param character character
     */
    public void getCharacterMangas(Character character, StringSelectInteractionEvent event) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendCharacterMangas(ALoader.loadCharacterInfo(character), event);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to make a get character voice actors call
     * @param character character
     */
    public void getCharacterVoices(Character character, StringSelectInteractionEvent event) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendCharacterVoices(ALoader.loadCharacterInfo(character), event);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to make a random quote call
     */
    public void randomQuote(InteractionHook message) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendRandomQuote(ALoader.loadQuote(), message);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to get episodes of an anime
     * @param id MAL id
     */
    public void getEpisodes(long id, StringSelectInteractionEvent event) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendEpisodes(ALoader.loadEpisodes(id), event);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to get characters of an anime
     * @param id MAL id
     */
    public void getCharacters(long id, StringSelectInteractionEvent event) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendCharacterList(ALoader.loadCharacterList(id), event);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to get news of an anime
     * @param id MAL id
     */
    public void getNews(long id, StringSelectInteractionEvent event) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendNews(ALoader.loadNews(id), event);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to get the statistics of an anime
     * @param id MAL id
     */
    public void getStatistics(long id, StringSelectInteractionEvent event) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendStatistics(ALoader.loadStatistics(id), event);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to get the themes of an anime
     * @param id MAL id
     */
    public void getThemes(long id, StringSelectInteractionEvent event) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendThemes(ALoader.loadThemes(id), event);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to get the recommendations for an anime
     * @param id MAL id
     */
    public void getRecommendation(long id, StringSelectInteractionEvent event) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendRecommendation(ALoader.loadRecommendations(id), event);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to get the top review of an anime
     * @param id MAL id
     */
    public void getReview(long id, StringSelectInteractionEvent event) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendReview(ALoader.loadReview(id), event);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to get the top 25 animes ranked in terms of score
     */
    public void getTop(InteractionHook message) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendTopAnime(ALoader.loadTop(), message);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to get the top 25 animes ranked in terms of popularity
     */
    public void getPopular(InteractionHook message) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendPopularAnime(ALoader.loadPopular(), message);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to get the latest animes of the season
     */
    public void getLatest(InteractionHook message) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendLatestAnime(ALoader.loadLatest(), message);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to get a randomly chosen anime from MALs database
     * @param nsfw nsfw allowed
     */
    public void randomAnime(boolean nsfw, InteractionHook message) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendRandomAnime(ALoader.loadRandomAnime(nsfw), message);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to get a randomly chosen anime from MALs database
     * @param nsfw nsfw allowed
     */
    public void randomCharacter(boolean nsfw, InteractionHook message) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendRandomCharacter(ALoader.loadRandomCharacter(nsfw), message);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to get a randomly chosen anime related GIF
     */
    public void randomGIF(InteractionHook message) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendRandomGIF(ALoader.loadGIF(), message);
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Runs a thread to get a random trivia question about anime
     */
    public void randomTrivia(HashSet<String> available, InteractionHook message) {
        ThreadManagement.execute(() -> {
            try {
                listener.sendTrivia(ALoader.loadTrivia(available), message);
            } catch(IOException e) { e.printStackTrace(); throw new RuntimeException(e); }
        });
    }

}
