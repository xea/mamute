package org.mamute.observer;

import br.com.caelum.vraptor.Result;
import org.joda.time.DateTime;
import org.mamute.dao.BadgeDAO;
import org.mamute.dao.CommentDAO;
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

    @Inject private CommentDAO commentDAO;

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

    public User eventSubject(final BadgeEvent event) {
        return event.getUser();
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
                evaluators.add(new Evaluator(FIRST_UPVOTE, this::currentUser, this::firstUpvote));
                /* not implemented yet
                evaluators.add(new Evaluator(QUESTION_SERIES_5, this::questionAuthor, this::questionSeries5));
                evaluators.add(new Evaluator(QUESTION_SERIES_30, this::questionAuthor, this::questionSeries30));
                evaluators.add(new Evaluator(QUESTION_SERIES_100, this::questionAuthor, this::questionSeries100));
                */
                break;
            case QUESTION_DOWNVOTE:
                evaluators.add(new Evaluator(FIRST_DOWNVOTE, this::currentUser, this::firstDownvote));
                break;
            case ANSWER_UPVOTE:
                evaluators.add(new Evaluator(FIRST_ANSWER_ACCEPTED_SCORE_10, this::answerAuthor, this::firstAnswerAcceptedScore10));
                evaluators.add(new Evaluator(ANSWER_SCORE_10, this::answerAuthor, this::answerScore10));
                evaluators.add(new Evaluator(ANSWER_SCORE_25, this::answerAuthor, this::answerScore25));
                evaluators.add(new Evaluator(ANSWER_SCORE_100, this::answerAuthor, this::answerScore100));
                evaluators.add(new Evaluator(ANSWER_OUTSCORE_ACCEPTED_2, this::answerAuthor, this::answerOutscore2));
                evaluators.add(new Evaluator(ANSWER_OUTSCORE_ACCEPTED_5, this::answerAuthor, this::answerOutscore5));
                evaluators.add(new Evaluator(ANSWER_REVIVE_QUESTION_30, this::answerAuthor, this::reviveAnswer30));
                evaluators.add(new Evaluator(ANSWER_REVIVE_QUESTION_60, this::answerAuthor, this::reviveAnswer60));
                evaluators.add(new Evaluator(ANSWER_OWN_QUESTION_SCORE_2, this::answerAuthor, this::answerOwnScore2));
                evaluators.add(new Evaluator(ANSWER_ACCEPTED_SCORE_40, this::answerAuthor, this::answerAcceptedScore40));
                evaluators.add(new Evaluator(FIRST_UPVOTE, this::currentUser, this::firstUpvote));
                break;
            case ANSWER_DOWNVOTE:
                evaluators.add(new Evaluator(FIRST_DOWNVOTE, this::currentUser, this::firstDownvote));
                break;
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
            case CREATED_COMMENT:
                evaluators.add(new Evaluator(COMMENT_10, this::currentUser, this::comment10));
                evaluators.add(new Evaluator(COMMENT_50, this::currentUser, this::comment50));
                evaluators.add(new Evaluator(COMMENT_10_SCORE_5, this::currentUser, this::comment10Score5));
                evaluators.add(new Evaluator(COMMENT_50_SCORE_5, this::currentUser, this::comment50Score5));
                break;
            case PROFILE_COMPLETED:
                evaluators.add(new Evaluator(AUTOBIOGRAPHY_COMPLETE, this::currentUser, this::autoBiography));
                break;
            case EDIT_APPROVED:
                evaluators.add(new Evaluator(FIRST_EDIT_APPROVED, this::eventSubject, this::firstEdit));
                break;
            case DELETED_ANSWER:
                evaluators.add(new Evaluator(DELETE_OWN_POST_SCORE_3, this::currentUser, this::deleteOwnPostScore3));
                evaluators.add(new Evaluator(DELETE_OWN_POST_SCORE_MINUES_3, this::currentUser, this::deleteOwnPostScoreMinus3));
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
        return visitConsecutiveDays(user, 30);
    }

    public boolean visit100ConsecutiveDays(final BadgeEvent event, final User user) {
        return visitConsecutiveDays(user, 100);
    }

    public boolean visitConsecutiveDays(final User user, final long threshold) {
        final boolean award = user.getMetadata().getConsecutiveDays() >= threshold;

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

    public boolean questionSeries5(final BadgeEvent event, final User user) {
        return questionSeries(user, 5);
    }

    public boolean questionSeries30(final BadgeEvent event, final User user) {
        return questionSeries(user, 30);
    }

    public boolean questionSeries100(final BadgeEvent event, final User user) {
        return questionSeries(user, 100);
    }

    // TODO needs implementing
    public boolean questionSeries(final User user, final long threshold) {
        if (user.getMetadata().getQuestionSeries().size() > threshold) {
            user.getMetadata().resetQuestionSeries();

            return true;
        } else {
            return false;
        }
    }

    public boolean answerScore10(final BadgeEvent event, final User user) {
        return answerScore(event, 10);
    }

    public boolean answerScore25(final BadgeEvent event, final User user) {
        return answerScore(event, 25);
    }

    public boolean answerScore100(final BadgeEvent event, final User user) {
        return answerScore(event, 100);
    }

    public boolean answerScore(final BadgeEvent event, final long threshold) {
        final Answer answer = (Answer) event.getContext();
        final boolean award = answer.getVoteCount() > threshold;

        return award;
    }

    public boolean answerAcceptedScore40(final BadgeEvent event, final User user) {
        final Answer answer = (Answer) event.getContext();

        final boolean award = answerScore(event, 40) && answer.isSolution();

        return award;
    }

    public boolean answerOutscore2(final BadgeEvent event, final User user) {
        return answerOutscore(event, 2);
    }

    public boolean answerOutscore5(final BadgeEvent event, final User user) {
        return answerOutscore(event, 5);
    }

    public boolean answerOutscore(final BadgeEvent event, final long multiplier) {
        final Answer answer = (Answer) event.getContext();
        final Answer solution = answer.getQuestion().getSolution();

        // TODO: this doesn't trigger well until there was an upvote *after* accepting another answer

        if (solution != null) {
            if (answer.getVoteCount() / multiplier >= solution.getVoteCount()) {
                return true;
            }
        }

        return false;
    }

    public boolean answerOwnScore2(final BadgeEvent event, final User user) {
        final Answer answer = (Answer) event.getContext();

        final boolean award = answer.isTheSameAuthorOfQuestion() && answer.getVoteCount() > 2;

        return award;
    }

    public boolean reviveAnswer30(final BadgeEvent event, final User user) {
        return reviveAnswer(event, 30, 2);
    }

    public boolean reviveAnswer60(final BadgeEvent event, final User user) {
        return reviveAnswer(event, 60, 5);
    }

    public boolean reviveAnswer(final BadgeEvent event, final long days, final long minScore) {
        final Answer answer = (Answer) event.getContext();

        final DateTime dateLimit = answer.getCreatedAt().minus(org.joda.time.Duration.standardDays(days));

        if (dateLimit.isAfter(answer.getQuestion().getCreatedAt())) {
            if (answer.getVoteCount() >= minScore) {
                return true;
            }
        }

        return false;
    }

    public boolean comment10(final BadgeEvent event, final User user) {
        return leaveComment(user, 10, 0);
    }

    public boolean comment50(final BadgeEvent event, final User user) {
        return leaveComment(user, 50, 0);
    }

    public boolean comment10Score5(final BadgeEvent event, final User user) {
        return leaveComment(user, 10, 5);
    }

    public boolean comment50Score5(final BadgeEvent event, final User user) {
        return leaveComment(user, 50, 5);
    }

    public boolean leaveComment(final User user, final long count, final long scoreLimit) {
        final List<Comment> comments = commentDAO.userComments(user);

        if (comments.size() > count) {
            if (scoreLimit > 0) {
                return (comments.stream().filter(c -> c.getVoteCount() >= scoreLimit).count() > count);
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean firstUpvote(final BadgeEvent event, final User user) {
        return true;
    }

    public boolean firstDownvote(final BadgeEvent event, final User user) {
        return true;
    }

    public boolean autoBiography(final BadgeEvent event, final User user) {
        return true;
    }

    public boolean firstEdit(final BadgeEvent event, final User user) {
        return true;
    }

    public boolean deleteOwnPostScore3(final BadgeEvent event, final User user) {
        final Answer answer = (Answer) event.getContext();

        final boolean award = (answer.getAuthor().equals(user) && answer.getVoteCount() >= 3);

        return award;
    }

    public boolean deleteOwnPostScoreMinus3(final BadgeEvent event, final User user) {
        final Answer answer = (Answer) event.getContext();

        final boolean award = (answer.getAuthor().equals(user) && answer.getVoteCount() <= -3);

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
