package com.lucidchart.piezo.admin.util

import javax.tools.SimpleJavaFileObject
import java.net.URI
import javax.tools.JavaFileObject.Kind

/**
  */
class SourceFromString(name: String, code: String) extends SimpleJavaFileObject(
  URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE) {

  override def getCharContent(ignoreEncodingErrors: Boolean): String = {
    code
  }
}
