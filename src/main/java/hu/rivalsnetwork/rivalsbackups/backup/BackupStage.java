package hu.rivalsnetwork.rivalsbackups.backup;

public enum BackupStage {
    STARTING,
    COMPRESSING,
    UPLOADING,
    FINISHED,
    OUT_OF_STORAGE
}
