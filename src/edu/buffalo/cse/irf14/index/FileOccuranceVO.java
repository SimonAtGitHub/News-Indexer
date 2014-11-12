/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.buffalo.cse.irf14.index;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author san
 */
public class FileOccuranceVO implements Comparable<FileOccuranceVO>{
    int docID;
    int numOfOcc;

    
    public FileOccuranceVO(){
        
    }
    
    public FileOccuranceVO(int docId, int numOfOccur){
        this.docID = docId;
        this.numOfOcc = numOfOccur;
    }
    
    
    public int getDocID() {
        return docID;
    }

    public void setDocID(int docID) {
        this.docID = docID;
    }

    public int getNumOfOcc() {
        return numOfOcc;
    }

    public void setNumOfOcc(int numOfOcc) {
        this.numOfOcc = numOfOcc;
    }
    
    public String toString(){
        return (docID + ":" + numOfOcc);
    }

    @Override
    public int compareTo(FileOccuranceVO o) {
        return this.docID - o.docID;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj instanceof FileOccuranceVO){
            if(this.docID == ((FileOccuranceVO) obj).docID){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.docID;
    }
    
    
    
    public static void main(String[] args) {
        
        List<FileOccuranceVO> s1 = new ArrayList<FileOccuranceVO>();
        s1.add(new FileOccuranceVO(1, 20));
        s1.add(new FileOccuranceVO(2, 10));
        s1.add(new FileOccuranceVO(3, 10));
        s1.add(new FileOccuranceVO(4, 20));
        
        List<FileOccuranceVO> s2 = new ArrayList<FileOccuranceVO>();
        s2.add(new FileOccuranceVO(1, 20));
        s2.add(new FileOccuranceVO(3, 5));
        s2.add(new FileOccuranceVO(4, 20));
        
        List<FileOccuranceVO> s3 = new ArrayList<FileOccuranceVO>();
        s3.add(new FileOccuranceVO(11, 20));
        s3.add(new FileOccuranceVO(3, 5));
        
    }
    
    
}
