package src;
import java.io.IOException;
import java.io.File;

import src.server.Server;
import src.server.http.HttpResponse;
import src.server.helpers.HtmlParser;


public class App {
    public static void main(String[] args) throws IOException {
        Server myServer = new Server(8080);
        String filepath = "src/server/resources/templates/landingPage.html";
        String parsedDocument = HtmlParser.parseHtmlDocument(new File(filepath));

        myServer.addRoute(
            Server.HttpMethod.GET, "/testOne", (req) -> new HttpResponse.Builder()
                        .setStatusCode(200)
                        .addHeader("Content-Type", "text/html")
                        .setEntity(parsedDocument)
                        .build()
        );
        myServer.start();
    }
}