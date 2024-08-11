package com.mycompany.myapp.repository.search;

import com.mycompany.myapp.domain.Book;
import com.mycompany.myapp.repository.BookRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Book} entity.
 */
public interface BookSearchRepository extends ElasticsearchRepository<Book, Long>, BookSearchRepositoryInternal {}

interface BookSearchRepositoryInternal {
    Page<Book> search(String query, Pageable pageable);
    Page<Book> searchByCategory(String query, long categoryId, Pageable pageable);
    Page<Book> search(Query query);

    void index(Book entity);
}

class BookSearchRepositoryInternalImpl implements BookSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final BookRepository repository;
    private final SearchUtil searchUtil;

    BookSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, BookRepository repository, SearchUtil searchUtil) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
        this.searchUtil = searchUtil;
    }

    @Override
    public Page<Book> searchByCategory(String query, long categoryId, Pageable pageable) {
        if (categoryId == -1) return search(query, pageable);
        BoolQueryBuilder boolQuery = QueryBuilders
            .boolQuery()
            .must(QueryBuilders.termQuery("category.id", categoryId))
            .should(QueryBuilders.matchPhrasePrefixQuery("title", query))
            .should(QueryBuilders.matchPhrasePrefixQuery("description", query))
            .should(QueryBuilders.matchPhrasePrefixQuery("authors.name", query))
            .minimumShouldMatch(1);
        Query searchQuery = new NativeSearchQueryBuilder()
            .withQuery(boolQuery)
            .withPageable(searchUtil.pageableWithModifiedSort(pageable))
            .build();
        return search(searchQuery);
    }

    @Override
    public Page<Book> search(String query, Pageable pageable) {
        BoolQueryBuilder boolQuery = QueryBuilders
            .boolQuery()
            .should(QueryBuilders.matchPhrasePrefixQuery("title", query))
            .should(QueryBuilders.matchPhrasePrefixQuery("description", query))
            .should(QueryBuilders.matchPhrasePrefixQuery("authors.name", query))
            .should(QueryBuilders.matchPhrasePrefixQuery("category.name", query))
            .minimumShouldMatch(1);
        Query searchQuery = new NativeSearchQueryBuilder()
            .withQuery(boolQuery)
            .withPageable(searchUtil.pageableWithModifiedSort(pageable))
            .build();

        return search(searchQuery);
    }

    @Override
    public Page<Book> search(Query query) {
        SearchHits<Book> searchHits = elasticsearchTemplate.search(query, Book.class);
        List<Book> hits = searchHits.map(SearchHit::getContent).stream().collect(Collectors.toList());
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Book entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
