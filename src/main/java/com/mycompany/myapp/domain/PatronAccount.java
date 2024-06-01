package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.domain.Persistable;

/**
 * A PatronAccount.
 */
@JsonIgnoreProperties(value = { "new" })
@Entity
@Table(name = "patron_account")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "patronaccount")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PatronAccount implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @Size(max = 10)
    @Id
    @Column(name = "card_number", length = 10)
    @org.springframework.data.annotation.Id
    private String cardNumber;

    @Transient
    private boolean isPersisted;

    @OneToOne
    @JoinColumn(unique = true)
    private User user;

    @OneToMany(mappedBy = "patronAccount")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "patronAccount" }, allowSetters = true)
    private Set<Notification> notifications = new HashSet<>();

    @OneToMany(mappedBy = "patronAccount")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "patronAccount", "book" }, allowSetters = true)
    private Set<WaitList> waitLists = new HashSet<>();

    @OneToMany(mappedBy = "patronAccount")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "bookCopy", "patronAccount" }, allowSetters = true)
    private Set<CheckOut> checkOuts = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getCardNumber() {
        return this.cardNumber;
    }

    public PatronAccount cardNumber(String cardNumber) {
        this.setCardNumber(cardNumber);
        return this;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public String getId() {
        return this.cardNumber;
    }

    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public PatronAccount setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PatronAccount user(User user) {
        this.setUser(user);
        return this;
    }

    public Set<Notification> getNotifications() {
        return this.notifications;
    }

    public void setNotifications(Set<Notification> notifications) {
        if (this.notifications != null) {
            this.notifications.forEach(i -> i.setPatronAccount(null));
        }
        if (notifications != null) {
            notifications.forEach(i -> i.setPatronAccount(this));
        }
        this.notifications = notifications;
    }

    public PatronAccount notifications(Set<Notification> notifications) {
        this.setNotifications(notifications);
        return this;
    }

    public PatronAccount addNotification(Notification notification) {
        this.notifications.add(notification);
        notification.setPatronAccount(this);
        return this;
    }

    public PatronAccount removeNotification(Notification notification) {
        this.notifications.remove(notification);
        notification.setPatronAccount(null);
        return this;
    }

    public Set<WaitList> getWaitLists() {
        return this.waitLists;
    }

    public void setWaitLists(Set<WaitList> waitLists) {
        if (this.waitLists != null) {
            this.waitLists.forEach(i -> i.setPatronAccount(null));
        }
        if (waitLists != null) {
            waitLists.forEach(i -> i.setPatronAccount(this));
        }
        this.waitLists = waitLists;
    }

    public PatronAccount waitLists(Set<WaitList> waitLists) {
        this.setWaitLists(waitLists);
        return this;
    }

    public PatronAccount addWaitList(WaitList waitList) {
        this.waitLists.add(waitList);
        waitList.setPatronAccount(this);
        return this;
    }

    public PatronAccount removeWaitList(WaitList waitList) {
        this.waitLists.remove(waitList);
        waitList.setPatronAccount(null);
        return this;
    }

    public Set<CheckOut> getCheckOuts() {
        return this.checkOuts;
    }

    public void setCheckOuts(Set<CheckOut> checkOuts) {
        if (this.checkOuts != null) {
            this.checkOuts.forEach(i -> i.setPatronAccount(null));
        }
        if (checkOuts != null) {
            checkOuts.forEach(i -> i.setPatronAccount(this));
        }
        this.checkOuts = checkOuts;
    }

    public PatronAccount checkOuts(Set<CheckOut> checkOuts) {
        this.setCheckOuts(checkOuts);
        return this;
    }

    public PatronAccount addCheckOut(CheckOut checkOut) {
        this.checkOuts.add(checkOut);
        checkOut.setPatronAccount(this);
        return this;
    }

    public PatronAccount removeCheckOut(CheckOut checkOut) {
        this.checkOuts.remove(checkOut);
        checkOut.setPatronAccount(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PatronAccount)) {
            return false;
        }
        return cardNumber != null && cardNumber.equals(((PatronAccount) o).cardNumber);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PatronAccount{" +
            "cardNumber=" + getCardNumber() +
            "}";
    }
}
