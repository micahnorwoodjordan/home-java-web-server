package src.server.http;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import src.server.Server;
import src.server.Server.HttpMethod;

public class HttpRequest {
    private final HttpMethod httpMethod;
    private final URI uri;
    private final Map<String, List<String>> requestHeaders;

    private HttpRequest(HttpMethod opCode, URI uri, Map<String, List<String>> requestHeaders) {
        this.httpMethod = opCode;
        this.uri = uri;
        this.requestHeaders = requestHeaders;
    }

    public URI getUri() {
        return uri;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public Map<String, List<String>> getRequestHeaders() {
        return requestHeaders;
    }

    public static class Builder {
        private HttpMethod httpMethod;
        private URI uri;
        private Map<String, List<String>> requestHeaders;

        public Builder() { }

        public void setHttpMethod(HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
        }

        public void setUri(URI uri) {
            this.uri = uri;
        }

        public void setRequestHeaders(Map<String, List<String>> requestHeaders) {
            this.requestHeaders = requestHeaders;
        }


        public HttpRequest build() {
            return new HttpRequest(httpMethod, uri, requestHeaders);
        }

        public static HttpRequest addRequestHeaders(final List<String> message, final Builder builder) {
            final Map<String, List<String>> requestHeaders = new HashMap<>();
        
            if (message.size() > 1) {
                for (int i = 1; i < message.size(); i++) {
                    String header = message.get(i);
                    int colonIndex = header.indexOf(':');
        
                    if (! (colonIndex > 0 && header.length() > colonIndex + 1)) {
                        break;
                    }
        
                    String headerName = header.substring(0, colonIndex);
                    String headerValue = header.substring(colonIndex + 1);
        
                    requestHeaders.compute(headerName, (key, values) -> {
                        if (values != null) {
                            values.add(headerValue);
                        } else {
                            values = new ArrayList<>();
                        }
                        return values;
                    });
                }
            }
        
            builder.setRequestHeaders(requestHeaders);
            return builder.build();
        }
    }
}
