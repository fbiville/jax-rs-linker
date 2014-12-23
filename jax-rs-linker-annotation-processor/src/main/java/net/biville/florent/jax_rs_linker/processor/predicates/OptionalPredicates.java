package net.biville.florent.jax_rs_linker.processor.predicates;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;

public class OptionalPredicates {

    public static <T> Predicate<Optional<T>> BY_PRESENCE() {
        return new Predicate<Optional<T>>() {
            @Override
            public boolean apply(@Nullable Optional<T> input) {
                return input != null && input.isPresent();
            }
        };
    }
}