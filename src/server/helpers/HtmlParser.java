package src.server.helpers;

import java.io.File;
import java.util.Scanner;

public class HtmlParser {
    private static String defaultHtmlDocument = """
        <!DOCTYPE html>
        <html lang=\"en\">
            <body>
                <p>you are accessing a private resource from my web server</p>
            </body>;
        </html>
    """;  // use a few bytes of characters instead of adding congestion to repo and filesystem 


    public static String parseHtmlDocument(File document) {
        StringBuilder parsedDocument = new StringBuilder("");
        try {
            Scanner scanner = new Scanner(document);
            while (scanner.hasNextLine()) {
                parsedDocument.append(scanner.nextLine());
            }
            scanner.close();
        } catch (Exception err) {  // resort to default html String regardless of root cause of failure 
            return defaultHtmlDocument;
        }
        return parsedDocument.toString();
    }
}
