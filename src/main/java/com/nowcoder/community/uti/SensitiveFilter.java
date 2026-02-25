package com.nowcoder.community.uti;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive_words.txt")) {

            if (is == null) {
                logger.error("Cannot find sensitive_words.txt");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String keyword;
                while ((keyword = reader.readLine()) != null) {
                    keyword = keyword.trim().toLowerCase();
                    if (!keyword.isEmpty()) {
                        addKeyword(keyword);
                    }
                }
            }

        } catch (IOException e) {
            logger.error("Fail to load sensitive_words.txt: {}", e.getMessage());
        }
    }

    // add sensitive word to trie
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i=0; i<keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            tempNode = subNode;

            // set end mark
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        // pointers
        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;
        StringBuilder sb = new StringBuilder();
        // check
        while (position < text.length()) {
            char original = text.charAt(position);
            char c = Character.toLowerCase(original);
            // skip notation
            if (isSymbol(original)) {
                if (tempNode == rootNode) {
                    sb.append(original);
                    begin++;
                }
                position++;
                continue;
            }
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                sb.append(REPLACEMENT);
                begin = ++position;
                tempNode = rootNode;
            } else {
                position++;
            }
        }
        sb.append(text.substring(begin));
        return sb.toString();
    }

    // detect notations
    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // Build up Trie
    private class TrieNode {
        // key word ending mark
        private boolean isKeywordEnd = false;

        // subnode
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // add subnode
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // get subnode
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
