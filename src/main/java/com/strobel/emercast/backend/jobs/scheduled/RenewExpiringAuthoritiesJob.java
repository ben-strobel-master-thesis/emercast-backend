package com.strobel.emercast.backend.jobs.scheduled;

import com.strobel.emercast.backend.jobs.Job;
import com.strobel.emercast.backend.jobs.enums.JobTypeEnum;
import com.strobel.emercast.backend.services.AuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class RenewExpiringAuthoritiesJob extends Job {

    private final AuthorityService authorityService;

    private RenewExpiringAuthoritiesJob(@Autowired AuthorityService authorityService) {
        super(JobTypeEnum.RenewExpiringAuthorities);
        this.authorityService = authorityService;
    }

    @Override
    public boolean checkParameters(HashMap<String, String> params) {
        // No params required
        return true;
    }

    @Override
    public void executeJob(HashMap<String, String> params) {
        authorityService.renewExpiringAuthorities();
    }
}
