package hu.rivalsnetwork.rivalsbackups;

import hu.rivalsnetwork.rivalsbackups.compress.Compressor;
import hu.rivalsnetwork.rivalsbackups.config.Config;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    private static File dataFolder;
    private static boolean running = false;

    public static void main(String[] args) throws Exception {
        dataFolder = new File("data/");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        Config.reload();
        for (String s : Config.NO_SAVE) {
            System.out.println(s);
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LocalDateTime now = LocalDateTime.now();
                if (now.getHour() == 19 && now.getMinute() == 28 && now.getSecond() == 0 && !running) {
                    running = true;


                }
            }
        }, 100, 100);


        File file = new File("files/");
        File output = new File("test.tar.gz");
        Compressor compressor = new Compressor();
        compressor.compressFile(file, output);
    }

    public static File getDataFolder() {
        return dataFolder;
    }
}