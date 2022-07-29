/*
 * Copyright (c) 2022 Works Applications Co., Ltd.
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

package com.worksap.nlp.lucene.sudachi.aliases

abstract class TokenFilterFactory(args: Map<String, String>) :
    org.apache.lucene.analysis.TokenFilterFactory(args)

/** this type should be used in overrides as argument */
typealias ResourceLoaderArgument = org.apache.lucene.util.ResourceLoader

interface ResourceLoaderAware : org.apache.lucene.util.ResourceLoaderAware

/** This type should be inherited */
interface ResourceLoaderParent : ResourceLoaderArgument
