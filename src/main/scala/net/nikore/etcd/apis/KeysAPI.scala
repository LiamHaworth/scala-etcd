package net.nikore.etcd.apis

import akka.actor.ActorRefFactory
import net.nikore.etcd.EtcdExceptions.KeyNotFoundException
import net.nikore.etcd.EtcdJsonProtocol.{Error, EtcdListResponse, EtcdResponse}
import spray.client.pipelining._
import spray.json._
import spray.http.{HttpRequest, HttpResponse, Uri}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Defines a API mix-in trait for the EtcD "Keys" API
  *
  * @author Liam Haworth
  * @author Matt Christiansen
  */
trait KeysAPI {

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
    * Provides a simply way of handling errors returned from the API server
    */
  private val mapRequestErrors = (response: HttpResponse) => {
    if (response.status.isSuccess) response
    else {
      response.entity.asString.parseJson.convertTo[Error] match {
        case e if e.errorCode == 100 => throw KeyNotFoundException(e.message, "not found", e.index)
        case e => throw new RuntimeException("General error: " + e.toString)
      }
    }
  }

  /**
    * Defines the default HttpRequest pipeline for this API
    */
  private val defaultPipeline: HttpRequest => Future[EtcdResponse] = (
    sendReceive
      ~> mapRequestErrors
      ~> unmarshal[EtcdResponse]
    )

  /**
    * Queries the Etcd API server for a key to retrieve its value
    *
    * @param key The entry's key
    * @return Returns the parsed response from the Etcd server
    */
  def getKey(key: String): Future[EtcdResponse] = getKeyAndWait(key, wait = false)

  /**
    * Queries the Etcd API server for a key to retrieve its value
    *
    * If <b>wait</b> is set to true, then the call will hand
    * till the key/value pair is updated on the server, it will
    * then return the new value along with the previous value
    *
    * @param key The key of the pair
    * @param wait Defines if the API should wait for the pair to be updated before returning a result
    * @return Returns the parsed response from the Etcd server
    */
  def getKeyAndWait(key: String, wait: Boolean = true): Future[EtcdResponse] =
    defaultPipeline(Get(Uri(s"$connectionURI/v2/keys/$key").withQuery("wait" -> wait.toString)))

  /**
    * Informs the Etcd API server to create or update a key with a value. If <b>ttl</b>
    * is set, the Etcd server will automatically expire and remove the entry after the
    * specified time
    *
    * @param key The key of the pair
    * @param value The value of the pair
    * @param ttl (Optional) How long the key-pair should live before automatically expiring
    * @return Returns the parsed response from the Etcd server
    */
  def setKey(key: String, value: String, ttl: Option[Duration] = None): Future[EtcdResponse] = {
    val query: Map[String, String] = Map("value" -> value, "ttl" -> ttl.map(_.toSeconds.toString).getOrElse(""))
    defaultPipeline(Put(Uri(s"$connectionURI/v2/keys/$key").withQuery(query)))
  }

  /**
    * Informs the Etcd API server to remove a key-pair from the cluster
    *
    * @param key The key of the pair
    * @return Returns the parsed response from the Etcd server
    */
  def deleteKey(key: String): Future[EtcdResponse] = defaultPipeline(Delete(s"$connectionURI/v2/keys/$key"))

  /**
    * Informs the Etcd API server to create a pseudo "directory"
    * that can store key-value pairs, these directories can be layered
    * and the parent directories don't have to exist to create the directory
    *
    * @param dir The path of the directory to create
    * @return Returns the parsed response from the Etcd server
    */
  def createDir(dir: String): Future[EtcdResponse] =
    defaultPipeline(Put(Uri(s"$connectionURI/v2/keys/${if (dir.endsWith("/")) dir.dropRight(1) else dir}").withQuery("dir" -> "true")))

  /**
    * Queries the Etcd API server for a list items (i.e. directories and keys)
    * in the selected directory
    *
    * @param dir The directory path to list items under
    * @param recursive (Optional) Defines if the API should recursively list in sub-folders of the directory
    * @return Returns the parsed response from the Etcd server
    */
  def listDir(dir: String, recursive: Boolean = false): Future[EtcdListResponse] = {
    val pipline: HttpRequest => Future[EtcdListResponse] = (
      sendReceive
        ~> mapRequestErrors
        ~> unmarshal[EtcdListResponse]
      )

    pipline(Get(Uri(s"$connectionURI/v2/keys/${if (dir.endsWith("/")) dir.dropRight(1) else dir}").withQuery("recursive" -> recursive.toString)))
  }

  /**
    * Informs the Etcd API server to delete a directory from the cluster
    *
    * @param dir The directory path to delete
    * @param recursive (Optional) Defines if the API should recursively delete in the directory
    * @return Returns the parsed response from the Etcd server
    */
  def deleteDir(dir: String, recursive: Boolean = false): Future[EtcdResponse] = {
    defaultPipeline(Delete(Uri(s"$connectionURI/v2/keys/$dir").withQuery("recursive" -> recursive.toString)))
  }
}
