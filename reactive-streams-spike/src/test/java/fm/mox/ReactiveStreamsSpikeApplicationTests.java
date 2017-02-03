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
        return null;
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
//        return Flux.just("1", "2");
        return null;
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
        return null;
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

    //interval duration take 3
    private Flux<Long> counter() {
        return null;
    }

    @Test
    public void countVirtualTime() throws Exception {
        //lambda:without, it would immediatly start to publish. needed for virtualtime to work
        expectOneHourOfElements(() -> Flux.interval(Duration.ofSeconds(1)).take(3600));
    }

    //virtual time then await
    private void expectOneHourOfElements(Supplier<Flux<Long>> supplier) {
        //
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
        return null;
    }

    // write the verifier instead
    @Test
    public void expectElementsThenComplete() throws Exception {
        expectFooBarComplete(Flux.just("foo", "bar"));
    }

    //    any type of assertions lib
    private void expectFooBarComplete(Flux<String> flux) {
//		fail();

    }

    @Test
    public void askAllExpectTwo() throws Exception {
        ReactiveUserRepository reactiveUserRepository = new ReactiveUserRepository(Arrays.asList(Users.BOB, Users.ALICE));
        Flux<Users.User> flux = reactiveUserRepository.findAll();
        StepVerifier verifier = requestAllExpectTwo(flux);
        verifier.verify();
    }

    private StepVerifier requestAllExpectTwo(Flux<Users.User> flux) {
        return null;
    }

    @Test
    public void askOneByOne() throws Exception {
        ReactiveUserRepository reactiveUserRepository = new ReactiveUserRepository(Arrays.asList(Users.BOB, Users.ALICE, Users.CARL));
        Flux<Users.User> flux = reactiveUserRepository.findAll();
        StepVerifier verifier = requestOneByOne(flux);
        verifier.verify();
    }

    //next request next cancel
    private StepVerifier requestOneByOne(Flux<Users.User> flux) {
        return null;
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
        return null;
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
        return null;
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
        return null;
    }

    @Test
    public void fluxFromTwoMonos() throws Exception {

        Mono<Users.User> monoBob = Mono.just(Users.BOB);
        Mono<Users.User> monoAlice = Mono.just(Users.ALICE);

        Flux<Users.User> fluxOfTwoMonos = Flux.concat(monoBob, monoAlice);

        StepVerifier.create(fluxOfTwoMonos)
            .expectNext(Users.BOB, Users.ALICE)
            .expectComplete()
            .verify();
    }

    /*

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
        return null;
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

    //publishOn run onNext on parallel, doOnNext save
    private Mono<Void> fluxToSaveBlockingRepository(Flux<Users.User> flux, BlockingRepository<Users.User> blockingRepository) {
        return null;
    }

}
