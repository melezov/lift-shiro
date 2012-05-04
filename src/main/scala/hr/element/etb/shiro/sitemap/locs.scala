package hr.element.etb
package shiro
package sitemap

import net.liftweb.http.S

/**
 * Lift SiteMap Integration
 */
object Locs {
  import net.liftweb.common.Full
  import net.liftweb.http.{RedirectResponse, RedirectWithState, S, RedirectState}
  import net.liftweb.sitemap.{Menu,Loc}
  import net.liftweb.sitemap.Loc.{If,EarlyResponse,DispatchLocSnippets}
  import Utils._

  implicit def listToPath(in: List[String]): String = in.mkString("/","/","")

  private val loginURL = Shirou.baseURL.vend ::: Shirou.loginURL.vend
  private val indexURL = Shirou.baseURL.vend ::: Shirou.indexURL.vend
  private val logoutURL = Shirou.baseURL.vend ::: Shirou.logoutURL.vend

  def RedirectBackToReferrer = {
    val uri = S.uriAndQueryString
    RedirectWithState(loginURL, RedirectState(() => { LoginRedirect.set(uri) }))
  }

  def RedirectToIndexURL = RedirectResponse(indexURL)

  private def DisplayError(message: String) = () =>
    RedirectWithState(loginURL, RedirectState(() => S.error(message)))

  def RequireAuthentication = If(
    () => isAuthenticated,
    () => RedirectBackToReferrer)

  def RequireNoAuthentication = If(
    () => !isAuthenticated,
    () => RedirectToIndexURL)

  def RequireRemembered = If(
    () => isAuthenticatedOrRemembered,
    () => RedirectBackToReferrer)

  def RequireNotRemembered = If(
    () => !isAuthenticatedOrRemembered,
    () => RedirectToIndexURL)

  def logoutMenu = Menu(Loc("Logout", logoutURL,
    S.??("logout"), logoutLocParams))

  private val logoutLocParams = RequireRemembered ::
    EarlyResponse(() => {
        if(isAuthenticatedOrRemembered){ subject().logout() }
      Full(RedirectResponse(Shirou.indexURL.vend))
    }) :: Nil

  def HasRole(role: String) =
    If(() => hasRole(role),
      DisplayError("You are the wrong role to access that resource."))

  def LacksRole(role: String) =
    If(() => lacksRole(role),
      DisplayError("You lack the sufficient role to access that resource."))

  def HasPermission(permission: String) =
    If(() => hasPermission(permission),
      DisplayError("Insufficient permissions to access that resource."))

  def LacksPermission(permission: String) =
    If(() => lacksPermission(permission),
      DisplayError("Overqualified permissions to access that resource."))

  def HasAnyRoles(roles: Seq[String]) =
    If(() => hasAnyRoles(roles),
       DisplayError("You are the wrong role to access that resource."))
}
