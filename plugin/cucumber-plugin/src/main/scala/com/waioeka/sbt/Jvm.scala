/*
 * Copyright (c) 2015, Michael Lewis
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.waioeka.sbt

import java.io.File
import java.lang.management.ManagementFactory

import org.apache.commons.lang3.SystemUtils
import sbt._


case class Jvm(classPath: List[File], envParams : Map[String, String]) {

  /** Classpath separator, must be ';' for Windows, otherwise : */
  private val sep = if (SystemUtils.IS_OS_WINDOWS) ";" else ":"


  /** Get the JVM options passed into SBT. */
  import scala.collection.JavaConverters._
  private val runtimeArgs = ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.toVector

  /** The Jvm parameters. */
  private val jvmArgs : Vector[String]
          = Vector("-classpath", classPath map(_.toPath) mkString sep) ++ runtimeArgs

  /**
    * Invoke the main class.
    *
    * @param mainClass        the class name containing the main method.
    * @param parameters       the parameters to pass to the main method.
    * @param outputStrategy   the SBT output strategy.
    * @return  the return code of the Jvm.
    */
  def run(mainClass : String, parameters : List[String], outputStrategy: OutputStrategy) : Int = {

    val logger = outputStrategy.asInstanceOf[LoggedOutput].logger
    val args =  jvmArgs :+ mainClass

    logger.debug(s"args ${args mkString " "}, env: $envParams, parameters: ${parameters.mkString(",")}")

    val opts = ForkOptions(javaHome = None,
                outputStrategy = Some(outputStrategy),
                bootJars = Vector.empty,
                workingDirectory = None,
                runJVMOptions = args,
                connectInput = false,
                envVars = envParams)


    Fork.java(opts,parameters)
  }


}
