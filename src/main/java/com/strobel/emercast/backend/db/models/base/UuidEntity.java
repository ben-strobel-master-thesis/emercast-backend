package com.strobel.emercast.backend.db.models.base;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

public abstract class UuidEntity<T> {
    @Id
    @Field(targetType = FieldType.STRING)
    protected TUID<T> id;

    public void setId(TUID<T> id) {

        if (this.id != null) {
            throw new UnsupportedOperationException("ID is already defined");
        }

        this.id = id;
    }

    public TUID<T> getId() {
        return id;
    }
}
