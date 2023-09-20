package hu.rivalsnetwork.rivalsbackups.config;

import hu.rivalsnetwork.rivalsbackups.Main;
import hu.rivalsnetwork.rivalsbackups.utils.FileUtils;

import java.nio.file.Path;
import java.util.List;

public class Config extends AbstractConfig {
    @Key("no-save")
    public static List<String> NO_SAVE = List.of("oreo", "boreo");
    @Key("bot.token")
    public static String TOKEN = "";
    @Key("bot.guild-id")
    public static String GUILD_ID = "";
    @Key("backup.channel-id")
    public static String CHANNEL_ID = "1009424038889803827";
    @Key("backup.start.hour")
    public static int BACKUP_HOUR = 4;
    @Key("backup.start.minute")
    public static int BACKUP_MINUTE = 0;
    @Key("backup.start.second")
    public static int BACKUP_SECOND = 0;
    @Key("backup.server-directory")
    public static String SERVER_DIRECTORY = "/home/daemon-data/";

    @Key("backup.embed.running.color")
    public static int BACKUP_RUNNING_COLOR = 0x03b1fc;
    @Key("backup.embed.running.title")
    public static String BACKUP_RUNNING_TITLE = "Backup $server - $time";
    @Key("backup.embed.running.compressing.title")
    public static String BACKUP_RUNNING_COMPRESSING_TITLE = "Compressing:";
    @Key("backup.embed.running.compressing.content")
    public static String BACKUP_RUNNING_COMPRESSING_CONTENT = "$bar (`$percent`)\nElapsed - $elapsed\nSize: $startingSize -> $finishedSize";
    @Key("backup.embed.running.compressing.progressbar.done")
    public static String BACKUP_RUNNING_COMPRESSING_PROGRESSBAR_DONE = "\uD83D\uDFE9";
    @Key("backup.embed.running.compressing.progressbar.not-done")
    public static String BACKUP_RUNNING_COMPRESSING_PROGRESSBAR_NOT_DONE = "⬜";
    @Key("backup.embed.running.compressing.progressbar.amount")
    @Comment("The amount of progress icons used for the progressbar")
    public static int BACKUP_RUNNING_COMPRESSING_PROGRESSBAR_AMOUNT = 10;

    @Key("backup.embed.running.uploading.title")
    public static String BACKUP_RUNNING_UPLOADING_TITLE = "Uploading:";
    @Key("backup.embed.running.uploading.content")
    public static String BACKUP_RUNNING_UPLOADING_CONTENT = "$bar (`$percent`)\nElapsed - $elapsed";
    @Key("backup.embed.running.uploading.progressbar.done")
    public static String BACKUP_RUNNING_UPLOADING_PROGRESSBAR_DONE = "\uD83D\uDFE9";
    @Key("backup.embed.running.uploading.progressbar.not-done")
    public static String BACKUP_RUNNING_UPLOADING_PROGRESSBAR_NOT_DONE = "⬜";
    @Key("backup.embed.running.uploading.progressbar.amount")
    @Comment("The amount of progress icons used for the progressbar")
    public static int BACKUP_RUNNING_UPLOADING_PROGRESSBAR_AMOUNT = 10;
    @Key("backup.sftp.address")
    public static String SFTP_ADDRESS = "";
    @Key("backup.sftp.password")
    public static String SFTP_PASSWORD = "";
    @Key("backup.sftp.user")
    public static String SFTP_USER = "";
    @Key("backup.sftp.folder")
    public static String SFTP_FILE = "node0/mc/$startDate/$serverName.tar.gz";
    @Key("different-interval")
    public static List<String> DIFFERENT_SAVE_INTERVAL = List.of("5d66101e-d5b7-4e6f-8762-afe7bf1d7b20", "2404eb1d-55b8-4462-96f9-9a9b08686003", "04f67f07-d22f-4b83-988a-77400b5cf690");

    public static final Config CONFIG = new Config();

    public static void reload() {
        Path mainDir = Main.getDataFolder().toPath();

        FileUtils.extractFile(Config.class, "config.yml", mainDir, false);

        CONFIG.reload(mainDir.resolve("config.yml"), Config.class);
    }
}
