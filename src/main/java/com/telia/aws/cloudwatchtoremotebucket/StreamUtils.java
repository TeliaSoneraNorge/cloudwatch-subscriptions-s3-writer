package com.telia.aws.cloudwatchtoremotebucket;

import java.util.function.BinaryOperator;
import java.util.function.Supplier;

public class StreamUtils {
    public static <T> BinaryOperator<T> toOnlyElement() {
        return toOneOrElseThrow(IllegalArgumentException::new);
    }

    public static <T, E extends RuntimeException> BinaryOperator<T> toOneOrElseThrow(Supplier<E> exception) {
        return (element, otherElement) -> {
            throw exception.get();
        };
    }

}
