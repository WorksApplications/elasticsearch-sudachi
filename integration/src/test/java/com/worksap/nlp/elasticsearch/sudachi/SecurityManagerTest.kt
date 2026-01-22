/*
 * Copyright (c) 2023-2026 Works Applications Co., Ltd.
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

package com.worksap.nlp.elasticsearch.sudachi

import kotlin.io.path.Path
import kotlin.io.path.exists
import org.junit.Assume
import org.junit.Test

class SecurityManagerTest : SudachiEnvTest() {
  @Test(expected = SecurityException::class)
  fun failsAsExpected() {
    // Skip this test on OpenSearch 3.0+ / Java 21+ which uses Java agent-based security
    // instead of Security Manager. getSecurityManager() returns null when no SM is installed.
    @Suppress("DEPRECATION")
    Assume.assumeTrue(
        "Security Manager is not installed (OpenSearch 3.0+ uses agent-based security)",
        System.getSecurityManager() != null)
    Path("settings.gradle").exists()
  }
}
