package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Book;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Book entity.
 * <p>
 * When extending this class, extend BookRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */
@Repository
public interface BookRepository extends BookRepositoryWithBagRelationships, JpaRepository<Book, Long> {
    default Optional<Book> findOneWithEagerRelationships(Long id) {
        return this.fetchBagRelationships(this.findOneWithToOneRelationships(id));
    }

    default List<Book> findAllWithEagerRelationships() {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships());
    }

    default Page<Book> findAllWithEagerRelationships(Pageable pageable) {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships(pageable));
    }

    @Query(
        value = "select distinct book from Book book left join fetch book.category",
        countQuery = "select count(distinct book) from Book book"
    )
    Page<Book> findAllWithToOneRelationships(Pageable pageable);

    @Query("select distinct book from Book book left join fetch book.category")
    List<Book> findAllWithToOneRelationships();

    @Query("select book from Book book left join fetch book.category where book.id =:id")
    Optional<Book> findOneWithToOneRelationships(@Param("id") Long id);

    @Query(
        "select distinct book from Book book " +
        "left join fetch book.category " +
        "left join fetch book.authors " +
        "left join fetch book.bookCopies " +
        "where book.id = :id"
    )
    Optional<Book> findOne(@Param("id") Long id);

    @EntityGraph(attributePaths = { "category", "authors" })
    @Query("select book from Book book where book.id in :ids")
    List<Book> findAllByIdWithAssociations(@Param("ids") List<Long> ids);

    @Query(
        value = "select book.id from Book book where book.category.id = :id",
        countQuery = "select count(book) from Book book where book.category.id = :id"
    )
    Page<Long> findBookIdsByCategoryId(@Param("id") long categoryId, Pageable pageable);

    @Query("select distinct book from Book book  join fetch book.category join fetch book.authors where book.category.id =:id")
    List<Book> findAllByCategoryIdNoPageable(@Param("id") long categoryId);

    @Query("select distinct book from Book book join  book.authors author where author.id=:authorId")
    List<Book> findAllByAuthorId(@Param("authorId") long authorId);
}
