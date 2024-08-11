package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.myapp.domain.enumeration.Status;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A CheckOut.
 */
@Entity
@Table(name = "check_out")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "checkout")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CheckOut implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "is_returned")
    private Boolean isReturned;

    @ManyToOne
    @JsonIgnoreProperties(value = { "checkOuts" }, allowSetters = true)
    private BookCopy bookCopy;

    @ManyToOne
    @JsonIgnoreProperties(value = { "notifications", "waitLists", "checkOuts" }, allowSetters = true)
    private PatronAccount patronAccount;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CheckOut id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getStartTime() {
        return this.startTime;
    }

    public CheckOut startTime(Instant startTime) {
        this.setStartTime(startTime);
        return this;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return this.endTime;
    }

    public CheckOut endTime(Instant endTime) {
        this.setEndTime(endTime);
        return this;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Status getStatus() {
        return this.status;
    }

    public CheckOut status(Status status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Boolean getIsReturned() {
        return this.isReturned;
    }

    public CheckOut isReturned(Boolean isReturned) {
        this.setIsReturned(isReturned);
        return this;
    }

    public void setIsReturned(Boolean isReturned) {
        this.isReturned = isReturned;
    }

    public BookCopy getBookCopy() {
        return this.bookCopy;
    }

    public void setBookCopy(BookCopy bookCopy) {
        this.bookCopy = bookCopy;
    }

    public CheckOut bookCopy(BookCopy bookCopy) {
        this.setBookCopy(bookCopy);
        return this;
    }

    public PatronAccount getPatronAccount() {
        return this.patronAccount;
    }

    public void setPatronAccount(PatronAccount patronAccount) {
        this.patronAccount = patronAccount;
    }

    public CheckOut patronAccount(PatronAccount patronAccount) {
        this.setPatronAccount(patronAccount);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CheckOut)) {
            return false;
        }
        return id != null && id.equals(((CheckOut) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CheckOut{" +
            "id=" + getId() +
            ", startTime='" + getStartTime() + "'" +
            ", endTime='" + getEndTime() + "'" +
            ", status='" + getStatus() + "'" +
            ", isReturned='" + getIsReturned() + "'" +
            "}";
    }
}
