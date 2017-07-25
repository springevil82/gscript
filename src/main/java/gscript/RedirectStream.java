package gscript;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.OutputStream;

public class RedirectStream extends OutputStream {

    private final byte DELIMITER = 10;
    private final RedirectPublisher publisher;
    private byte[] buffer = new byte[0];

    public RedirectStream(RedirectPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void write(int b) throws IOException {
        buffer = ArrayUtils.add(buffer, (byte) b);

        if (b == DELIMITER) {
            publisher.println(new String(buffer).trim());
            buffer = new byte[0];
        }
    }
}
