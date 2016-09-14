package net.nikore.etcd

import akka.actor.ActorRefFactory
import net.nikore.etcd.apis.{KeysAPI, MembersAPI}

/**
  * Provides a quick and simple way to build
  * the client without the end user having to
  * define a new client each time
  *
  * @author Liam Haworth
  * @author Matt Christiansen
  */
object EtcdClient {

  /**
    * Builds a new EtcdClient with all the default settings
    *
    * @param system The governing actor system that spray can leverage when making requests
    * @return A newly created EtcdClient
    */
  def apply()(implicit system: ActorRefFactory): EtcdClient = {
    new EtcdClient("localhost", 4001, "http")(system)
  }

  /**
    * Builds a new EtcdClient with the selected host and
    * with the default port and protocol
    *
    * @param host The host name or OP address of the EtcD API server
    * @param system The governing actor system that spray can leverage when making requests
    * @return A newly created EtcdClient
    */
  def apply(host: String)(implicit system: ActorRefFactory): EtcdClient = {
    new EtcdClient(host, 4001, "http")(system)
  }

  /**
    * Builds a new EtcdClient with the selected host and port
    * and with the default protocol
    *
    * @param host The host name or OP address of the EtcD API server
    * @param port The port that the API server is listening for queries
    * @param system The governing actor system that spray can leverage when making requests
    * @return A newly created EtcdClient
    */
  def apply(host: String, port: Int)(implicit system: ActorRefFactory): EtcdClient = {
    new EtcdClient(host, port, "http")(system)
  }

  /**
    * Builds a new EtcdClient with the selected host, port and protocol
    *
    * @param host The host name or OP address of the EtcD API server
    * @param port The port that the API server is listening for queries
    * @param protocol The protocol to use when communicating with the server (i.e HTTP or HTTPS)
    * @param system The governing actor system that spray can leverage when making requests
    * @return A newly created EtcdClient
    */
  def apply(host: String, port: Int, protocol: String)(implicit system: ActorRefFactory): EtcdClient = {
    new EtcdClient(host, port, protocol)(system)
  }
}

/**
  * The EtcD Client provides the end user with a
  * simply and easy to understand method for interacting
  * with the CoreOS EtcD server API.
  *
  * The client is built upon Spray and uses mix-ins to
  * define separate API groups.
  *
  * @param host The host name or IP address of the EtcD API server
  * @param port The port that the API server is listening for queries
  * @param protocol The protocol to use when communicating with the server (i.e HTTP or HTTPS)
  * @param system The governing actor system that spray can leverage when making requests
  *
  * @author Liam Haworth
  * @author Matt Christiansen
  */
class EtcdClient(host: String, port: Int, protocol: String)(implicit override val system: ActorRefFactory)
  extends MembersAPI
  with    KeysAPI {

  /**
    * Defines the base URI used when building requests to the API server
    */
  override protected val connectionURI: String = s"$protocol://$host:$port"
}

