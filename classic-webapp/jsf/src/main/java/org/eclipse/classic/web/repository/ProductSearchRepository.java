package org.eclipse.classic.web.repository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.List;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import org.apache.hc.core5.http.HttpHost;
import org.eclipse.classic.web.model.Product;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

@ApplicationScoped
public class ProductSearchRepository {

    private Rest5ClientTransport transport;
    private ElasticsearchClient client;

    @PostConstruct
    void init() {
        JsonMapper mapper = JsonMapper.builder()
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        Rest5Client restClient = Rest5Client
                .builder(new HttpHost("http", "localhost", 9200))
                .build();
        transport = new Rest5ClientTransport(restClient, new JacksonJsonpMapper(mapper));
        client = new ElasticsearchClient(transport);
    }

    @PreDestroy
    void close() {
        try {
            transport.close();
        } catch (IOException e) {
            // ปิดไม่สำเร็จตอน undeploy ก็ไม่มีอะไรทำต่อ แค่ไม่ให้ทำให้ deploy พัง
        }
    }

    public List<Product> search(String q) throws IOException {
        var res = client.search(s -> s
                .index("products")
                .query(qb -> (q == null || q.isBlank())
                        ? qb.matchAll(m -> m)
                        : qb.multiMatch(m -> m
                                .fields("productname", "productline", "productdescription")
                                .query(q))),
                Product.class);

        return res.hits().hits().stream().map(Hit::source).toList();
    }
}
