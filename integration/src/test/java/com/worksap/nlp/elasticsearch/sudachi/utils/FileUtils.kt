/*
 * Copyright (c) 2023 Works Applications Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worksap.nlp.elasticsearch.sudachi.utils

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.Predicate

private object AllPaths : Predicate<Path> {
  override fun test(t: Path): Boolean = true
}

fun Path.copyRecursively(destination: Path, filter: Predicate<Path> = AllPaths) {
  val source = this
  Files.createDirectories(destination)
  Files.walkFileTree(
      source,
      object : FileVisitor<Path> {
        override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
          val relative = source.relativize(dir)
          val dest = destination.resolve(relative)
          if (Files.notExists(dest)) {
            Files.createDirectory(dest)
          } else check(Files.isDirectory(dest)) { "$dest must be a directory" }
          return FileVisitResult.CONTINUE
        }

        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
          val relative = source.relativize(file)
          if (filter.test(file)) {
            Files.copy(file, destination.resolve(relative))
          }
          return FileVisitResult.CONTINUE
        }

        override fun visitFileFailed(file: Path, exc: IOException?): FileVisitResult {
          return FileVisitResult.TERMINATE
        }

        override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
          return FileVisitResult.CONTINUE
        }
      })
}

object FileUtils {
  @JvmStatic
  @JvmOverloads
  fun copyRecursively(source: Path, destination: Path, filter: Predicate<Path> = AllPaths) {
    source.copyRecursively(destination, filter)
  }

  @JvmStatic
  fun deleteRecursively(directory: Path) {
    Files.walkFileTree(
        directory,
        object : FileVisitor<Path> {
          override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes): FileVisitResult {
            return FileVisitResult.CONTINUE
          }

          override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            try {
              Files.delete(file)
            } catch (e: Exception) {
              // do nothing
            }
            return FileVisitResult.CONTINUE
          }

          override fun visitFileFailed(file: Path, exc: IOException?): FileVisitResult {
            return FileVisitResult.TERMINATE
          }

          override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
            try {
              Files.delete(dir)
            } catch (e: Exception) {
              // do nothing
            }
            return FileVisitResult.CONTINUE
          }
        })
  }
}
