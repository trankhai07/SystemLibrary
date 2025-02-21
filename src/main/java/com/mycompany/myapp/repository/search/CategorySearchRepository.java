package com.mycompany.myapp.repository.search;

import com.mycompany.myapp.domain.Category;
import com.mycompany.myapp.repository.CategoryRepository;
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
 * Spring Data Elasticsearch repository for the {@link Category} entity.
 */
public interface CategorySearchRepository extends ElasticsearchRepository<Category, Long>, CategorySearchRepositoryInternal {}

interface CategorySearchRepositoryInternal {
    Page<Category> search(String query, Pageable pageable);

    Page<Category> search(Query query);

    void index(Category entity);
}

class CategorySearchRepositoryInternalImpl implements CategorySearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final CategoryRepository repository;
    private final SearchUtil searchUtil;

    CategorySearchRepositoryInternalImpl(
        ElasticsearchRestTemplate elasticsearchTemplate,
        CategoryRepository repository,
        SearchUtil searchUtil
    ) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
        this.searchUtil = searchUtil;
    }

    @Override
    public Page<Category> search(String query, Pageable pageable) {
        Query searchQuery = new NativeSearchQueryBuilder()
            .withQuery(searchUtil.buildSearchQuery(query))
            .withPageable(searchUtil.pageableWithModifiedSort(pageable))
            .build();
        return search(searchQuery);
    }

    @Override
    public Page<Category> search(Query query) {
        SearchHits<Category> searchHits = elasticsearchTemplate.search(query, Category.class);
        List<Category> hits = searchHits.map(SearchHit::getContent).stream().collect(Collectors.toList());
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Category entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
