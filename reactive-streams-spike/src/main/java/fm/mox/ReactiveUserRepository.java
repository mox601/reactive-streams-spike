package fm.mox;

import java.time.Duration;
import java.util.List;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by mmoci (mmoci at expedia dot com).
 */
public class ReactiveUserRepository implements ReactiveRepository<Users.User> {

    private final List<Users.User> users;

    private final Duration delay;

    public ReactiveUserRepository(List<Users.User> users) {
        this(users, Duration.ofMillis(0L));
    }

    public ReactiveUserRepository(List<Users.User> users, Duration delay) {
        this.users = users;
        this.delay = delay;
    }

    @Override
    public Mono<Void> save(Publisher<Users.User> p) {
        return null;
    }

    @Override
    public Mono<Users.User> findFirst() {
        return null;
    }

    @Override
    public Flux<Users.User> findAll() {
        return Flux.fromIterable(this.users).delay(this.delay);
    }

    @Override
    public Mono<Users.User> findById(String id) {
        return null;
    }
}
