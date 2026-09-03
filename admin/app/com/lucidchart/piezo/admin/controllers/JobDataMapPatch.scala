package com.lucidchart.piezo.admin.controllers

import org.quartz.JobKey
import play.api.libs.json.*

/**
 * A request to set some keys in a single job's data map, leaving the rest of the job definition alone.
 *
 * Only the supplied keys are written. Keys already on the job that the request does not mention keep their values, and
 * there is no way to ask this endpoint to remove a key or clear a map.
 *
 * @param jobKey
 *   the job to update
 * @param entries
 *   the keys to set, in order
 */
case class JobDataMapPatch(jobKey: JobKey, entries: List[DataMap])

object JobDataMapPatch {

  /**
   * Parses the body of a bulk job data map update.
   *
   * The body looks like
   * {{{
   * {
   *   "jobs": [
   *     {
   *       "group": "sqs",
   *       "name": "enqueue-documents",
   *       "job-data-map": { "lastRunTime": "2026-08-30T17:00:00Z" }
   *     }
   *   ]
   * }
   * }}}
   *
   * `job-data-map` may also be given in the `[{"key": ..., "value": ...}]` shape that the job read endpoints return. It
   * is required, so that a misspelled field name is a visible error rather than a request that reports success while
   * changing nothing.
   *
   * Errors are collected across the whole `jobs` list, so a caller patching hundreds of jobs sees every bad entry at
   * once rather than one per round trip.
   */
  def parse(json: JsValue): JsResult[List[JobDataMapPatch]] = {
    for {
      jobs <- pathed((json \ "jobs").validate[List[JsValue]], JsPath \ "jobs")
      patches <- traverse(jobs.zipWithIndex) { case (job, index) =>
        pathed(parseJob(job), JsPath \ "jobs" \ index)
      }
    } yield patches
  }

  private def parseJob(json: JsValue): JsResult[JobDataMapPatch] = {
    for {
      group <- pathed((json \ "group").validate[String], JsPath \ "group")
      name <- pathed((json \ "name").validate[String], JsPath \ "name")
      entries <- pathed(parseEntries(json \ "job-data-map"), JsPath \ "job-data-map")
    } yield JobDataMapPatch(new JobKey(name, group), entries)
  }

  private def parseEntries(dataMap: JsLookupResult): JsResult[List[DataMap]] = {
    dataMap.toOption match {
      case Some(obj: JsObject) =>
        traverse(obj.fields.toList) { case (key, value) =>
          pathed(parseEntry(key, value), JsPath \ key)
        }
      case Some(JsArray(pairs)) =>
        traverse(pairs.toList.zipWithIndex) { case (pair, index) =>
          pathed(parsePairEntry(pair), JsPath \ index)
        }
      case _ =>
        JsError(JsPath, "Expected an object of key/value pairs or an array of {key, value} objects")
    }
  }

  private def parsePairEntry(pair: JsValue): JsResult[DataMap] = {
    for {
      key <- pathed((pair \ "key").validate[String], JsPath \ "key")
      value <- (pair \ "value").toOption
        .map(JsSuccess(_))
        .getOrElse(JsError(JsPath \ "value", "error.path.missing"))
      entry <- pathed(parseEntry(key, value), JsPath \ "value")
    } yield entry
  }

  private def parseEntry(key: String, value: JsValue): JsResult[DataMap] = {
    value match {
      case JsString(string) => JsSuccess(DataMap(key, string))
      case JsNumber(number) => JsSuccess(DataMap(key, number.bigDecimal.toPlainString))
      case JsBoolean(boolean) => JsSuccess(DataMap(key, boolean.toString))
      case _ => JsError(JsPath, "Job data values must be a string, number, or boolean")
    }
  }

  private def pathed[A](result: JsResult[A], path: JsPath): JsResult[A] = {
    result match {
      case JsSuccess(value, _) => JsSuccess(value)
      case error: JsError => error.repath(path)
    }
  }

  private def traverse[A, B](inputs: List[A])(f: A => JsResult[B]): JsResult[List[B]] = {
    inputs.map(f).foldRight[JsResult[List[B]]](JsSuccess(Nil)) { (next, sofar) =>
      (next, sofar) match {
        case (JsSuccess(value, _), JsSuccess(rest, _)) => JsSuccess(value +: rest)
        case (JsError(errors), JsError(restErrors)) => JsError(errors ++ restErrors)
        case (JsError(errors), _) => JsError(errors)
        case (_, error: JsError) => error
      }
    }
  }
}
