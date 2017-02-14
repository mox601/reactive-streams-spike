package fm.mox;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Publisher;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matteo (dot) moci (at) gmail (dot) com
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class E09ComplexTest {
    @Test
    public void getFromCache() throws Exception {

        Map<String, String> map = new HashMap<>();
        ReactiveCache<String, String> reactiveCache = new InMemoryReactiveCache<>(map);

        reactiveCache.put(Mono.just(Tuples.of("1", "one")));

        Mono<String> stringMono = reactiveCache.get(Mono.just("1"));

        StepVerifier.create(stringMono)
                .expectNext("one")
                .expectComplete();

        Mono<String> empty = reactiveCache.get(Mono.just("2"));

        StepVerifier.create(empty)
                .expectComplete();
    }

//    @Test
    public void testCache() throws Exception {
        Map<String, String> map = new HashMap<>();
        Map<String, String> aCacheMap = new HashMap<>();

        ReactiveRepository<String, String> reactiveRepository =
                new InMemoryRepository<>(map);
        ReactiveCache<String, String> reactiveCache = new InMemoryReactiveCache<>(aCacheMap);

        String key = "1";

        Mono<String> just = Mono.just(key);

        Mono<String> fromCache = reactiveCache.get(just);

        Mono<String> fromRepository = reactiveRepository.getById(just);

        //TODO update cache
        Mono<String> fromRepositoryWithSave = fromRepository
                .doOnNext(value -> reactiveCache.put(Mono.just(Tuples.of(key, value))));

//        Mono<String> stringMono = fromCache.otherwiseIfEmpty(single);
        Flux<String> concat = Flux.concat(fromCache, fromRepositoryWithSave);

        fromCache.otherwiseIfEmpty(fromRepositoryWithSave);

    }

    //        TODO get from cache if present, populate if not, return
    public interface ReactiveRepository<ID, V> {
        Mono<V> getById(Publisher<ID> id);
    }

    public static class InMemoryRepository<K, V> implements ReactiveRepository<K, V> {

        private final Map<K, V> map;
        private final long delayInMs;

        public InMemoryRepository(Map<K, V> map) {
            this.map = map;
            this.delayInMs = 2_000L;
        }

        @Override
        public Mono<V> getById(Publisher<K> k) {
            return withDelay(Mono.from(k).map(this.map::get));
        }

        private Mono<V> withDelay(Mono<V> map) {
            return Mono.delay(Duration.ofMillis(this.delayInMs)).then(c -> map);
        }
    }

    public interface ReactiveCache<K, V> {
        Mono<V> get(Publisher<K> key);

        Mono<V> put(Publisher<Tuple2<K, V>> kv);
    }

    public static class InMemoryReactiveCache<K, V> implements ReactiveCache<K, V> {

        private final Map<K, V> map;

        public InMemoryReactiveCache(Map<K, V> map) {
            this.map = map;
        }

        @Override
        public Mono<V> get(Publisher<K> key) {
            return Mono.from(key).map(map::get);
        }

        @Override
        public Mono<V> put(Publisher<Tuple2<K, V>> kv) {
            return Mono.from(kv).map(kvTuple2 -> {
                V val = kvTuple2.getT2();
                K key = kvTuple2.getT1();
                map.put(key, val);
                return val;
            });
        }
    }
}
