package fm.mox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ReactiveStreamsSpikeApplicationTests {

    @Test
    public void withDelay() throws Exception {
        Flux<Long> flux = counter();

        StepVerifier.create(flux)
                .expectNext(0L, 1L, 2L)
                .verifyComplete();
//                .verify();
    }

    private Flux<Long> counter() {
        return Flux.interval(Duration.ofMillis(100L)).take(3); // infinite stream. fails. should take just 3
    }

    @Test
    public void countVirtualTime() throws Exception {
        //lambda:without, it would immediatly start to publish. needed for virtualtime to work
        expectOneHourOfElements(() -> Flux.interval(Duration.ofSeconds(1)).take(3600));
    }

    private void expectOneHourOfElements(Supplier<Flux<Long>> supplier) {
        StepVerifier.withVirtualTime(supplier)
                .thenAwait(Duration.ofHours(1))
                .expectNextCount(3600)
                .expectComplete();
    }

    @Test
    public void noSignal() throws Exception {

        Mono<String> never = noSignalMono();

        StepVerifier.create(never)
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(1L)) //no events are emitted
                .thenCancel()
                .verify();

    }

    private Mono<String> noSignalMono() {
        return Mono.never();
    }

    @Test
    public void transformAsync() throws Exception {

        ReactiveUserRepository reactiveUserRepository = new ReactiveUserRepository(Arrays.asList(User.BOB, User.ALICE));

        Flux<User> all = reactiveUserRepository.findAll();

        StepVerifier.create(asyncCapitalizeMany(all))
                .expectNext(
                        new User("BOB", "MARSHALL"),
                        new User("ALICE", "FRENCH"))
                .verifyComplete();
//                .verify();
    }

    //    to go async we use flatMap and return
    //e.g. remote service with latency
    private Flux<User> asyncCapitalizeMany(Flux<User> all) {
        return all.flatMap(this::asyncCapitalizeUser);
    }

    private Mono<User> asyncCapitalizeUser(User user) {
        return Mono.just(new User(user.getName().toUpperCase(), user.getSurname().toUpperCase()));
    }

    @Test
    public void mergeDelayWithInterleave() throws Exception {

        ReactiveUserRepository slowRepository = new ReactiveUserRepository(500, Arrays.asList(User.BOB, User.ALICE)
        );
        ReactiveUserRepository normalRepository = new ReactiveUserRepository(Arrays.asList(User.CARL, User.DAVE));

        Flux<User> mergedWithInterleave = mergeFluxWithInterleave(slowRepository.findAll(), normalRepository.findAll());

        StepVerifier.create(mergedWithInterleave)
                .expectNext(User.CARL, User.DAVE, User.BOB, User.ALICE)
                .verifyComplete();
    }

    //merge
    private Flux<User> mergeFluxWithInterleave(Flux<User> one, Flux<User> two) {
        return Flux.merge(one, two);
    }

    @Test
    public void mergeDelayWithNoInterleave() throws Exception {

        ReactiveUserRepository slowRepository = new ReactiveUserRepository(500, Arrays.asList(User.BOB, User.ALICE));
        ReactiveUserRepository normalRepository = new ReactiveUserRepository(Arrays.asList(User.CARL, User.DAVE));

        Flux<User> mergedWithInterleave = mergeFluxWithNoInterleave(slowRepository.findAll(), normalRepository.findAll());

        StepVerifier.create(mergedWithInterleave)
                .expectNext(User.BOB, User.ALICE, User.CARL, User.DAVE)
                .verifyComplete();
    }

    //concat
    private Flux<User> mergeFluxWithNoInterleave(Flux<User> one, Flux<User> two) {
        return Flux.concat(one, two);
    }

    @Test
    public void fluxFromTwoMonos() throws Exception {

        Mono<User> monoBob = Mono.just(User.BOB);
        Mono<User> monoAlice = Mono.just(User.ALICE);

        Flux<User> fluxOfTwoMonos = Flux.concat(monoBob, monoAlice);

        StepVerifier.create(fluxOfTwoMonos)
                .expectNext(User.BOB, User.ALICE)
                .verifyComplete();
//                .verify();
    }

    /*
    *

how to debug

doOnSubscribe
those methods run on the publishing thread so don't do high latency methods
small non blocking operations
otherwise() to replace errors and switch publisher
checked exception handling propagate()

flux zip to merge 3 different apis in 1 object
the internal buffer can be changed, there is default

first of 2, get the fastest returned

firstemitting

no null in streams

block() will block the current thread, you should be aware which threadpool you are. use in tests

	* */

    //	slow publisher e.g., blocking IO, fast consumer(s) scenarios.
    @Test
    public void slowPublisherFastSubscriber() throws Exception {
        //e.g. jdbc
        BlockingUserRepository blockingUserRepository = new BlockingUserRepository(Arrays.asList(User.BOB, User.ALICE));
        Flux<User> flux = blockingRepositoryToFlux(blockingUserRepository);

        assertEquals("findAll should be deferred until subscribe", 0, blockingUserRepository.getCallCount());

        StepVerifier.create(flux)
                .expectNext(User.BOB, User.ALICE)
                .verifyComplete();
//                .verify();
    }

    //run subscribe, onsubscribe and request on different thread
    //findAll is triggered by subscription
    private Flux<User> blockingRepositoryToFlux(BlockingUserRepository blockingUserRepository) {
//        return Flux.fromIterable(blockingUserRepository.findAll()).subscribeOn(Schedulers.elastic());
        //defer
        return Flux.defer(() -> Flux.fromIterable(blockingUserRepository.findAll()).subscribeOn(Schedulers.elastic()));
    }

    @Test
    public void fastPublisherSlowSubscriber() throws Exception {

        BlockingUserRepository blockingRepository = new BlockingUserRepository(new ArrayList<>());
        ReactiveRepository<User, String> reactiveRepository = new ReactiveUserRepository(Arrays.asList(User.BOB, User.ALICE));
        Mono<Void> complete = fluxToSaveBlockingRepository(reactiveRepository.findAll(), blockingRepository);

        assertEquals(0, blockingRepository.getCallCount());

        StepVerifier.create(complete).expectComplete().verify();

        Iterator<User> it = blockingRepository.findAll().iterator();
        assertEquals(User.BOB, it.next());
        assertEquals(User.ALICE, it.next());
        assertFalse(it.hasNext());

    }

    //publishOn run onNext on parallel
    private Mono<Void> fluxToSaveBlockingRepository(Flux<User> flux, CrudRepository<User, String> blockingRepository) {
        return flux.publishOn(Schedulers.parallel()).doOnNext(blockingRepository::save).then();
    }

    //TODO more:
    /*
    *  Backpressure: how to tune backpressure?
    *  https://github.com/ReactiveX/RxJava/wiki/Backpressure
    *
·   *  Multithreading: messages order. use concatMap?
    *  * http://stackoverflow.com/questions/36131991/rxjava-flatmap-vs-concatmap-why-is-ordering-the-same-on-subscription
    *  * https://tomstechnicalblog.blogspot.it/2015/11/rxjava-achieving-parallelization.html
    *  * http://fernandocejas.com/2015/01/11/rxjava-observable-tranformation-concatmap-vs-flatmap/
    * */

}
