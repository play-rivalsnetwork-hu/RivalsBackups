package hu.rivalsnetwork.rivalsbackups;

import hu.rivalsnetwork.rivalsbackups.backup.Backup;
import hu.rivalsnetwork.rivalsbackups.config.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Main {
    private static final List<Backup> runningBackups = new ArrayList<>();
    private static File dataFolder;
    private static JDA jda;

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

        new Backup(new File("input"));
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