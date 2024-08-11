package com.mycompany.myapp.repository.search;

import com.mycompany.myapp.domain.Publisher;
import com.mycompany.myapp.repository.PublisherRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Publisher} entity.
 */
public interface PublisherSearchRepository extends ElasticsearchRepository<Publisher, Long>, PublisherSearchRepositoryInternal {}

interface PublisherSearchRepositoryInternal {
    Page<Publisher> search(String query, Pageable pageable);

    Page<Publisher> search(Query query);

    void index(Publisher entity);
}

class PublisherSearchRepositoryInternalImpl implements PublisherSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final PublisherRepository repository;
    private final SearchUtil searchUtil;

    PublisherSearchRepositoryInternalImpl(
        ElasticsearchRestTemplate elasticsearchTemplate,
        PublisherRepository repository,
        SearchUtil searchUtil
    ) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
        this.searchUtil = searchUtil;
    }

    @Override
    public Page<Publisher> search(String query, Pageable pageable) {
        Query searchQuery = new NativeSearchQueryBuilder()
            .withQuery(searchUtil.buildSearchQuery(query))
            .withPageable(searchUtil.pageableWithModifiedSort(pageable))
            .build();
        return search(searchQuery);
    }

    @Override
    public Page<Publisher> search(Query query) {
        SearchHits<Publisher> searchHits = elasticsearchTemplate.search(query, Publisher.class);
        List<Publisher> hits = searchHits.map(SearchHit::getContent).stream().collect(Collectors.toList());
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Publisher entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
