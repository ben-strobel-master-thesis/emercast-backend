package com.strobel.emercast.backend.jobs;

import com.strobel.emercast.backend.jobs.enums.JobTypeEnum;

import java.util.HashMap;

public abstract class Job {

    public Job(JobTypeEnum jobType) {
        this.jobType = jobType;
    }

    private JobTypeEnum jobType;

    public abstract boolean checkParameters(HashMap<String, String> params);

    public abstract void executeJob(HashMap<String, String> params);

    public JobTypeEnum getJobType() {
        return jobType;
    }
}
