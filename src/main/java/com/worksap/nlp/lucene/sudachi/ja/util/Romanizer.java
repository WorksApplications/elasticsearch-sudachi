/*
 *  Copyright (c) 2018 Works Applications Co., Ltd.
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


import java.io.IOException;

public class Romanizer {

    /*
     * Romanize katakana
     */
    public static String getRomanization(String s) {
        StringBuilder output = new StringBuilder();
        try {
            getRomanization(output, s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output.toString();
    }
  

    /*
     * Romanize katakana with MS-IME format
     */
    public static void getRomanization(Appendable builder, CharSequence s) throws IOException {
        final int len = s.length();
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            char ch2 = (i < len - 1) ? s.charAt(i + 1) : 0;
      
            main: switch (ch) {
            case 'ッ':
                switch (ch2) {
                case 'カ':
                case 'キ':
                case 'ク':
                case 'ケ':
                case 'コ':
                    builder.append('k');
                    break main;
                case 'サ':
                case 'シ':
                case 'ス':
                case 'セ':
                case 'ソ':
                    builder.append('s');
                    break main;
                case 'タ':
                case 'チ':
                case 'ツ':
                case 'テ':
                case 'ト':
                    builder.append('t');
                    break main;
                case 'ハ':
                case 'ヒ':
                case 'フ':
                case 'ヘ':
                case 'ホ':
                    builder.append('h');
                    break main;
                case 'マ':
                case 'ミ':
                case 'ム':
                case 'メ':
                case 'モ':
                    builder.append('m');
                    break main;
                case 'ヤ':
                case 'ユ':
                case 'ヨ':
                    builder.append('y');
                    break main;
                case 'ワ':
                    builder.append('w');
                    break main;
                case 'ガ':
                case 'ギ':
                case 'グ':
                case 'ゲ':
                case 'ゴ':
                    builder.append('g');
                    break main;
                case 'ザ':
                case 'ジ':
                case 'ズ':
                case 'ゼ':
                case 'ゾ':
                    builder.append('z');
                    break main;
                case 'ダ':
                case 'ヂ':
                case 'ヅ':
                case 'デ':
                case 'ド':
                    builder.append('d');
                    break main;
                case 'バ':
                case 'ビ':
                case 'ブ':
                case 'ベ':
                case 'ボ':
                    builder.append('b');
                    break main;
                case 'パ':
                case 'ピ':
                case 'プ':
                case 'ペ':
                case 'ポ':
                    builder.append('p');
                    break main;
                case 'ヴ':
                    builder.append('v');
                    break main;
                default:
                    builder.append("ltu");
                }
                break;
            case 'ア':
                builder.append('a');
                break;
            case 'イ':
                builder.append('i');
                break;
            case 'ウ':
                switch(ch2) {
                case 'ァ':
                    builder.append("wha");
                    i++;
                    break;
                case 'ィ':
                    builder.append("whi");
                    i++;
                    break;
                case 'ェ':
                    builder.append("whe");
                    i++;
                    break;
                case 'ォ':
                    builder.append("who");
                    i++;
                    break;
                default:
                    builder.append('u');
                    break;
                }
                break;
            case 'エ':
                builder.append('e');
                break;
            case 'オ':
                builder.append('o');
                break;
            case 'カ':
                builder.append("ka");
                break;
            case 'キ':
                switch(ch2) {
                case 'ャ':
                    builder.append("kya");
                    i++;
                    break;
                case 'ィ':
                    builder.append("kyi");
                    i++;
                    break;
                case 'ュ':
                    builder.append("kyu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("kye");
                    i++;
                    break;
                case 'ョ':
                    builder.append("kyo");
                    i++;
                    break;
                default:
                    builder.append("ki");
                    break;
                }
                break;
            case 'ク':
                switch(ch2) {
                case 'ァ':
                    builder.append("qwa");
                    i++;
                    break;
                case 'ィ':
                    builder.append("qwi");
                    i++;
                    break;
                case 'ゥ':
                    builder.append("qwu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("qwe");
                    i++;
                    break;
                case 'ォ':
                    builder.append("qwo");
                    i++;
                    break;
                default:
                    builder.append("ku");
                    break;
                }
                break;
            case 'ケ':
                builder.append("ke");
                break;
            case 'コ':
                builder.append("ko");
                break;
            case 'サ':
                builder.append("sa");
                break;
            case 'シ':
                switch(ch2) {
                case 'ャ':
                    builder.append("sya");
                    i++;
                    break;
                case 'ィ':
                    builder.append("syi");
                    i++;
                    break;
                case 'ュ':
                    builder.append("syu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("sye");
                    i++;
                    break;
                case 'ョ':
                    builder.append("syo");
                    i++;
                    break;
                default:
                    builder.append("si");
                    break;
                }
                break;
            case 'ス':
                switch(ch2) {
                case 'ァ':
                    builder.append("swa");
                    i++;
                    break;
                case 'ィ':
                    builder.append("swi");
                    i++;
                    break;
                case 'ゥ':
                    builder.append("swu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("swe");
                    i++;
                    break;
                case 'ォ':
                    builder.append("swo");
                    i++;
                    break;
                default:
                    builder.append("su");
                    break;
                }
                break;
            case 'セ':
                builder.append("se");
                break;
            case 'ソ':
                builder.append("so");
                break;
            case 'タ':
                builder.append("ta");
                break;
            case 'チ':
                switch(ch2) {
                case 'ャ':
                    builder.append("tya");
                    i++;
                    break;
                case 'ィ':
                    builder.append("tyi");
                    i++;
                    break;
                case 'ュ':
                    builder.append("tyu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("tye");
                    i++;
                    break;
                case 'ョ':
                    builder.append("tyo");
                    i++;
                    break;
                default:
                    builder.append("ti");
                    break;
                }
                break;
            case 'ツ':
                switch(ch2) {
                case 'ァ':
                    builder.append("tsa");
                    i++;
                    break;
                case 'ィ':
                    builder.append("tsi");
                    i++;
                    break;
                case 'ェ':
                    builder.append("tse");
                    i++;
                    break;
                case 'ォ':
                    builder.append("tso");
                    i++;
                    break;
                default:
                    builder.append("tu");
                    break;
                }
                break;
            case 'テ':
                switch(ch2) {
                case 'ャ':
                    builder.append("tha");
                    i++;
                    break;
                case 'ィ':
                    builder.append("thi");
                    i++;
                    break;
                case 'ュ':
                    builder.append("thu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("the");
                    i++;
                    break;
                case 'ョ':
                    builder.append("tho");
                    i++;
                    break;
                default:
                    builder.append("te");
                    break;
                }
                break;
            case 'ト':
                switch(ch2) {
                case 'ァ':
                    builder.append("twa");
                    i++;
                    break;
                case 'ィ':
                    builder.append("twi");
                    i++;
                    break;
                case 'ゥ':
                    builder.append("twu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("twe");
                    i++;
                    break;
                case 'ォ':
                    builder.append("two");
                    i++;
                    break;
                default:
                    builder.append("to");
                    break;
                }
                break;
            case 'ナ':
                builder.append("na");
                break;
            case 'ニ':
                switch(ch2) {
                case 'ャ':
                    builder.append("nya");
                    i++;
                    break;
                case 'ィ':
                    builder.append("nyi");
                    i++;
                    break;
                case 'ュ':
                    builder.append("nyu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("nye");
                    i++;
                    break;
                case 'ョ':
                    builder.append("nyo");
                    i++;
                    break;
                default:
                    builder.append("ni");
                    break;
                }
                break;
            case 'ヌ':
                builder.append("nu");
                break;
            case 'ネ':
                builder.append("ne");
                break;
            case 'ノ':
                builder.append("no");
                break;
            case 'ハ':
                builder.append("ha");
                break;
            case 'ヒ':
                switch(ch2) {
                case 'ャ':
                    builder.append("hya");
                    i++;
                    break;
                case 'ィ':
                    builder.append("hyi");
                    i++;
                    break;
                case 'ュ':
                    builder.append("hyu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("hye");
                    i++;
                    break;
                case 'ョ':
                    builder.append("hyo");
                    i++;
                    break;
                default:
                    builder.append("hi");
                    break;
                }
                break;
            case 'フ':
                switch(ch2) {
                case 'ァ':
                    builder.append("fwa");
                    i++;
                    break;
                case 'ィ':
                    builder.append("fwi");
                    i++;
                    break;
                case 'ゥ':
                    builder.append("fwu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("fwe");
                    i++;
                    break;
                case 'ォ':
                    builder.append("fwo");
                    i++;
                    break;
                case 'ャ':
                    builder.append("fya");
                    i++;
                    break;
                case 'ュ':
                    builder.append("fyu");
                    i++;
                    break;
                case 'ョ':
                    builder.append("fyo");
                    i++;
                    break;
                default:
                    builder.append("hu");
                    break;
                }
                break;
            case 'ヘ':
                builder.append("he");
                break;
            case 'ホ':
                builder.append("ho");
                break;
            case 'マ':
                builder.append("ma");
                break;
            case 'ミ':
                switch(ch2) {
                case 'ャ':
                    builder.append("mya");
                    i++;
                    break;
                case 'ィ':
                    builder.append("myi");
                    i++;
                    break;
                case 'ュ':
                    builder.append("myu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("mye");
                    i++;
                    break;
                case 'ョ':
                    builder.append("myo");
                    i++;
                    break;
                default:
                    builder.append("mi");
                    break;
                }
                break;
            case 'ム':
                builder.append("mu");
                break;
            case 'メ':
                builder.append("me");
                break;
            case 'モ':
                builder.append("mo");
                break;
            case 'ヤ':
                builder.append("ya");
                break;
            case 'ユ':
                builder.append("yu");
                break;
            case 'ヨ':
                builder.append("yo");
                break;
            case 'ラ':
                builder.append("ra");
                break;
            case 'リ':
                switch(ch2) {
                case 'ャ':
                    builder.append("rya");
                    i++;
                    break;
                case 'ィ':
                    builder.append("ryi");
                    i++;
                    break;
                case 'ュ':
                    builder.append("ryu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("rye");
                    i++;
                    break;
                case 'ョ':
                    builder.append("ryo");
                    i++;
                    break;
                default:
                    builder.append("ri");
                    break;
                }
                break;
            case 'ル':
                builder.append("ru");
                break;
            case 'レ':
                builder.append("re");
                break;
            case 'ロ':
                builder.append("ro");
                break;
            case 'ワ':
                builder.append("wa");
                break;
            case 'ヰ':
                builder.append("wi");
                break;
            case 'ヱ':
                builder.append("we");
                break;
            case 'ヲ':
                builder.append("wo");
                break;
            case 'ン':
                switch (ch2) {
                case 'ア':
                case 'イ':
                case 'ウ':
                case 'エ':
                case 'オ':
                case 'ヤ':
                case 'ユ':
                case 'ヨ':
                    builder.append("nn");
                    break;
                default:
                    builder.append('n');
                    break;
                }
                break;
            case 'ガ':
                builder.append("ga");
                break;
            case 'ギ':
                switch(ch2) {
                case 'ャ':
                    builder.append("gya");
                    i++;
                    break;
                case 'ィ':
                    builder.append("gyi");
                    i++;
                    break;
                case 'ュ':
                    builder.append("gyu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("gye");
                    i++;
                    break;
                case 'ョ':
                    builder.append("gyo");
                    i++;
                    break;
                default:
                    builder.append("gi");
                    break;
                }
                break;
            case 'グ':
                switch(ch2) {
                case 'ァ':
                    builder.append("gwa");
                    i++;
                    break;
                case 'ィ':
                    builder.append("gwi");
                    i++;
                    break;
                case 'ゥ':
                    builder.append("gwu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("gwe");
                    i++;
                    break;
                case 'ォ':
                    builder.append("gwo");
                    i++;
                    break;
                default:
                    builder.append("gu");
                    break;
                }
                break;
            case 'ゲ':
                builder.append("ge");
                break;
            case 'ゴ':
                builder.append("go");
                break;
            case 'ザ':
                builder.append("za");
                break;
            case 'ジ':
                switch(ch2) {
                case 'ャ':
                    builder.append("zya");
                    i++;
                    break;
                case 'ィ':
                    builder.append("zyi");
                    i++;
                    break;
                case 'ュ':
                    builder.append("zyu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("zye");
                    i++;
                    break;
                case 'ョ':
                    builder.append("zyo");
                    i++;
                    break;
                default:
                    builder.append("zi");
                    break;
                }
                break;
            case 'ズ':
                builder.append("zu");
                break;
            case 'ゼ':
                builder.append("ze");
                break;
            case 'ゾ':
                builder.append("zo");
                break;
            case 'ダ':
                builder.append("da");
                break;
            case 'ヂ':
                switch(ch2) {
                case 'ャ':
                    builder.append("dya");
                    i++;
                    break;
                case 'ィ':
                    builder.append("dyi");
                    i++;
                    break;
                case 'ュ':
                    builder.append("dyu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("dye");
                    i++;
                    break;
                case 'ョ':
                    builder.append("dyo");
                    i++;
                    break;
                default:
                    builder.append("di");
                    break;
                }
                break;
            case 'ヅ':
                builder.append("du");
                break;
            case 'デ':
                switch(ch2) {
                case 'ャ':
                    builder.append("dha");
                    i++;
                    break;
                case 'ィ':
                    builder.append("dhi");
                    i++;
                    break;
                case 'ュ':
                    builder.append("dhu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("dhe");
                    i++;
                    break;
                case 'ョ':
                    builder.append("dho");
                    i++;
                    break;
                default:
                    builder.append("de");
                    break;
                }
                break;
            case 'ド':
                switch(ch2) {
                case 'ァ':
                    builder.append("dwa");
                    i++;
                    break;
                case 'ィ':
                    builder.append("dwi");
                    i++;
                    break;
                case 'ゥ':
                    builder.append("dwu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("dwe");
                    i++;
                    break;
                case 'ォ':
                    builder.append("dwo");
                    i++;
                    break;
                default:
                    builder.append("do");
                    break;
                }
                break;
            case 'バ':
                builder.append("ba");
                break;
            case 'ビ':
                switch(ch2) {
                case 'ャ':
                    builder.append("bya");
                    i++;
                    break;
                case 'ィ':
                    builder.append("byi");
                    i++;
                    break;
                case 'ュ':
                    builder.append("byu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("bye");
                    i++;
                    break;
                case 'ョ':
                    builder.append("byo");
                    i++;
                    break;
                default:
                    builder.append("bi");
                    break;
                }
                break;
            case 'ブ':
                builder.append("bu");
                break;
            case 'ベ':
                builder.append("be");
                break;
            case 'ボ':
                builder.append("bo");
                break;
            case 'パ':
                builder.append("pa");
                break;
            case 'ピ':
                switch(ch2) {
                case 'ャ':
                    builder.append("pya");
                    i++;
                    break;
                case 'ィ':
                    builder.append("pyi");
                    i++;
                    break;
                case 'ュ':
                    builder.append("pyu");
                    i++;
                    break;
                case 'ェ':
                    builder.append("pye");
                    i++;
                    break;
                case 'ョ':
                    builder.append("pyo");
                    i++;
                    break;
                default:
                    builder.append("pi");
                    break;
                }
                break;
            case 'プ':
                builder.append("pu");
                break;
            case 'ペ':
                builder.append("pe");
                break;
            case 'ポ':
                builder.append("po");
                break;
            case 'ヴ':
                switch(ch2) {
                case 'ァ':
                    builder.append("va");
                    i++;
                    break;
                case 'ィ':
                    builder.append("vi");
                    i++;
                    break;
                case 'ェ':
                    builder.append("ve");
                    i++;
                    break;
                case 'ォ':
                    builder.append("vo");
                    i++;
                    break;
                case 'ャ':
                    builder.append("vya");
                    i++;
                    break;
                case 'ュ':
                    builder.append("vyu");
                    i++;
                    break;
                case 'ョ':
                    builder.append("vyo");
                    i++;
                    break;
                default:
                    builder.append("vu");
                    break;
                }
                break;
            case 'ァ':
                builder.append("la");
                break;
            case 'ィ':
                builder.append("li");
                break;
            case 'ゥ':
                builder.append("lu");
                break;
            case 'ェ':
                builder.append("le");
                break;
            case 'ォ':
                builder.append("lo");
                break;
            case 'ヵ':
                builder.append("lka");
                break;
            case 'ヶ':
                builder.append("lke");
                break;
            case 'ャ':
                builder.append("lya");
                break;
            case 'ュ':
                builder.append("lyu");
                break;
            case 'ョ':
                builder.append("lyo");
                break;
            case 'ヮ':
                builder.append("lwa");
                break;
            case 'ー':
                builder.append('-');
                break;
            case '・':
            case '＝':
                /* drop these characters */
                break;
            default:
                builder.append(ch);
                break;
            }
        }
    }
}
