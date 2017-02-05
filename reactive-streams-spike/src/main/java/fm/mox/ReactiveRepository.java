package fm.mox;

import org.reactivestreams.Publisher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by mmoci (mmoci at expedia dot com).
 */
public interface ReactiveRepository<T, ID> {

    Mono<T> save(Publisher<T> p);

    Flux<T> findAll();

    Mono<T> findById(ID id);

    Mono<Page<T>> findAll(Publisher<Pageable> pageable);
}
