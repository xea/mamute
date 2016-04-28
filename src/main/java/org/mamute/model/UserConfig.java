package org.mamute.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.mamute.providers.SessionFactoryCreator;

import javax.persistence.*;

@Entity
@Cacheable
@Cache(usage= CacheConcurrencyStrategy.READ_WRITE, region="cache")
@Table(name="UserConfig")
public class UserConfig {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    private String variable;

    private String value;

    private String comment;

    @Type(type = SessionFactoryCreator.JODA_TIME_TYPE)
    private final DateTime createdAt = new DateTime();

    public UserConfig() {
    }

    public UserConfig(final String variable, final String value) {
        this(variable, value, null);
    }

    public UserConfig(final String variable, final String value, final String comment) {
        this.variable = variable;
        this.value = value;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
