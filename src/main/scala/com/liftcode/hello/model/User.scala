package com.liftcode.hello.model

import _root_.net.liftweb.mapper._
import DB._
import _root_.net.liftweb.http._
import _root_.net.liftweb.util._
import _root_.java.sql.Connection
import _root_.scala.xml.{NodeSeq, Node, Group, Text, Elem}
import S._

/**
 * The singleton that has methods for accessing the database
 */
object User extends User with MetaMegaProtoUser[User] {
  override def dbTableName = "users" // define the DB table name
  override def screenWrap = Full(<lift:surround with="default" at="content">
			       <lift:bind /></lift:surround>)
  // define the order fields will appear in forms and output
  override def fieldOrder = List(id, firstName, lastName, email,
  locale, timezone, password, textArea)

  // comment this line out to require email validations
  override def skipEmailValidation = true

	override def afterCreate = createAddresses _ :: super.afterCreate
	override def beforeSave = saveAddresses _ :: super.beforeSave
	override def beforeUpdate = saveAddresses _ :: super.beforeUpdate

	private def createAddresses(u:User) : Unit = {
		u.address(new Address)
	}
	
	private def saveAddresses(u:User) : Unit = {
		u.address(u.address.obj.open_!.saveMe)
		Log.info(u.address.obj.open_!.toString)
	}
	
	override def clean_?(toCheck: User): Boolean = 
		Address.clean_?(toCheck.getAddress) && super.clean_?(toCheck)
	
	override def dirty_?(toCheck: User): Boolean = 
		Address.dirty_?(toCheck.getAddress) && super.dirty_?(toCheck)

	override def editXhtml(user: User) = 
		<lift:surround with="default" at="content">
			<form method="post" action={S.uri}>
			<h4>Account details</h4>
      <div>
		      { localForm(user, false) }
			</div>
			<h4>Address</h4>
			<div>
				{ addressForm( user.getAddress ) }
			</div>
			<p><user:submit /></p>
			</form>
		</lift:surround>

  override def signupXhtml(user: User) = 
    <lift:surround with="default" at="content">
			<form method="post" action={S.uri}>
			<h4>Account details</h4>
      <div>
		      { localForm(user, false) }
			</div>
			<h4>Address</h4>
			<div>
				{ addressForm( user.getAddress ) }
			</div>
			<p><user:submit /></p>
			</form>
    </lift:surround>


	private def localForm(user: User, ignorePassword: Boolean): NodeSeq = {  
		  signupFields.  
			  map(fi => getSingleton.getActualBaseField(user, fi)).  
			  filter(f => !ignorePassword || (f match {  
			        case f: MappedPassword[User] => false  
			        case _ => true  
			      })).  
			  flatMap(f =>  
			    f.toForm.toList.map(form => <p><label>{f.displayName}</label> {form}</p>) )
	}
	
	private def addressForm(address : Address): NodeSeq = {
		val formFields = address.formFields
		formFields.
			map(fi => address.getSingleton.getActualBaseField(address, fi)).
			flatMap( f => 
				f.toForm.toList.map(form => <p><label>{f.displayName}</label> {form}</p>))
	}
}

/**
 * An O-R mapped "User" class that includes first name, last name, password and we add a "Personal Essay" to it
 */
class User extends MegaProtoUser[User] {
  def getSingleton = User // what's the "meta" server

  // define an additional field for a personal essay
  object textArea extends MappedTextarea(this, 2048) {
    override def textareaRows  = 10
    override def textareaCols = 50
    override def displayName = "Personal Essay"
  }

	object address extends MappedLongForeignKey(this, Address) {
    override def dbIndexed_? = true
  }
	
	def getAddress : Address =
		address.obj match {
			case Full(a) => a
			case _ =>
				val a = new Address
				address(a)
				address.primeObj(Full(a))
				a
		}
}
