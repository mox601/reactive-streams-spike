package fm.mox;

import java.time.Duration;
import java.util.List;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by mmoci (mmoci at expedia dot com).
 */
public class ReactiveUserRepository implements ReactiveRepository<User> {

    private final List<User> users;

    private final Duration delay;

    public ReactiveUserRepository(List<User> users) {
        this(users, Duration.ofMillis(0L));
    }

    public ReactiveUserRepository(List<User> users, Duration delay) {
        this.users = users;
        this.delay = delay;
    }

    @Override
    public Mono<Void> save(Publisher<User> p) {
        return null;
    }

    @Override
    public Mono<User> findFirst() {
        return null;
    }

    @Override
    public Flux<User> findAll() {
        return Flux.fromIterable(this.users).delay(this.delay);
    }

    @Override
    public Mono<User> findById(String id) {
        return null;
    }
}
