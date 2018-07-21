/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package experiments;

import evoPuzzle.PuzzleConfig;
import evoPuzzle.PuzzleDecoder;
import evoPuzzle.PuzzleEvaluation;
import evoPuzzle.PuzzleGA;
import evoPuzzle.PuzzleIndividual;
import io.LevelFileReader;
import io.PuzzleFileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graphstream.graph.Graph;
import org.graphstream.ui.swingViewer.ViewPanel;

/**
 *
 * @author Kurumin
 */
public class StandardPuzzleExperiment extends AbstractExperiment implements Experiment{
    
    private int[] puzzleCurrent;
    private int puzzleExecutions;
            
    public DescriptiveStatistics time;
    public DescriptiveStatistics fitness;
    public DescriptiveStatistics penalty;
    public DescriptiveStatistics din; // Distance from Ideal Non-linearity
    public DescriptiveStatistics td;  // Travel Distance
    public DescriptiveStatistics vr;  // Visited Rooms
    
    public StandardPuzzleExperiment() {
        //puzzleCurrent = 0;
        puzzleExecutions = 30;
        current    =  0;
        executions = 30;
        graph   = new Graph[executions];
        time    = new DescriptiveStatistics(executions);
        fitness = new DescriptiveStatistics(executions);
        din     = new DescriptiveStatistics(executions);
        td      = new DescriptiveStatistics(executions);
        vr      = new DescriptiveStatistics(executions);
        penalty = new DescriptiveStatistics(executions);
    }
    
    public static void main(String args[]){  
       StandardPuzzleExperiment exp = new StandardPuzzleExperiment();
       exp.run();
    }
    
    @Override
    public void run(){
        setup();
        verifyFolder(PuzzleConfig.folder);
        execute();
        readResults();
    }

    @Override
    public void setup() {
        PuzzleConfig.folder = "..\\data\\experiments\\standard\\";
        
        PuzzleConfig.useIdealNonLinearity = true;
        PuzzleConfig.useTravelDistance = true;
        PuzzleConfig.useVisitedRooms = true;
        
        PuzzleConfig.defaultMaxKeys = 5;
        PuzzleConfig.defaultMaxSwitches = 0;
        PuzzleConfig.defaultMaxTries = 10;
        
        PuzzleConfig.idealNonLinearity = 3;
        
        PuzzleConfig.crossoverProb = 0.90;
        PuzzleConfig.mutationProb  = 0.10;
        
        PuzzleConfig.maxGen  = 100;
        PuzzleConfig.popSize = 100;
    }
    
    @Override
    public void verifyFolder(String path){
        File folder = new File(path);
        if(!folder.exists())
            folder.mkdir();
        ArrayList<File> levels = new ArrayList<>();
        for(int i = 0; i < folder.listFiles().length; i++){
            if(folder.listFiles()[i].isDirectory())
                levels.add(folder.listFiles()[i]);
        }
        int totalCurrent = 0;
        puzzleCurrent = new int[levels.size()];
        for(int i = 0; i < levels.size(); i++){
            File dir = new File(folder, levels.get(i).getName());
            System.out.println("Reading level "+dir+" folder");
            int existingResults = 0;
            for(int j = 0; j < dir.listFiles().length; j++){
                if(dir.listFiles()[j].getName().startsWith("puzzle"))
                    existingResults++;
            }
            puzzleCurrent[i] = existingResults;
            totalCurrent += existingResults;
        }
        if(levels.size() > 0){
            int remainingLv = Math.max(0, executions - levels.size());
            int remainingPz = Math.max(0, (executions*puzzleExecutions) - totalCurrent);
            JOptionPane.showMessageDialog(null,
                    "There are "+levels.size()+" levels in the speficied folder out of "+executions+" required. We need "+remainingLv+" more levels.\n"
                  + "In total, there are "+totalCurrent+" generated puzzles. It is defined that each level will have "+puzzleExecutions+" puzzles.\n"
                  + "Thus, we need more "+remainingPz+" puzzles.",
                    "Atention!", JOptionPane.WARNING_MESSAGE);
            String opt = JOptionPane.showInputDialog("Choose an option:\n"
                    + "[0] - Proceed anyway (new "+(levels.size()*puzzleExecutions)+" executions).\n"
                    + "[1] - Proceed just "+remainingPz+" more times.\n"
                    + "[2] - Only read existing results.\n"
                    + "[3] - Exit.");
            if(opt.compareTo("0")==0){
                puzzleCurrent = new int[executions];
                for(int i = 0; i < puzzleCurrent.length; i++)
                    puzzleCurrent[i] = 0;
            }
            else if(opt.compareTo("1")==0){
                //current = results.size();
            }
            else if(opt.compareTo("2")==0){
                puzzleCurrent = new int[executions];
                for(int i = 0; i < puzzleCurrent.length; i++)
                    puzzleCurrent[i] = puzzleExecutions;
            }
            else{
                System.exit(0);
            }
        }
    }

    @Override
    public void execute() {
        File folder = new File(PuzzleConfig.folder);
        ArrayList<File> results = new ArrayList<>();
        for (File listFile : folder.listFiles()) {
            if (listFile.isDirectory()) {
                results.add(listFile);
            }
        }
        for (int f = 0; f < executions; f++) {
            File dir = results.get(f);
            //System.out.println(dir.getName());

            for (int i = puzzleCurrent[f]; i < puzzleExecutions; i++) {
                try {
                    System.out.println("\nFolder " + dir + "\n++Execution number " + (i + 1) + " / " + puzzleExecutions + "++\n");
                    if(graph[f] != null){
                        graph[f].clear();
                        graph[f].clearAttributes();
                    }
                    LevelFileReader reader = new LevelFileReader(PuzzleConfig.folder, dir.getName());
                    graph[f] = reader.parseJsonToGraph();

                    long initialTime = System.currentTimeMillis();
                    PuzzleGA pga = new PuzzleGA(graph[f]);
                    Thread t = new Thread(pga);
                    t.start();
                    t.join();
                    long finalTime = System.currentTimeMillis();

                    PuzzleIndividual puzzle = pga.getBestIndividual();

                    PuzzleEvaluation pEva = new PuzzleEvaluation();
                    double[] fitness = pEva.fitness(graph[f], puzzle, false);
                    
                    PuzzleDecoder decoder = new PuzzleDecoder(graph[f]);
                    Graph resultingGraph = decoder.decode(puzzle, false);
                    pEva.solve(resultingGraph, puzzle);
                    String[] path = new String[puzzle.getSolution().size()];
                    for(int p = 0; p < path.length; p++)
                        path[p] = puzzle.getSolution().get(p);
                    resultingGraph.addAttribute("puzzle_path", Arrays.toString(path));
                    resultingGraph.addAttribute("puzzle_fitness", fitness[0]);
                    resultingGraph.addAttribute("puzzle_runtime", (finalTime-initialTime));
                    
                    pga.exportGraph(resultingGraph, resultingGraph.getId(), i+"");
                } catch (InterruptedException ex) {
                    Logger.getLogger(StandardPuzzleExperiment.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } 
    }

    @Override
    public void readResults() {
        File folder = new File(PuzzleConfig.folder);
        ArrayList<File> results = new ArrayList<>();
        //ArrayList<File> puzzles = new ArrayList<>();
        for (File listFile : folder.listFiles()) {
            if (listFile.isDirectory()) {
                results.add(listFile);
            }
        }
        
        executions = results.size();
        int totalExecutions = executions * puzzleExecutions;
        
        graph = new Graph[executions];
        fitness = new DescriptiveStatistics();
        penalty = new DescriptiveStatistics();
        din = new DescriptiveStatistics();
        td = new DescriptiveStatistics();
        vr = new DescriptiveStatistics();
        
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        String minID = "";
        String maxID = "";
        String pID = "\n";
        //int version = 0;
        for(int f = 0; f < results.size(); f++){
            File dir = results.get(f);
            //System.out.println("Working on folder: "+dir.getName());
            for(int j = 0; j < dir.listFiles().length; j++){
                String filename = dir.listFiles()[j].getName();
                if(filename.startsWith("puzzle")){
                    if(graph[f] != null){
                        graph[f].clear();
                        graph[f].clearAttributes();
                    }
                    LevelFileReader reader = new LevelFileReader(dir.getParent(), dir.getName());
                    graph[f] = reader.parseJsonToGraph();
                    
                    String v = filename.substring(filename.lastIndexOf("_")+1, filename.indexOf(".json"));
                    PuzzleFileReader puzzleReader = new PuzzleFileReader(dir.getParent(), dir.getName(), v);
                    puzzleReader.improveGraph(graph[f]);
                    time.addValue((double) (long) graph[f].getAttribute("puzzle_runtime"));
                    PuzzleDecoder decoder = new PuzzleDecoder(graph[f]);
                    PuzzleIndividual puzzle = decoder.encode();
                    PuzzleEvaluation pEva = new PuzzleEvaluation();
                    double[] detailedFitness = pEva.fitness(graph[f], puzzle, false);
                    /*
                    // debug
                    pEva.solve(graph[f], puzzle);
                    String[] path = new String[puzzle.getSolution().size()];
                    for(int p = 0; p < path.length; p++)
                        path[p] = puzzle.getSolution().get(p);
                    String computedPath = Arrays.toString(path);
                    String recordedPath = (String) graph[f].getAttribute("puzzle_path");
                    if(computedPath.compareTo(recordedPath) != 0){
                        System.err.println(filename+" path inconsistency!\n"
                                + computedPath+ "\n!=\n"+recordedPath);
                    }
                    
                    // debug
                    double computedFitness = Double.parseDouble(String.format("%.6f", detailedFitness[0]));
                    double recordedFitness = graph[f].getAttribute("puzzle_fitness");
                    if(computedFitness != recordedFitness){
                        System.err.println(filename+" fitness inconsistency!\n"
                                + computedFitness+ " != "+recordedFitness);
                    }*/
                    if(detailedFitness[4] == 0){
                        fitness.addValue(detailedFitness[0]);
                        din.addValue(detailedFitness[1]);
                        td.addValue(1.0/detailedFitness[2]);
                        vr.addValue(1.0/detailedFitness[3]);
                        if (detailedFitness[0] < min) {
                            min = detailedFitness[0];
                            minID = filename;
                        }
                        if (detailedFitness[0] > max) {
                            max = detailedFitness[0];
                            maxID = filename;
                        }
                    }
                    else{
                        penalty.addValue(detailedFitness[4]);
                        pID += filename+", \n";
                        System.err.println(
                            filename+"\n-## Fitness: " + detailedFitness[0] + 
                            "(DIN: " + detailedFitness[1] + 
                            " TD: " + detailedFitness[2] + 
                            " VR: "  + detailedFitness[3] + 
                            " P: " + detailedFitness[4] +
                            ") ##-");/* + // debug
                            "Computed Fitness: "+computedFitness+" | Recorded Fitness: "+recordedFitness+"\n"+
                            "Computed Path: "+computedPath+"\n"+
                            "Recorded Path: "+recordedPath);*/
                    }
                }
            }
        }
        double minFitness = fitness.getMin();
        double meanFitness = fitness.getMean();
        double stdFitness = fitness.getStandardDeviation();
        double maxFitness = fitness.getMax();
        
        double minDIN = din.getMin();
        double meanDIN = din.getMean();
        double stdDIN = din.getStandardDeviation();
        double maxDIN = din.getMax();
        
        double minTD = td.getMin();
        double meanTD = td.getMean();
        double stdTD = td.getStandardDeviation();
        double maxTD = td.getMax();
        
        double minVR = vr.getMin();
        double meanVR = vr.getMean();
        double stdVR = vr.getStandardDeviation();
        double maxVR = vr.getMax();
        
        double minTime = time.getMin();
        double meanTime = time.getMean();
        double stdTime = time.getStandardDeviation();
        double maxTime = time.getMax();
        
        String tableLine =   "DIN & $"+String.format("%s",minDIN)+"$ & $"+String.format("%.3f",meanDIN)+"_{ \\pm "+String.format("%.3f",stdDIN)+"}$ & $"+String.format("%s",maxDIN)+"$ \\\\ \n"
                           + "TD  & $"+String.format("%.3f",minTD)+"$ & $"+String.format("%.3f",meanTD)+"_{ \\pm "+String.format("%.3f",stdTD)+"}$ & $"+String.format("%.3f",maxTD)+"$ \\\\ \n"
                           + "VR  & $"+String.format("%.3f",minVR)+"$ & $"+String.format("%.3f",meanVR)+"_{ \\pm "+String.format("%.3f",stdVR)+"}$ & $"+String.format("%.3f",maxVR)+"$ \\\\ \n" 
                           + "Fitness & $"+String.format("%.3f",minFitness)+"$ & $"+String.format("%.3f",meanFitness)+"_{ \\pm "+String.format("%.3f",stdFitness)+"}$ & $"+String.format("%.3f",maxFitness)+"$ \\\\ \n"
                           + "Time (s) & $"+String.format("%.3f",minTime/1000)+"$ & $"+String.format("%.3f",meanTime/1000)+"_{ \\pm "+String.format("%.3f",stdTime/1000)+"}$ & $"+String.format("%.3f",maxTime/1000)+"$\\\\ \n"; 
                ;
                
        tableLine += "\n\n"
                + "Min fitness ID: "+minID+"\n"
                + "Max fitness ID: "+maxID+"\n"
                + "Penalized solutions: "+penalty.getN()+" / "+fitness.getN()+"\n"
                + "pID: "+pID;
          System.out.println(tableLine);
        try {
            PrintWriter pw = new PrintWriter(PuzzleConfig.folder+"puzzle_table.txt");
            pw.printf(tableLine);
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StandardPuzzleExperiment.class.getName(), ex.getMessage());
        }
        System.out.println("Results done at: "+PuzzleConfig.folder+"puzzle_table.txt");
    }
    
}
