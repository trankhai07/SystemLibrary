package com.mycompany.myapp.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.mycompany.myapp.domain.Book;
import com.mycompany.myapp.repository.BookRepository;
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
 * Spring Data Elasticsearch repository for the {@link Book} entity.
 */
public interface BookSearchRepository extends ElasticsearchRepository<Book, Long>, BookSearchRepositoryInternal {}

interface BookSearchRepositoryInternal {
    Page<Book> search(String query, Pageable pageable);

    Page<Book> search(Query query);

    void index(Book entity);
}

class BookSearchRepositoryInternalImpl implements BookSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final BookRepository repository;

    BookSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, BookRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Book> search(String query, Pageable pageable) {
        BoolQueryBuilder boolQuery = QueryBuilders
            .boolQuery()
            .should(QueryBuilders.matchQuery("title", query).fuzziness("AUTO"))
            .should(QueryBuilders.matchQuery("description", query).fuzziness("AUTO"))
            .should(QueryBuilders.matchQuery("authors.name", query).fuzziness("AUTO"))
            .should(QueryBuilders.matchQuery("category.name", query).fuzziness("AUTO"));

        Query searchQuery = new NativeSearchQueryBuilder().withQuery(boolQuery).withPageable(pageable).build();

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
