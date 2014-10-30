package com.rolandkuhn.rsinterop;

import org.reactivestreams.Publisher;



import akka.actor.ActorSystem;
import akka.stream.FlowMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import ratpack.http.ResponseChunks;
import ratpack.rx.RxRatpack;
import ratpack.test.embed.EmbeddedApp;import reactor.rx.Stream;
import reactor.rx.Streams;
import rx.Observable;
import rx.RxReactiveStreams;


public class JavaMain {

	public static void main(String[] args) {
		final ActorSystem system = ActorSystem.create("InteropTest");
		final FlowMaterializer mat = FlowMaterializer.create(system);
		RxRatpack.initialize();

		EmbeddedApp.fromHandler(ctx -> {
			final Integer[] ints = new Integer[10];
			for (int i = 0; i < ints.length; ++i) {
				ints[i] = i;
			}
			// RxJava Observable
			final Observable<Integer> intObs = Observable.from(ints);
			// Reactive Streams Publisher
			final Publisher<Integer> intPub = RxReactiveStreams.toPublisher(intObs);
			// Akka Streams Source
			final Source<String> stringSource = Source.from(intPub).map(Object::toString);
			// Reactive Streams Publisher
			final Publisher<String> stringPub = stringSource.runWith(Sink.<String>fanoutPublisher(1, 1), mat);
			// Reactor Stream
			final Stream<String> linesStream = Streams.create(stringPub).map(i -> i + "\n");
			// and now render the HTTP response
			ctx.render(ResponseChunks.stringChunks(linesStream));
		}).test(client -> {
			final String text = client.getText();
			System.out.println(text);
			system.shutdown();
		});;
	}
}
