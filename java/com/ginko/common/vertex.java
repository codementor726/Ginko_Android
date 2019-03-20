package com.ginko.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by YongJong on 08/13/16.
 */
public class vertex {

    private HashMap<Character, vertex> vertexSons;
    private List<Integer> wordsIndexList;
    private List<Integer> prefixesIndexList;
    private int wordsNumber;
    private int prefixesNumber;

    public vertex() {
        vertexSons = new HashMap<Character, vertex>();
        wordsIndexList = new ArrayList<Integer>();
        prefixesIndexList = new ArrayList<Integer>();
        wordsNumber = 0;
        prefixesNumber = 0;
    }

    public boolean hasWords() {
        if (wordsNumber > 0) {
            return true;
        }
        return false;
    }

    public boolean hasPrefixes() {
        if (prefixesNumber > 0) {
            return true;
        }
        return false;
    }

    public void addvertexSon(Character character) {
        vertexSons.put(character, new vertex());
    }

    public void addIndexToWordsIndexList(int index) {
        wordsIndexList.add(index);
    }

    public void addIndexToPrefixesIndexList(int index) {
        prefixesIndexList.add(index);
    }

    public HashMap<Character, vertex> getvertexSons() {
        return vertexSons;
    }

    public List<Integer> getWordsIndexList() {
        return wordsIndexList;
    }

    public List<Integer> getPrefixesIndexList() {
        return prefixesIndexList;
    }

    public int getWordsNumber() {
        return wordsNumber;
    }

    public int getPrefixesNumber() {
        return prefixesNumber;
    }

    public void increaseWordsNumber() {
        wordsNumber++;
    }

    public void increasePrefixesNumber() {
        prefixesNumber++;
    }
}