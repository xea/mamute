package org.mamute.observer;

import br.com.caelum.vraptor.Result;
import org.mamute.dao.BadgeDAO;
import org.mamute.dao.ReputationEventDAO;
import org.mamute.dao.UserDAO;
import org.mamute.event.BadgeEvent;
import org.mamute.factory.MessageFactory;
import org.mamute.model.*;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class BadgeEventObserver {

    @Inject private Result result;

    @Inject private BadgeDAO badgeDAO;

    @Inject private ReputationEventDAO reputationDAO;

    @Inject private UserDAO userDAO;

    @Inject private MessageFactory messageFactory;

//    @Inject private LoggedUser currentUser;

    public void subscribeEvents(@Observes BadgeEvent badgeEvent) {
        final List<Function<BadgeEvent, Optional<Badge>>> evaluators = new ArrayList<>();

        switch (badgeEvent.getEventType()) {
            case CREATED_QUESTION:
                evaluators.add(this::considerFirstQuestion);
                break;
            case CREATED_ANSWER:
                break;
            case QUESTION_UPVOTE:
                evaluators.add(this::considerFirstQuestionWithScore1);
                evaluators.add(this::considerQuestionScore10);
                break;
            case MARKED_SOLUTION:
                evaluators.add(this::considerAcceptFirstSolution);
                break;
            case LOGIN:
                evaluators.add(this::consider30ConsecutiveDays);
                evaluators.add(this::consider100ConsecutiveDays);
            default:
                break;
        }

        for (final Function<BadgeEvent, Optional<Badge>> fn : evaluators) {
            final Optional<Badge> badge = fn.apply(badgeEvent);

            if (badge.isPresent()) {
                result.include("mamuteMessages", Arrays.asList(messageFactory.build("badge-award", "badge.awarded", badge.get().getBadgeKey())));
                badgeDAO.awardBadge(badge.get());
            }
        }
    }

    protected boolean canAward(final BadgeType badgeType, final User user) {
        return (!user.hasBadge(badgeType) || badgeType.isMultiBadge());
    }

    public Optional<Badge> considerFirstQuestion(final BadgeEvent event) {
        final User user = event.getUser();

        if (canAward(BadgeType.FIRST_QUESTION, user)) {
            return Optional.of(new Badge(user, BadgeType.FIRST_QUESTION));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Badge> considerFirstQuestionWithScore1(final BadgeEvent event) {
        final User user = event.getUser();
        final Question question = (Question) event.getContext();

        if (canAward(BadgeType.FIRST_QUESTION_SCORE_1, user) && question.getVoteCount() > 0) {
            return Optional.of(new Badge(user, BadgeType.FIRST_QUESTION_SCORE_1));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Badge> consider30ConsecutiveDays(final BadgeEvent event) {
        final User user = event.getUser();

        if (canAward(BadgeType.VISIT_30_CONSECUTIVE_DAYS, user) && user.getMetadata().getConsecutiveDays() >= 30) {
            return Optional.of(new Badge(user, BadgeType.VISIT_30_CONSECUTIVE_DAYS));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Badge> consider100ConsecutiveDays(final BadgeEvent event) {
        final User user = event.getUser();

        if (canAward(BadgeType.VISIT_100_CONSECUTIVE_DAYS, user) && user.getMetadata().getConsecutiveDays() >= 100) {
            return Optional.of(new Badge(user, BadgeType.VISIT_100_CONSECUTIVE_DAYS));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Badge> considerAcceptFirstSolution(final BadgeEvent event) {
        final User user = event.getUser(); //currentUser.getCurrent();

        if (canAward(BadgeType.FIRST_QUESTION_ACCEPTED, user)) {
            return Optional.of(new Badge(user, BadgeType.FIRST_QUESTION_ACCEPTED));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Badge> considerQuestionScore10(final BadgeEvent event) {
        final User user = event.getUser();
        final Question question = (Question) event.getContext();

        if (canAward(BadgeType.QUESTION_SCORE_10, user) && question.getVoteCount() >= 10) {
            return Optional.of(new Badge(user, BadgeType.QUESTION_SCORE_10));
        } else {
            return Optional.empty();
        }
    }
}
