package com.strobel.emercast.backend.services;

import com.strobel.emercast.backend.jobs.scheduled.RenewExpiringAuthoritiesJob;
import com.strobel.emercast.backend.jobs.enums.JobTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final ApplicationContext applicationContext;

    private final RenewExpiringAuthoritiesJob renewExpiringAuthoritiesJob;

    private JobService(
            @Autowired ApplicationContext applicationContext,
            @Autowired RenewExpiringAuthoritiesJob renewExpiringAuthoritiesJob
    ) {
        this.applicationContext = applicationContext;
        this.renewExpiringAuthoritiesJob = renewExpiringAuthoritiesJob;
    }

    @Value("${emercast.run.job.id:}")
    private String runJobId;

    @EventListener(ApplicationReadyEvent.class)
    public void executeStartUpJob() {
        if(runJobId == null || runJobId.isEmpty()) return;
        var jobKind = JobTypeEnum.parse(runJobId);
        if(jobKind.isEmpty()) return;
        logger.info("Executing job: " + jobKind.get());
        executeJob(jobKind.get());
        logger.info("Job has finished");
        if (applicationContext instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) applicationContext).close();
        }
    }

    public void executeJob(JobTypeEnum jobType) {
        switch (jobType) {
            case RenewExpiringAuthorities -> renewExpiringAuthoritiesJob.executeJob(new HashMap<>());
        }
    }

}
