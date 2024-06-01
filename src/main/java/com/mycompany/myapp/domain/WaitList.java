package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A WaitList.
 */
@Entity
@Table(name = "wait_list")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "waitlist")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class WaitList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "creat_at")
    private Instant creatAt;

    @ManyToOne
    @JsonIgnoreProperties(value = { "user", "notifications", "waitLists", "checkOuts" }, allowSetters = true)
    private PatronAccount patronAccount;

    @ManyToOne
    @JsonIgnoreProperties(value = { "bookCopies", "waitLists", "authors", "category" }, allowSetters = true)
    private Book book;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public WaitList id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreatAt() {
        return this.creatAt;
    }

    public WaitList creatAt(Instant creatAt) {
        this.setCreatAt(creatAt);
        return this;
    }

    public void setCreatAt(Instant creatAt) {
        this.creatAt = creatAt;
    }

    public PatronAccount getPatronAccount() {
        return this.patronAccount;
    }

    public void setPatronAccount(PatronAccount patronAccount) {
        this.patronAccount = patronAccount;
    }

    public WaitList patronAccount(PatronAccount patronAccount) {
        this.setPatronAccount(patronAccount);
        return this;
    }

    public Book getBook() {
        return this.book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public WaitList book(Book book) {
        this.setBook(book);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WaitList)) {
            return false;
        }
        return id != null && id.equals(((WaitList) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "WaitList{" +
            "id=" + getId() +
            ", creatAt='" + getCreatAt() + "'" +
            "}";
    }
}
