package org.mamute.model;

public enum BadgeType {
    // Ask first question ever
    FIRST_QUESTION(BadgeClass.BRONZE, "badge.first_question"),
    // First question that has a score of 1
    FIRST_QUESTION_SCORE_1(BadgeClass.BRONZE, "badge.first_question_score_1"),
    // First question with an accepted answer
    FIRST_QUESTION_ACCEPTED(BadgeClass.BRONZE, "badge.first_question_accepted"),

    // Ask a question that gets at least 10 score
    QUESTION_SCORE_10(BadgeClass.BRONZE, "badge.question_score.10_score", true),
    // Ask a question that gets at least 25 score
    QUESTION_SCORE_25(BadgeClass.SILVER, "badge.question_score.25_score", true),
    // Ask a question that gets at least 100 score
    QUESTION_SCORE_100(BadgeClass.GOLD, "badge.question_score.100_score", true),
    // Ask a question that is viewed at least 50 times
    QUESTION_VIEW_50(BadgeClass.BRONZE, "badge.question_view.50_times", true),
    // Ask a question that is viewed at least 250 times
    QUESTION_VIEW_250(BadgeClass.SILVER, "badge.question_view.250_times", true),
    // Ask a question that is viewed at least 500 times
    QUESTION_VIEW_500(BadgeClass.GOLD, "badge.question_view.500_times", true),

    /* Not imlemented yet

    // Ask a question with a positive score on 5 different days
    QUESTION_SERIES_5(BadgeClass.BRONZE, "badge.question_series.5_days", true),
    // Ask a question with a positive score on 30 different days
    QUESTION_SERIES_30(BadgeClass.SILVER, "badge.question_series.30_days", true),
    // Ask a question with a positive score on 100 different days
    QUESTION_SERIES_100(BadgeClass.GOLD, "badge.question_series.100_days", true),
     */

    // Post your first answer
    FIRST_ANSWER(BadgeClass.BRONZE, "badge.first_answer"),
    // First answer to the a question that is accepted with a score of 10 or more
    FIRST_ANSWER_ACCEPTED_SCORE_10(BadgeClass.SILVER, "badge.first_to_answer_accepted_score_10", true),
    // Post an answer that receives a score of 10
    ANSWER_SCORE_10(BadgeClass.BRONZE, "badge.answer_score.10_score", true),
    // Post an answer that receives a score of 25
    ANSWER_SCORE_25(BadgeClass.SILVER, "badge.answer_score.25_score", true),
    // Post an answer that receives a score of 100
    ANSWER_SCORE_100(BadgeClass.GOLD, "badge.answer_score.100_score", true),
    // Post an answer that outscores the accepted answer by the factor of two
    ANSWER_OUTSCORE_ACCEPTED_2(BadgeClass.SILVER, "badge.answer_outscore.2_factor", true),
    // Post an answer that outscores the accepted answer by the factor of five
    ANSWER_OUTSCORE_ACCEPTED_5(BadgeClass.GOLD, "badge.answer_outscore.5_factor", true),
    // Answer a question more than 30 days after it was asked with a score of 2
    ANSWER_REVIVE_QUESTION_30(BadgeClass.BRONZE, "badge.answer_revive_question.30_day"),
    // Answer a question more than 60 days after it was asked with a score of 5
    ANSWER_REVIVE_QUESTION_60(BadgeClass.SILVER, "badge.answer_revive_question.60_day"),

    // Log in on 30 consecutive days (days as in not more than 24 hours than last login)
    VISIT_30_CONSECUTIVE_DAYS(BadgeClass.SILVER, "badge.visit.30_days"),
    // Log in on 100 consecutive days (days as in not more than 24 hours than last login)
    VISIT_100_CONSECUTIVE_DAYS(BadgeClass.GOLD, "badge.visit.100_days"),

    // Leave 10 comments
    COMMENT_10(BadgeClass.BRONZE, "badge.comment_10"),
    // Leave 50 comments
    COMMENT_50(BadgeClass.SILVER, "badge.comment_50"),
    // Leave 10 comments with a score of at least 5
    COMMENT_10_SCORE_5(BadgeClass.SILVER, "badge.comment_10.score_5"),
    // Leave 50 comments with a score of at least 5
    COMMENT_50_SCORE_5(BadgeClass.GOLD, "badge.comment_50.score_5"),

/*
    // first question completely ignored for a week (no answer, no comment, no score, no views)
    FIRST_QUESTION_IGNORED(BadgeClass.BRONZE, "badge.first_question_ignored"),
    QUESTION_FAVOURITE_10(BadgeClass.BRONZE, "badge.question_favourite.10_people"),
    QUESTION_FAVOURITE_25(BadgeClass.SILVER, "badge.question_favourite.25_people"),
    QUESTION_FAVOURITE_50(BadgeClass.GOLD, "badge.question_favourite.50_people"),

    FIRST_TO_ANSWER_ACCEPTED(BadgeClass.SILVER, "badge.first_accepted_answer"),
    ANSWER_ACCEPTED_DIFFERENT_TAGS_10(BadgeClass.BRONZE, "badge.answer_accepted.10_tags"),
    ANSWER_ACCEPTED_DIFFERENT_TAGS_25(BadgeClass.SILVER, "badge.answer_accepted.25_tags"),
    ANSWER_ACCEPTED_DIFFERENT_TAGS_50(BadgeClass.GOLD, "badge.answer_accepted.50_tags"),
    ANSWER_OWN_QUESTION_SCORE_2(BadgeClass.BRONZE, "badge.answer_own_question.2_score"),
    ANSWER_ACCEPTED_NO_SCORE_5(BadgeClass.SILVER, "badge.answer_accepted_no_score.5_times"),
    ANSWER_ACCEPTED_NO_SCORE_10(BadgeClass.GOLD, "badge.answer_accepted_no_score.10_times"),

    FIRST_BOUNTY_OFFER_SELF(BadgeClass.BRONZE, "badge.bounty_offer.self"),
    FIRST_BOUNTY_OFFER_OTHER(BadgeClass.BRONZE, "badge.bounty_offer.other"),

    AUTOBIOGRAPHY_COMPLETE(BadgeClass.BRONZE, "badge.autobiography_complete"),


    EARN_200_DAILY_REPUTATION_1_DAY(BadgeClass.BRONZE, "badge.earn_200_rep.1_day"),
    EARN_200_DAILY_REPUTATION_50_DAYS(BadgeClass.SILVER, "badge.earn_200_rep.50_days"),
    EARN_200_DAILY_REPUTATION_150_DAYS(BadgeClass.GOLD, "badge.earn_200_rep.150_days"),
    */
    ;



    private String id;

    private BadgeClass badgeClass;

    private boolean multiBadge;

    BadgeType(final BadgeClass badgeClass, final String id) {
        this(badgeClass, id, false);
    }

    BadgeType(final BadgeClass badgeClass, final String id, final boolean multiBadge) {
        this.id = id;
        this.badgeClass = badgeClass;
        this.multiBadge = multiBadge;
    }

    public String getId() {
        return id;
    }

    public String getDescriptionId() {
        return id + ".description";
    }

    public BadgeClass getBadgeClass() {
        return badgeClass;
    }

    public boolean isMultiBadge() {
        return multiBadge;
    }
}
