package com.strobel.emercast.backend.db.repositories;

import com.strobel.emercast.backend.db.models.authority.Authority;
import com.strobel.emercast.backend.db.models.base.TUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends MongoRepository<Authority, TUID<Authority>> {
    public Optional<Authority> findByLoginName(String name);
}
