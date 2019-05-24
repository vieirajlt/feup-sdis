package connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Connection {

    protected static final String[] protocols = new String[]{"TLSv1.3"};
    protected static final String[] cipher_suites = new String[]{"TLS_AES_128_GCM_SHA256"};

    protected static final int BUFF_SIZE = 2048;

    protected InputStream is;
    protected OutputStream os;

    public void write(byte[] data) throws IOException {
        os.write(data, 0, data.length);
        os.flush();
    }

    public void write(byte[] data, int len) throws IOException {
        os.write(data, 0, len);
        os.flush();
    }

    public void write(byte[] data, int ini, int end) throws IOException {
        os.write(data, ini, end);
        os.flush();
    }
}
