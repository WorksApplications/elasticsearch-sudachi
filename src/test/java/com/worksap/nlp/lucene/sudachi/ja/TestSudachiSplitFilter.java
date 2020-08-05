package com.worksap.nlp.lucene.sudachi.ja;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;

import com.worksap.nlp.sudachi.Tokenizer.SplitMode;

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestSudachiSplitFilter extends BaseTokenStreamTestCase {
    TokenStream tokenStream;

    @Rule
    public TemporaryFolder tempFolderForDictionary = new TemporaryFolder();

    public void setUp() throws Exception {
        super.setUp();
        tempFolderForDictionary.create();
        File tempFileForDictionary = tempFolderForDictionary
                .newFolder("sudachiDictionary");
        ResourceUtil.copy(tempFileForDictionary);

        String settings;
        try (InputStream is = this.getClass().getResourceAsStream("sudachi.json")) {
            settings = ResourceUtil.getSudachiSetting(is);
        }

        tokenStream = new SudachiTokenizer(true, SplitMode.C, tempFileForDictionary.getPath(), settings);
    }

    @Test
    public void testSearchMode() throws IOException {
        tokenStream = setUpTokenStream("search", "東京都に行った。");
        assertTokenStreamContents(tokenStream,
                                  new String[] { "東京都", "東京", "都", "に", "行っ", "た" },
                                  new int[] { 0, 0, 2, 3, 4, 6 },
                                  new int[] { 3, 2, 3, 4, 6, 7 },
                                  new int[] { 1, 0, 1, 1, 1, 1 },
                                  new int[] { 2, 1, 1, 1, 1, 1 },
                                  8);
    } 
    @Test
    public void testExtendedMode() throws IOException {
        tokenStream = setUpTokenStream("extended", "東京都に行った。");
        assertTokenStreamContents(tokenStream,
                                  new String[] { "東京都", "東京", "都", "に", "行っ", "た" },
                                  new int[] { 0, 0, 2, 3, 4, 6 },
                                  new int[] { 3, 2, 3, 4, 6, 7 },
                                  new int[] { 1, 0, 1, 1, 1, 1 },
                                  new int[] { 2, 1, 1, 1, 1, 1 },
                                  8);
    }

    @Test
    public void testWithPunctuationsBySearchMode() throws IOException {
        tokenStream = setUpTokenStream("search", "東京都に行った。東京都に行った。");
        assertTokenStreamContents(tokenStream,
                                  new String[] { "東京都", "東京", "都", "に", "行っ", "た", "東京都", "東京", "都", "に", "行っ", "た" },
                                  new int[] { 0, 0, 2, 3, 4, 6, 8, 8, 10, 11, 12, 14 },
                                  new int[] { 3, 2, 3, 4, 6, 7, 11, 10, 11, 12, 14, 15 },
                                  new int[] { 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1 },
                                  new int[] { 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
                                  16);
    }

    @Test
    public void testWithPunctuationsByExtendedMode() throws IOException {
        tokenStream = setUpTokenStream("extended", "東京都に行った。東京都に行った。");
        assertTokenStreamContents(tokenStream,
                                  new String[] { "東京都", "東京", "都", "に", "行っ", "た", "東京都", "東京", "都", "に", "行っ", "た" },
                                  new int[] { 0, 0, 2, 3, 4, 6, 8, 8, 10, 11, 12, 14 },
                                  new int[] { 3, 2, 3, 4, 6, 7, 11, 10, 11, 12, 14, 15 },
                                  new int[] { 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1 },
                                  new int[] { 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
                                  16);
    }

    @Test
    public void testWithOOVBySearchMode() throws IOException {
        tokenStream = setUpTokenStream("search", "アマゾンに行った。");
        assertTokenStreamContents(tokenStream,
                                  new String[] { "アマゾン", "に", "行っ", "た" },
                                  new int[] { 0, 4, 5, 7 },
                                  new int[] { 4, 5, 7, 8 },
                                  new int[] { 1, 1, 1, 1 },
                                  new int[] { 1, 1, 1, 1 },
                                  9);
    }

    @Test
    public void testWithOOVByExtendedMode() throws IOException {
        tokenStream = setUpTokenStream("extended", "アマゾンに行った。");
        assertTokenStreamContents(tokenStream,
                                  new String[] { "アマゾン", "ア", "マ", "ゾ", "ン", "に", "行っ", "た" },
                                  new int[] { 0, 0, 1, 2, 3, 4, 5, 7 },
                                  new int[] { 4, 1, 2, 3, 4, 5, 7, 8 },
                                  new int[] { 1, 0, 1, 1, 1, 1, 1, 1 },
                                  new int[] { 4, 1, 1, 1, 1, 1, 1, 1 },
                                  9);
    }

    @Test
    public void testWithSingleCharOOVBySearchMode() throws IOException {
        tokenStream = setUpTokenStream("search", "あ");
        assertTokenStreamContents(tokenStream,
                                  new String[] { "あ" },
                                  new int[] { 0 },
                                  new int[] { 1 },
                                  new int[] { 1 },
                                  new int[] { 1 },
                                  1);
    }

    @Test
    public void testWithSingleCharOOVByExtendedMode() throws IOException {
        tokenStream = setUpTokenStream("extended", "あ");
        assertTokenStreamContents(tokenStream,
                                  new String[] { "あ" },
                                  new int[] { 0 },
                                  new int[] { 1 },
                                  new int[] { 1 },
                                  new int[] { 1 },
                                  1);
    }

    @Test
    public void testWithOOVSequenceBySearchMode() throws IOException {
        tokenStream = setUpTokenStream("search", "アマゾンにワニ");
        assertTokenStreamContents(tokenStream,
                                  new String[] { "アマゾン", "に", "ワニ" },
                                  new int[] { 0, 4, 5 },
                                  new int[] { 4, 5, 7 },
                                  new int[] { 1, 1, 1 },
                                  new int[] { 1, 1, 1 },
                                  7);
    }

    @Test
    public void testWithOOVSequenceByExtendedMode() throws IOException {
        tokenStream = setUpTokenStream("extended", "アマゾンにワニ");
        assertTokenStreamContents(tokenStream,
                                  new String[] { "アマゾン", "ア", "マ", "ゾ", "ン", "に", "ワニ", "ワ", "ニ" },
                                  new int[] { 0, 0, 1, 2, 3, 4, 5, 5, 6 },
                                  new int[] { 4, 1, 2, 3, 4, 5, 7, 6, 7 },
                                  new int[] { 1, 0, 1, 1, 1, 1, 1, 0, 1 },
                                  new int[] { 4, 1, 1, 1, 1, 1, 2, 1, 1 },
                                  7);
    }

    TokenStream setUpTokenStream(String mode, String input) {
        SudachiSplitFilterFactory factory = new SudachiSplitFilterFactory(new HashMap<String, String>() {{ put("mode", mode); }});
        ((Tokenizer)tokenStream).setReader(new StringReader(input));
        return factory.create(tokenStream);
    }
}