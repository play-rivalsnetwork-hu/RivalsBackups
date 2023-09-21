package hu.rivalsnetwork.rivalsbackups.compress;

import java.io.IOException;
import java.io.OutputStream;

public class GZIPOutputStream extends java.util.zip.GZIPOutputStream {

    public GZIPOutputStream(OutputStream out) throws IOException {
        super(out);
    }
}
