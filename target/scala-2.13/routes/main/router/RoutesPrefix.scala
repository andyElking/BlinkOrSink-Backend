// @GENERATOR:play-routes-compiler
// @SOURCE:/home/andy/sbt Projects/play starter/play-samples-play-scala-starter-example/conf/routes
// @DATE:Sun May 31 03:04:29 CEST 2020


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
