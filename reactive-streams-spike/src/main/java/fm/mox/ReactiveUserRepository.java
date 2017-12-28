package fm.mox;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.reactivestreams.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by mmoci (mmoci at expedia dot com).
 */
public class ReactiveUserRepository implements ReactiveRepository<User, String> {

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
    public Mono<User> save(Publisher<User> userPublisher) {
        return Mono.from(withDelay(Flux.from(userPublisher)).map(user -> {
            this.users.add(user);
            return user;
        }));
    }

    @Override
    public Flux<User> findAll() {
        return withDelay(Flux.fromIterable(this.users));
    }

    @Override
    public Mono<User> findById(String name) {
        User user = this.users.stream().filter((p) -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No user with name " + name + " found!"));
        return withDelay(Mono.just(user));
    }

    @Override
    public Mono<Page<User>> findAll(Publisher<Pageable> pageablePublisher) {

        return Mono.from(withDelay(Flux.from(pageablePublisher)).flatMap(pageable -> {

            long fromIndex = pageable.getOffset();

            int pageNumber = pageable.getPageNumber();

            int pageSize = pageable.getPageSize();

            int total = this.users.size();

            int toIndex = (pageNumber + 1) * pageSize;

            int minToIndex = Math.min(toIndex, total);

            Mono<Page<User>> usersPage = Mono.empty();

            if (fromIndex < total) {
                List<User> arrayList = this.users.subList(Integer.parseInt(Long.toString(fromIndex)), minToIndex);
                Pageable aPageable = PageRequest.of(pageNumber, pageSize);
                Page<User> data = new PageImpl<>(arrayList, aPageable, total);
                usersPage = Mono.just(data);
            }

            return usersPage;
        }));

    }

    private <T> Mono<T> withDelay(Mono<T> aMono) {
        return Mono
                .delay(Duration.ofMillis(this.delayInMs))
                .flatMap(c -> aMono);
    }

    private <T> Flux<T> withDelay(Flux<T> userFlux) {
        return Flux
                .interval(Duration.ofMillis(this.delayInMs))
                .zipWith(userFlux, (i, user) -> user);
    }
}
