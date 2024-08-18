package com.strobel.emercast.backend.db.repositories;

import com.strobel.emercast.backend.db.models.authority.Authority;
import com.strobel.emercast.backend.db.models.base.TUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorityRepository extends MongoRepository<Authority, TUID<Authority>> {
    Optional<Authority> findByLoginName(String name);

    @Query("{'path': ?0}")
    List<Authority> findByPathContaining(TUID<Authority> id, Pageable pageable);
}
