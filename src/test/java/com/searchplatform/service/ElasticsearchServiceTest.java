package com.searchplatform.service;

import com.searchplatform.model.Position;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticsearchServiceTest {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Mock
    private SearchResponse<Position> searchResponse;

    @Mock
    private HitsMetadata<Position> hitsMetadata;

    @Mock
    private Hit<Position> hit;

    private ElasticsearchService elasticsearchService;

    @BeforeEach
    void setUp() {
        elasticsearchService = new ElasticsearchService(elasticsearchClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    void search_shouldReturnPositionsFromElasticsearch() throws IOException {
        Position position = new Position(null, "AAPL", "100", "150.00");

        when(hit.source()).thenReturn(position);
        when(hit.id()).thenReturn("1");
        when(hitsMetadata.hits()).thenReturn(List.of(hit));
        when(searchResponse.hits()).thenReturn(hitsMetadata);
        when(elasticsearchClient.search(any(java.util.function.Function.class), eq(Position.class)))
                .thenReturn(searchResponse);

        Map<String, List<Position>> results = elasticsearchService.search("AAPL", List.of("Position"));

        assertNotNull(results);
        assertTrue(results.containsKey("position"));
        assertEquals(1, results.get("position").size());
        assertEquals("AAPL", results.get("position").get(0).getSecurity());
    }

    @SuppressWarnings("unchecked")
    @Test
    void search_shouldReturnEmptyListWhenNoResults() throws IOException {
        when(hitsMetadata.hits()).thenReturn(List.of());
        when(searchResponse.hits()).thenReturn(hitsMetadata);
        when(elasticsearchClient.search(any(java.util.function.Function.class), eq(Position.class)))
                .thenReturn(searchResponse);

        Map<String, List<Position>> results = elasticsearchService.search("unknown", List.of("Position"));

        assertNotNull(results);
        assertTrue(results.get("position").isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    void search_shouldThrowIOExceptionOnElasticsearchFailure() throws IOException {
        when(elasticsearchClient.search(any(java.util.function.Function.class), eq(Position.class)))
                .thenThrow(new IOException("Connection refused"));

        assertThrows(IOException.class, () ->
                elasticsearchService.search("test", List.of("Position"))
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    void search_shouldSearchMultipleIndices() throws IOException {
        Position position = new Position(null, "AAPL", "100", "150.00");

        when(hit.source()).thenReturn(position);
        when(hit.id()).thenReturn("1");
        when(hitsMetadata.hits()).thenReturn(List.of(hit));
        when(searchResponse.hits()).thenReturn(hitsMetadata);
        when(elasticsearchClient.search(any(java.util.function.Function.class), eq(Position.class)))
                .thenReturn(searchResponse);

        Map<String, List<Position>> results = elasticsearchService.search(
                "AAPL", List.of("Position", "Trade"));

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.containsKey("position"));
        assertTrue(results.containsKey("trade"));
    }
}
