/*
 *  Copyright (c) 2017 Works Applications Co., Ltd.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.charfilter.NormalizeCharMap;

import com.worksap.nlp.sudachi.Tokenizer.SplitMode;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

// Test of character segmentation using incrementToken(tokenizer)
public class TestSudachiTokenizer extends BaseTokenStreamTestCase {
    private SudachiTokenizer tokenizer;
    private SudachiTokenizer tokenizerA;
    private SudachiTokenizer tokenizerB;
    private SudachiTokenizer tokenizerPunctuation;

    @Rule
    public TemporaryFolder tempFolderForDictionary = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        tempFolderForDictionary.create();
        File tempFileForDictionary = tempFolderForDictionary
                .newFolder("sudachiDictionary");
        ResourceUtil.copy(tempFileForDictionary);

        String settings;
        try(InputStream is = this.getClass()
                .getResourceAsStream("sudachi.json");){
            settings = ResourceUtil.getSudachiSetting(is);
        }

        tokenizer = new SudachiTokenizer(true, SplitMode.C,
                tempFileForDictionary.getPath(), settings);
        tokenizerA = new SudachiTokenizer(true, SplitMode.A,
                tempFileForDictionary.getPath(), settings);
        tokenizerB = new SudachiTokenizer(true, SplitMode.B,
                tempFileForDictionary.getPath(), settings);
        tokenizerPunctuation = new SudachiTokenizer(false,
                SplitMode.C, tempFileForDictionary.getPath(), settings);
    }

    @Test
    public void incrementTokenWithShiftJis() throws IOException {
        String str = new String("東京都に行った。".getBytes("Shift_JIS"), "Shift_JIS");
        tokenizer.setReader(new StringReader(str));
        assertTokenStreamContents(tokenizer,
                                  new String[] { "東京都", "に", "行っ", "た" },
                                  new int[] { 0, 3, 4, 6 },
                                  new int[] { 3, 4, 6, 7 },
                                  new int[] { 1, 1, 1, 1 },
                                  new int[] { 1, 1, 1, 1 },
                                  8);
    }

    @Test
    public void incrementTokenByDefaultMode() throws IOException {
        tokenizer.setReader(new StringReader("東京都に行った。"));
        assertTokenStreamContents(tokenizer,
                                  new String[] { "東京都", "に", "行っ", "た" },
                                  new int[] { 0, 3, 4, 6 },
                                  new int[] { 3, 4, 6, 7 },
                                  new int[] { 1, 1, 1, 1 },
                                  new int[] { 1, 1, 1, 1 },
                                  8);
    }

    @Test
    public void incrementTokenByPunctuationMode() throws IOException {
        tokenizerPunctuation.setReader(new StringReader("東京都に行った。"));
        assertTokenStreamContents(tokenizerPunctuation,
                                  new String[] { "東京都", "に", "行っ", "た", "。" },
                                  new int[] { 0, 3, 4, 6, 7 },
                                  new int[] { 3, 4, 6, 7, 8 },
                                  new int[] { 1, 1, 1, 1, 1 },
                                  new int[] { 1, 1, 1, 1, 1 },
                                  8);
    }

    @Test
    public void incrementTokenWithPunctuationsByDefaultMode() throws IOException {
        tokenizer.setReader(new StringReader("東京都に行った。東京都に行った。"));
        assertTokenStreamContents(tokenizer,
                                  new String[] { "東京都", "に", "行っ", "た", "東京都", "に", "行っ", "た" },
                                  new int[] { 0, 3, 4, 6, 8, 11, 12, 14 },
                                  new int[] { 3, 4, 6, 7, 11, 12, 14, 15 },
                                  new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                                  new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                                  16);
    }

    @Test
    public void incrementTokenWithPunctuationsByPunctuationMode() throws IOException {
        tokenizerPunctuation.setReader(new StringReader("東京都に行った。東京都に行った。"));
        assertTokenStreamContents(tokenizerPunctuation,
                                  new String[] { "東京都", "に", "行っ", "た", "。", "東京都", "に", "行っ", "た", "。" },
                                  new int[] { 0, 3, 4, 6, 7, 8, 11, 12, 14, 15 },
                                  new int[] { 3, 4, 6, 7, 8, 11, 12, 14, 15, 16 },
                                  new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                                  new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                                  16);
    }

    @Test
    public void incrementTokenWithOOVByDefaultMode() throws IOException {
        tokenizer.setReader(new StringReader("アマゾンに行った。"));
        assertTokenStreamContents(tokenizer,
                                  new String[] { "アマゾン", "に", "行っ", "た" },
                                  new int[] { 0, 4, 5, 7 },
                                  new int[] { 4, 5, 7, 8 },
                                  new int[] { 1, 1, 1, 1 },
                                  new int[] { 1, 1, 1, 1 },
                                  9);
    }

    @Test
    public void incrementTokenWithOOVByPunctuationMode() throws IOException {
        tokenizerPunctuation.setReader(new StringReader("アマゾンに行った。"));
        assertTokenStreamContents(tokenizerPunctuation,
                                  new String[] { "アマゾン", "に", "行っ", "た", "。" },
                                  new int[] { 0, 4, 5, 7, 8 },
                                  new int[] { 4, 5, 7, 8, 9 },
                                  new int[] { 1, 1, 1, 1, 1 },
                                  new int[] { 1, 1, 1, 1, 1 },
                                  9);
    }

    @Test
    public void incrementTokenByAMode() throws IOException {
        tokenizerA.setReader(new StringReader("東京都に行った。"));
        assertTokenStreamContents(tokenizerA,
                                  new String[] { "東京", "都", "に", "行っ", "た" },
                                  new int[] { 0, 2, 3, 4, 6 },
                                  new int[] { 2, 3, 4, 6, 7 },
                                  new int[] { 1, 1, 1, 1, 1 },
                                  new int[] { 1, 1, 1, 1, 1 },
                                  8);
    }

    @Test
    public void incrementTokenByBMode() throws IOException {
        tokenizerB.setReader(new StringReader("東京都に行った。"));
        assertTokenStreamContents(tokenizerB,
                                  new String[] { "東京都", "に", "行っ", "た" },
                                  new int[] { 0, 3, 4, 6 },
                                  new int[] { 3, 4, 6, 7 },
                                  new int[] { 1, 1, 1, 1 },
                                  new int[] { 1, 1, 1, 1 },
                                  8);
    }

    @Test
    public void incrementTokenWithCorrectOffset() throws IOException {
        NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
        builder.add("東京都", "京都");
        MappingCharFilter filter = new MappingCharFilter(builder.build(), new StringReader("東京都に行った。"));
        tokenizer.setReader(filter);
        assertTokenStreamContents(tokenizer,
                                  new String[] { "京都", "に", "行っ", "た" },
                                  new int[] { 0, 3, 4, 6 },
                                  new int[] { 3, 4, 6, 7 },
                                  new int[] { 1, 1, 1, 1 },
                                  new int[] { 1, 1, 1, 1 },
                                  8);
    }
}
