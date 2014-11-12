/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.buffalo.cse.irf14.query;

/**
 *
 * @author san
 */
public class Token {
    
    private String content;
    private TokenType type;
    private String indexType;
    private boolean defaultIndexType;
    public boolean isPhraseTerm;
    
    public Token(){
        
    }
    
    public Token cloneToken(){
        Token t = new Token();
        
        t.content = this.content;
        t.type = this.type;
        t.indexType = this.indexType;
        t.defaultIndexType = this.defaultIndexType;
        t.isPhraseTerm = this.isPhraseTerm;
        
        return t;
    }
    
    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    public boolean isDefaultIndexType() {
        return defaultIndexType;
    }

    public void setDefaultIndexType(boolean defaultIndexType) {
        this.defaultIndexType = defaultIndexType;
    }

    public Token(String content, TokenType type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }
    
}
