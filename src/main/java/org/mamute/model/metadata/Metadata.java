package org.mamute.model.metadata;

import org.mamute.model.MetadataType;
import org.mamute.model.Question;
import org.mamute.model.User;
import org.mamute.model.UserMetadata;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
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

    protected Optional<UserMetadata> findMetadata(final MetadataType metaType) {
        return metadataList.stream()
                .filter(md -> metaType.getId().equals(md.getVariable()))
                .findFirst();
    }
}
