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

typealias SearchEngineTestCase = org.elasticsearch.test.ESTestCase

typealias AnalysisRegistryAlias = org.elasticsearch.index.analysis.AnalysisRegistry

typealias EnvironmentAlias = org.elasticsearch.env.Environment

typealias SettingsAlias = org.elasticsearch.common.settings.Settings

typealias AnalysisPluginAlias = org.elasticsearch.plugins.AnalysisPlugin

typealias AnalyzeActionRequestAlias =
    org.elasticsearch.action.admin.indices.analyze.AnalyzeAction.Request

typealias TransportAnalyzeActionAlias =
    org.elasticsearch.action.admin.indices.analyze.TransportAnalyzeAction
