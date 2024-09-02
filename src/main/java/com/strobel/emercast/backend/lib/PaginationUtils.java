package com.strobel.emercast.backend.lib;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class PaginationUtils {

    public static <T> void doForEveryPagedItem(int pageSize, Function<Pageable, List<T>> getRequest, Consumer<T> consumer) {
        doForEveryPage(pageSize, getRequest, (list) -> {
            for (T t : list) {
                consumer.accept(t);
            }
        });
    }

    public static <T> void doForEveryPage(int pageSize, Function<Pageable, List<T>> getRequest, Consumer<List<T>> consumer) {
        var lastPageItemCount = 0;
        var first = true;
        var pageable = Pageable.ofSize(pageSize).first();

        while (lastPageItemCount >= pageSize || first) {
            var items = getRequest.apply(pageable);
            consumer.accept(items);
            lastPageItemCount = items.size();
            pageable = pageable.next();
            first = false;
        }
    }

}
