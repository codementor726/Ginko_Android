package com.ginko.common;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by YongJong on 08/13/16.
 */
public class Trie<Trieable>{

    private vertex rootvertex;

    public Trie(List<Trieable> objectList) {
        rootvertex = new vertex();

        for (int i = 0; i<objectList.size(); i++) {
            String word = objectList.get(i).toString().toLowerCase();
            addWord(rootvertex, word, i);
        }
    }

    public vertex getRootvertex() {
        return rootvertex;
    }

    public void addWord(vertex vertex, String word, int index) {
        if (word.isEmpty()) {
            vertex.addIndexToWordsIndexList(index);
            vertex.increaseWordsNumber();
        }
        else {
            vertex.addIndexToPrefixesIndexList(index);
            vertex.increasePrefixesNumber();
            Character fChar = word.charAt(0);
            HashMap<Character, vertex> vertexSons = vertex.getvertexSons();

            if (!vertexSons.containsKey(fChar)) {
                vertex.addvertexSon(fChar);
            }

            word = (word.length() == 1) ? "" : word.substring(1);
            addWord(vertexSons.get(fChar), word, index);
        }
    }

    public List<Integer> getWordsIndexes(vertex vertex, String word) {
        if (word.isEmpty()) {
            return vertex.getWordsIndexList();
        }
        else {
            Character fChar = word.charAt(0);
            if (!(vertex.getvertexSons().containsKey(fChar))) {
                return null;
            }
            else {
                word = (word.length() == 1) ? "" : word.substring(1);
                return getWordsIndexes(vertex.getvertexSons().get(fChar), word);
            }
        }
    }

    public List<Integer> getPrefixesIndexes(vertex vertex, String prefix) {
        if (prefix.isEmpty()) {
            return vertex.getWordsIndexList();
        }
        else {
            Character fChar = prefix.charAt(0);
            if (!(vertex.getvertexSons().containsKey(fChar))) {
                return null;
            }
            else {
                prefix = (prefix.length() == 1) ? "" : prefix.substring(1);
                return getWordsIndexes(vertex.getvertexSons().get(fChar), prefix);
            }
        }
    }
}