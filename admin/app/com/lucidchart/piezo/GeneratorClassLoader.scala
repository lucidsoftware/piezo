package com.lucidchart.piezo

import org.objectweb.asm.{ClassWriter, Opcodes, Type}
import org.quartz.Job
import org.quartz.spi.ClassLoadHelper
import org.slf4j.LoggerFactory

class GeneratorClassLoader extends ClassLoader(classOf[GeneratorClassLoader].getClassLoader) with ClassLoadHelper {
  val logger = LoggerFactory.getLogger(this.getClass)

  private[this] def generate(name: String) = {
    val classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
    classWriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, name.replace('.', '/'), null, Type.getInternalName(classOf[Object]), Array(Type.getInternalName(classOf[Job])))

    val constructorWriter = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
    constructorWriter.visitVarInsn(Opcodes.ALOAD, 0)
    constructorWriter.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(classOf[Object]), "<init>", "()V", false)
    constructorWriter.visitInsn(Opcodes.RETURN)
    constructorWriter.visitMaxs(0, 0)
    constructorWriter.visitEnd()

    classOf[Job].getDeclaredMethods.foreach { method =>
      val methodWriter = classWriter.visitMethod(Opcodes.ACC_PUBLIC, method.getName, Type.getMethodDescriptor(method), null, null)
      methodWriter.visitTypeInsn(Opcodes.NEW, Type.getInternalName(classOf[UnsupportedOperationException]))
      methodWriter.visitInsn(Opcodes.DUP)
      methodWriter.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(classOf[UnsupportedOperationException]), "<init>", "()V", false)
      methodWriter.visitInsn(Opcodes.ATHROW)
      methodWriter.visitMaxs(0, 0)
      methodWriter.visitEnd()
    }

    classWriter.visitEnd()

    classWriter.toByteArray
  }

  def getClassLoader = this

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
