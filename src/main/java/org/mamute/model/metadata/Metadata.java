package org.mamute.model.metadata;

import org.mamute.model.MetadataType;
import org.mamute.model.Question;
import org.mamute.model.User;
import org.mamute.model.UserMetadata;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
public class Metadata {

    private User user;
    private List<UserMetadata> metadataList;

    public Metadata(final List<UserMetadata> metadataList, final User user) {
        this.metadataList = metadataList;
        this.user = user;
    }

    public long updateLastLogin() {
        final Optional<UserMetadata> searchResult = findMetadata(MetadataType.CONSECUTIVE_LOGINS);
        final Instant now = Instant.now();
        Long loginCount = 1L;

        if (searchResult.isPresent()) {
            final UserMetadata metadata = searchResult.get();

            final String value = metadata.getValue();
            final String[] parts = value.split(":");

            final Instant lastLogin = Instant.ofEpochSecond(Long.valueOf(parts[0]));
            loginCount = Long.valueOf(parts[1]);

            if (now.minus(Duration.ofDays(1)).isBefore(lastLogin) && now.atZone(ZoneId.systemDefault()).get(ChronoField.DAY_OF_WEEK) != lastLogin.atZone(ZoneId.systemDefault()).get(ChronoField.DAY_OF_WEEK)) {
                loginCount += 1;
            } else {
                loginCount = 1L;
            }

            metadata.setValue(String.format("%d:%d", now.getEpochSecond(), loginCount));
        } else {
            final String value = String.format("%d:%d", now.getEpochSecond(), loginCount);

            user.setRawMetadata(MetadataType.CONSECUTIVE_LOGINS, value);
        }

        return loginCount;
    }

    public long getConsecutiveDays() {
        final Optional<UserMetadata> searchResult = findMetadata(MetadataType.CONSECUTIVE_LOGINS);

        if (searchResult.isPresent()) {
            final UserMetadata metadata = searchResult.get();

            final String value = metadata.getValue();
            final String[] parts = value.split(":");

            //final Instant lastLogin = Instant.ofEpochSecond(Long.valueOf(parts[0]));
            final Long loginCount = Long.valueOf(parts[1]);

            return loginCount;
        } else {
            return 1;
        }
    }

    public void updateCurrentQuestionSeries(final Question question) {
        final Optional<UserMetadata> searchResult = findMetadata(MetadataType.QUESTION_SERIES);

        if (searchResult.isPresent()) {
            final UserMetadata metadata = searchResult.get();
            final List<String> parts = Arrays.asList(metadata.getValue().split(":"));

            final List<Long> questionIds = parts.stream().map(Long::valueOf).collect(Collectors.toList());

            final String ids = questionIds.stream().map(id -> id.toString()).reduce("", (acc, x) -> acc + x + ": ");
            final String idString = ids + question.getId();

            metadata.setValue(idString);
        } else {
            final String value = question.getId().toString();

            user.setRawMetadata(MetadataType.QUESTION_SERIES, value);
        }
    }

    public void resetQuestionSeries() {
        user.setRawMetadata(MetadataType.QUESTION_SERIES, "");
    }

    public List<Long> getQuestionSeries() {
        final Optional<UserMetadata> searchResult = findMetadata(MetadataType.QUESTION_SERIES);

        if (searchResult.isPresent()) {
            final UserMetadata metadata = searchResult.get();
            final List<String> parts = Arrays.asList(metadata.getValue().split(":"));

            final List<Long> questionIds = parts.stream().map(Long::valueOf).collect(Collectors.toList());

            return questionIds;
        } else {
            return new ArrayList<>();
        }
    }

    public Long getDailyVoteCount() {
        final Optional<UserMetadata> searchResult = findMetadata(MetadataType.DAILY_VOTES);

        if (searchResult.isPresent()) {
            final UserMetadata metadata = searchResult.get();
            final Instant now = Instant.now();

            final String[] parts = metadata.getValue().split(":");
            final Instant lastVote = Instant.ofEpochSecond(Long.valueOf(parts[0]));
            final Long voteCount = Long.valueOf(parts[1]);

            if (lastVote.truncatedTo(ChronoUnit.DAYS).equals(now.truncatedTo(ChronoUnit.DAYS))) {
                return voteCount;
            }
        }

        return 0L;
    }

    public void addDailyVoteCount(final long difference) {
        final Optional<UserMetadata> searchResult = findMetadata(MetadataType.DAILY_VOTES);
        final Instant now = Instant.now();

        if (searchResult.isPresent()) {
            final UserMetadata metadata = searchResult.get();

            final String[] parts = metadata.getValue().split(":");
            final Instant lastVote = Instant.ofEpochSecond(Long.valueOf(parts[0]));
            final Long voteCount = Long.valueOf(parts[1]);
            final String newValue;

            if (lastVote.truncatedTo(ChronoUnit.DAYS).equals(now.truncatedTo(ChronoUnit.DAYS))) {
                newValue = String.format("%d:%d", now.getEpochSecond(), voteCount + difference);
            } else {
                newValue = String.format("%d:%d", now.getEpochSecond(), difference);
            }

            metadata.setValue(newValue);
        } else {
            final String value = String.format("%d:%d", now.getEpochSecond(), difference);

            user.setRawMetadata(MetadataType.DAILY_VOTES, value);
        }
    }

    public long getDailyReputation() {
        final Optional<UserMetadata> searchResult = findMetadata(MetadataType.DAILY_REPUTATION);
        final TimestampCounter ctr = new TimestampCounter(searchResult);

        return ctr.getCounter();
    }

    public long addDailyReputation(final long delta) {
        final Optional<UserMetadata> searchResult = findMetadata(MetadataType.DAILY_REPUTATION);
        final TimestampCounter ctr = new TimestampCounter(searchResult);

        if (!ctr.isToday()) {
            ctr.reset();
        }

        ctr.modifyCounter(delta);

        if (!searchResult.isPresent()) {
            user.setRawMetadata(MetadataType.DAILY_REPUTATION, ctr.toMetadata());
        }

        return ctr.getCounter();
    }

    public long getMaxDailyReputationCount() {
        final Optional<UserMetadata> searchResult = findMetadata(MetadataType.MAX_DAILY_REPUTATION_COUNT);
        final TimestampCounter ctr = new TimestampCounter(searchResult);

        return ctr.getCounter();
    }

    public long increaseMaxDailyReputationCount() {
        final Optional<UserMetadata> searchResult = findMetadata(MetadataType.MAX_DAILY_REPUTATION_COUNT);
        final TimestampCounter ctr = new TimestampCounter(searchResult);

        if (!ctr.isToday()) {
            ctr.reset();
        }

        ctr.modifyCounter(1);

        if (!searchResult.isPresent()) {
            user.setRawMetadata(MetadataType.MAX_DAILY_REPUTATION_COUNT, ctr.toMetadata());
        }

        return ctr.getCounter();
    }

    protected Optional<UserMetadata> findMetadata(final MetadataType metaType) {
        return metadataList.stream()
                .filter(md -> metaType.getId().equals(md.getVariable()))
                .findFirst();
    }

    private class TimestampCounter {

        private final Optional<UserMetadata> userMetadata;

        private Instant timestamp;

        private long counter;

        private final long defaultValue;

        public TimestampCounter(final Optional<UserMetadata> userMetadata) {
            this(userMetadata, 0L);
        }

        public TimestampCounter(final Optional<UserMetadata> userMetadata, final long defaultValue) {
            if (userMetadata.isPresent()) {
                final String[] parts = userMetadata.get().getValue().split(":");

                timestamp = Instant.ofEpochSecond(Long.valueOf(parts[0]));
                counter = Long.valueOf(parts[1]);
            } else {
                counter = defaultValue;
            }

            this.userMetadata = userMetadata;
            this.defaultValue = defaultValue;
        }

        public boolean isToday() {
            final Instant nowDays = Instant.now().truncatedTo(ChronoUnit.DAYS);
            final Instant timestampDays = timestamp.truncatedTo(ChronoUnit.DAYS);

            return nowDays.equals(timestampDays);
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public long getCounter() {
            return counter;
        }

        public long changeCounter(final long value) {
            timestamp = Instant.now();
            counter = value;

            userMetadata.map(md -> { md.setValue(toMetadata()); return counter; });

            return counter;
        }

        public long modifyCounter(final long delta) {
            return modifyCounter(counter + delta);
        }

        public long reset() {
            return changeCounter(defaultValue);
        }

        public String toMetadata() {
            return String.format("%d:%d", timestamp.getEpochSecond(), counter);
        }
    }
}
