/*
 *  Copyright (c) 2019 Works Applications Co., Ltd.
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

package com.worksap.nlp.lucene.sudachi.ja.util;

import junit.framework.TestCase;

public class TestRomanizer extends TestCase {
    public void testGetRomanizationWithSmallTsu() {
        String input = "ッカッキックッケッコッサッシッスッセッソッタッチッツッテットッハッヒッフッヘッホッマッミッムッメッモッヤッユッヨッワッガッギッグッゲッゴッザッジッズッゼッゾッダッヂッヅッデッドッバッビッブッベッボッパッピップッペッポッヴッナ";
        String expected = "kkakkikkukkekkossassissussessottattittuttettohhahhihhuhhehhommammimmummemmoyyayyuyyowwaggaggigguggeggozzazzizzuzzezzoddaddidduddeddobbabbibbubbebboppappippuppeppovvultuna";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithA() {
        String input = "アイウウァウィウェウォ";
        String expected = "aiuwhawhiwhewho";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithKa() {
        String input = "カキキャキィキュキェキョククァクィクゥクェクォケコ";
        String expected = "kakikyakyikyukyekyokuqwaqwiqwuqweqwokeko";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithSa() {
        String input = "サシシャシィシュシェショススァスィスゥスェスォセソ";
        String expected = "sasisyasyisyusyesyosuswaswiswusweswoseso";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithTa() {
        String input = "タチチャチィチュチェチョツツァツィツゥツェツォテテャティテュテェテョトトァトィトゥトェトォ";
        String expected = "tatityatyityutyetyotutsatsitulutsetsotethathithuthethototwatwitwutwetwo";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithNa() {
        String input = "ナニニャニィニュニェニョヌネノ";
        String expected = "naninyanyinyunyenyonuneno";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithHa() {
        String input = "ハヒヒャヒィヒュヒェヒョフファフィフゥフェフォフャフュフョヘホ";
        String expected = "hahihyahyihyuhyehyohufwafwifwufwefwofyafyufyoheho";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithMa() {
        String input = "マミミャミィミュミェミョムメモ";
        String expected = "mamimyamyimyumyemyomumemo";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithYa() {
        String input = "ヤユヨ";
        String expected = "yayuyo";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithRa() {
        String input = "ラリリャリィリュリェリョルレロ";
        String expected = "rariryaryiryuryeryorurero";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithWa() {
        String input = "ワヰヱヲ";
        String expected = "wawiwewo";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithN() {
        String input = "ンンア";
        String expected = "nnna";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithGa() {
        String input = "ガギギャギィギュギェギョググァグィグゥグェグォゲゴ";
        String expected = "gagigyagyigyugyegyogugwagwigwugwegwogego";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithZa() {
        String input = "ザジジャジィジュジェジョズゼゾ";
        String expected = "zazizyazyizyuzyezyozuzezo";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithDa() {
        String input = "ダヂヂャヂィヂュヂェヂョヅデデャディデュデェデョドドァドィドゥドェドォ";
        String expected = "dadidyadyidyudyedyodudedhadhidhudhedhododwadwidwudwedwo";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithBa() {
        String input = "バビビャビィビュビェビョブベボ";
        String expected = "babibyabyibyubyebyobubebo";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithPa() {
        String input = "パピピャピィピュピェピョプペポ";
        String expected = "papipyapyipyupyepyopupepo";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithVa() {
        String input = "ヴァヴィヴヴェヴォヴャヴュヴョ";
        String expected = "vavivuvevovyavyuvyo";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithSmall() {
        String input = "ァィゥェォヵヶャュョヮ";
        String expected = "lalilulelolkalkelyalyulyolwa";

        assertEquals(expected, Romanizer.getRomanization(input));
    }

    public void testGetRomanizationWithSymbols() {
        assertEquals("-", Romanizer.getRomanization("・＝ー"));
    }

    public void testGetRomanizationWithoutJapanese() {
        assertEquals("", Romanizer.getRomanization(""));
        assertEquals("abc-", Romanizer.getRomanization("abc-"));
    }
}
