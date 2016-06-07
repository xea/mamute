package org.mamute.observer;

import br.com.caelum.vraptor.environment.Environment;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.entity.ContentType;
import org.mamute.controllers.QuestionController;
import org.mamute.event.QuestionCreated;
import org.mamute.vraptor.Linker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class SlackNotificationObserver {

    private static final String SLACK_URL_KEY = "slack.notification_url";
    private static final String SLACK_FEATURE_KEY = "feature.slack";
    private static final Logger logger = LoggerFactory.getLogger(SlackNotificationObserver.class);

    @Inject private QuestionController questionController;
    @Inject private Environment environment;
    @Inject private Linker linker;

    public void subscribeUsers(@Observes QuestionCreated questionCreated) {
        if (environment.supports(SLACK_FEATURE_KEY)) {
            final String slackUrl = environment.get(SLACK_URL_KEY);

            if (slackUrl != null) {
                try {
                    final URL url = new URL(slackUrl);

                    final String userName = questionCreated.getQuestion().getAuthor().getName();
                    final String linkLabel = questionCreated.getQuestion().getTitle();
                    linker.linkTo(questionController).showQuestion(questionCreated.getQuestion(), questionCreated.getQuestion().getSluggedTitle());
                    final String questionUrl = linker.get();
                    final String payload = getRequestPayload(userName, questionUrl, linkLabel);

                    final HttpClient client = new HttpClient();
                    final PostMethod method = new PostMethod(url.toURI().toString());

                    final StringRequestEntity entity = new StringRequestEntity(
                            payload,
                            "application/json",
                            "UTF-8"
                    );

                    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
                    method.setRequestEntity(entity);

                    int status = client.executeMethod(method);

                    if (status == HttpStatus.SC_OK || status == HttpStatus.SC_ACCEPTED) {
                        //final String response = new String(method.getResponseBody());
                        logger.info("Sent slack notification");
                    } else {
                        logger.error("Couldn't send slack notification, status code: " + status + " response body: " + method.getResponseBodyAsString());
                        logger.error("Submitted payload: %s" + payload);
                    }

                    method.releaseConnection();
                } catch (MalformedURLException e) {
                    logger.error("Error while sending slack notification", e);
                } catch (URISyntaxException e) {
                    logger.error("Error while sending slack notification", e);
                } catch (HttpException e) {
                    logger.error("Error while sending slack notification", e);
                } catch (IOException e) {
                    logger.error("Error while sending slack notification", e);
                }
            }
        }
    }

    private String getRequestPayload(final String userName, final String url, final String linkLabel) {
        return String.format("{\"text\": \"%s has posted a new question to RTFM: <%s|%s>!\"}", userName, url, linkLabel);
    }
}
