package net.nikore.etcd

import akka.actor.ActorSystem
import net.nikore.etcd.EtcdJsonProtocol.EtcdMember
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.sys.process._

/**
  * Defines unit tests for the EtcD Client.
  *
  * @author Liam Haworth
  */
class EtcdClientSpec extends FlatSpec with Matchers {

  /**
    * Defines the Actor System for this test
    */
  implicit val system = ActorSystem("scala-etcd-test")

  /**
    * Defines the default timeout for futures in the test
    */
  implicit val timeout = Timeout(60.seconds)

  /**
    * Defines the client for this test
    */
  val client = EtcdClient()


  ///////////////////////////
  // etcd - Keys API Tests //
  ///////////////////////////

  "Keys API" should "support inserting a new key-value pair" in {
    val setKeyFuture = client.setKey("testKey", "testValue")

    whenReady(setKeyFuture, timeout) { response =>
      response.action should equal("set")
      response.node.key should equal("/testKey")
      response.node.value.get should equal("testValue")
    }
  }

  it should "return a value for a key" in {
    val getKeyFuture = client.getKey("testKey")

    whenReady(getKeyFuture, timeout) { response =>
      response.action should equal("get")
      response.node.key should equal("/testKey")
      response.node.value.get should equal("testValue")
    }
  }

  it should "update the value of a key" in {
    val updateKeyFuture = client.setKey("testKey", "newValue")

    whenReady(updateKeyFuture, timeout) { response =>
      response.action should equal("set")
      response.node.key should equal("/testKey")
      response.node.value.get should equal("newValue")
      response.prevNode.get.value.get should equal("testValue")
    }
  }

  it should "delete a key-value pair" in {
    val deleteKeyFuture = client.deleteKey("testKey")

    whenReady(deleteKeyFuture, timeout) { response =>
      response.action should equal("delete")
      response.node.key should equal("/testKey")
    }
  }


  //////////////////////////////
  // etcd - Members API Tests //
  //////////////////////////////

  "Members API" should "list a default member" in {
    val memberListFuture = client.listMembers

    whenReady(memberListFuture, timeout) { list =>
      list.exists(_.name.get == "default") should equal(true)
    }
  }

  it should "add a new member to the cluster" in {
    val addMemberFuture = client.addNewMember(
      List[String]("http://etcd-server2:2380", "http://etcd-server2:7001"),
      name = Some("peer")
    )

    whenReady(addMemberFuture, timeout) { member =>
      member.id.isDefined should equal(true)
    }

    "docker-compose up -d etcd-server2".!
    Thread.sleep(5000)

    val memberListFuture = client.listMembers

    whenReady(memberListFuture, timeout) { list =>
      list.exists(_.name.get == "peer") should equal(true)
    }
  }

  it should "update peer URLs of a member" ignore {
    //TODO(LiamHaworth): I'm unsure of how to do this test
  }

  it should "remove a member from the cluser" in {
    val listMemberFuture = client.listMembers
    var member: EtcdMember = null

    whenReady(listMemberFuture, timeout) { list =>
      member = list.find(_.name.get == "peer").get
    }

    val deleteMemberFuture = client.deleteMember(member.id.get)

    whenReady(deleteMemberFuture, timeout) { response =>
      response.status.isSuccess should equal(true)
    }
  }
}
