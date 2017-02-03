package fm.mox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@Slf4j
public class ReactiveStreamsSpikeApplicationTests {

    @Test
    public void nothing() throws Exception {
        Flux<Integer> just = Flux.just(1, 2, 3);
    }

    @Test
    public void empty() throws Exception {

        Flux<String> flux = emptyFlux();

        StepVerifier.create(flux) //automatically subscribes, request data
            .expectComplete()
            .verify();
    }

    private Flux<String> emptyFlux() {
        return Flux.empty();
    }

    @Test
    public void twoValues() throws Exception {
        Flux<String> flux = twoVals();

        StepVerifier.create(flux) //can control bounded demand. requesting 1 would block
            .expectNext("1", "2")
            .expectComplete()
            .verify();

    }

    private Flux<String> twoVals() {
//		return Flux.fromIterable(Arrays.asList("1", "3"));
// 		.log()
        return Flux.just("1", "2");
    }

    @Test
    public void errors() throws Exception {
        Flux<String> flux = errorFlux();

        StepVerifier.create(flux)
            .expectError(IllegalStateException.class)
            .verify();
    }

    //throwing won't work in an async context. exception might bubble up in different threads
    private Flux<String> errorFlux() {
//		throw new IllegalStateException(); // can't do this
        return Flux.error(new IllegalStateException());
    }
    //publishers flux and mono are try-catch-block all included

    @Test
    public void withDelay() throws Exception {
        Flux<Long> flux = counter();

        StepVerifier.create(flux)
            .expectNext(0L, 1L, 2L)
            .expectComplete()
            .verify();
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

    ///////////////////// write the verifier instead

    @Test
    public void expectElementsThenComplete() throws Exception {
        expectFooBarComplete(Flux.just("foo", "bar"));
    }

//    any type of assertions lib
    private void expectFooBarComplete(Flux<String> flux) {
//		fail();
        StepVerifier.create(flux)
            .expectNext("foo", "bar")
            .expectComplete()
            .verify();
    }

    @Test
    public void transformAsync() throws Exception {

        ReactiveUserRepository reactiveUserRepository = new ReactiveUserRepository(Arrays.asList(Users.BOB, Users.ALICE));

        Flux<Users.User> all = reactiveUserRepository.findAll();

        StepVerifier.create(asyncCapitalizeMany(all))
            .expectNext(
                new Users.User("BOB", "MARSHALL"),
                new Users.User("ALICE", "FRENCH"))
            .expectComplete()
            .verify();
    }

    //    to go async we use flatMap and return
    //e.g. remote service with latency
    private Flux<Users.User> asyncCapitalizeMany(Flux<Users.User> all) {
        return all.flatMap(this::asyncCapitalizeUser);
    }

    private Mono<Users.User> asyncCapitalizeUser(Users.User user) {
        return Mono.just(new Users.User(user.getName().toUpperCase(), user.getSurname().toUpperCase()));
    }

    @Test
    public void mergeDelayWithInterleave() throws Exception {

        ReactiveUserRepository slowRepository = new ReactiveUserRepository(Arrays.asList(Users.BOB, Users.ALICE),
            Duration.ofMillis(500));
        ReactiveUserRepository normalRepository = new ReactiveUserRepository(Arrays.asList(Users.CARL, Users.DAVE));

        Flux<Users.User> mergedWithInterleave = mergeFluxWithInterleave(slowRepository.findAll(), normalRepository.findAll());

        StepVerifier.create(mergedWithInterleave)
            .expectNext(Users.CARL, Users.DAVE, Users.BOB, Users.ALICE)
            .expectComplete().verify();
    }

    //merge
    private Flux<Users.User> mergeFluxWithInterleave(Flux<Users.User> one, Flux<Users.User> two) {
        return Flux.merge(one, two);
    }

    @Test
    public void mergeDelayWithNoInterleave() throws Exception {

        ReactiveUserRepository slowRepository = new ReactiveUserRepository(Arrays.asList(Users.BOB, Users.ALICE), Duration.ofMillis(500));
        ReactiveUserRepository normalRepository = new ReactiveUserRepository(Arrays.asList(Users.CARL, Users.DAVE));

        Flux<Users.User> mergedWithInterleave = mergeFluxWithNoInterleave(slowRepository.findAll(), normalRepository.findAll());

        StepVerifier.create(mergedWithInterleave)
            .expectNext(Users.BOB, Users.ALICE, Users.CARL, Users.DAVE)
            .expectComplete().verify();
    }

    //concat
    private Flux<Users.User> mergeFluxWithNoInterleave(Flux<Users.User> one, Flux<Users.User> two) {
        return Flux.concat(one, two);
    }

    /*
    * show unit tests:

write the stepverifier instead


control the demand in create(..., 1)

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

adapt flux to/from rxjava

block() will block the current thread, you should be aware which threadpool you are. use in tests

how to integrate in reactive pipeline a jdbc operation?
run on an elastic io scheduler
subscribeOn(elastic())

flux.defer().subscribeOn(elastic())
run the query on subscription

save is publishOn()
run onNext on parallel , ... then()

threading examples
http://www.grahamlea.com/2014/07/rxjava-threading-examples/
http://stackoverflow.com/questions/30791902/rxjava-subscribeonschedulers-newthread-questions#30801055
http://stackoverflow.com/questions/31276164/rxjava-schedulers-use-cases
	* */


    //	slow publisher e.g., blocking IO, fast consumer(s) scenarios.
    @Test
    public void slowPublisherFastSubscriber() throws Exception {
        //e.g. jdbc
        BlockingUserRepository blockingUserRepository = new BlockingUserRepository(Arrays.asList(Users.BOB, Users.ALICE));
        Flux<Users.User> flux = blockingRepositoryToFlux(blockingUserRepository);

        assertEquals("findAll should be deferred until subscribe", 0, blockingUserRepository.getCallCount());

        StepVerifier.create(flux)
            .expectNext(Users.BOB, Users.ALICE)
            .expectComplete()
            .verify();
    }

    //run subscribe, onsubscribe and request on different thread
    //findAll is triggered by subscription
    private Flux<Users.User> blockingRepositoryToFlux(BlockingUserRepository blockingUserRepository) {
//        return Flux.fromIterable(blockingUserRepository.findAll()).subscribeOn(Schedulers.elastic());
        //defer
        return Flux.defer(() -> Flux.fromIterable(blockingUserRepository.findAll()).subscribeOn(Schedulers.elastic()));
    }

    @Test
    public void fastPublisherSlowSubscriber() throws Exception {

        BlockingRepository<Users.User> blockingRepository = new BlockingUserRepository(new ArrayList<>());
        ReactiveRepository<Users.User> reactiveRepository = new ReactiveUserRepository(Arrays.asList(Users.BOB, Users.ALICE));
        Mono<Void> complete = fluxToSaveBlockingRepository(reactiveRepository.findAll(), blockingRepository);

        assertEquals(0, blockingRepository.getCallCount());

        StepVerifier.create(complete).expectComplete().verify();

        Iterator<Users.User> it = blockingRepository.findAll().iterator();
        assertEquals(Users.BOB, it.next());
        assertEquals(Users.ALICE, it.next());
        assertFalse(it.hasNext());

    }

    //publishOn run onNext on parallel
    private Mono<Void> fluxToSaveBlockingRepository(Flux<Users.User> flux, BlockingRepository<Users.User> blockingRepository) {
        return flux.publishOn(Schedulers.parallel()).doOnNext(blockingRepository::save).then();
    }
}
