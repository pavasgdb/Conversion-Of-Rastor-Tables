package src;


import java.util.ArrayList;
import java.util.List;

public class Graph {

    //  Graph modeled as list of edges (u, v, edge_id)
    static int[][] graph =
        {
            {1, 2, 1}, {1, 3, 2}, {1, 4, 3}, {2, 3, 4},
            {3, 4, 5}, {2, 6, 6}, {4, 6, 7}, {7, 8, 8},
            {8, 9, 9}, {9, 7, 10}
        };

    //list that store all cycles
    static ArrayList<int[]> cycles = new ArrayList<int[]>();

    // Constructor to form graph with the input set of edges
    public Graph(int [][] Edges)
    {
        Graph.graph = Edges;
    }
    
    public int[] get_edge_ids(int[] cycle_vertex, int n)
    {
        int[] cycle_edge= new int[n];

        // finding edges between vertices which form the loop
        for(int i=0;i<n-1; i++)
        {
            for(int j=0;j<graph.length;j++)
                if((graph[j][0]==cycle_vertex[i] && graph[j][1]==cycle_vertex[i+1])||
                    (graph[j][1]==cycle_vertex[i] && graph[j][0]==cycle_vertex[i+1]))
                {
                    cycle_edge[i]=(graph[j][2]);
                    System.out.println("edge: "+ graph[j][2]+ " Vertices: "+cycle_vertex[i]+", " +cycle_vertex[i+1]);
                    break;
                }
                
        }
        // finding the closing edge of the loop
        for(int j=0;j<graph.length;j++)
            if((graph[j][0]==cycle_vertex[n-1] && graph[j][1]==cycle_vertex[0])||
                (graph[j][1]==cycle_vertex[n-1] && graph[j][0]==cycle_vertex[0]))
            {
                cycle_edge[n-1]=(graph[j][2]);
                System.out.println("edge: "+ graph[j][2]+ " Vertices: "+cycle_vertex[n-1]+", " +cycle_vertex[0]);
                // System.out.println("edge: "+ graph[j][2]);
            }
        return cycle_edge;
    }




    //  check of both arrays have same lengths and contents
    static Boolean equals(int[] a, int[] b)
    {
        Boolean ret = (a[0] == b[0]) && (a.length == b.length);

        for (int i = 1; ret && (i < a.length); i++)
        {
            if (a[i] != b[i])
            {
                ret = false;
            }
        }

        return ret;
    }

    //  create a path array with reversed order such that it starts with the smallest node
    static int[] invert(int[] path)
    {
        int[] p = new int[path.length];

        for (int i = 0; i < path.length; i++)
        {
            p[i] = path[path.length - 1 - i];
        }

        return normalize(p);
    }

    //  rotate cycle path such that it begins with the smallest node
    static int[] normalize(int[] path)
    {
        int[] new_path = new int[path.length];
        int smallest_vertex = smallest(path); //returns the smallest node in the cycle
        int next_vertex;

        System.arraycopy(path, 0, new_path, 0, path.length);

        //rotate the new_path until the smallest vertex is the first entry in the new_path
        while (new_path[0] != smallest_vertex)
        {
            next_vertex = new_path[0];
            System.arraycopy(new_path, 1, new_path, 0, new_path.length - 1);
            new_path[new_path.length - 1] = next_vertex;
        }

        return new_path;
    }

    //  compare path against known cycles
    //  return true, iff path is not a known cycle
    static Boolean isNew(int[] path)
    {
        Boolean ret = true;

        for(int[] p : cycles)
        {
            if (equals(p, path)) //checks if any known cycle matches path
            {
                ret = false;
                break;
            }
        }

        return ret;
    }


    //  return the smallest array element
    static int smallest(int[] path)
    {
        int min = path[0];

        for (int p : path)
        {
            if (p < min)
            {
                min = p;
            }
        }

        return min;
    }

    //  check if vertex v is contained in path
    static Boolean visited(int v, int[] path)
    {
        Boolean ret = false;
        for (int p : path)
        {
            if (p == v)
            {
                ret = true;
                break;
            }
        }
        return ret;
    }

    static void findNewCycles(int[] path)
    {
        int cur_vertex = path[0]; // current vertex
        int next_vertex;  // next neighbour of cur_vertex
        int[] sub_path = new int[path.length + 1]; 

        //dfs
        for (int i = 0; i < graph.length; i++) //loop on number of edges
            for (int y = 0; y <= 1; y++)//loop on (u,v)
                // traverse the graph to reach cur_vertex
                if (graph[i][y] == cur_vertex)
                {
                    //on reaching current vertex, neighbour is stored in next_vertex.
                    next_vertex = graph[i][(y + 1) % 2];
                    // if next_vertex is not visited, make a recursive call on next_vertex and append next_vertex in the path beginning
                    if (!visited(next_vertex, path))
                    //  neighbor node not on path yet
                    {
                        sub_path[0] = next_vertex;
                        //Parameters: (Object source_arr, int sourcePos, Object dest_arr, int destPos, int len)
                        System.arraycopy(path, 0, sub_path, 1, path.length);
                        //sub_path = [next_vertex, v_n-1, v_n, ... v0] 
                        //  explore extended path
                        findNewCycles(sub_path);
                    }
                    // if it is visited, check if the path length greater than 2 (condition for cycle) and first vertex of path is last vertex (next_vertex == v0)
                    else if ((path.length > 2) && (next_vertex == path[path.length - 1]))
                    //  cycle found
                    {
                        // System.out.println("cycle found "+path[0]+" "+path[1]);
                        int[] new_path = normalize(path); //rotates the path such that it starts with smallest  node
                        int[] inv = invert(new_path); //inverted path starting with smallest node (e.g., abca -> acba)
                        if (isNew(new_path) && isNew(inv))
                        {

                            cycles.add(new_path); //insert the new cycle in the set of cycles
                            System.out.println("path "+new_path[0]+", "+new_path[1]);
                        }
                    }
                }
    }
    public ArrayList<int[]> findloop() {

        // nested loop finds all the cycles
        for (int i = 0; i < graph.length; i++) // loop on number of edges
            for (int j = 0; j < 2; j++) //loop on (u, v)
            {
                findNewCycles(new int[] {graph[i][j]});
            }
        
        //formatting the data structure
        ArrayList<int[]> final_cycles= new ArrayList<int[]>();
        int j=1;
        System.out.println("number of cycles: "+cycles.size());
        // for each cycle
        for (int[] cycle_vertex : cycles)
        {
            int n = cycle_vertex.length;
            int[] cycle_edge;
            int[] cycle_array=new int [2*n]; //cycle_array = cycle_vertex + cycle_edge

            System.out.println("Cycle "+ (j)+ ":");
            j+=1;
            //cycle_edge stores the edge ids of the cycle in order
            cycle_edge = get_edge_ids(cycle_vertex, cycle_vertex.length);

            String s = "" + cycle_vertex[0];
            for (int i = 1; i < cycle_vertex.length; i++)
                s += "," + cycle_vertex[i];

            // cycle_array[i] till 0 to n-1 stores the vertices in cycle
            for(int i=0;i<n;i++)
                cycle_array[i]=cycle_vertex[i];
            // f[i] from n to 2n-1  stores the edges of loop
            for(int i=n;i<2*n;i++)
                cycle_array[i]=cycle_edge[i-n];

            final_cycles.add(cycle_array);

            // System.out.println(s);
            
        }
        return final_cycles;

    }
}
