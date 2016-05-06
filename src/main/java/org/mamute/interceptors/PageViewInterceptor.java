package org.mamute.interceptors;

import br.com.caelum.vraptor.AfterCall;
import br.com.caelum.vraptor.Intercepts;
import org.mamute.dao.UserDAO;
import org.mamute.event.BadgeEvent;
import org.mamute.model.EventType;
import org.mamute.model.LoggedUser;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@Intercepts
@RequestScoped
public class PageViewInterceptor {

    @Inject private LoggedUser currentUser;
    @Inject private UserDAO userDAO;
    @Inject private Event<BadgeEvent> badgeEvent;

    @AfterCall
    public void afterView() {
        if (currentUser.isLoggedIn()) {
            currentUser.getCurrent().getMetadata().updateLastLogin();
            userDAO.save(currentUser.getCurrent());
            badgeEvent.fire(new BadgeEvent(EventType.LOGIN, currentUser.getCurrent()));
        }
    }
}
