package hu.rivalsnetwork.rivalsbackups.backup;

import hu.rivalsnetwork.rivalsbackups.Main;
import hu.rivalsnetwork.rivalsbackups.compress.Compressor;
import hu.rivalsnetwork.rivalsbackups.config.Config;
import hu.rivalsnetwork.rivalsbackups.sftp.Uploader;
import hu.rivalsnetwork.rivalsbackups.utils.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.time.LocalDateTime;

public class Backup {
    private final File file;
    private BackupStage stage;
    private Message message;
    private long compressingTook = 0;
    private long uploadTook = 0;
    private int compressingProgress = 0;
    private int uploadProgress = 0;
    private File compressedFile = null;
    private long startingSize;
    private long finishSize = 0;
    private final LocalDateTime startDate;
    private Uploader uploader;

    public Backup(File file) {
        this.stage = BackupStage.STARTING;
        this.file = file;
        startDate = LocalDateTime.now();

        startingSize = FileUtils.sizeOfDirectory(file);
        if (!isSpaceEnough()) {
            this.stage = BackupStage.OUT_OF_STORAGE;
        }

        Main.getRunningBackups().add(this);
        updateEmbed();
        start();
    }

    public boolean isSpaceEnough() {
        // We should assume a 10% compression, so there are no oversights
        return startingSize * 0.90 < (double) new File("/").getFreeSpace();
    }

    public void start() {
        long compressStart = System.currentTimeMillis();
        System.out.println("a");
        Compressor compressor = new Compressor();
        try {
            compressedFile = File.createTempFile("backup", ".tar.gz");
            compressor.compressFile(file, compressedFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finishSize = FileUtils.sizeOf(file);

        compressingProgress = 100;
        compressingTook = System.currentTimeMillis() - compressStart;
        updateEmbed();

        System.out.println("b");
        System.out.println(compressedFile);
        stage = BackupStage.UPLOADING;
        // Start uploading!
        uploader = new Uploader();
        try {
            uploader.setup(this);
            System.out.println("upla");
            uploader.upload(file.getName(), compressedFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateEmbed() {
        if (stage == BackupStage.STARTING) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(Config.BACKUP_RUNNING_TITLE);
            embed.setColor(Config.BACKUP_RUNNING_COLOR);
            embed.addField(new MessageEmbed.Field(Config.BACKUP_RUNNING_COMPRESSING_TITLE, Config.BACKUP_RUNNING_COMPRESSING_CONTENT.replace("$progress", "---").replace("$bar", "---").replace("$elapsed", "---").replace("$startingSize", ""), false));
            embed.addField(new MessageEmbed.Field(Config.BACKUP_RUNNING_UPLOADING_TITLE, Config.BACKUP_RUNNING_UPLOADING_CONTENT.replace("$progress", "---").replace("$bar", "---").replace("$elapsed", "---"), false));

            TextChannel channel = Main.getJda().getGuildById(Config.GUILD_ID).getTextChannelById(Config.CHANNEL_ID);

            channel.sendMessageEmbeds(embed.build()).queue(message -> {
                this.message = message;
            });
        }

        if (stage == BackupStage.COMPRESSING || stage == BackupStage.UPLOADING) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(Config.BACKUP_RUNNING_TITLE);
            embed.setColor(Config.BACKUP_RUNNING_COLOR);
            embed.addField(new MessageEmbed.Field(Config.BACKUP_RUNNING_COMPRESSING_TITLE, Config.BACKUP_RUNNING_COMPRESSING_CONTENT.replace("$progress", compressingProgress + "%").replace("$bar", StringUtils.createProgressBar(Config.BACKUP_RUNNING_COMPRESSING_PROGRESSBAR_NOT_DONE, Config.BACKUP_RUNNING_COMPRESSING_PROGRESSBAR_DONE, Config.BACKUP_RUNNING_COMPRESSING_PROGRESSBAR_AMOUNT, compressingProgress)).replace("$elapsed", StringUtils.fancyTime(compressingTook)).replace("$startingSize", String.format("%.2fGB", convertBytesToGigabytes(startingSize))).replace("$finishedSize", String.format("%.2fGB", convertBytesToGigabytes(startingSize))), false));
            embed.addField(new MessageEmbed.Field(Config.BACKUP_RUNNING_UPLOADING_TITLE, Config.BACKUP_RUNNING_UPLOADING_CONTENT.replace("$progress", uploadProgress + "%").replace("$bar", StringUtils.createProgressBar(Config.BACKUP_RUNNING_UPLOADING_PROGRESSBAR_NOT_DONE, Config.BACKUP_RUNNING_UPLOADING_PROGRESSBAR_DONE, Config.BACKUP_RUNNING_UPLOADING_PROGRESSBAR_AMOUNT, compressingProgress)).replace("$elapsed", StringUtils.fancyTime(compressingTook)), false));

            message.editMessageEmbeds(embed.build()).queue();
        }
    }

    public void startOutOfStorageTimer() {

    }

    public void updatePercentage(int percent) {
        this.uploadProgress = percent;
        updateEmbed();
    }

    public void uploadStart() {

    }

    public void finished() {
        uploader.close();
        compressedFile.delete();
        Main.getRunningBackups().remove(this);
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
}
