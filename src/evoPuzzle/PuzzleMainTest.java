package evoPuzzle;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import util.GraphStreamUtil;
import org.graphstream.graph.Graph;
import config.GeneralConfig;
import io.LevelFileReader;
/**
 *
 * @author andre
 */
public class PuzzleMainTest {
    
    public static void puzzleConfigSetup(){
        PuzzleConfig.popSize = 100;
        PuzzleConfig.maxGen  = 100;
        PuzzleConfig.crossoverProb  =  0.9;
        PuzzleConfig.mutationProb   =  0.1;
    }
    
    public static void main(String args[]) throws InterruptedException{
        //setup();
        puzzleConfigSetup();
        System.setProperty("org.graphstream.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        
        String folder = "..\\data\\levels\\";
        String filename = "test\\level_test.json";
        System.out.println(filename);
        LevelFileReader reader = new LevelFileReader(folder, filename);
        Graph graph = reader.parseJsonToGraph();
        
        PuzzleGA pga = new PuzzleGA(graph);
        Thread t = new Thread(pga);
        t.start();
        t.join();
        
        PuzzleIndividual puzzle = pga.getBestIndividual();
        
        PuzzleEvaluation peva = new PuzzleEvaluation();
        double[] fitness = peva.fitness(graph, puzzle, false);
        System.out.println(puzzle+"\nFitness: "+String.format("%.6f", fitness[0])+" / "
                + "DIN: "+fitness[1]+" "
                + "TS: "+String.format("%.6f", fitness[2])+"("+String.format("%.2f", 1.0/fitness[2])+") "
                + "VR: "+String.format("%.6f", fitness[3])+"("+String.format("%.2f", 1.0/fitness[3])+") "
                + "P: "+fitness[4]);
        
        PuzzleDecoder decoder = new PuzzleDecoder(graph);
        graph = decoder.decode(puzzle, false);
        
        GraphStreamUtil gUtil = new GraphStreamUtil();
        graph.addAttribute("ui.stylesheet", "url('"+GeneralConfig.media+"')");
        gUtil.normalizeNodesSizes(graph, 15, 30);
        gUtil.setupStyle(graph); 
        graph.display(false);
    }
}
