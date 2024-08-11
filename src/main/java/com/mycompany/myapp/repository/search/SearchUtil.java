package com.mycompany.myapp.repository.search;

import java.util.Objects;
import java.util.stream.Collectors;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class SearchUtil {

    public BoolQueryBuilder buildSearchQuery(String query) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (isNumeric(query)) {
            boolQuery.should(QueryBuilders.matchPhrasePrefixQuery("id", query));
        }

        boolQuery
            .should(QueryBuilders.matchPhrasePrefixQuery("name", query))
            .should(QueryBuilders.matchPhrasePrefixQuery("description", query));
        return boolQuery;
    }

    private boolean isNumeric(String str) {
        if (Objects.isNull(str) || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public Pageable pageableWithModifiedSort(Pageable pageable) {
        Sort sort = pageable.getSort();
        Sort modifiedSort = Sort.by(
            sort
                .get()
                .map(order -> {
                    if (!order.getProperty().equals("id")) {
                        return Sort.Order.by(order.getProperty() + ".keyword").with(order.getDirection());
                    }
                    return order;
                })
                .collect(Collectors.toList())
        );

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), modifiedSort);
    }
}
