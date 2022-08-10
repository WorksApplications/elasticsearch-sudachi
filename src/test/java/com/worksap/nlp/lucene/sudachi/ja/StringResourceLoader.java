/*
 * Copyright (c) 2018-2022 Works Applications Co., Ltd.
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

import com.worksap.nlp.lucene.sudachi.aliases.ResourceLoaderParent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class StringResourceLoader implements ResourceLoaderParent {
    String text;

    public StringResourceLoader(String text) {
        this.text = text;
    }

    @Override
    public <T> Class<? extends T> findClass(String cname, Class<T> expectedType) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public <T> T newInstance(String cname, Class<T> expectedType) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public InputStream openResource(String resource) throws IOException {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }
}
