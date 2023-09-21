package hu.rivalsnetwork.rivalsbackups.scheduler;

import hu.rivalsnetwork.rivalsbackups.Main;
import hu.rivalsnetwork.rivalsbackups.backup.Backup;
import hu.rivalsnetwork.rivalsbackups.config.Config;
import hu.rivalsnetwork.rivalsbackups.sftp.Uploader;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;

public class BackupTimer implements Runnable {
    private int i = 0;
    private LocalDateTime lastRan = LocalDateTime.now();

    @Override
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        if (i > 100 && Main.running) {
            Main.running = false;
        }

        if (ChronoUnit.HOURS.between(now, lastRan) == 4) {
            lastRan = LocalDateTime.now();
            try {
                Uploader uploader = new Uploader();
                uploader.setup(now);
                uploader.deleteOld();
                uploader.createDirectory();
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            File serverFolder = new File(Config.SERVER_DIRECTORY);
            for (File file : serverFolder.listFiles()) {
                if (!file.isDirectory()) continue;

                for (String s : Config.DIFFERENT_SAVE_INTERVAL) {
                    if (file.getName().contains(s)) {
                        Executors.newSingleThreadScheduledExecutor().execute(() -> {
                            new Backup(file, now);
                        });
                        break;
                    }
                }
            }
        }

        if (now.getHour() == Config.BACKUP_HOUR && now.getMinute() == Config.BACKUP_MINUTE && now.getSecond() == Config.BACKUP_SECOND && !Main.running) {
            System.out.println("Starting backup!");
            Main.running = true;

            try {
                Uploader uploader = new Uploader();
                uploader.setup(now);
                uploader.deleteOld();
                uploader.createDirectory();
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            File serverFolder = new File(Config.SERVER_DIRECTORY);
            x: for (File file : serverFolder.listFiles()) {
                if (!file.isDirectory()) continue;
                for (String s : Config.DIFFERENT_SAVE_INTERVAL) {
                    if (file.getName().contains(s)) continue x;
                }

                Executors.newSingleThreadScheduledExecutor().execute(() -> {
                    new Backup(file, now);
                });
            }
        }

        i++;
    }
}
