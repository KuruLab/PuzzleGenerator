/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoPuzzle;

/**
 *
 * @author andre
 */
public class PuzzleConfig {
    
    public static String folder = "..\\data\\puzzles\\";
    
    public static int defaultMaxTries = 10;
    public static int defaultMaxKeys = 4;
    public static int defaultMaxSwitches = 1;
    
    public static int popSize = 100;
    public static int maxGen  = 50;
    public static double crossoverProb  =  0.9;
    public static double mutationProb   =  0.1;
    
    public static boolean useIdealNonLinearity = true;
    public static boolean useTravelDistance = true;
    public static boolean useVisitedRooms = true;
    public static int idealNonLinearity = 3; // attention: minimum value is 1
    
}
