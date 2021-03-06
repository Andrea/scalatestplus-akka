package org.scalatestplus.akka

import java.util.concurrent.ExecutionException

import akka.actor.{Props, Actor, ActorSystem}
import akka.testkit.{ImplicitSender, TestActors, TestKit}

import org.scalatest._
import org.scalatest.time.{ Milliseconds, Seconds, Span}


class IgnoringActor extends Actor {
  override def receive = {
    case message ⇒
  }
}

class ReceivingMsgSpec(system: ActorSystem) extends AsyncTestKit(system) with ImplicitSender
  with AsyncWordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("ReceivingMsg"))

  val ignoringActor = system.actorOf(Props[IgnoringActor])
  val echo = system.actorOf(TestActors.echoActorProps)

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  "Receive message using patience" should {
    "succeed" in {
      echo ! "hello world"
      receivingMsg("hello world") map { _ =>
        Succeeded
      }
    }
  }

  it should {
    "Fail if the message is incorrect" in {
      echo ! "hello world"
      val str = "goodbye cruel world"
      recoverToExceptionIf[ExecutionException] {
        receivingMsg(str)
      }.map{ ex  =>
        ex.getCause shouldBe an [AssertionError]
        ex.getCause.getMessage should include (str)
      }
    }
  }

  it should {
    "Fail if the message times out" in {
      ignoringActor ! "42!"
      recoverToExceptionIf[ExecutionException] {
        receivingMsg("42!")
      }.map{ ex  =>
        ex.getCause shouldBe an [AssertionError]
        ex.getCause.getMessage should include ("42")
      }
    }
  }

  "Receive message using timespan" should {
    "succeed" in {
      echo ! "hello world"
      receivingMsg("hello world", Span(10, Seconds) ) map { _ =>
        Succeeded
      }
    }
  }

  it should {
    "Fail if the message is incorrect" in {
      echo ! "hello world"
      val str = "goodbye cruel world"
      recoverToExceptionIf[ExecutionException] {
        receivingMsg(str,Span(10, Seconds))
      }.map{ ex  =>
        ex.getCause shouldBe an [AssertionError]
        ex.getCause.getMessage should include (str)
      }
    }
  }

  it should {
    "Fail if the message times out" in {
      ignoringActor ! "42!"
      recoverToExceptionIf[ExecutionException] {
        receivingMsg("42!", Span(100, Milliseconds))
      }.map{ ex  =>
        ex.getCause shouldBe an [AssertionError]
        ex.getCause.getMessage should include ("42")
      }
    }
  }
}

