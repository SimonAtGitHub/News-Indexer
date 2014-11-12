package edu.buffalo.cse.irf14.commons;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author san
 */
public class VO {
    private int numOfMatches = 0;
    private String lineContent;

    public VO(String lineContent) {
        this.lineContent = lineContent;
    }
    
    public int getNumOfMatches() {
        return numOfMatches;
    }

    public void setNumOfMatches(int numOfMatches) {
        this.numOfMatches = numOfMatches;
    }

    public String getLineContent() {
        return lineContent;
    }

    public void setLineContent(String lineContent) {
        this.lineContent = lineContent;
    }

    public void incrementMatchcount() {
        numOfMatches++;
    }
}
