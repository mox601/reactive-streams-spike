package fm.mox;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by mmoci (mmoci at expedia dot com).
 */
public class ReactiveUserRepository implements ReactiveRepository<User> {


    private final static long DEFAULT_DELAY_IN_MS = 100;

    private final long delayInMs;

    private final List<User> users;

    public ReactiveUserRepository() {
        this(DEFAULT_DELAY_IN_MS);
    }

    public ReactiveUserRepository(long delayInMs) {
        this.delayInMs = delayInMs;
        users = new ArrayList<>();
    }

    public ReactiveUserRepository(List<User> users) {
        this(DEFAULT_DELAY_IN_MS, users);
    }

    public ReactiveUserRepository(long delayInMs, List<User> users) {
        this.delayInMs = delayInMs;
        this.users = users;
    }


    @Override
    public Mono<Void> save(Publisher<User> userPublisher) {
        return withDelay(Flux.from(userPublisher)).doOnNext(users::add).then();
    }

    @Override
    public Mono<User> findFirst() {
        return withDelay(Mono.just(users.get(0)));
    }

    @Override
    public Flux<User> findAll() {
        return withDelay(Flux.fromIterable(users));
    }

    @Override
    public Mono<User> findById(String name) {
        User user = users.stream().filter((p) -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No user with name " + name + " found!"));
        return withDelay(Mono.just(user));
    }


    private Mono<User> withDelay(Mono<User> userMono) {
        return Mono
                .delay(Duration.ofMillis(delayInMs))
                .then(c -> userMono);
    }

    private Flux<User> withDelay(Flux<User> userFlux) {
        return Flux
                .interval(Duration.ofMillis(delayInMs))
                .zipWith(userFlux, (i, user) -> user);
    }
}
