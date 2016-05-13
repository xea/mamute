package org.mamute.cron;

import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.quartzjob.CronTask;
import org.apache.log4j.Logger;

@Controller
public class ScheduledBadgeJob implements CronTask {

    private static Logger LOG = Logger.getLogger(Logger.class);

    @Override
    public void execute() {
        LOG.info("executing ScheduledBadgeJob...");
    }

    @Override
    public String frequency() {
        return "0 0 0 * * ?";
    }
}
