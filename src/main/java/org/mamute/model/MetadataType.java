package org.mamute.model;

public enum MetadataType {

    DAILY_REPUTATION("daily_reputation"),
    DAILY_VOTES("daily_votes"),
    QUESTION_SERIES("question_series"),
    CONSECUTIVE_LOGINS("consecutive_logins");

    private String id;

    MetadataType(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
