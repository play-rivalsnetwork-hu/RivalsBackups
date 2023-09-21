package hu.rivalsnetwork.rivalsbackups;

import hu.rivalsnetwork.rivalsbackups.backup.Backup;
import hu.rivalsnetwork.rivalsbackups.config.Config;
import hu.rivalsnetwork.rivalsbackups.scheduler.BackupTimer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final List<Backup> runningBackups = new ArrayList<>();
    private static File dataFolder;
    private static JDA jda;
    public static volatile boolean running = false;

    public static void main(String[] args) throws Exception {
        dataFolder = new File("data/");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        Config.reload();

        jda = JDABuilder.createDefault(Config.TOKEN)
                .enableIntents(EnumSet.allOf(GatewayIntent.class))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build().awaitReady();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new BackupTimer(), 0, 1, TimeUnit.SECONDS);
    }

    public static File getDataFolder() {
        return dataFolder;
    }

    public static List<Backup> getRunningBackups() {
        return runningBackups;
    }

    public static JDA getJda() {
        return jda;
    }
}