package src.server.helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class HtmlParser {
    public static String parseHtmlDocument(File document) {
        StringBuilder parsedDocument = new StringBuilder("");
        try {
            Scanner scanner = new Scanner(document);
            while (scanner.hasNextLine()) {
                parsedDocument.append(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException err) {
            System.out.println(err);
        }
        return parsedDocument.toString();
    }
}
