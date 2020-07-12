 package com.uom.georgevio;
/**
 * Java Program to Implement Tarjan Algorithm
 * https://www.sanfoundry.com/java-program-tarjan-algorithm/
 *     
 * Tarjan Algorithm is used for
 * finding all strongly connected components in a graph.
 **/

import java.util.*;

class Tarjan{

    /** number of vertices **/
    private int V;    

    /** preorder number counter **/
    private int preCount;

    /** low number of v **/
    private int[] low;

    /** to check if v is visited **/
    private boolean[] visited;      

    /** to store given graph **/
    private List<Integer>[] graph;

    /** to store all scc **/
    private List<List<Integer>> sccComp;

    private Stack<Integer> stack;

    /** function to get all strongly connected components **/
    public List<List<Integer>> getSCComponents(List<Integer>[] graph) {
        V = graph.length;
        this.graph = graph;
        low = new int[V];
        visited = new boolean[V];
        stack = new Stack<Integer>();
        sccComp = new ArrayList<>();

        for (int v = 0; v < V; v++)
              if (!visited[v])
                dfs(v);
        return sccComp;
    }

    /** function dfs **/
    public void dfs(int v) {

        low[v] = preCount++;
        visited[v] = true;
        stack.push(v);
        int min = low[v];

        for (int w : graph[v]) {
            if (!visited[w])
                dfs(w);

            if (low[w] < min) 
                min = low[w];
        }

        if (min < low[v]){ 
            low[v] = min; 
            return; 
        }        

        List<Integer> component = new ArrayList<Integer>();
        int w;
        do{
            w = stack.pop();
            component.add(w);
            low[w] = V;                
        } while (w != v);

        sccComp.add(component);        
    }    

    public static void main(String[] args){    
        Scanner scan = new Scanner(System.in);
        System.out.println("Tarjan algorithm Test\n");
        
        System.out.println("Enter number of Vertices");
        /** number of vertices **/
        int V = scan.nextInt();

        /** make graph **/
        List<Integer>[] g = new List[V];        
        for (int i = 0; i < V; i++)
            g[i] = new ArrayList<Integer>();        

        /** accept all edges **/
        System.out.println("\nEnter number of edges");
        int E = scan.nextInt();

        /** all edges **/
        System.out.println("Enter "+ E +" x, y coordinates");
        for (int i = 0; i < E; i++){
            int x = scan.nextInt();
            int y = scan.nextInt();
            g[x].add(y);
        }

        Tarjan t = new Tarjan();        
        System.out.println("\nSCC : ");

        /** print all strongly connected components **/
        List<List<Integer>> scComponents = t.getSCComponents(g);
        System.out.println(scComponents);        
    }    
}