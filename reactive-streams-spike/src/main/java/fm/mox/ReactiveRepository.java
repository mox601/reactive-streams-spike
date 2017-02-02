package fm.mox;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by mmoci (mmoci at expedia dot com).
 */
public interface ReactiveRepository<T> {

    Mono<Void> save(Publisher<T> p);

    Mono<T> findFirst();

    Flux<T> findAll();

    Mono<T> findById(String id);
}
