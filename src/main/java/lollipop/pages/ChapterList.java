package lollipop.pages;

import mread.model.Chapter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.HashMap;

public class ChapterList {

    public ArrayList<Chapter> chapters;
    public ArrayList<StringSelectMenu> menus;
    public int currentChapter;
    public int pages;
    public int pageNumber;
    public Message msg;
    public User user;

    public static HashMap<Long, ChapterList> messageToChapterList = new HashMap<>();

    /**
     * Constructor initializes variables
     * @param cs chapters
     * @param ms select menus
     * @param pn page number
     * @param ps pages count
     * @param m message
     * @param u user
     */
    public ChapterList(ArrayList<Chapter> cs, ArrayList<StringSelectMenu> ms, int pn, int ps, Message m, User u) {
        this.chapters = cs;
        this.menus = ms;
        this.pageNumber = pn;
        this.msg = m;
        this.user = u;
        this.pages = ps;
    }

    // default constructor
    public ChapterList() {
        this.chapters = new ArrayList<>();
    }

}
