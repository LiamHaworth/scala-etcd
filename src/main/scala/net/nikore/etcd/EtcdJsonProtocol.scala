package net.nikore.etcd

import spray.json._

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
  implicit val etcdMemberFormat: JsonFormat[EtcdMember] = jsonFormat4(EtcdMember)

  /**
    * Defines a list of Etcd member nodes returned from the Etcd API
    *
    * @param members A list of <code>EtcdMember</code>
    */
  case class EtcdMemberList(members: List[EtcdMember])

  /**
    * Defines the spray-json format for <code>EtcdMemberList</code>
    */
  implicit  val etcdMemberListFormat: JsonFormat[EtcdMemberList] = jsonFormat1(EtcdMemberList)

  //for handling error messages
  case class Error(errorCode: Int, message: String, cause: String, index: Int)

  implicit val nodeResponseFormat = jsonFormat5(NodeResponse)
  implicit val etcdResponseFormat = jsonFormat3(EtcdResponse)

  implicit val nodeListElementFormat: JsonFormat[NodeListElement] = lazyFormat(jsonFormat4(NodeListElement))
  implicit val etcdResponseListFormat = jsonFormat2(EtcdListResponse)

  implicit val errorFormat = jsonFormat4(Error)
}
