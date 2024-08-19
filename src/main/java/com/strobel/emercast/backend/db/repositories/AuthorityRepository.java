package com.strobel.emercast.backend.db.repositories;

import com.strobel.emercast.backend.db.models.authority.Authority;
import com.strobel.emercast.backend.db.models.base.TUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorityRepository extends MongoRepository<Authority, TUID<Authority>> {
    Optional<Authority> findByLoginName(String name);

    @Aggregation(pipeline = {
            "{$match: {'path': ?0}}",
            "{$match: {$expr:  {$cond: [?1, {$ne: ['$revoked', null]}, {$eq: ['$revoked', null]}]}}}",
            "{$addFields:  {'pathIndex': {indexOfArray: ['$path', ?0]}}}",
            "{$sort: { 'pathIndex': 1 }}"
    })
    List<Authority> findByPathContainingSortedByPathIndex(TUID<Authority> id, boolean revoked, Pageable pageable);

    List<Authority> findAllByRevokedIsNull(Pageable pageable);

    Optional<Authority> findByKeyPairValidUntilBefore(Instant timestamp);
}
