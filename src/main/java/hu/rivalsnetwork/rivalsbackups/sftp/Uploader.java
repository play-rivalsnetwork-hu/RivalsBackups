package hu.rivalsnetwork.rivalsbackups.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import hu.rivalsnetwork.rivalsbackups.backup.Backup;
import hu.rivalsnetwork.rivalsbackups.config.Config;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Vector;

public class Uploader {
    private ChannelSftp channel;
    private Session session;
    private Backup backup;
    private LocalDateTime startTime;

    public void setup(Backup backup) throws Exception {
        this.backup = backup;

        JSch jsch = new JSch();
        session = jsch.getSession(Config.SFTP_USER, Config.SFTP_ADDRESS, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(Config.SFTP_PASSWORD);
        session.connect();

        channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
    }

    public void setup(LocalDateTime time) throws Exception {
        this.startTime = time;

        JSch jsch = new JSch();
        session = jsch.getSession(Config.SFTP_USER, Config.SFTP_ADDRESS, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(Config.SFTP_PASSWORD);
        session.setDaemonThread(true);
        session.connect();

        channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
    }

    public void deleteOld() throws Exception {
        Vector<ChannelSftp.LsEntry> entries = channel.ls("node0/mc/");
        for (ChannelSftp.LsEntry entry : entries) {
            Instant instant = Instant.ofEpochSecond(entry.getAttrs().getMTime());

            LocalDate epochDate = instant.atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate currentDate = LocalDate.now();
            long daysAgo = ChronoUnit.DAYS.between(epochDate, currentDate);

            if (daysAgo == 10 || daysAgo == 11) {
                channel.rmdir("node0/mc/" + entry.getFilename());
            }
        }
    }

    public void close() {

    }

    public void upload(String name, File file) throws Exception {
        channel.put(file.toString(), Config.SFTP_FILE.replace("$serverName", name).replace("$startDate", backup.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))), new UploadMonitor(backup));
        channel.exit();
        session.disconnect();
    }

    public void createDirectory() throws Exception {
        channel.mkdir("node0/mc/$startDate/".replace("$startDate", startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))));
    }
}
