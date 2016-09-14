package net.nikore.etcd

import spray.http.HttpResponse
import spray.json._
import spray.json.lenses.JsonLenses._
import spray.httpx.unmarshalling.{FromResponseUnmarshaller, MalformedContent}

object EtcdJsonProtocol extends DefaultJsonProtocol {

  //single key/values
  case class NodeResponse(key: String, value: Option[String], modifiedIndex: Int, createdIndex: Int, ttl: Option[Int])
  case class EtcdResponse(action: String, node: NodeResponse, prevNode: Option[NodeResponse])

  //for hanlding dirs
  case class NodeListElement(key: String, dir: Option[Boolean], value: Option[String], nodes: Option[List[NodeListElement]])
  case class EtcdListResponse(action: String, node: NodeListElement)

  /**
    * Defines a Etcd node that is a member of the cluster
    *
    * @param id The ID of the member
    * @param name The name of the member
    * @param peerURLs A list of peer URLs related to the member
    * @param clientURLs A list of clientURLs related to the member
    */
  case class EtcdMember(id: String, name: String, peerURLs: List[String], clientURLs: List[String])

  /**
    * Defines the spray-json format for <code>EtcdMember</code>
    */
  implicit val etcdMemberFormat: RootJsonFormat[EtcdMember] = jsonFormat4(EtcdMember)

  /**
    * Provides a response unmarshaller to retreive a list of EtcdMembers from a nested JSON response
    */
  implicit val etcdMemberListUnmarshaller = new FromResponseUnmarshaller[List[EtcdMember]] {
    def apply(response: HttpResponse): Either[MalformedContent, List[EtcdMember]] = try {
      Right(response.entity.asString.extract[List[EtcdMember]](
        'members
      ))
    } catch { case ex: Throwable =>
      Left(MalformedContent("Unable to unmarshal a EtcdMember list response", ex))
    }
  }

  //for handling error messages
  case class Error(errorCode: Int, message: String, cause: String, index: Int)

  implicit val nodeResponseFormat = jsonFormat5(NodeResponse)
  implicit val etcdResponseFormat = jsonFormat3(EtcdResponse)

  implicit val nodeListElementFormat: JsonFormat[NodeListElement] = lazyFormat(jsonFormat4(NodeListElement))
  implicit val etcdResponseListFormat = jsonFormat2(EtcdListResponse)

  implicit val errorFormat = jsonFormat4(Error)
}
