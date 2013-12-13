package com.lucidchart.piezo.admin.views

import _root_.views.html.helper.FieldConstructor

object FormHelpers {
  implicit val myFields = FieldConstructor(com.lucidchart.piezo.admin.views.html.helpers.fieldConstructor.f)
}
