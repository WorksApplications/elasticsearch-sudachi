package com.worksap.nlp.sudachi;

import java.util.List;

import com.worksap.nlp.sudachi.InputText;
import com.worksap.nlp.sudachi.Lattice;
import com.worksap.nlp.sudachi.LatticeNode;
import com.worksap.nlp.sudachi.PathRewritePlugin;
import com.worksap.nlp.sudachi.dictionary.Grammar;

public class JoinOovPlugin extends PathRewritePlugin {

    short oovPosId;

    @Override
    public void setUp(Grammar grammar) {
        List<String> pos = settings.getStringList("oovPOS");
        if (pos.isEmpty()) {
            throw new IllegalArgumentException("oovPOS is undefined");
        }
        oovPosId = grammar.getPartOfSpeechId(pos);
        if (oovPosId < 0) {
            throw new IllegalArgumentException("oovPOS is invalid");
        }
    }

    @Override
    public void rewrite(InputText<?> text, List<LatticeNode> path,
            Lattice lattice) {
        boolean isOOVNode = false;
        int begin = 0;

        for (int i = 0; i < path.size(); i++) {
            LatticeNode node = path.get(i);
            if (node.isOOV()) {
                if (!isOOVNode) {
                    begin = i;
                    isOOVNode = true;
                }
            } else {
                if (isOOVNode) {
                    isOOVNode = false;
                    concatenateOov(path, begin, i, oovPosId, lattice);
                }
            }
        }
    }
}
