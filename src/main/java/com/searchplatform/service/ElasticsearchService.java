package com.searchplatform.service;

import com.searchplatform.model.Position;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchService {

    private final ElasticsearchClient elasticsearchClient;

    public ElasticsearchService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public Map<String, List<Position>> search(String query, List<String> indices) throws IOException {
        Map<String, List<Position>> results = new HashMap<>();

        for (String index : indices) {
            String indexName = index.toLowerCase();
            List<Position> positions = searchIndex(query, indexName);
            results.put(indexName, positions);
        }

        return results;
    }

    private List<Position> searchIndex(String query, String indexName) throws IOException {
        SearchResponse<Position> response = elasticsearchClient.search(s -> s
                        .index(indexName)
                        .query(q -> q
                                .multiMatch(m -> m
                                        .query(query)
                                        .fields("*")
                                )
                        ),
                Position.class
        );

        List<Position> positions = new ArrayList<>();
        for (Hit<Position> hit : response.hits().hits()) {
            Position position = hit.source();
            if (position != null) {
                if (position.getId() == null) {
                    position.setId(hit.id());
                }
                positions.add(position);
            }
        }

        return positions;
    }
}
