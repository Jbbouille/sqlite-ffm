package top.petit.sqlite.callback;

/**
 * A functional interface for close operations that may throw checked exceptions.
 *
 * @param <T> the type of object to close
 * @param <E> the type of exception that may be thrown
 */
@FunctionalInterface
public interface CloseCallback<T, E extends Exception> {
    void close(T object) throws E;
}
