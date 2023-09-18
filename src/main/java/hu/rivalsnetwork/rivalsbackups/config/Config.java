package hu.rivalsnetwork.rivalsbackups.config;

import hu.rivalsnetwork.rivalsbackups.Main;
import hu.rivalsnetwork.rivalsbackups.utils.FileUtils;

import java.nio.file.Path;
import java.util.List;

public class Config extends AbstractConfig {
    @Key("no-save")
    public static List<String> NO_SAVE = List.of("oreo", "boreo");

    public static final Config CONFIG = new Config();

    public static void reload() {
        Path mainDir = Main.getDataFolder().toPath();

        FileUtils.extractFile(Config.class, "config.yml", mainDir, false);

        CONFIG.reload(mainDir.resolve("config.yml"), Config.class);
    }
}
