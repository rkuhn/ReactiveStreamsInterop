package com.rolandkuhn.rsinterop

import ratpack.rx.RxRatpack
import ratpack.test.embed.EmbeddedApp
import ratpack.handling.Handler
import ratpack.handling.Context
import rx.Observable
import scala.collection.JavaConverters._
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import rx.RxReactiveStreams
import akka.stream.scaladsl.Sink
import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import ratpack.http.ResponseChunks
import java.util.function.Consumer
import ratpack.test.http.TestHttpClient
import reactor.rx.Streams

object ScalaMain extends App {
  val system = ActorSystem("InteropTest")
  implicit val mat = FlowMaterializer()(system)
  
  RxRatpack.initialize()
  
  EmbeddedApp.fromHandler(new Handler {
    override def handle(ctx: Context): Unit = {
      // RxJava Observable
      val intObs = Observable.from((1 to 10).asJava)
      // Reactive Streams Publisher
      val intPub = RxReactiveStreams.toPublisher(intObs)
      // Akka Streams Source
      val stringSource = Source(intPub).map(_.toString)
      // Reactive Streams Publisher
      val stringPub = stringSource.runWith(Sink.fanoutPublisher(1, 1))
      // Reactor Stream
      val linesStream = Streams.create(stringPub).map[String](new reactor.function.Function[String, String] {
        override def apply(in: String) = in + "\n"
      })
      // and now render the HTTP response
      ctx.render(ResponseChunks.stringChunks(linesStream))
    }
  }).test(new Consumer[TestHttpClient] {
    override def accept(client: TestHttpClient): Unit = {
      val text = client.getText()
      println(text)
      system.shutdown()
    }
  })
}