package hu.rivalsnetwork.rivalsbackups.compress;

import hu.rivalsnetwork.rivalsbackups.backup.Backup;
import hu.rivalsnetwork.rivalsbackups.config.Config;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Compressor {
    private Backup backup;
    private int percentage = 0;

    public Compressor(Backup backup) {
        this.backup = backup;
    }

    public void compressFile(File input, File output) throws Exception {
        try (FileOutputStream fileOutputStream = new FileOutputStream(output);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bufferedOutputStream);
             TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(gzipOutputStream)
        ) {
            tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            tarArchiveOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
            Path path = input.toPath();
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path target = path.relativize(file);

                    String fileName = target.toString();
                    for (String s : Config.NO_SAVE) {
                        if (fileName.contains(s)) {
                            return FileVisitResult.CONTINUE;
                        }
                    }

                    TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file.toFile(), target.toString());
                    tarArchiveOutputStream.putArchiveEntry(tarArchiveEntry);

                    try (InputStream stream = Files.newInputStream(file)) {
                        byte[] buffer = new byte[16384];
                        int bytesRead;
                        while ((bytesRead = stream.read(buffer)) != -1) {
                            tarArchiveOutputStream.write(buffer, 0, bytesRead);
                            backup.addCompressedSize(bytesRead);
                            int progress = (int) ((double) backup.getCompressedSize() / backup.getStartingSize() * 100);

                            if (percentage + 10 == progress) {
                                backup.updateCompressPercentage(progress);
                                percentage = progress;
                            }
                        }
                    }

                    tarArchiveOutputStream.closeArchiveEntry();

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    exc.printStackTrace();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
