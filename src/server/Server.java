package src.server;

import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import src.server.http.HttpRequest;
import src.server.http.HttpResponse;


public class Server {
    private final Map<String, RequestRunner> routes;
    private final Executor threadPool;
    private HttpHandler handler;
    private ServerSocket socket;
    public static Map<Integer, String> STATUS_CODES;
    public enum HttpMethod {
        GET,
        PUT,
        POST,
        PATCH
    }
    static {
        STATUS_CODES = new HashMap<>();
        STATUS_CODES.put(200, "Success");
        STATUS_CODES.put(201, "Created");
        STATUS_CODES.put(400, "Bad Request");
        STATUS_CODES.put(401, "Unauthorized");
        STATUS_CODES.put(403, "Forbidden");
        STATUS_CODES.put(500, "Internal Server Error");
    }

    public Server(int port) {
        routes = new HashMap<>();
        threadPool = Executors.newFixedThreadPool(100);
        try {
            socket = new ServerSocket(port);
        } catch (Exception err) {
            System.out.println(err);
        }
    }

    public void addRoute(HttpMethod opCode, String route, RequestRunner runner) {
        routes.put(opCode.name().concat(route), runner);
    }

    private void handleRequest(final HttpRequest request, final BufferedWriter bufferedWriter) {
        final String routeKey = request.getHttpMethod().name().concat(request.getUri().getRawPath());
        if (routes.containsKey(routeKey)) {
            ResponseWriter.writeResponse(bufferedWriter, routes.get(routeKey).run(request));
        } else {
            ResponseWriter.writeResponse(bufferedWriter, new HttpResponse.Builder().setStatusCode(404).setEntity("Not Found...").build());
        }
    }

    private void handleConnection(Socket clientConnection) {
        Runnable httpRequestRunner = () -> {
            try {
                handler.handleConnection(clientConnection.getInputStream(), clientConnection.getOutputStream());
            } catch (IOException ignored) { }
        };
        threadPool.execute(httpRequestRunner);
    }

    public class HttpHandler {
        private final Map<String, RequestRunner> routes;
        public HttpHandler(final Map<String, RequestRunner> routes) {
            this.routes = routes;
        }
        public void handleConnection(final InputStream inputStream, final OutputStream outputStream) throws IOException {
            final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            Optional<HttpRequest> request = HttpDecoder.decode(inputStream);
            request.ifPresentOrElse((r) -> handleRequest(r, bufferedWriter), () -> handleInvalidRequest(bufferedWriter));
            bufferedWriter.close();
            inputStream.close();
        }
    }

    public void start() throws IOException {
        handler = new HttpHandler(routes);
        while (true) {
            Socket clientConnection = socket.accept();
            handleConnection(clientConnection);
        }
    }

    public interface RequestRunner {
        HttpResponse run(HttpRequest request);
    }

    public class HttpDecoder {
        private static Optional<HttpRequest> buildRequest(List<String> message) {
            if (message.isEmpty()) {
                return Optional.empty();
            }

            String firstLine = message.get(0);
            String[] httpInfo = firstLine.split(" ");

            if (httpInfo.length != 3) {
                return Optional.empty();
            }

            String protocolVersion = httpInfo[2];
            if (!protocolVersion.equals("HTTP/1.1")) {
                return Optional.empty();
            }

            try {
                HttpRequest.Builder requestBuilder = new HttpRequest.Builder();
                requestBuilder.setHttpMethod(HttpMethod.valueOf(httpInfo[0]));
                requestBuilder.setUri(new URI(httpInfo[1]));
                return Optional.of(HttpRequest.Builder.addRequestHeaders(message, requestBuilder));
            } catch (URISyntaxException | IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        public static Optional<HttpRequest> decode(final InputStream inputStream) {
            return readMessage(inputStream).flatMap(HttpDecoder::buildRequest);
        }
    }

    public class ResponseWriter {
        private static Optional<String> getResponseString(final Object entity) {
            if (entity instanceof String) {  // Currently only supporting Strings
                try {
                    return Optional.of(entity.toString());
                } catch (Exception ignored) { }
            }
            return Optional.empty();
        }

        public static void writeResponse(final BufferedWriter outputStream, final HttpResponse response) {
            try {
                    final int statusCode = response.getStatusCode();
                    final String statusCodeMeaning = STATUS_CODES.get(statusCode);
                    final List<String> responseHeaders = buildHeaderStrings(response.getResponseHeaders());
            
                    outputStream.write("HTTP/1.1 " + statusCode + " " + statusCodeMeaning + "\r\n");
            
                    for (String header : responseHeaders) {
                        outputStream.write(header);
                    }
            
                    final Optional<String> entityString = response.getEntity().flatMap(ResponseWriter::getResponseString);
                    if (entityString.isPresent()) {
                        final String encodedString = new String(entityString.get().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                        outputStream.write("Content-Length: " + encodedString.getBytes().length + "\r\n");
                        outputStream.write("\r\n");
                        outputStream.write(encodedString);
                    } else {
                        outputStream.write("\r\n");
                    }
            } catch (Exception ignored) {}
        }
    }

    private void handleInvalidRequest(final BufferedWriter bufferedWriter) {
        HttpResponse notFoundResponse = new HttpResponse.Builder().setStatusCode(400).setEntity("Bad Request...").build();
        ResponseWriter.writeResponse(bufferedWriter, notFoundResponse);
    }

    private static Optional<List<String>> readMessage(final InputStream inputStream) {
        try {
            if (!(inputStream.available() > 0)) {
                return Optional.empty();
            }
            final char[] inBuffer = new char[inputStream.available()];
            final InputStreamReader inReader = new InputStreamReader(inputStream);
            final int read = inReader.read(inBuffer);
            List<String> message = new ArrayList<>();

            try (Scanner sc = new Scanner(new String(inBuffer))) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    message.add(line);
                }
            }
            return Optional.of(message);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static List<String> buildHeaderStrings(final Map<String, List<String>> responseHeaders) {
        final List<String> responseHeadersList = new ArrayList<>();
    
        responseHeaders.forEach((name, values) -> {
            final StringBuilder valuesCombined = new StringBuilder();
            values.forEach(valuesCombined::append);
            valuesCombined.append(";");
            responseHeadersList.add(name + ": " + valuesCombined + "\r\n");
        });
    
        return responseHeadersList;
    }
}
