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

    public User answerAuthor(final BadgeEvent event) {
        final Answer answer = (Answer) event.getContext();

        return answer.getAuthor();
    }

    public void subscribeEvents(@Observes BadgeEvent badgeEvent) {
        final List<Evaluator> evaluators = new ArrayList<>();

        switch (badgeEvent.getEventType()) {
            case CREATED_QUESTION:
                evaluators.add(new Evaluator(FIRST_QUESTION, this::currentUser, this::firstQuestion));
                break;
            case CREATED_ANSWER:
                evaluators.add(new Evaluator(FIRST_ANSWER, this::currentUser, this::firstAnswer));
                break;
            case QUESTION_UPVOTE:
                evaluators.add(new Evaluator(FIRST_QUESTION_SCORE_1, this::questionAuthor, this::firstQuestionWithScore1));
                evaluators.add(new Evaluator(QUESTION_SCORE_10, this::questionAuthor, this::questionScore10));
                evaluators.add(new Evaluator(QUESTION_SCORE_25, this::questionAuthor, this::questionScore25));
                evaluators.add(new Evaluator(QUESTION_SCORE_100, this::questionAuthor, this::questionScore100));
                break;
            case ANSWER_UPVOTE:
                evaluators.add(new Evaluator(FIRST_ANSWER_ACCEPTED_SCORE_10, this::answerAuthor, this::firstAnswerAcceptedScore10));
            case MARKED_SOLUTION:
                evaluators.add(new Evaluator(FIRST_QUESTION_ACCEPTED, this::currentUser, this::acceptFirstSolution));
                evaluators.add(new Evaluator(FIRST_ANSWER_ACCEPTED_SCORE_10, this::answerAuthor, this::firstAnswerAcceptedScore10));
                break;
            case LOGIN:
                evaluators.add(new Evaluator(VISIT_30_CONSECUTIVE_DAYS, this::currentUser, this::visit30ConsecutiveDays));
                evaluators.add(new Evaluator(VISIT_100_CONSECUTIVE_DAYS, this::currentUser, this::visit100ConsecutiveDays));
                break;
            case QUESTION_VIEW:
                evaluators.add(new Evaluator(QUESTION_VIEW_50, this::questionAuthor, this::questionView50));
                evaluators.add(new Evaluator(QUESTION_VIEW_250, this::questionAuthor, this::questionView250));
                evaluators.add(new Evaluator(QUESTION_VIEW_500, this::questionAuthor, this::questionView500));
                break;
            default:
                break;
        }

        // For some reason this doesn't work if it's written using stream expressions
        // I'm suspecting BCI magic here.
        for (final Evaluator ev : evaluators) {
            final User user = ev.extractor.apply(badgeEvent);

            if (canAward(ev.badgeType, user, badgeEvent) && ev.evaluator.apply(badgeEvent, user)) {
                final Badge newBadge = new Badge(user, ev.badgeType, badgeEvent.getContext());
                result.include("mamuteMessages", Arrays.asList(messageFactory.build("badge-award", "badge.awarded", newBadge.getBadgeKey())));
                badgeDAO.awardBadge(newBadge);
            }
        }
    }

    protected boolean canAward(final BadgeType badgeType, final User user, final BadgeEvent event) {
        final boolean hasBadgeCtx = user.hasBadge(badgeType, Optional.ofNullable(event.getContext()));
        final boolean hasBadge = user.hasBadge(badgeType);

        final boolean cantHave = hasBadgeCtx || (hasBadge && !badgeType.isMultiBadge());

        return !cantHave;
    }

    public boolean firstQuestion(final BadgeEvent event, final User badgeUser) {
        return true;
    }

    public boolean firstQuestionWithScore1(final BadgeEvent event, final User badgeUser) {
        final Question question = (Question) event.getContext();

        final boolean awardBadge = question.getVoteCount() > 0;

        return awardBadge;
    }

    public boolean firstAnswer(final BadgeEvent event, final User user) {
        return true;
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

    public boolean firstAnswerAcceptedScore10(final BadgeEvent event, final User user) {
        final Answer answer = (Answer) event.getContext();

        final boolean isFirstAnswer = answer.getQuestion().getAnswers().get(0).getId().equals(answer.getId());

        if (answer.isSolution() && answer.getVoteCount() >= 10 && isFirstAnswer) {
            return true;
        }

        return false;
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

    public boolean questionView50(final BadgeEvent event, final User user) {
        return questionView(event, 50);
    }

    public boolean questionView250(final BadgeEvent event, final User user) {
        return questionView(event, 250);
    }

    public boolean questionView500(final BadgeEvent event, final User user) {
        return questionView(event, 500);
    }

    public boolean questionView(final BadgeEvent event, final long threshold) {
        final Question question = (Question) event.getContext();

        final boolean award = question.getViews() > threshold;

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
