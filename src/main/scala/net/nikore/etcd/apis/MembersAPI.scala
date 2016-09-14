package net.nikore.etcd.apis

import akka.actor.ActorRefFactory
import net.nikore.etcd.EtcdJsonProtocol._
import spray.client.pipelining._
import spray.http._
import spray.httpx.SprayJsonSupport._

import scala.concurrent.Future

/**
  * Defines a API mix-in trait for the EtcD "Members" API
  *
  * @author Liam Haworth
  */
trait MembersAPI {

  /**
    * Defines the actor system under which all Http request will be spawned
    */
  implicit val system: ActorRefFactory

  /**
    * Defines the base URI used when building requests to the API server
    */
  protected val connectionURI: String

  /**
    * Imports the Akka event dispatcher used by Spray
    */
  import system.dispatcher

  /**
    * Returns a list of EtcD members
    *
    * @return EtcdMember list
    */
  def listMembers: Future[List[EtcdMember]] = {
    val pipeline: HttpRequest => Future[List[EtcdMember]] = (
      sendReceive
        ~> mapRequestErrors
        ~> unmarshal[List[EtcdMember]]
    )

    pipeline(Get(s"$connectionURI/v2/members"))
  }

  /**
    * Informs the EtcD server of a new
    * peer to add to the cluster
    *
    * @param peerURLs A list of peer URLs for the new member
    * @return The EtcdMember with it's ID as assigned by the cluster
    */
  def addNewMember(peerURLs: List[String]): Future[EtcdMember] = {
    if(peerURLs.isEmpty)
      throw new Exception("The list of peer URLs provided is empty, a new member must have at least 1 peer URL")

    val pipeline: HttpRequest => Future[EtcdMember] = (
      sendReceive
        ~> mapRequestErrors
        ~> unmarshal[EtcdMember]
    )

    pipeline(Post(s"$connectionURI/v2/members", """{ \"peerURLs\": ${peerURLs.toJson.compactPrint} }"""))
  }

  /**
    * Informs the EtcD cluster to remove
    * and delete a member
    *
    * @param memberID The ID of the EtcdMember to remove
    * @return Returns the HttpResponse from the API server
    */
  def deleteMember(memberID: String): Future[HttpResponse] = {
    val pipeline: HttpRequest => Future[HttpResponse] = (
      sendReceive
        ~> mapRequestErrors
    )

    pipeline(Delete(s"$connectionURI/v2/members/$memberID"))
  }

  /**
    * Updates the peer URLs for an existing member
    *
    * @param memberID The ID of the EtcdMember to update
    * @param peerURLs A list of peer URLs to update the member listing with
    * @return Returns the HttpResponse from the API server
    */
  def updateMemberPeerURLs(memberID: String, peerURLs: List[String]): Future[HttpResponse] = {
    if(peerURLs.isEmpty)
      throw new Exception("The list of peer URLs provided is empty, a new member must have at least 1 peer URL")

    val pipeline: HttpRequest => Future[HttpResponse] = (
      sendReceive
        ~> mapRequestErrors
    )

    pipeline(Put(s"$connectionURI/v2/members/$memberID", """{ \"peerURLs\": ${peerURLs.toJson.compactPrint} }"""))
  }

  /**
    * Provides a simply way of handling errors returned from the API server
    */
  private val mapRequestErrors = (response: HttpResponse) => {
    if (response.status.isSuccess)
      response
    else
      response.status match {
        case StatusCodes.BadRequest =>
          throw new Exception("The request was malformed or missing fields, please ensure everything is correct in your request")
        case StatusCodes.Conflict =>
          throw new Exception("There was a conflict when executing the request, please ensure peer hasn't exited in the cluster before")
        case StatusCodes.InternalServerError =>
          throw new Exception("The API server encounted an internal error when processing the request")
      }
  }
}