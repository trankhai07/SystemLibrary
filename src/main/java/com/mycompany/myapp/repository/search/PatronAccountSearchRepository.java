package com.mycompany.myapp.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.mycompany.myapp.domain.Book;
import com.mycompany.myapp.domain.PatronAccount;
import com.mycompany.myapp.repository.PatronAccountRepository;
import java.util.List;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
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

    PatronAccountSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, PatronAccountRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<PatronAccount> search(String query, Pageable pageable) {
        BoolQueryBuilder boolQuery = QueryBuilders
            .boolQuery()
            .should(QueryBuilders.matchQuery("cardNumber", query).fuzziness("AUTO"))
            .should(QueryBuilders.matchQuery("user.login", query).fuzziness("AUTO"))
            .should(QueryBuilders.matchQuery("user.email", query).fuzziness("AUTO"))
            .should(QueryBuilders.matchQuery("user.lastName", query).fuzziness("AUTO"))
            .should(QueryBuilders.matchQuery("user.firstName", query).fuzziness("AUTO"));

        Query searchQuery = new NativeSearchQueryBuilder().withQuery(boolQuery).withPageable(pageable).build();

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
