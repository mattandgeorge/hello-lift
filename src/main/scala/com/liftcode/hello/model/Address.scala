package com.liftcode.hello.model

import _root_.net.liftweb.mapper._
import DB._
import _root_.net.liftweb.util._
import _root_.java.sql.Connection

class Address extends LongKeyedMapper[Address] with IdPK {
  def getSingleton = Address

	object line1 extends MappedString(this, 255)
	object line2 extends MappedString(this, 255)
	object line3 extends MappedString(this, 255)
	object postcode extends MappedString(this, 255)
	object country extends MappedString(this, 255)
}

object Address extends Address with LongKeyedMetaMapper[Address] {
  override def dbTableName = "addresses"
	override def formFields(toMap: Address) = line1 :: line2 :: line3 :: postcode :: country :: Nil
}