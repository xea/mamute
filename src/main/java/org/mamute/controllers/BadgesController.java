package org.mamute.controllers;

import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Result;
import org.mamute.factory.MessageFactory;
import org.mamute.model.BadgeClass;
import org.mamute.model.BadgeType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class BadgesController {

    @Inject private Result result;

    @Get
    public void showBadges() {
        final List<BadgeDescription> badges = new ArrayList<>();

        final List<BadgeType> badgeTypes = Arrays.asList(BadgeType.values());

        badgeTypes.stream().forEach(type -> badges.add(new BadgeDescription( type.getId(), type.getDescriptionId(), type.getBadgeClass().toString().toLowerCase())));

        result.include("badgesActive", true);
        result.include("badges", badges);
    }

    public static class BadgeDescription {

        private String name;
        private String description;
        private String badgeClass;

        public BadgeDescription(final String name, final String description, final String badgeClass) {
            this.badgeClass = badgeClass;
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getBadgeClass() {
            return badgeClass;
        }
    }
}
