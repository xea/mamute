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
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.mamute.model.BadgeType.*;

public class BadgeEventObserver {

    @Inject private Result result;

    @Inject private BadgeDAO badgeDAO;

    @Inject private ReputationEventDAO reputationDAO;

    @Inject private UserDAO userDAO;

    @Inject private MessageFactory messageFactory;

    @Inject private LoggedUser currentUser;

    public User currentUser(final BadgeEvent event) {
        return currentUser.getCurrent();
    }

    public User questionAuthor(final BadgeEvent event) {
        final Question question = (Question) event.getContext();

        return question.getAuthor();
    }

    public void subscribeEvents(@Observes BadgeEvent badgeEvent) {
        final List<Evaluator> evaluators = new ArrayList<>();

        switch (badgeEvent.getEventType()) {
            case CREATED_QUESTION:
                evaluators.add(new Evaluator(FIRST_QUESTION, this::currentUser, this::firstQuestion));
                break;
            case CREATED_ANSWER:
                break;
            case QUESTION_UPVOTE:
                evaluators.add(new Evaluator(FIRST_QUESTION_SCORE_1, this::questionAuthor, this::firstQuestionWithScore1));
                evaluators.add(new Evaluator(QUESTION_SCORE_10, this::questionAuthor, this::questionScore10));
                evaluators.add(new Evaluator(QUESTION_SCORE_25, this::questionAuthor, this::questionScore25));
                evaluators.add(new Evaluator(QUESTION_SCORE_100, this::questionAuthor, this::questionScore100));
                break;
            case MARKED_SOLUTION:
                evaluators.add(new Evaluator(FIRST_QUESTION_ACCEPTED, this::currentUser, this::acceptFirstSolution));
                break;
            case LOGIN:
                evaluators.add(new Evaluator(VISIT_30_CONSECUTIVE_DAYS, this::currentUser, this::visit30ConsecutiveDays));
                evaluators.add(new Evaluator(VISIT_100_CONSECUTIVE_DAYS, this::currentUser, this::visit100ConsecutiveDays));
            default:
                break;
        }

        // For some reason this doesn't work if it's written using stream expressions
        // I'm suspecting BCI magic here.
        for (final Evaluator ev : evaluators) {
            final User user = ev.extractor.apply(badgeEvent);

            if (canAward(ev.badgeType, user) && ev.evaluator.apply(badgeEvent, user)) {
                final Badge newBadge = new Badge(user, ev.badgeType);
                result.include("mamuteMessages", Arrays.asList(messageFactory.build("badge-award", "badge.awarded", newBadge.getBadgeKey())));
                badgeDAO.awardBadge(newBadge);
            }
        }
    }

    protected boolean canAward(final BadgeType badgeType, final User user) {
        return (!user.hasBadge(badgeType) || badgeType.isMultiBadge());
    }

    public boolean firstQuestion(final BadgeEvent event, final User badgeUser) {
        return true;
    }

    public boolean firstQuestionWithScore1(final BadgeEvent event, final User badgeUser) {
        final Question question = (Question) event.getContext();

        final boolean awardBadge = question.getVoteCount() > 0;

        return awardBadge;
    }

    public boolean visit30ConsecutiveDays(final BadgeEvent event, final User user) {
        final boolean award = user.getMetadata().getConsecutiveDays() >= 30;

        return award;
    }

    public boolean visit100ConsecutiveDays(final BadgeEvent event, final User user) {
        final boolean award = user.getMetadata().getConsecutiveDays() >= 100;

        return award;
    }

    public boolean acceptFirstSolution(final BadgeEvent event, final User user) {
        return true;
    }

    public boolean questionScore10(final BadgeEvent event, final User user) {
        return questionScore(event, 10);
    }

    public boolean questionScore25(final BadgeEvent event, final User user) {
        return questionScore(event, 25);
    }

    public boolean questionScore100(final BadgeEvent event, final User user) {
        return questionScore(event, 100);
    }

    public boolean questionScore(final BadgeEvent event, final long threshold) {
        final Question question = (Question) event.getContext();

        final boolean award = question.getVoteCount() >= threshold;

        return award;
    }

    private static class Evaluator {

        public final BadgeType badgeType;

        public final Function<BadgeEvent, User> extractor;

        public final BiFunction<BadgeEvent, User, Boolean> evaluator;

        public Evaluator(final BadgeType badgeType, final Function<BadgeEvent, User> extractor, final BiFunction<BadgeEvent, User, Boolean> evaluator) {
            this.badgeType = badgeType;
            this.extractor = extractor;
            this.evaluator = evaluator;
        }
    }
}
