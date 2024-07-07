package com.strobel.emercast.backend.db.models;

import java.io.Serial;
import java.util.UUID;

public class TUID<T>implements java.io.Serializable, Comparable<TUID<T>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;

    public TUID(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TUID<?> tuid = (TUID<?>) o;
        return id.equals(tuid.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * @deprecated Only use for serialization in other "toOpenAPI" methods
     */
    @Deprecated
    public UUID toOpenAPI(){
        return id;
    }

    @Override
    public int compareTo(TUID<T> o) {
        return id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public static class WriteConverter implements org.springframework.core.convert.converter.Converter<TUID<?>, String> {

        @Override
        public String convert(TUID<?> source) {
            return source.toString();
        }
    }

    public static class ReadConverter implements org.springframework.core.convert.converter.Converter<String, TUID<?>> {

        @Override
        public TUID<?> convert(String source) {
            return new TUID<>(UUID.fromString(source));
        }
    }

}
