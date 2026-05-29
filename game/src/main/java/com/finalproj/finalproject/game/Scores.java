/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finalproj.finalproject.game;
import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;


/**
 *
 * @author Shreyan Ganguly, Aarush Jain
 */
public class Scores {
    private PriorityQueue<Float> times; 
    
    public Scores() {
        times = new PriorityQueue<>();
    }
    
    public List<Float> getTopTenTimes() {
        PriorityQueue<Float> tempQueue = new PriorityQueue<>(this.times);
        List<Float> topTen = new ArrayList<>();

        while (!tempQueue.isEmpty() && topTen.size() < 10) {
            topTen.add(tempQueue.poll());
        }
        return topTen;
    }
    
    public void add(float f) {
        times.add(f);
    }
}
