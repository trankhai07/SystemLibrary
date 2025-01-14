package com.mycompany.myapp.repository.search;

import com.mycompany.myapp.domain.Author;
import com.mycompany.myapp.repository.AuthorRepository;
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
 * Spring Data Elasticsearch repository for the {@link Author} entity.
 */
public interface AuthorSearchRepository extends ElasticsearchRepository<Author, Long>, AuthorSearchRepositoryInternal {}

interface AuthorSearchRepositoryInternal {
    Page<Author> search(String query, Pageable pageable);

    Page<Author> search(Query query);

    void index(Author entity);
}

class AuthorSearchRepositoryInternalImpl implements AuthorSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final AuthorRepository repository;
    private final SearchUtil searchUtil;

    AuthorSearchRepositoryInternalImpl(
        ElasticsearchRestTemplate elasticsearchTemplate,
        AuthorRepository repository,
        SearchUtil searchUtil
    ) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
        this.searchUtil = searchUtil;
    }

    @Override
    public Page<Author> search(String query, Pageable pageable) {
        Query searchQuery = new NativeSearchQueryBuilder()
            .withQuery(searchUtil.buildSearchQuery(query))
            .withPageable(searchUtil.pageableWithModifiedSort(pageable))
            .build();
        return search(searchQuery);
    }

    @Override
    public Page<Author> search(Query query) {
        SearchHits<Author> searchHits = elasticsearchTemplate.search(query, Author.class);
        List<Author> hits = searchHits.map(SearchHit::getContent).stream().collect(Collectors.toList());
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Author entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
