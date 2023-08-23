package src.server.http;

import java.util.HashMap;

import java.io.File;


public class Routes {
    public static String index = "/index";
    public static String test = "/test";
    public static HashMap<String, File> routeTemplateMap;

    static {
        routeTemplateMap = new HashMap<>();
        routeTemplateMap.put(index, new File("src/server/resources/templates/landingPage.html"));
        routeTemplateMap.put(test, new File("src/server/resources/templates/testPage.html"));
    }
}
