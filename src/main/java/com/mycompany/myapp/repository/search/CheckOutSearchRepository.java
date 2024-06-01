package com.mycompany.myapp.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.mycompany.myapp.domain.CheckOut;
import com.mycompany.myapp.repository.CheckOutRepository;
import java.util.List;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data Elasticsearch repository for the {@link CheckOut} entity.
 */
public interface CheckOutSearchRepository extends ElasticsearchRepository<CheckOut, Long>, CheckOutSearchRepositoryInternal {}

interface CheckOutSearchRepositoryInternal {
    Page<CheckOut> search(String query, Pageable pageable);

    Page<CheckOut> search(Query query);

    void index(CheckOut entity);
}

class CheckOutSearchRepositoryInternalImpl implements CheckOutSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final CheckOutRepository repository;

    CheckOutSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, CheckOutRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<CheckOut> search(String query, Pageable pageable) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return search(nativeSearchQuery.setPageable(pageable));
    }

    @Override
    public Page<CheckOut> search(Query query) {
        SearchHits<CheckOut> searchHits = elasticsearchTemplate.search(query, CheckOut.class);
        List<CheckOut> hits = searchHits.map(SearchHit::getContent).stream().collect(Collectors.toList());
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(CheckOut entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
