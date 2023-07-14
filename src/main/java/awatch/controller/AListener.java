package awatch.controller;

import awatch.model.*;
import awatch.model.Character;
import awatch.model.Question;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.ArrayList;

public interface AListener {

    void sendSearchAnime(ArrayList<Anime> animes, InteractionHook message);
    void sendSearchCharacter(ArrayList<Character> character, InteractionHook message);
    void sendSearchUser(User user, InteractionHook message);
    void sendCharacterAnimes(Character character, StringSelectInteractionEvent event);
    void sendCharacterMangas(Character character, StringSelectInteractionEvent event);
    void sendCharacterVoices(Character character, StringSelectInteractionEvent event);
    void sendRandomQuote(Quote quote, InteractionHook message);
    void sendEpisodes(ArrayList<Episode> episodes, StringSelectInteractionEvent event);
    void sendCharacterList(ArrayList<Character> characters, StringSelectInteractionEvent event);
    void sendNews(ArrayList<Article> articles, StringSelectInteractionEvent event);
    void sendStatistics(Statistic statistics, StringSelectInteractionEvent event);
    void sendThemes(Themes themes, StringSelectInteractionEvent event);
    void sendRecommendation(Recommendation recommendation, StringSelectInteractionEvent event);
    void sendReview(Review review, StringSelectInteractionEvent event);
    void sendTopAnime(ArrayList<Anime> top, InteractionHook message);
    void sendPopularAnime(ArrayList<Anime> popular, InteractionHook message);
    void sendLatestAnime(ArrayList<Anime> latest, InteractionHook message);
    void sendRandomAnime(Anime random, InteractionHook message);
    void sendRandomCharacter(Character random, InteractionHook message);
    void sendRandomGIF(GIF gif, InteractionHook message);
    void sendTrivia(Question question, InteractionHook message);

}
