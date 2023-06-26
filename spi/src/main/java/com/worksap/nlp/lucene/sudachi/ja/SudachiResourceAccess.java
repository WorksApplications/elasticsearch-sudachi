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

package com.worksap.nlp.lucene.sudachi.ja;

/**
 * This is a marker interface which gives access to child plugin classpath from
 * SudachiAnalysisPlugin. If an ES plugin contains Sudachi plugins or
 * configurations in the classpath, it must provide an implementation of this
 * interface. The implementation must have a single public no-argument
 * constructor. <br>
 * The implementation must be registered using ServiceLoader functionality
 * within the ElasticSearch/OpenSearch plugin JAR.
 *
 * @see java.util.ServiceLoader
 */
public interface SudachiResourceAccess {
    /**
     * Implementation must provide the classloader which could load the Sudachi
     * plugin classes. Because of SecurityManager limitations, the implementation
     * code must reside in the subplugin jar and can not be default method here.
     * Usual implementation can be simple {@code getClass().getClassloader()}.
     * 
     * @return classloader which can be used to load Sudachi plugins
     */
    ClassLoader getClassloader();
}
