package com.strobel.emercast.backend.db.repositories;

import com.strobel.emercast.backend.db.models.BroadcastMessage;
import com.strobel.emercast.backend.db.models.authority.Authority;
import com.strobel.emercast.backend.db.models.base.TUID;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BroadcastMessageRepository extends MongoRepository<BroadcastMessage, TUID<BroadcastMessage>> {

    @Aggregation(pipeline = {
            "{$lookup: {from:  'authorities', localField: 'issuedAuthorityId', foreignField: '_id', as: 'issuedAuthority'}}",
            "{$match:  {'issuedAuthority.$path':  ?0}}",
            "{$sort: {forwardUntil: -1}}"
    })
    Optional<BroadcastMessage> findUnderJurisdictionWithYoungestForwardUntil(TUID<Authority> authorityId);
}
