/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoPuzzle;

import puzzle.Condition;
import puzzle.Symbol;
import java.util.ArrayList;
import java.util.Random;
import org.graphstream.graph.Graph;

/**
 *
 * @author andre
 */
public class RandomPuzzleGenerator {
    
    public PuzzleIndividual newPuzzle(Graph graph){
        PuzzleIndividual puzzle = new PuzzleIndividual();
        
        ArrayList<Integer> selected = new ArrayList<>();
        Random random = new Random(System.nanoTime());
        int start = random.nextInt(graph.getNodeCount());
        selected.add(start);
        int boss = random.nextInt(graph.getNodeCount());
        while(selected.contains(boss)){
            boss = random.nextInt(graph.getNodeCount());
        }
        selected.add(boss);
        
        PuzzleGene startGene = new PuzzleGene(start);
        startGene.getSymbols().add(new Symbol(Symbol.START));
        puzzle.add(startGene);
        
        PuzzleGene bossGene = new PuzzleGene(boss);
        bossGene.getSymbols().add(new Symbol(Symbol.BOSS));
        puzzle.add(bossGene);
        
        for(int i = 0; i < PuzzleConfig.defaultMaxKeys; i++){
            int nextKey = random.nextInt(graph.getNodeCount());
            while(selected.contains(nextKey)){
                nextKey = random.nextInt(graph.getNodeCount());
            }
            selected.add(nextKey);
            
            PuzzleGene keyGene = new PuzzleGene(nextKey);
            
            Condition condition = new Condition(new Symbol(i));
            Symbol symbol = new Symbol(i+1);
            
            keyGene.getConditions().add(condition);
            keyGene.getSymbols().add(symbol);
            
            puzzle.add(keyGene);
        }
        // switches not supported yet
        return puzzle;
    }
    
    //public PuzzleIndividual newPuzzle(Graph graph){
    //    
    //}
    
}
