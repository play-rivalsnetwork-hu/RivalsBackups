package hu.rivalsnetwork.rivalsbackups.backup;

import hu.rivalsnetwork.rivalsbackups.Main;
import hu.rivalsnetwork.rivalsbackups.compress.Compressor;
import hu.rivalsnetwork.rivalsbackups.config.Config;
import hu.rivalsnetwork.rivalsbackups.scheduler.BackupTimer;
import hu.rivalsnetwork.rivalsbackups.sftp.Uploader;
import hu.rivalsnetwork.rivalsbackups.utils.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Backup {
    private final File file;
    private BackupStage stage;
    private Message message;
    private long compressingStart = 0;
    private long uploadStart = 0;
    private int compressingProgress = 0;
    private int uploadProgress = 0;
    private File compressedFile = null;
    private long startingSize;
    private long finishSize = 0;
    private LocalDateTime startDate;
    private Uploader uploader;
    private MessageCreateAction action;
    private long compressedSize = 0;

    public Backup(File file) {
        this.stage = BackupStage.STARTING;
        this.file = file;
    }

    public boolean isSpaceEnough() {
        // We should assume a 10% compression, so there are no oversights
        return startingSize * 0.90 < (double) new File("/").getFreeSpace();
    }

    public void start(LocalDateTime startDate) {
        this.startDate = startDate;

        startingSize = FileUtils.sizeOfDirectory(file);
        if (!isSpaceEnough()) {
            this.stage = BackupStage.OUT_OF_STORAGE;
        }

        Main.getRunningBackups().add(this);
        updateEmbed();

        Compressor compressor = new Compressor(this);
        try {
            compressingStart = System.currentTimeMillis();
            stage = BackupStage.COMPRESSING;
            compressedFile = File.createTempFile("backup", ".tar.gz");
            compressor.compressFile(file, compressedFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finishSize = FileUtils.sizeOf(compressedFile);

        compressingProgress = 100;
        updateEmbed();

        stage = BackupStage.UPLOADING;
        System.out.println("Countdown!");
        BackupTimer.countdown.countDown();
        // Start uploading!
        uploader = new Uploader();
        try {
            uploader.setup(this);
            uploadStart = System.currentTimeMillis();
            new Thread(() -> {
                try {
                    System.out.printf("Uploading: %s, %s", compressedFile, file.getName());
                    uploader.upload(file.getName(), compressedFile);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateEmbed() {
        if (stage == BackupStage.STARTING) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(Config.BACKUP_RUNNING_TITLE.replace("$server", file.getName()).replace("$time", startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))));
            embed.setColor(Config.BACKUP_RUNNING_COLOR);
            embed.addField(new MessageEmbed.Field(Config.BACKUP_RUNNING_COMPRESSING_TITLE, Config.BACKUP_RUNNING_COMPRESSING_CONTENT.replace("$progress", "---").replace("$bar", "---").replace("$elapsed", "---").replace("$startingSize", String.format("%.2fGB", convertBytesToGigabytes(startingSize))).replace("$finishedSize", "---").replace("$percent", "0%"), false));
            embed.addField(new MessageEmbed.Field(Config.BACKUP_RUNNING_UPLOADING_TITLE, Config.BACKUP_RUNNING_UPLOADING_CONTENT.replace("$progress", "---").replace("$bar", "---").replace("$elapsed", "---").replace("$percent", "0%"), false));

            TextChannel channel = Main.getJda().getGuildById(Config.GUILD_ID).getTextChannelById(Config.CHANNEL_ID);

            action = channel.sendMessageEmbeds(embed.build());
            message = action.complete();
        }

        if (stage == BackupStage.COMPRESSING || stage == BackupStage.UPLOADING) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(Config.BACKUP_RUNNING_TITLE.replace("$server", file.getName()).replace("$time", startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))));
            embed.setColor(Config.BACKUP_RUNNING_COLOR);
            embed.addField(new MessageEmbed.Field(Config.BACKUP_RUNNING_COMPRESSING_TITLE, Config.BACKUP_RUNNING_COMPRESSING_CONTENT.replace("$progress", compressingProgress + "%").replace("$bar", StringUtils.createProgressBar(Config.BACKUP_RUNNING_COMPRESSING_PROGRESSBAR_NOT_DONE, Config.BACKUP_RUNNING_COMPRESSING_PROGRESSBAR_DONE, Config.BACKUP_RUNNING_COMPRESSING_PROGRESSBAR_AMOUNT, compressingProgress)).replace("$elapsed", StringUtils.fancyTime(System.currentTimeMillis() - compressingStart)).replace("$startingSize", String.format("%.2fGB", convertBytesToGigabytes(startingSize))).replace("$finishedSize", String.format("%.2fGB", convertBytesToGigabytes(finishSize))).replace("$percent", compressingProgress + "%"), false));
            embed.addField(new MessageEmbed.Field(Config.BACKUP_RUNNING_UPLOADING_TITLE, Config.BACKUP_RUNNING_UPLOADING_CONTENT.replace("$progress", uploadProgress + "%").replace("$bar", StringUtils.createProgressBar(Config.BACKUP_RUNNING_UPLOADING_PROGRESSBAR_NOT_DONE, Config.BACKUP_RUNNING_UPLOADING_PROGRESSBAR_DONE, Config.BACKUP_RUNNING_UPLOADING_PROGRESSBAR_AMOUNT, uploadProgress)).replace("$elapsed", uploadStart != 0 ? StringUtils.fancyTime(System.currentTimeMillis() - uploadStart) : "00:00:00").replace("$percent", uploadProgress + "%"), false));

            MessageEditAction action1 = message.editMessageEmbeds(embed.build());
            action1.setEmbeds(embed.build()).complete();
        }
    }

    public void updateUploadPercentage(int percent) {
        this.uploadProgress = percent;
        updateEmbed();
    }

    public void updateCompressPercentage(int percent) {
        this.compressingProgress = percent;
        updateEmbed();
    }

    public void finished() {
        try {
            FileUtils.forceDelete(compressedFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Main.getRunningBackups().remove(this);
        uploader.close();
        uploader = null;
        compressedFile = null;
    }

    public void setStage(BackupStage stage) {
        this.stage = stage;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public double convertBytesToGigabytes(long bytes) {
        return bytes / (1024.0 * 1024.0 * 1024.0);
    }

    public long getCompressedSize() {
        return compressedSize;
    }

    public long getStartingSize() {
        return startingSize;
    }

    public void addCompressedSize(long compressedSize) {
        this.compressedSize += compressedSize;
    }
}
