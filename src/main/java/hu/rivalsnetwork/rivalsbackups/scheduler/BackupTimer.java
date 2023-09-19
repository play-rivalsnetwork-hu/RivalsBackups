package hu.rivalsnetwork.rivalsbackups.scheduler;

import hu.rivalsnetwork.rivalsbackups.backup.Backup;
import hu.rivalsnetwork.rivalsbackups.config.Config;

import java.io.File;
import java.time.LocalDateTime;
import java.util.TimerTask;
import java.util.concurrent.Executors;

public class BackupTimer extends TimerTask {
    private boolean running = false;
    private int i = 0;

    @Override
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        if (i > 100 && running) {
            running = false;
        }

        if (now.getHour() == Config.BACKUP_HOUR && now.getMinute() == Config.BACKUP_MINUTE && now.getSecond() == Config.BACKUP_SECOND) {
            running = true;

            File serverFolder = new File(Config.SERVER_DIRECTORY);
            for (File file : serverFolder.listFiles()) {
                if (!file.isDirectory()) continue;

                Executors.newSingleThreadScheduledExecutor().execute(() -> {
                    new Backup(file);
                });
            }
        }

        i++;
    }
}
