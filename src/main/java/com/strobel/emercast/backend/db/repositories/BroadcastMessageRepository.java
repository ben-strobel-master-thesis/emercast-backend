package com.strobel.emercast.backend.db.repositories;

import com.strobel.emercast.backend.db.models.BroadcastMessage;
import com.strobel.emercast.backend.db.models.TUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BroadcastMessageRepository extends MongoRepository<BroadcastMessage, TUID<BroadcastMessage>> {
}
