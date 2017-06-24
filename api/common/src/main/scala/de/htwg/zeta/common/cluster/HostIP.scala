package de.htwg.zeta.common.cluster

import java.net.InetAddress

import grizzled.slf4j.Logging


object HostIP extends Logging {

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def load(): String = Option(InetAddress.getLocalHost.getHostAddress).get // throw exception when null

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def lookupNodeAddress(address: String): String = {
    val ret = address match {
      case IpAddress(parsedAddress) => parsedAddress
      case _ =>
        throw new IllegalArgumentException(s"cannot lookup node address: $address. address must have format: ip:port")
    }
    info(s"looked up seed: $address. Found: $ret")
    ret
  }

  def lookupNodeAddressOption(address: String): Option[String] = {
    IpAddress.unapply(address)
  }

  object IpAddress {
    private val delimiter = ":"
    private val maxPortNumber = 65535

    private def parseAddress(addressName: String): Option[String] = {
      val address =
        if (addressName == "localhost") {
          HostIP.load()
        } else {
          InetAddress.getByName(addressName).getHostAddress
        }
      Some(address)
    }

    private def parsePort(portString: String): Option[Int] = {
      val port = portString.toInt
      if (port < 0 || port > maxPortNumber) {
        None
      }
      else {
        Some(port)
      }
    }

    def unapply(ip: String): Option[String] = try {
      val s = ip.split(delimiter, -1).toList
      s.reverse match {
        // list size must be at least 2. tail can be Nil
        case stringPort :: head :: tail =>
          parsePort(stringPort).flatMap(port => parseAddress((head :: tail).reverse.mkString(delimiter)).map(address => s"$address$delimiter$port"))
        case _ => None
      }
    } catch {
      case _: Throwable => None
    }
  }

}
