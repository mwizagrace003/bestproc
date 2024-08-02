package clb;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;

public class LoggingSetup {
    public static void setupLogging() throws IOException {
        // Load logging configuration from file
        FileInputStream configFile = new FileInputStream("logging.properties");
        LogManager.getLogManager().readConfiguration(configFile);
    }
}
