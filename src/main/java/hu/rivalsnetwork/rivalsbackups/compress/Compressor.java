package hu.rivalsnetwork.rivalsbackups.compress;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Compressor {

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
                    if (fileName.contains("CoreProtect" + File.separator + "database.db") || fileName.contains(".hprof")) {
                        return FileVisitResult.CONTINUE;
                    }

                    TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file.toFile(), target.toString());
                    tarArchiveOutputStream.putArchiveEntry(tarArchiveEntry);
                    Files.copy(file, tarArchiveOutputStream);
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
