package fm.mox;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Created by matteo (dot) moci (at) gmail (dot) com
 * https://github.com/reactor/reactor-core/blob/master/src/test/java/reactor/core/publisher/scenarios/ScatterGatherTests.java#L40
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ScatterGatherTest {

    @Testg
    public void test() throws Exception {

        Flux.just("red", "white", "blue")
            .log("source")
            .flatMap(value -> Mono.fromCallable(() -> {
                Thread.sleep(1000);
                return value;
            }).subscribeOn(Schedulers.elastic()))
            .log("merged")
            .collect(Result::new, Result::add)
            .doOnNext(Result::stop)
            .log("accumulated")
            .toFuture()
            .get();
    }

    final class Result {

        private ConcurrentMap<String, AtomicLong> counts = new ConcurrentHashMap<>();

        private long timestamp = System.currentTimeMillis();

        private long duration;

        public long add(String colour) {
            AtomicLong value = counts.getOrDefault(colour, new AtomicLong());
            counts.putIfAbsent(colour, value);
            return value.incrementAndGet();
        }

        public void stop() {
            this.duration = System.currentTimeMillis() - timestamp;
        }

        public long getDuration() {
            return duration;
        }

        public Map<String, AtomicLong> getCounts() {
            return counts;
        }

        @Override
        public String toString() {
            return "Result [duration=" + duration + ", counts=" + counts + "]";
        }

    }

}