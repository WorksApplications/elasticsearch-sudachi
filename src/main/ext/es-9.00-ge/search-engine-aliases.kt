/*
 * Copyright (c) 2022-2025 Works Applications Co., Ltd.
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

@file:Suppress("PackageDirectoryMismatch")
@file:JvmName("SearchEngineAliasesEs9")

package com.worksap.nlp.search.aliases

/** ES 9.0+ specific extensions: Environment.configFile() was changed to Environment.configDir() */
import java.nio.file.Path

fun org.elasticsearch.env.Environment.configFile(): Path {
  return this.configDir()
}
