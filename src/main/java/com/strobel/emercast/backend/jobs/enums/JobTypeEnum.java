package com.strobel.emercast.backend.jobs.enums;

import java.util.Optional;

public enum JobTypeEnum {
    RenewExpiringAuthorities;

    public static Optional<JobTypeEnum> parse(String id) {
        try {
            return Optional.of(JobTypeEnum.valueOf(id));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
