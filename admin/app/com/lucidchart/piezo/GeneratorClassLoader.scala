package com.lucidchart.piezo

import org.objectweb.asm.{ClassWriter, Opcodes, Type}
import org.quartz.{Job, JobExecutionContext}
import org.quartz.spi.ClassLoadHelper
import org.slf4j.LoggerFactory

class DummyJob extends Job {
  def execute(context: JobExecutionContext): Unit = {
    throw new UnsupportedOperationException()
  }
}

class GeneratorClassLoader extends ClassLoader(classOf[GeneratorClassLoader].getClassLoader) with ClassLoadHelper {
  val logger = LoggerFactory.getLogger(this.getClass)

  private[this] def generate(name: String) = {
    val classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
    classWriter.visit(
      Opcodes.V1_8,
      Opcodes.ACC_PUBLIC,
      name.replace('.', '/'),
      null,
      Type.getInternalName(classOf[DummyJob]),
      null,
    )

    // Minimal constructor that just calls the super constructor and returns.
    val constructorWriter = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
    constructorWriter.visitVarInsn(Opcodes.ALOAD, 0)
    constructorWriter.visitMethodInsn(
      Opcodes.INVOKESPECIAL,
      Type.getInternalName(classOf[Object]),
      "<init>",
      "()V",
      false,
    )
    constructorWriter.visitInsn(Opcodes.RETURN)
    constructorWriter.visitMaxs(0, 0)
    constructorWriter.visitEnd()

    classWriter.visitEnd()

    classWriter.toByteArray
  }

  def getClassLoader: GeneratorClassLoader = this

  def loadClass[T](name: String, clazz: Class[T]) = loadClass(name).asInstanceOf[Class[_ <: T]]

  def initialize() = ()

  override def loadClass(name: String): Class[_] = try {
    super.loadClass(name)
  } catch {
    case _: ClassNotFoundException =>
      logger.info(s"Dynamically generated dummy job for $name")
      val bytes = generate(name)
      defineClass(name, bytes, 0, bytes.length)
  }

}
