package models.actors

import akka.actor.{Actor, ActorLogging, DeadLetter, Props}

object DeadLetterMonitorActor {
  def props: Props = Props(new DeadLetterMonitorActor())
}

class DeadLetterMonitorActor extends Actor with ActorLogging{
  override def receive: Receive = {
    case d: DeadLetter => {
      log.error(s"DeadLetterMonitorActor : saw dead letter $d")
    }
    case _ => log.info("DeadLetterMonitorActor : got a message")
  }
}
