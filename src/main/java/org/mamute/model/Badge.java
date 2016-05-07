package org.mamute.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.mamute.providers.SessionFactoryCreator;

import javax.persistence.*;

@Entity
@Cacheable
@Cache(usage= CacheConcurrencyStrategy.READ_WRITE, region="cache")
@Table(name="Badges")
public class Badge {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    private String badgeKey;

    private String comment;

    private Long contextId;

    @Type(type = SessionFactoryCreator.JODA_TIME_TYPE)
    private final DateTime createdAt = new DateTime();

    public Badge() {
    }

    public Badge(final User user, final String badgeKey) {
        this.user = user;
        this.badgeKey = badgeKey;
        this.contextId = 0L;
    }

    public Badge(final User user, final BadgeType badgeType) {
        this.user = user;
        this.badgeKey = badgeType.toString();
        this.contextId = 0L;
    }

    public Badge(final User user, final BadgeType badgeType, final ReputationEventContext context) {
        this.user = user;
        this.badgeKey = badgeType.toString();

        if (context == null) {
            this.contextId = 0L;
        } else {
            this.contextId = context.getId();
        }

    }

    public Badge(final User user, final BadgeType badgeType, final long contextId) {
        this.user = user;
        this.badgeKey = badgeType.toString();
        this.contextId = contextId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBadgeKey() {
        return badgeKey;
    }

    public void setBadgeKey(String badgeKey) {
        this.badgeKey = badgeKey;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public Long getContextId() {
        return contextId;
    }

    public void setContextId(Long contextId) {
        this.contextId = contextId;
    }
}
