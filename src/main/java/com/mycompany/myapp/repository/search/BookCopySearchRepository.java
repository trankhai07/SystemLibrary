package com.mycompany.myapp.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.mycompany.myapp.domain.BookCopy;
import com.mycompany.myapp.repository.BookCopyRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link BookCopy} entity.
 */
public interface BookCopySearchRepository extends ElasticsearchRepository<BookCopy, Long>, BookCopySearchRepositoryInternal {}

interface BookCopySearchRepositoryInternal {
    Page<BookCopy> search(String query, Pageable pageable);

    Page<BookCopy> search(Query query);

    void index(BookCopy entity);
}

class BookCopySearchRepositoryInternalImpl implements BookCopySearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final BookCopyRepository repository;
    private final SearchUtil searchUtil;

    BookCopySearchRepositoryInternalImpl(
        ElasticsearchRestTemplate elasticsearchTemplate,
        BookCopyRepository repository,
        SearchUtil searchUtil
    ) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
        this.searchUtil = searchUtil;
    }

    @Override
    public Page<BookCopy> search(String query, Pageable pageable) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return search(nativeSearchQuery.setPageable(searchUtil.pageableWithModifiedSort(pageable)));
    }

    @Override
    public Page<BookCopy> search(Query query) {
        SearchHits<BookCopy> searchHits = elasticsearchTemplate.search(query, BookCopy.class);
        List<BookCopy> hits = searchHits.map(SearchHit::getContent).stream().collect(Collectors.toList());
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(BookCopy entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
