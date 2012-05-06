package hr.element.etb
package shiro

import scala.actors.Actor

import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject

object Karina extends Actor {

  lazy val cm = 
    net.sf.ehcache.CacheManager.getInstance.getCache("platform-admin-shiro-common")
  
  def act() {
    while (true) {
      receive {
        case ("CM") =>
          
        case ("HASROLE", x: String) =>
          val s = SecurityUtils.getSubject().getPrincipal()
          println("Actor power! : " + s)
          
          println("ACTOR IS IM: " + Thread.currentThread)
          
          val isit = SecurityUtils.getSubject().hasRole(x)
          
          println("IMA ROLU2 " + x + ": " + isit)
        
          reply(isit)          
      }
    }
  }    
  
  println("Starcinggu?")
  start()
}


object Utils extends Utils
private[shiro] trait Utils {
  import net.liftweb.common.Box

  implicit val subject = () => SecurityUtils.getSubject()

  private def test(f: Subject => Boolean)(implicit subject: () => Subject): Boolean =
    f(subject())

  def principal[T]: Box[T] =
    Box !! subject().getPrincipal.asInstanceOf[T]

  def isAuthenticated =
    test { _.isAuthenticated }

  def isRemembered =
    test { _.isRemembered }

  def isAuthenticatedOrRemembered = {
    isAuthenticated || isRemembered
  }

  def hasRole(role: String) =
    test { s =>
    
    
      println("##############################################3")
      println("##############################################3")
      println("SUBJECT: " + s.getPrincipal())
      println("IMA ROLU " + role + ": " + s.hasRole(role))

     // new Exception("res0").getStackTrace.toList.foreach(println)
      
      
      println("ICH BIN IM: " + Thread.currentThread)
      
      
      val result = Karina !! (("HASROLE", role))
      println("SUBJECT2: " + result)

      
      
      
      println("##############################################3")
      println("##############################################3")
      s.hasRole(role)
    }

  def lacksRole(role: String) =
    !hasRole(role)

  def hasPermission(permission: String) =
    test { _.isPermitted(permission) }

  def lacksPermission(permission: String) =
    !hasPermission(permission)

  def hasAnyRoles(roles: Seq[String]) =
    roles exists (r => hasRole(r.trim))

  def hasAllRoles(roles: Seq[String]) =
    roles forall(r => hasRole(r.trim))
}

import net.liftweb.common.{Box,Failure,Full}
import net.liftweb.util.Helpers
import net.liftweb.http.S
import org.apache.shiro.authc.{
  AuthenticationToken, IncorrectCredentialsException, UnknownAccountException,
  LockedAccountException, ExcessiveAttemptsException}



trait SubjectLifeCycle {
  import Utils._

  protected def logout() = subject().logout

  protected def login[T <: AuthenticationToken](token: T){
    def redirect = S.redirectTo(LoginRedirect.is.openOr("/"))
    if(!isAuthenticated){

      Helpers.tryo(subject().login(token)) match {
        case Failure(_,Full(err),_) => err match {
          case x: UnknownAccountException =>
            S.error("Unkown user account")
          case x: IncorrectCredentialsException =>
            S.error("Invalid username or password")
          case x: LockedAccountException =>
            S.error("Your account has been locked")
          case x: ExcessiveAttemptsException =>
            S.error("You have exceeded the number of login attempts")
          case _ =>
            S.error("Unexpected login error")
        }
        case _ => redirect
      }
    } else redirect
  }
}
