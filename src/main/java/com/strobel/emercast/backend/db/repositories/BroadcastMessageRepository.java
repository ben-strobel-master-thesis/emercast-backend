package com.strobel.emercast.backend.db.repositories;

import com.strobel.emercast.backend.db.models.BroadcastMessage;
import com.strobel.emercast.backend.db.models.authority.Authority;
import com.strobel.emercast.backend.db.models.base.TUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BroadcastMessageRepository extends MongoRepository<BroadcastMessage, TUID<BroadcastMessage>> {

    @Aggregation(pipeline = {
            "{$lookup: {from:  'authorities', localField: 'issuedAuthorityId', foreignField: '_id', as: 'issuedAuthority'}}",
            "{$match:  {'issuedAuthority.$path':  ?0}}",
            "{$sort: {forwardUntil: -1}}"
    })
    Optional<BroadcastMessage> findUnderJurisdictionWithYoungestForwardUntil(TUID<Authority> authorityId);

    // If this aggregation gets changed (sort order / which elements are selected) it also needs to be adjusted on the clientside
    @Aggregation(pipeline = {
        "{$match: {forwardUntil: {$gt: ?0}, $or: [{forwardUntilOverride: {$gt: ?0}}, {forwardUntilOverride: null}], systemMessage: ?1}}",
        "{$sort: {created: -1}}",
        "{$group: {_id: {}, issuerSignatureList: {$push: '$issuerSignature'}}}",
        "{$replaceRoot: {newRoot: {result: {$reduce: {input: '$issuerSignatureList', initialValue: '', in: {$concat: ['$$value', '$$this']}}}}}}"
    })
    String getCurrentChainHashInput(Instant now, boolean systemMessage);

    @Query(value = "{forwardUntil: {$gt: ?0}, $or: [{forwardUntilOverride: {$gt: ?0}}, {forwardUntilOverride: null}], systemMessage: ?1, }", sort = "{created: -1}")
    List<BroadcastMessage> findByForwardUntilBeforeAndSystemMessageIs(Instant now, boolean systemMessage, Pageable pageable);

    @Query("{systemMessageRegardingAuthority: ?0, forwardUntil: {$lt: ?2}, forwardUntilOverride: {$exists: false}, title: ?1, systemMessage: true}")
    @Update("{$set: {forwardUntilOverride: ?2}}")
    void setForwardUntilOverrideForSystemMessagesRegardingAuthorityWithTitle(TUID<Authority> authorityId, String titleFilter, Instant newForwardUntil);
}
