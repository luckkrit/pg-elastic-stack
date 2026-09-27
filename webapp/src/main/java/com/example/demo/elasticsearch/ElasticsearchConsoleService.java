package com.example.demo.elasticsearch;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import co.elastic.clients.transport.rest5_client.low_level.Request;
import co.elastic.clients.transport.rest5_client.low_level.Response;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;

@Service
public class ElasticsearchConsoleService {

    private final Rest5Client rest5Client;
    private static final Pattern COMMAND_PATTERN = Pattern.compile(
            "^(GET|POST|PUT|DELETE|HEAD)\\s+(/?\\S+)$");

    public ElasticsearchConsoleService(
            @Value("${spring.elasticsearch.uris}") String elasticsearchUrl) {

        this.rest5Client = Rest5Client.builder(
                URI.create(elasticsearchUrl)).build();
    }

    public String search(String command) throws IOException, ParseException {

        String[] parts = command.strip().split("\\R", 2);

        String firstLine = parts[0].trim();

        String json = parts.length > 1
                ? parts[1].trim()
                : "";
        Matcher matcher = COMMAND_PATTERN.matcher(firstLine);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid Elasticsearch command");
        }

        String method = matcher.group(1);

        String endpoint = matcher.group(2);

        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }

        Request request = new Request(method, endpoint);

        if (!json.isBlank()) {
            request.setJsonEntity(json);
        }

        Response response = rest5Client.performRequest(request);

        return EntityUtils.toString(
                response.getEntity());
    }
}
