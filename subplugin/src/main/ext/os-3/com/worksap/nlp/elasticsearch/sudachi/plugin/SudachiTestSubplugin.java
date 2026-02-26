/*
 * Copyright (c) 2026 Works Applications Co., Ltd.
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

package com.worksap.nlp.elasticsearch.sudachi.plugin;

import org.opensearch.plugins.Plugin;

/**
 * Minimal sub-plugin used by integration tests.
 *
 * This is provided as an OS 3.0+ specific source so that the generated
 * subplugin zip contains the class referenced by plugin-descriptor.properties.
 */
public class SudachiTestSubplugin extends Plugin {
}
