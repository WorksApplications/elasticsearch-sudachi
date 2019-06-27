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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.apache.lucene.analysis.BaseTokenStreamTestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

// Test of character segmentation using incrementToken(tokenizer)
public class TestSudachiTokenizer extends BaseTokenStreamTestCase {
    private SudachiTokenizer tokenizer;
    private SudachiTokenizer tokenizerExtended;
    private SudachiTokenizer tokenizerNormal;
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

        tokenizer = new SudachiTokenizer(true, SudachiTokenizer.Mode.SEARCH,
                tempFileForDictionary.getPath(), settings);
        tokenizerExtended = new SudachiTokenizer(true,
                SudachiTokenizer.Mode.EXTENDED, tempFileForDictionary.getPath(), settings);
        tokenizerNormal = new SudachiTokenizer(true,
                SudachiTokenizer.Mode.NORMAL, tempFileForDictionary.getPath(), settings);
        tokenizerPunctuation = new SudachiTokenizer(false,
                SudachiTokenizer.Mode.SEARCH, tempFileForDictionary.getPath(), settings);
    }

    @Test
    public void incrementTokenWithShiftJis() throws IOException {
        String str = new String("東京都に行った。".getBytes("Shift_JIS"), "Shift_JIS");
        tokenizer.setReader(new StringReader(str));
        assertTokenStreamContents(tokenizer,
                                  new String[] { "東京都", "東京", "都", "に", "行っ", "た" },
                                  new int[] { 0, 0, 2, 3, 4, 6 },
                                  new int[] { 3, 2, 3, 4, 6, 7 },
                                  new int[] { 1, 0, 1, 1, 1, 1 },
                                  new int[] { 2, 1, 1, 1, 1, 1 },
                                  8);
    }

    @Test
    public void incrementTokenByDefaultMode() throws IOException {
        tokenizer.setReader(new StringReader("東京都に行った。"));
        assertTokenStreamContents(tokenizer,
                                  new String[] { "東京都", "東京", "都", "に", "行っ", "た" },
                                  new int[] { 0, 0, 2, 3, 4, 6 },
                                  new int[] { 3, 2, 3, 4, 6, 7 },
                                  new int[] { 1, 0, 1, 1, 1, 1 },
                                  new int[] { 2, 1, 1, 1, 1, 1 },
                                  8);
    }

    @Test
    public void incrementTokenByExtendedMode() throws IOException {
        tokenizerExtended.setReader(new StringReader("東京都に行った。"));
        assertTokenStreamContents(tokenizerExtended,
                                  new String[] { "東京都", "東京", "都", "に", "行っ", "た" },
                                  new int[] { 0, 0, 2, 3, 4, 6 },
                                  new int[] { 3, 2, 3, 4, 6, 7 },
                                  new int[] { 1, 0, 1, 1, 1, 1 },
                                  new int[] { 2, 1, 1, 1, 1, 1 },
                                  8);
    }

    @Test
    public void incrementTokenByNormalMode() throws IOException {
        tokenizerNormal.setReader(new StringReader("東京都に行った。"));
        assertTokenStreamContents(tokenizerNormal,
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
                                  new String[] { "東京都", "東京", "都", "に", "行っ", "た", "。" },
                                  new int[] { 0, 0, 2, 3, 4, 6, 7 },
                                  new int[] { 3, 2, 3, 4, 6, 7, 8 },
                                  new int[] { 1, 0, 1, 1, 1, 1, 1 },
                                  new int[] { 2, 1, 1, 1, 1, 1, 1 },
                                  8);
    }

    @Test
    public void incrementTokenWithPunctuationsByDefaultMode() throws IOException {
        tokenizer.setReader(new StringReader("東京都に行った。東京都に行った。"));
        assertTokenStreamContents(tokenizer,
                                  new String[] { "東京都", "東京", "都", "に", "行っ", "た", "東京都", "東京", "都", "に", "行っ", "た" },
                                  new int[] { 0, 0, 2, 3, 4, 6, 8, 8, 10, 11, 12, 14 },
                                  new int[] { 3, 2, 3, 4, 6, 7, 11, 10, 11, 12, 14, 15 },
                                  new int[] { 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1 },
                                  new int[] { 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
                                  16);
    }

    @Test
    public void incrementTokenWithPunctuationsByExtendedMode() throws IOException {
        tokenizerExtended.setReader(new StringReader("東京都に行った。東京都に行った。"));
        assertTokenStreamContents(tokenizerExtended,
                                  new String[] { "東京都", "東京", "都", "に", "行っ", "た", "東京都", "東京", "都", "に", "行っ", "た" },
                                  new int[] { 0, 0, 2, 3, 4, 6, 8, 8, 10, 11, 12, 14 },
                                  new int[] { 3, 2, 3, 4, 6, 7, 11, 10, 11, 12, 14, 15 },
                                  new int[] { 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1 },
                                  new int[] { 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1 },
                                  16);
    }

    @Test
    public void incrementTokenWithPunctuationsByNormalMode() throws IOException {
        tokenizerNormal.setReader(new StringReader("東京都に行った。東京都に行った。"));
        assertTokenStreamContents(tokenizerNormal,
                                  new String[] { "東京都", "に", "行っ", "た", "東京都", "に", "行っ", "た" },
                                  new int[] { 0, 3, 4, 6, 8, 11, 12, 14 },
                                  new int[] { 3, 4, 6, 7, 11, 12, 14, 15 },
                                  new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
                                  new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
                                  16);
    }

    @Test
    public void incrementTokenWithPunctuationsByPunctuationMode() throws IOException {
        tokenizerPunctuation.setReader(new StringReader("東京都に行った。東京都に行った。"));
        assertTokenStreamContents(tokenizerPunctuation,
                                  new String[] { "東京都", "東京", "都", "に", "行っ", "た", "。", "東京都", "東京", "都", "に", "行っ", "た", "。" },
                                  new int[] { 0, 0, 2, 3, 4, 6, 7, 8, 8, 10, 11, 12, 14, 15 },
                                  new int[] { 3, 2, 3, 4, 6, 7, 8, 11, 10, 11, 12, 14, 15, 16 },
                                  new int[] { 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1 },
                                  new int[] { 2, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1 },
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
    public void incrementTokenWithOOVByExtendedMode() throws IOException {
        tokenizerExtended.setReader(new StringReader("アマゾンに行った。"));
        assertTokenStreamContents(tokenizerExtended,
                                  new String[] { "アマゾン", "ア", "マ", "ゾ", "ン", "に", "行っ", "た" },
                                  new int[] { 0, 0, 1, 2, 3, 4, 5, 7 },
                                  new int[] { 4, 1, 2, 3, 4, 5, 7, 8 },
                                  new int[] { 1, 0, 1, 1, 1, 1, 1, 1 },
                                  new int[] { 4, 1, 1, 1, 1, 1, 1, 1 },
                                  9);
    }

    @Test
    public void incrementTokenWithOOVByNormalMode() throws IOException {
        tokenizerNormal.setReader(new StringReader("アマゾンに行った。"));
        assertTokenStreamContents(tokenizerNormal,
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
    public void testReadSentences() throws IOException {
        tokenizer.setReader(new StringReader("東京都に行った。"));
        tokenizer.reset();
        assertThat(tokenizer.readSentences(), is("東京都に行った。"));

        tokenizerExtended.setReader(new StringReader("東京都に行った。"));
        tokenizerExtended.reset();
        assertThat(tokenizerExtended.readSentences(), is("東京都に行った。"));
    }

    @Test
    public void testReadSentencesWithTwoSentences() throws IOException {
        tokenizer.setReader(new StringReader("東京都に行った。東京都に行った。"));
        tokenizer.reset();
        assertThat(tokenizer.readSentences(), is("東京都に行った。東京都に行った。"));
    }

    @Test
    public void testReadSentencesWithoutLastPunctuation() throws IOException {
        tokenizer.setReader(new StringReader("東京都に行った。東京都に行った"));
        tokenizer.reset();
        String[] answerList = { "東京都に行った。", "東京都に行った" };
        for (int i = 0; i < answerList.length; i++) {
            assertThat(tokenizer.readSentences(), is(answerList[i]));
        }
    }

    @Test
    public void testReadSentencesWithTwoPunctuations() throws IOException {
        tokenizer.setReader(new StringReader("東京都に行った。。東京都に行った"));
        tokenizer.reset();
        String[] answerList = { "東京都に行った。。", "東京都に行った" };
        for (int i = 0; i < answerList.length; i++) {
            assertThat(tokenizer.readSentences(), is(answerList[i]));
        }
    }

    @Test
    public void testReadSentencesWithIdeographicComma() throws IOException {
        tokenizer.setReader(new StringReader("東京都に行った、東京都に行った"));
        tokenizer.reset();
        String[] answerList = { "東京都に行った、", "東京都に行った" };
        for (int i = 0; i < answerList.length; i++) {
            assertThat(tokenizer.readSentences(), is(answerList[i]));
        }
    }

    @Test
    public void testReadSentencesWithPeriod() throws IOException {
        tokenizer.setReader(new StringReader("東京都に行った。。東京都に行った."));
        tokenizer.reset();
        assertThat(tokenizer.readSentences(), is("東京都に行った。。東京都に行った."));
    }

    @Test
    public void testReadSentencesWithComma() throws IOException {
        tokenizer.setReader(new StringReader("東京都に行った,東京都に行った"));
        tokenizer.reset();
        String[] answerList = { "東京都に行った,", "東京都に行った" };
        for (int i = 0; i < answerList.length; i++) {
            assertThat(tokenizer.readSentences(), is(answerList[i]));
        }
    }

    @Test
    public void testReadSentencesWithLongSentence() throws IOException {
        tokenizer.setReader(new StringReader("岩波文庫は平福百穂画伯の装幀をもって昭和二年刊行された。"
                + "これを発表した時の影響の絶大なりしことは実に驚いた。" + "讃美、激励、希望等の書信が数千通に達した。"
                + "「私の教養の一切を岩波文庫に托する」などという感激の文字もあった。"
                + "私はよい仕事だ、高貴な永遠の事業だ、達成すべき企てだ、"
                + "後には必ず成就する仕事だと考えたが、かくまで速かに、かくまで盛んに歓迎されるとは思わなかった。"
                + "Du kannst, denn du sollst. は私の絶愛の句であるが、誠心誠意、"
                + "読書子のために計る仕事は必ず酬いられるものであるとの確信を得た。"
                + "私は本屋になった甲斐のあったことを初めて知り、責任のますます重きを痛感した。　"
                + "岩波文庫は古今東西の古典の普及を使命とする。古典の尊重すべきは言うまでもない。"
                + "その普及の程度は直ちに文化の水準を示すものである。したがって文庫出版については敬虔なる態度を持し、"
                + "古典に対する尊敬と愛とを失ってはならない。私は及ばずながらもこの理想を実現しようと心がけ、"
                + "一般単行本に対するよりも、さらに厳粛なる態度をもって文庫の出版に臨んだ。"
                + "文庫の編入すべき典籍の厳選はもちろん、編集、校訂、翻訳等、"
                + "その道の権威者を煩わして最善をつくすことに人知れぬ苦心をしたのである。"
                + "従来行なわれているものをそのまま編入すれば便宜なる時も、"
                + "よりよい原稿を得るために新たに最適任者に懇請して数年を費やしてようやく刊行するに至った例も少なくない。"
                + "しかるにこの態度は今もなお一般に理解されず、"
                + "往々にして著者から謙遜のつもりで文庫にでも入れてもらいたいなど出版を申し込まれる場合がある。"
                + "私は単行本には引き受けられても文庫には引き受けぬといって拒絶するほど、文庫を尊重愛護するのである。　"
                + "典籍の範囲をいずれに限定すべきか、従来既刊の岩波文庫を見るに必ずしも標準が一定しなかったうらみがあるが、"
                + "今後の方針としては古典的価値の水準をますます高めるとともに、厳選方針を強化することにした。"
                + "経済的価値高くとも本質的価値乏しいものはこれを編入しないことに特に意を用いるとともに、"
                + "経済的価値低くとも古典的価値の豊かなるものはつとめて編入し、この点において岩波文庫本来の特色を発揮しようと思っている。"
                + "往年外遊の際、レクラム会社を見学してその事業的規模の大なるには驚いたが、"
                + "編集態度においては岩波文庫が、その先蹤たるレクラム文庫を恥ずかしむるものでないと、ひそかに慰むるところがあった。"));
        tokenizer.reset();
        String[] answerList = {
                "岩波文庫は平福百穂画伯の装幀をもって昭和二年刊行された。"
                        + "これを発表した時の影響の絶大なりしことは実に驚いた。"
                        + "讃美、激励、希望等の書信が数千通に達した。"
                        + "「私の教養の一切を岩波文庫に托する」などという感激の文字もあった。"
                        + "私はよい仕事だ、高貴な永遠の事業だ、達成すべき企てだ、後には必ず成就する仕事だと考えたが、"
                        + "かくまで速かに、かくまで盛んに歓迎されるとは思わなかった。Du kannst, denn du sollst. は私の絶愛の句であるが、"
                        + "誠心誠意、読書子のために計る仕事は必ず酬いられるものであるとの確信を得た。"
                        + "私は本屋になった甲斐のあったことを初めて知り、責任のますます重きを痛感した。　"
                        + "岩波文庫は古今東西の古典の普及を使命とする。古典の尊重すべきは言うまでもない。"
                        + "その普及の程度は直ちに文化の水準を示すものである。したがって文庫出版については敬虔なる態度を持し、"
                        + "古典に対する尊敬と愛とを失ってはならない。私は及ばずながらもこの理想を実現しようと心がけ、一般単行本に対するよりも、"
                        + "さらに厳粛なる態度をもって文庫の出版に臨んだ。文庫の編入すべき典籍の厳選はもちろん、編集、校訂、翻訳等、",
                "その道の権威者を煩わして最善をつくすことに人知れぬ苦心をしたのである。従来行なわれているものをそのまま編入すれば便宜なる時も、"
                        + "よりよい原稿を得るために新たに最適任者に懇請して数年を費やしてようやく刊行するに至った例も少なくない。"
                        + "しかるにこの態度は今もなお一般に理解されず、"
                        + "往々にして著者から謙遜のつもりで文庫にでも入れてもらいたいなど出版を申し込まれる場合がある。"
                        + "私は単行本には引き受けられても文庫には引き受けぬといって拒絶するほど、文庫を尊重愛護するのである。　"
                        + "典籍の範囲をいずれに限定すべきか、従来既刊の岩波文庫を見るに必ずしも標準が一定しなかったうらみがあるが、"
                        + "今後の方針としては古典的価値の水準をますます高めるとともに、厳選方針を強化することにした。"
                        + "経済的価値高くとも本質的価値乏しいものはこれを編入しないことに特に意を用いるとともに、"
                        + "経済的価値低くとも古典的価値の豊かなるものはつとめて編入し、"
                        + "この点において岩波文庫本来の特色を発揮しようと思っている。"
                        + "往年外遊の際、レクラム会社を見学してその事業的規模の大なるには驚いたが、編集態度においては岩波文庫が、"
                        + "その先蹤たるレクラム文庫を恥ずかしむるものでないと、", "ひそかに慰むるところがあった。" };
        for (int i = 0; i < answerList.length; i++) {
            assertThat(tokenizer.readSentences(), is(answerList[i]));
        }
    }

}
