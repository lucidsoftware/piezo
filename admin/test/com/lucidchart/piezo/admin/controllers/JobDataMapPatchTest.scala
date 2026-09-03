package com.lucidchart.piezo.admin.controllers

import org.specs2.mutable.*
import play.api.libs.json.*

class JobDataMapPatchTest extends Specification {

  private def parseOne(json: JsValue): JobDataMapPatch = {
    JobDataMapPatch.parse(json) match {
      case JsSuccess(List(patch), _) => patch
      case other => throw new AssertionError(s"Expected exactly one patch, got $other")
    }
  }

  private def body(dataMap: JsValue): JsValue = {
    Json.obj("jobs" -> Json.arr(Json.obj("group" -> "g", "name" -> "n", "job-data-map" -> dataMap)))
  }

  "JobDataMapPatch.parse" should {

    "read the job key and the keys to set" in {
      val patch = parseOne(body(Json.obj("a" -> "1")))
      patch.jobKey.getGroup must equalTo("g")
      patch.jobKey.getName must equalTo("n")
      patch.entries must equalTo(List(DataMap("a", "1")))
    }

    "stringify numbers and booleans" in {
      val entries = parseOne(body(Json.obj("i" -> 1756400000000L, "d" -> 1.5, "b" -> true))).entries
      entries must containTheSameElementsAs(
        List(DataMap("i", "1756400000000"), DataMap("d", "1.5"), DataMap("b", "true")),
      )
    }

    "not use scientific notation for large numbers" in {
      parseOne(body(Json.obj("i" -> BigDecimal("1e20")))).entries must equalTo(
        List(DataMap("i", "100000000000000000000")),
      )
    }

    "accept the key/value array shape returned by the read endpoints" in {
      val dataMap = Json.arr(Json.obj("key" -> "a", "value" -> "1"), Json.obj("key" -> "b", "value" -> "2"))
      parseOne(body(dataMap)).entries must equalTo(List(DataMap("a", "1"), DataMap("b", "2")))
    }

    "accept an empty data map as a job with nothing to change" in {
      parseOne(body(Json.obj())).entries must beEmpty
    }

    "reject a null value rather than read it as a removal" in {
      JobDataMapPatch.parse(body(Json.obj("a" -> JsNull))) must beAnInstanceOf[JsError]
    }

    "reject a null value in the key/value array shape" in {
      val dataMap = Json.arr(Json.obj("key" -> "a", "value" -> JsNull))
      JobDataMapPatch.parse(body(dataMap)) must beAnInstanceOf[JsError]
    }

    "reject a value that is not a scalar" in {
      JobDataMapPatch.parse(body(Json.obj("a" -> Json.obj("b" -> "1")))) must beAnInstanceOf[JsError]
    }

    "reject a job with no data map, so a misspelled field is not a silent no-op" in {
      val json = Json.obj("jobs" -> Json.arr(Json.obj("group" -> "g", "name" -> "n", "jobDataMap" -> Json.obj())))
      JobDataMapPatch.parse(json) must beAnInstanceOf[JsError]
    }

    "reject a body with no jobs field" in {
      JobDataMapPatch.parse(Json.obj()) must beAnInstanceOf[JsError]
    }

    "report every bad job in one response" in {
      val json = Json.obj(
        "jobs" -> Json.arr(
          Json.obj("name" -> "missing-group", "job-data-map" -> Json.obj()),
          Json.obj("group" -> "g", "name" -> "n", "job-data-map" -> Json.obj()),
          Json.obj("group" -> "g", "name" -> "n", "job-data-map" -> Json.obj("a" -> Json.arr())),
        ),
      )
      JobDataMapPatch.parse(json) match {
        case JsError(errors) =>
          errors.map(_._1.toJsonString).toList must containTheSameElementsAs(
            List("obj.jobs[0].group", "obj.jobs[2].job-data-map.a"),
          )
        case other => throw new AssertionError(s"Expected errors, got $other")
      }
    }
  }
}
