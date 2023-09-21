package hu.rivalsnetwork.rivalsbackups.sftp;

import com.jcraft.jsch.SftpProgressMonitor;
import hu.rivalsnetwork.rivalsbackups.backup.Backup;
import hu.rivalsnetwork.rivalsbackups.backup.BackupStage;

public class UploadMonitor implements SftpProgressMonitor {
    private Backup backup;
    private long max = 0;
    private long transferred = 0;
    private int percent = 0;

    public UploadMonitor(Backup backup) {
        this.backup = backup;
    }

    @Override
    public void init(int op, String src, String dest, long max) {
        this.max = max;
        backup.setStage(BackupStage.UPLOADING);
        backup.updateEmbed();
    }

    @Override
    public boolean count(long count) {
        transferred += count;

        int percentage = (int) ((this.transferred / (float) this.max) * 100);

        if (percentage == percent + 10) {
            percent = percentage;
            backup.updateUploadPercentage(percent);
        }

        return true;
    }

    @Override
    public void end() {
        backup.finished();
    }
}
