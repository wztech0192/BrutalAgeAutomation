package dispatcher;

import java.io.IOException;

public interface IDispatcherReader {
    boolean read(String str) throws InterruptedException, IOException;
}
