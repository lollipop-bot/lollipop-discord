package threading;

import awatch.controller.ALoader;
import lollipop.BotStatistics;
import mread.controller.RLoader;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Manages all the threads being used
 * (For now only 1 thread shall be used)
 */
public class ThreadManagement {

    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(8);
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        executor.setMaximumPoolSize(10);
    }

    /**
     * Execute a call from the API or from ALoader
     * @param runnable runnable action
     */
    public static void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    /**
     * Set up a cache refresh cycle which refreshes the cache every day so the new animes and new information reaches as soon as possible
     */
    public static void setupCacheRefresh() {
        Runnable apiRefresh = () -> {
            ALoader.animeCache.clear();
            ALoader.animeNSFWCache.clear();
            ALoader.characterCache.clear();
            ALoader.userCache.clear();
            RLoader.mangaCache.clear();
        };
        scheduler.scheduleWithFixedDelay(apiRefresh, 6, 12, TimeUnit.HOURS);
    }

    /**
     * Set up a statistics refresh cycle to reset the statistics on the bot list websites
     */
    public static void setupStatisticsCycle() {
        Runnable statsRefresh = BotStatistics::setStatistics;
        scheduler.scheduleWithFixedDelay(statsRefresh, 6, 12, TimeUnit.HOURS);
    }

    public static void setupHearbeat() {
        Runnable heartbeat = () -> {
            // Statuspage heartbeat
            HttpClient client = HttpClientBuilder.create().build();
            try {
                URL web = new URL("https://uptime.betterstack.com/api/v1/heartbeat/F7UDxUpdNi91efmmdJriY4zN");
                HttpsURLConnection con = (HttpsURLConnection) web.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
                con.getInputStream();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        };
        scheduler.scheduleAtFixedRate(heartbeat, 0, 1, TimeUnit.HOURS);
    }

}
