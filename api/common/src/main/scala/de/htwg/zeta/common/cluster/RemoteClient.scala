package de.htwg.zeta.common.cluster

import javax.inject.Inject
import javax.inject.Singleton

import akka.actor.ActorPath
import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.AddressFromURIString
import akka.actor.RootActorPath
import akka.cluster.client.ClusterClientSettings
import akka.cluster.client.ClusterClient

@Singleton
class RemoteClient @Inject()(system: ActorSystem, clusterAddressSettings: ClusterAddressSettings) {

  val settings: ClusterClientSettings = {

    val initialContacts: Set[ActorPath] = clusterAddressSettings.addresses.map(clusterAddress => {
      s"${ClusterManager.clusterPathPrefix}${clusterAddress}"
    }).map {
      case AddressFromURIString(address) => RootActorPath(address) / "system" / "receptionist"
    }.toSet

    ClusterClientSettings(system).withInitialContacts(initialContacts)
  }

  val client: ActorRef = system.actorOf(ClusterClient.props(settings), "clusterClient")
}

/**
 * @param addresses list of addresses in format s"${ip}:${port}"
 */
case class ClusterAddressSettings(addresses: List[String])
