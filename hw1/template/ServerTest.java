import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by davidliu on 8/26/15.
 */
public class ServerTest {

    @Test
    public void simpleServerTest() {
        String[] args = {"20", "5555", "5000"};
        Server.main(args);
    }
}