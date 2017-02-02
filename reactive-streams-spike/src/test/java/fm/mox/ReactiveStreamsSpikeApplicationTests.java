package fm.mox;

import static org.junit.Assert.assertTrue;

import java.time.Duration;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
public class ReactiveStreamsSpikeApplicationTests {

	@Test
	public void contextLoads() {
		log.info("ddd");
		assertTrue(true);
		Mono.never();
	}

	/*
	* imperative/blocking spring repository api
async/non blocking show spring 5 repository
items may be available in the same thread
you show what should be done
	* */

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

		StepVerifier.create(flux) //can control bounded demand. 1 blocks
			.expectNext("1", "2")
			.expectComplete()
			.verify();

	}

	private Flux<String> twoVals() {
//		return Flux.fromIterable(Arrays.asList("1", "3"));
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

	///////////////////// verifier

	@Test
	public void expectElementsThenComplete() throws Exception {
		expectFooBarComplete(Flux.just("foo", "bar"));
	}

	private void expectFooBarComplete(Flux<String> flux) {
//		fail();
		StepVerifier.create(flux)
			.expectNext("foo", "bar")
			.expectComplete()
			.verify();
	}







	/*
	* show unit tests:

write the stepverifier instead
any type of assertions lib

with virtualtime use a supplier
to go async we use flatMap and return

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
}
