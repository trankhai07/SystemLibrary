package com.mycompany.myapp.repository.search;

import com.mycompany.myapp.domain.PatronAccount;
import com.mycompany.myapp.repository.PatronAccountRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
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
 * Spring Data Elasticsearch repository for the {@link PatronAccount} entity.
 */
public interface PatronAccountSearchRepository
    extends ElasticsearchRepository<PatronAccount, String>, PatronAccountSearchRepositoryInternal {}

interface PatronAccountSearchRepositoryInternal {
    Page<PatronAccount> search(String query, Pageable pageable);

    Page<PatronAccount> search(Query query);

    void index(PatronAccount entity);
}

class PatronAccountSearchRepositoryInternalImpl implements PatronAccountSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final PatronAccountRepository repository;
    private final SearchUtil searchUtil;

    PatronAccountSearchRepositoryInternalImpl(
        ElasticsearchRestTemplate elasticsearchTemplate,
        PatronAccountRepository repository,
        SearchUtil searchUtil
    ) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
        this.searchUtil = searchUtil;
    }

    @Override
    public Page<PatronAccount> search(String query, Pageable pageable) {
        BoolQueryBuilder boolQuery = QueryBuilders
            .boolQuery()
            .should(QueryBuilders.matchPhrasePrefixQuery("cardNumber", query))
            .should(QueryBuilders.matchPhrasePrefixQuery("user.login", query))
            .should(QueryBuilders.matchPhrasePrefixQuery("user.email", query))
            .should(QueryBuilders.matchPhrasePrefixQuery("user.lastName", query))
            .should(QueryBuilders.matchPhrasePrefixQuery("user.firstName", query));

        Query searchQuery = new NativeSearchQueryBuilder()
            .withQuery(boolQuery)
            .withPageable(searchUtil.pageableWithModifiedSort(pageable))
            .build();

        return search(searchQuery);
    }

    @Override
    public Page<PatronAccount> search(Query query) {
        SearchHits<PatronAccount> searchHits = elasticsearchTemplate.search(query, PatronAccount.class);
        List<PatronAccount> hits = searchHits.map(SearchHit::getContent).stream().collect(Collectors.toList());
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(PatronAccount entity) {
        repository.findOneWithEagerRelationships(entity.getCardNumber()).ifPresent(elasticsearchTemplate::save);
    }
}
