package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {

    /**
     * This function reads a file and returns the content as a String
     *
     * @param filepath
     * @return
     * @throws IOException
     */
    public static String getStringFromFile(String filepath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filepath)), StandardCharsets.UTF_8);
    }
}
