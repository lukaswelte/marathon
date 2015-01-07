package mesosphere.marathon.api.v2.json

import mesosphere.marathon.MarathonSpec
import mesosphere.marathon.state.{ AppDefinition, UpgradeStrategy }
import mesosphere.marathon.state.PathId._
import mesosphere.marathon.state.Timestamp

import org.scalatest.Matchers
import play.api.libs.json._

class AppDefinitionFormatsTest
    extends MarathonSpec
    with AppDefinitionFormats
    with HealthCheckFormats
    with Matchers {

  import Formats.PathIdFormat

  object Fixture {
    val a1 = AppDefinition(
      id = "app1".toPath,
      cmd = Some("sleep 10"),
      version = Timestamp(1)
    )

    val j1 = Json.parse("""
      {
        "id": "app1",
        "cmd": "sleep 10",
        "version": "1970-01-01T00:00:00.001Z"
      }
    """)
  }

  test("ToJson") {
    import Fixture._
    import AppDefinition._

    val r1 = Json.toJson(a1)
    // check supplied values
    r1 \ "id" should equal (JsString("app1"))
    r1 \ "cmd" should equal (JsString("sleep 10"))
    r1 \ "version" should equal (JsString("1970-01-01T00:00:00.001Z"))
    // check default values
    r1 \ "args" should equal (JsNull)
    r1 \ "user" should equal (JsNull)
    r1 \ "env" should equal (JsObject(DefaultEnv.mapValues(JsString(_)).toSeq))
    r1 \ "instances" should equal (JsNumber(DefaultInstances))
    r1 \ "cpus" should equal (JsNumber(DefaultCpus))
    r1 \ "mem" should equal (JsNumber(DefaultMem))
    r1 \ "disk" should equal (JsNumber(DefaultDisk))
    r1 \ "executor" should equal (JsString(DefaultExecutor))
    r1 \ "constraints" should equal (Json.toJson(DefaultConstraints))
    r1 \ "uris" should equal (Json.toJson(DefaultUris))
    r1 \ "storeUrls" should equal (Json.toJson(DefaultStoreUrls))
    r1 \ "ports" should equal (JsArray(DefaultPorts.map { p => JsNumber(p.toInt) }))
    r1 \ "requirePorts" should equal (JsBoolean(DefaultRequirePorts))
    r1 \ "backoffSeconds" should equal (JsNumber(DefaultBackoff.toSeconds))
    r1 \ "backoffFactor" should equal (JsNumber(DefaultBackoffFactor))
    r1 \ "container" should equal (JsNull)
    r1 \ "healthChecks" should equal (Json.toJson(DefaultHealthChecks))
    r1 \ "dependencies" should equal (Json.toJson(DefaultDependencies))
    r1 \ "upgradeStrategy" should equal (Json.toJson(DefaultUpgradeStrategy))
  }

  test("FromJson") {
    import Fixture._
    import AppDefinition._

    val r1 = j1.as[AppDefinition]
    // check supplied values
    r1.id should equal (a1.id)
    r1.cmd should equal (a1.cmd)
    r1.version should equal (Timestamp(1))
    // check default values
    r1.args should equal (DefaultArgs)
    r1.user should equal (DefaultUser)
    r1.env should equal (DefaultEnv)
    r1.instances should equal (DefaultInstances)
    r1.cpus should equal (DefaultCpus)
    r1.mem should equal (DefaultMem)
    r1.disk should equal (DefaultDisk)
    r1.executor should equal (DefaultExecutor)
    r1.constraints should equal (DefaultConstraints)
    r1.uris should equal (DefaultUris)
    r1.storeUrls should equal (DefaultStoreUrls)
    r1.ports should equal (DefaultPorts)
    r1.requirePorts should equal (DefaultRequirePorts)
    r1.backoff should equal (DefaultBackoff)
    r1.backoffFactor should equal (DefaultBackoffFactor)
    r1.container should equal (DefaultContainer)
    r1.healthChecks should equal (DefaultHealthChecks)
    r1.dependencies should equal (DefaultDependencies)
    r1.upgradeStrategy should equal (DefaultUpgradeStrategy)
  }

}

