// @GENERATOR:play-routes-compiler
// @SOURCE:/home/andy/sbt Projects/BlinkOrSinkBackend/conf/routes
// @DATE:Thu Jun 04 16:31:50 CEST 2020


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
