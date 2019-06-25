package src;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Stack;
import javafx.util.Pair;
import java.util.HashMap; 

import src.All_class.*;
import src.SVG_Generation.*;
import src.Utilities.*;

public class DetectShape {


    //Sorts the Pair<Integer, Double> list in increasing order of second argument
    public void sortPair(ArrayList<Pair<Integer, Double> > l)
    {
        l.sort(new Comparator<Pair<Integer, Double>>() {
        @Override
        public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
            if (o1.getValue() > o2.getValue()) {
                return 1;
            } else if (o1.getValue().equals(o2.getValue())) {
                return 0; 
            } else {
                return -1;
            }
        }
        });
    }  

    //Sorts the list of integer array in decreasing order of their length
    public void sortArray(ArrayList<int[] > c)
    {
        c.sort(new Comparator<int[]>() {
        @Override
        public int compare(int[] o1, int[] o2) {
            if (o1.length > o2.length) {
                return -1;
            } else if (o1.length==o2.length) {
                return 0; 
            } else {
                return 1;
            }
        }
        });
    }  


    //find anlge of all vertices w.r.t. the bottom left vertex in increasing order
    public void findAnglePivot(int[] cycle_vertex, int cycle_length, ArrayList<Pair<Integer, Double> > angles, double minx, double miny, ArrayList<Vertex> vertices)
    {
        // storing angle of all other vertices with the bottom-left vertex
        for(int i = 0; i<cycle_length; i++)
        {
            Vertex v = vertices.get(cycle_vertex[i]);
            angles.add(new Pair<Integer, Double>(v.id, Math.abs(Math.atan2((v.y-miny), (v.x-minx)))));
        }
        // sort on angles
        sortPair(angles);   
        double dist1, dist2;
        int index;

        // if 2 vertices are at the same angle, find the vertex which is farthest and remove the other vertex from angles list
        for(int i = 0; i<cycle_length-1; i++)
        {
            if(angles.get(i).getValue() == angles.get(i+1).getValue())
            {
                Vertex v2 = Utilities.getVertexWithId(vertices, angles.get(i+1).getKey());
                Vertex v1 = Utilities.getVertexWithId(vertices, angles.get(i).getKey());
                
                dist1 = Math.sqrt(Math.abs(v1.x - minx)*Math.abs(v1.x - minx)
                    +Math.abs(v1.y - miny)*Math.abs(v1.y - miny) );

                dist2 = Math.sqrt(Math.abs(v2.x - minx)*Math.abs(v2.x - minx)
                    +Math.abs(v2.y - miny)*Math.abs(v2.y - miny) );

                if(dist1<dist2)
                    index = i+1;
                else 
                    index = i;
                
                Pair<Integer, Double> p = new Pair<Integer, Double>(angles.get(index).getKey(), angles.get(index).getValue());
                angles.remove(p);
            } 
        }
    }


    //  create a path array with reversed order such that it starts with the smallest node
    static int[] invert(int[] path, int smallest_vertex)
    {
        int[] p = new int[path.length];

        for (int i = 0; i < path.length; i++)
        {
            p[i] = path[path.length - 1 - i];
        }

        return normalize(p, smallest_vertex);
    }

    //  rotate cycle path such that it begins with the smallest node
    static int[] normalize(int[] path, int smallest_vertex)
    {
        int[] new_path = new int[path.length];
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

    //check if the number of edges in convex hull(stack contents) is same as the loop  and the edges are in order as the polygon(returned by dfs in cycle_vertex)
    public boolean inOrder(Stack<Integer> stack, int[] cycle_vertex, int[] cur_cycle)
    {
        int [] path = new int[cycle_vertex.length], cycle_vertex_temp = new int[cycle_vertex.length];
        // System.out.println("Stack contents");
        
        //store the convex hull in path[]\
        //Note: stack bottom is the bottom-most-left-most element and others are listed in anticlockwise manner
        //Path: is inverse of this
        for(int i=0; stack.size()!=0; i++)
        {
            path[i]=stack.pop();
            // System.out.print(path[i]+" ");
        }
        // System.out.println("");
        path = invert(path, path[path.length-1]);
        // for(int i=0; i<path.length; i++)
        // {
        //     System.out.print(path[i]+" ");
        // }
        // System.out.println("");

        int[] cycle_edge = new int[cycle_vertex.length];
        for(int i = 0; i< cycle_vertex.length; i++)
        {
            cycle_edge[i] = cur_cycle[i + cycle_vertex.length];
        }

        //rotate and invert cycle_vertex according to the order and smallest vertex in path
        for(int i = 0; i<cycle_vertex.length; i++ )
        {
            // System.out.println("cycle_vertex[i]: "+cycle_vertex[i]);

            //Smallest vertex found in cycle_vertex
            if(path[0] == cycle_vertex[i])
            {

                //if the next vertex is at position+1, then just rotate
                //if the next vertex is at position-1, then rotate and invert
                int n = (cycle_vertex.length+i+1)%cycle_vertex.length;
                int p = (cycle_vertex.length+i-1)%cycle_vertex.length;

                if(path[1] == cycle_vertex[n])
                {
                    //rotate the cycle_vertex such that path[0] is the first element
                    cycle_vertex_temp = normalize(cycle_vertex, path[0]); 
                    cycle_edge = normalize(cycle_edge, cycle_edge[i]);
                }
                else if(path[1] == cycle_vertex[p])
                {
                    //invert and then rotate the cycle_vertex such that path[0] is the first element
                    cycle_vertex_temp = invert(cycle_vertex, path[0]);
                    cycle_edge = invert(cycle_edge, cycle_edge[p]);
                }
                break;
            }
        }

        

        // System.out.println("after rotation in order");
        // for(int i = 0; i<cycle_vertex.length;i++)
        // {
        //     System.out.print(cycle_vertex[i]+", ");
        // }
        // System.out.println(" ");
        //compare element-wise, if convex hull and the polygon contents are same
        //if at any point there is a mismatch, then this is not a convex polygon
        for(int i = 0; i<cycle_vertex_temp.length; i++)
        {
            if(cycle_vertex_temp[i] != path[i])
                return false;
        }
        for(int i = 0; i<cycle_vertex.length; i++)
        {
            cycle_vertex[i] = cycle_vertex_temp[i];
            cur_cycle[i] = cycle_vertex_temp[i];
            cur_cycle[i+cycle_vertex.length] = cycle_edge[i];
        }
        return true;
    }


    //Graham Scan Algorithm to Check convexity
    public boolean grahamScan(ArrayList<Vertex> vertices, int[] cycle_vertex, int cycle_length, double minx, double miny, int ind, int[] cur_cycle)
    {
        ArrayList<Pair<Integer, Double> > angles = new ArrayList<Pair<Integer, Double>>();
        //finds angles of all vertices corresponding to the bottom left point
        findAnglePivot(cycle_vertex, cycle_length, angles, minx, miny, vertices);
        // for(int i = 0; i<angles.size(); i++)
        //     System.out.println(angles.get(i).getKey()+" "+(180/3.14)*angles.get(i).getValue());
        // System.out.println("angles.size(): "+angles.size());
        if(angles.size()<3)
            return false;
        double x1, x2, x3, y1, y2, y3, val =0;
        Vertex v1, v2=new Vertex(), v3=new Vertex();
        int count=0, temp_vertex=0 ;
        Stack<Integer> stack = new Stack<Integer>();//will store vertex id
        //insert bottom-left point in stack
        stack.push(ind);
        int k = 0, cnt = 0;
        // push another vertices in stack
        for(k=0; cnt<1 && k<angles.size(); k++)
        {
        	if(angles.get(k).getKey()!=ind)
            {
                stack.push(angles.get(k).getKey());
                // System.out.println(angles.get(k).getKey()+" k:"+k);
                cnt++;
            }
        }
        // System.out.println("k: "+k);

        //Graham Scan loop
        for(int i=k; i<cycle_length; i++)
        {
            while(val<=0 && i<cycle_length)
            {
                // temp_vertex stores the second vertex, pop it
                temp_vertex = stack.pop();
                // v2 is the 2nd vertex
                v2 =Utilities.getVertexWithId(vertices, temp_vertex);
                // left turns in a concave loop will render the stack empty since no popped vertices will be pushed back into it, hence return false
                // System.out.println("stack.empty(): "+stack.empty());
                if(stack.empty())
                    return false;
                // v1 is the first vertex
                v1 = Utilities.getVertexWithId(vertices, stack.peek());

                // System.out.println("i0: "+i);
                // if bottom left vertex is found again, ignore it- not sure if this condition will ever arise
                while(angles.get(i).getKey()==ind)
                    i++;
                // System.out.println("i1: "+i);

                // v3 is the vertex being looked at in the for loop
                v3 = Utilities.getVertexWithId(vertices, angles.get(i).getKey());
                // System.out.println("v3.id: "+v3.id);

                // finding determinant to check if left turn is being formed by v1v2v3
                val = (v2.y - v1.y) * (v3.x - v2.x) - (v2.x - v1.x) * (v3.y - v2.y);
                // if right turn is formed, push back v2v3
                // System.out.println("val: "+val+" "+v1.id+" "+v2.id+" "+v3.id);
                
            }
            if (val > 0) 
            {
                stack.push(v2.id);
                stack.push(v3.id);
                // System.out.println("stack.size(): "+stack.size());
                
            }
            // set val to 0 and check for next vertex
            val = 0; 
            
        }

        //check if the number of edges in convex hull is same as the loop  and the edges are in order as the polygon

        // System.out.println("before rotation");
        // for(int i = 0; i<cur_cycle.length;i++)
        // {
        //     System.out.print(cur_cycle[i]+", ");
        // }
        // System.out.println(" ");
        Boolean b = inOrder(stack, cycle_vertex, cur_cycle);

        if( b == false)
        {
            int[] cycle_edge = new int [cycle_vertex.length];
            int[] cycle_vertex_temp = new int [cycle_vertex.length];
            for(int i = 0; i<cycle_vertex.length; i++)
            {
                cycle_edge[i] = cur_cycle[i+cycle_vertex.length];
            }
            for(int i = 0; i<cycle_vertex.length; i++)
            {
                if(cycle_vertex[i] == ind)
                {
                    int n = (cycle_vertex.length+i+1)%cycle_vertex.length;
                    int p = (cycle_vertex.length+i-1)%cycle_vertex.length;

                    Vertex vp = Utilities.getVertexWithId(vertices, cycle_vertex[p]);
                    Vertex vn = Utilities.getVertexWithId(vertices, cycle_vertex[n]);
                    Vertex vc = Utilities.getVertexWithId(vertices, cycle_vertex[i]);
                    
                    double a1 = Math.abs(Math.atan2((double)(vn.y-vc.y), (double)(vn.x-vc.x)));
                    double a2 = Math.abs(Math.atan2((double)(vp.y-vc.y), (double)(vp.x-vc.x)));

                    if(a1<a2)
                    {
                        cycle_vertex_temp = normalize(cycle_vertex, cycle_vertex[i]);
                        cycle_edge = normalize(cycle_edge, cycle_edge[i]);
                    }
                    else
                    {
                        cycle_vertex_temp = invert(cycle_vertex, cycle_vertex[i]);
                        cycle_edge = invert(cycle_edge, cycle_edge[p]);
                    }
                    break;
                }
            }
            for(int i = 0; i<cycle_vertex.length; i++)
            {
                cycle_vertex[i] = cycle_vertex_temp[i];
                cur_cycle[i] = cycle_vertex_temp[i];
                cur_cycle[i+cycle_vertex.length] = cycle_edge[i];
            }
        }


        // System.out.println("after rotation");
        // for(int i = 0; i<cur_cycle.length;i++)
        // {
        //     System.out.print(cur_cycle[i]+", ");
        // }
        // System.out.println(" ");
        // for(int i = 0; i<cycle_vertex.length;i++)
        // {
        //     System.out.print(cycle_vertex[i]+", ");
        // }
        // System.out.println(" ");
        return b;

    }

    //checks if the loop is convex and non-crossing
    public boolean isconvex(int[] cycle_vertex, ArrayList<Vertex> vertices, int[] cur_cycle)
    {
        // miny and minx has x-y of first vertex
        int cycle_length = cycle_vertex.length; 
        double miny=vertices.get(cycle_vertex[0]).y, minx=vertices.get(cycle_vertex[0]).x;
        int ind=vertices.get(cycle_vertex[0]).id;
        //finding the bottom-left point
        for(int i = 1; i<cycle_length; i++)
        {
            Vertex v = vertices.get(cycle_vertex[i]);
            if(v.y>miny)//computer coordinate system is different
            {
                miny = v.y;
                minx = v.x;
                ind = v.id;
            }
            else if(v.y == miny && v.x < minx)
            {
                miny = v.y;
                minx = v.x;
                ind = v.id;
            } 
        }

        
        System.out.println("bottom left: "+minx+" "+miny+" "+ind);
        return grahamScan(vertices, cycle_vertex, cycle_length, minx, miny, ind, cur_cycle);
    }

    // copies contents of arraylist<vertex_edge>-table into a 2d array
    public int [][] formEdges(ArrayList<Vertex_Edge> table)
    {
        int Edges[][] = new int [table.size()][3];
        for(int i=0;i<table.size();i++)
        {
            Edges[i][0] = table.get(i).v1;
            Edges[i][1] = table.get(i).v2;
            Edges[i][2] = table.get(i).e;
            // System.out.println("Edges table: "+Edges[i][0]+" "+Edges[i][1]+" "+Edges[i][2]);
        }
        return Edges;  
    }

    public double perpendicular_dist_segment(Vertex p1, Vertex p2, Circle c)
    {
        double vx = p2.x - p1.x, vy = p2.y - p1.y;
        double cpx = c.x - p1.x, cpy = c.y - p1.y;
        double t = ((cpx * vx)+(cpy * vy))/(vx*vx + vy*vy);
        double d = (cpx - t*vx) * (cpx - t*vx) + (cpy - t*vy) * (cpy - t*vy);
        if(t>0 && t <1)    
            return Math.sqrt(d);
        else return Double.MAX_VALUE;

    }

    //Associates lines that are radius/diameter of a circle
    public void circleAssociations(Circle cir, ArrayList<Line> lines, ArrayList<Vertex> vertices, HashMap<Integer, Integer> lines_visited, ArrayList<Associated_Line> associations, HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> tangents, int sid)
    {
        for(int i = 0; i < lines.size(); i++)
        {
            // if(lines_visited.get(lines.get(i).id) != 1)
            {
                Line l = lines.get(i);
                Vertex v1 = Utilities.getVertexWithId(vertices, l.v1);
                Vertex v2 = Utilities.getVertexWithId(vertices, l.v2);
                double d1 = Math.sqrt((v1.x - cir.x)*(v1.x - cir.x) + (v1.y - cir.y)*(v1.y - cir.y));
                double d2 = Math.sqrt((v2.x - cir.x)*(v2.x - cir.x) + (v2.y - cir.y)*(v2.y - cir.y));
                double length = Math.sqrt((v2.x - v1.x)*(v2.x - v1.x) + (v2.y - v1.y)*(v2.y - v1.y));

                double a = (v1.y-v2.y);
                double b = (v2.x-v1.x);
                double c = v1.x*(v2.y-v1.y)-v1.y*(v2.x-v1.x);
                double num = Math.abs((cir.x*a)+(cir.y*b)+c);
                double den = Math.sqrt((a*a)+(b*b));
                double per_dist = perpendicular_dist_segment(v1, v2, cir);//num/den;

                // System.out.println("D1: "+d1+" d2 "+d2+" length "+length+" c "+cir.id+" line "+l.id);

                //if v1 is near the circumference and v2 is near the center
                if(Math.abs(cir.r - d1) <= 10 && d2 <= 10)
                {
                    associations.add(new Associated_Line(l.id, "Radius", l.v2, l.v1, "Center", "Vertex"));
                    lines_visited.put(l.id, 1);
                }
                //if v2 is near the circumference and v1 is near the center
                else if(Math.abs(cir.r - d2) <= 10 && d1 <= 10)
                {
                    associations.add(new Associated_Line(l.id, "Radius", l.v1, l.v2, "Center", "Vertex"));
                    lines_visited.put(l.id, 1);
                }
                //if v1 , v2 are near the circumference
                else if(Math.abs(cir.r - d1) <= 6 && Math.abs(cir.r - d2) <= 6)
                {
                    //if length = 2*r 
                    double mx = (v2.x - v1.x)/2, my = (v2.y - v1.y)/2;
                    double md = Math.sqrt((cir.x - mx)*(cir.x - mx) + (cir.y - my)*(cir.y - my));
                    if(Math.abs(length - 2*cir.r) <= 12 && md<=6)
                    {
                        associations.add(new Associated_Line(l.id, "Diameter", l.v1, l.v2, "Vertex", "Vertex"));
                        lines_visited.put(l.id, 1);
                    }
                    else if(length < 2*cir.r && lines_visited.get(l.id)!=1 && per_dist < cir.r)
                    {
                        associations.add(new Associated_Line(l.id, "Chord", l.v1, l.v2, "Vertex", "Vertex"));
                        lines_visited.put(l.id, 1);
                    }
                }
                else if(Math.abs(per_dist - cir.r) <= 12)
                {
                    associations.add(new Associated_Line(l.id, "Tangent", -1, -1, "", ""));
                    lines_visited.put(l.id, 1);

                    tangents.put(new Pair<Integer, Integer> (cir.id, l.id), new Pair<Integer, Integer> (sid, associations.size()-1));
                }
                System.out.println("per_dist: "+per_dist+" "+(cir.r));
            }
        }
    }

    //finds all convex shapes
    public void findConvexPolygon(ArrayList<int[]> cycles, ArrayList<Line> lines, ArrayList<Circle> circles, ArrayList<Vertex> vertices, ArrayList<Shape> shapes, HashMap<Integer, Integer> lines_visited)
    {
        

        int num_cycles = cycles.size();

        for (int id = 0; id < num_cycles; id++)
        {
            int cycle_length = cycles.get(id).length/2;
            int [] cycle_vertex = new int [cycle_length]; //only the vertices
                
            int numVisitedLines = 0, lineFound = 0;
            //this loop counts the number of edges in the current loop that have already been covered in previous loops
            for(int i = 0; i<cycle_length; i++)
            {
                lineFound = 0;
                // cycle_vertex[i] stores the ith vertex id of the loop
                cycle_vertex[i] = cycles.get(id)[i];

                // see if current edge has already been visited
                for(int j = 0; j<lines_visited.size(); j++)
                {
                    if(lines_visited.get(cycles.get(id)[i+cycle_length]) == 1)
                    {
                        lineFound = 1;
                        break;
                    }
                }
                //if line is visited, increment count of number of visited lines
                numVisitedLines += lineFound;
            }
            // System.out.println("numVisitedLines: "+numVisitedLines);

            // if there is even a single edge which have not been covered in earlier loops, then consider the current loop, else ignore
            if(numVisitedLines<cycle_length)
            {
                Shapes s = new Shapes();
                //if shape is convex and a non crossing polygon the identify its shape
                if(isconvex(cycle_vertex, vertices, cycles.get(id)))
                {
                    System.out.println("Convex");
                    s.identify_shape(cycles.get(id), lines, shapes, vertices, lines_visited, cycle_vertex, cycle_length, true);
                }
            }
            
        }


    }

    public void findNonConvexPolygon(ArrayList<int[]> cycles, ArrayList<Line> lines, ArrayList<Circle> circles, ArrayList<Vertex> vertices, ArrayList<Shape> shapes, HashMap<Integer, Integer> lines_visited)
    {

        int num_cycles = cycles.size();

        for (int id = 0; id < num_cycles; id++)
        {
            int cycle_length = cycles.get(id).length/2;
            int [] cycle_vertex = new int [cycle_length]; //only the vertices
                
            int numVisitedLines = 0, lineFound = 0;
            //this loop counts the number of edges in the current loop that have already been covered in previous loops
            for(int i = 0; i<cycle_length; i++)
            {
                lineFound = 0;
                // cycle_vertex[i] stores the ith vertex id of the loop
                cycle_vertex[i] = cycles.get(id)[i];

                // see if current edge has already been visited
                for(int j = 0; j<lines_visited.size(); j++)
                {
                    if(lines_visited.get(cycles.get(id)[i+cycle_length]) == 1)
                    {
                        lineFound = 1;
                        break;
                    }
                }
                //if line is visited, increment count of number of visited lines
                numVisitedLines += lineFound;
            }
            // System.out.println("numVisitedLines: "+numVisitedLines);

            // if there are >= 2 edges which have not been covered in earlier loops, then consider the current loop, else ignore
            // System.out.println("numVisitedLines: "+numVisitedLines+" cycle_length "+cycle_length);
            if(numVisitedLines<cycle_length && numVisitedLines < cycle_length-1)
            {
                Shapes s = new Shapes();
                //if shape is convex and a non crossing polygon the identify its shape
                if(!isconvex(cycle_vertex, vertices, cycles.get(id)))
                {
                    System.out.println("Not Convex");
                    s.identify_shape(cycles.get(id), lines, shapes, vertices, lines_visited, cycle_vertex, cycle_length, false);
                }
            }
            
        }
    }
    
    public void findStandAlone(ArrayList<int[]> cycles, ArrayList<Line> lines, ArrayList<Circle> circles, ArrayList<Vertex> vertices, ArrayList<Shape> shapes, HashMap<Integer, Integer> lines_visited, 
        HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> tangents)

    {

        //add the circles as stand alone shapes with associated lines
        for(int i = 0; i < circles.size(); i++)
        {
            ArrayList<Associated_Line> associations = new ArrayList<Associated_Line>();

            //associated radius and diameter
            circleAssociations(circles.get(i), lines, vertices, lines_visited, associations, tangents, shapes.size());

            // shape_component will be the circle
            ArrayList<Integer> circle_component = new ArrayList<Integer>();
            circle_component.add(circles.get(i).id);
            
            // shape_angles for circle is stored as zero
            ArrayList<AngleBetweenComponents> shape_angles = new ArrayList<AngleBetweenComponents>();
            double a1= 0;
            shape_angles.add(new AngleBetweenComponents(-1, -1, -1, a1));

            shapes.add(new Shape("Circle", new ArrayList<Integer>(), circle_component, shape_angles, new int[]{}, associations));    
            // System.out.println("tangent insertion: "+circles.get(i).id+" "+tangents_list.size()+" "+(shapes.size()-1));
        }
        
        // Add stand alone lines as shapes
        for(int i=0;i<lines.size();i++)
        {
            Line l=lines.get(i);
            int b=0;
            // check if the line is visited or not
            for(int j=0;j<lines_visited.size();j++)
            {
                if(lines_visited.get(l.id) == 1)
                {
                    b=1;
                    break;
                }
            }
            //if the line is not visited, add it as a stand alone shape
            if(b == 0)
            {
                
                ArrayList<Associated_Line> associations = new ArrayList<Associated_Line>();
                
                // shape_component stores the line itself
                ArrayList<Integer> line_component = new ArrayList<Integer>();
                line_component.add(l.id);
                
                // shape_angles stores slope of line in degrees
                ArrayList<AngleBetweenComponents> shape_angles = new ArrayList<AngleBetweenComponents>();
                double a1 = l.slope; 
                //Math.atan2((double)(l.points[3]-l.points[1]),(double)(l.points[2]-l.points[0]))*(180/Math.PI);
                // if(a1<0)
                //     a1 += 180;
                System.out.println("l.id: "+l.id+" a1: "+a1);
                shape_angles.add(new AngleBetweenComponents(l.id, -1, -1, a1));
                
                // v stores end points
                int [] v = new int [2];
                v[0] = l.v1;
                v[1] = l.v2;
                // adding to shapes
                shapes.add(new Shape("Line", line_component, new ArrayList<Integer>(), shape_angles, v, associations));
            }
        }
    }

    public void detectShape(ArrayList<Line> lines, ArrayList<Circle> circles, ArrayList<Vertex> vertices, ArrayList<Vertex_Edge> table, ArrayList<Shape> shapes, String fname, String svgfile, 
        HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> tangents) throws FileNotFoundException
    {
        
        //prinitng information to template file
        PrintStream o1 = new PrintStream(new File(fname));
        // Store current System.out before assigning a new value
        PrintStream console = System.out;
        // Assign o to output stream
        System.setOut(o1);


        //converting values in table of form ArrayList<Vertex_Edge> to 2D array Edges
        int Edges[][] = formEdges(table); // each edge is of the form(u, v, edge_id)
        Graph g = new Graph(Edges);
        ArrayList<int[]> cycles = g.findloop();//each element in a cyclesarray is a list of n/2 Verices and rest n/2 of Line ID
       
        //This sorts cycles based on the length of the integer arrays in descending order
        sortArray(cycles);
        // System.out.println("sorted list length: "+cycles.size());


        //mask to check which line is visited
        HashMap<Integer, Integer> lines_visited = new HashMap<>(); 
        for(int i=0;i<lines.size();i++)
            lines_visited.put(lines.get(i).id, 0);
        //finds and stores all convex polygons
        findConvexPolygon(cycles, lines, circles, vertices, shapes, lines_visited);
        //finds and stores all non-convex polygons
        findNonConvexPolygon(cycles, lines, circles, vertices, shapes, lines_visited) ;

        //stores all stand-alone lines and circles
        findStandAlone(cycles, lines, circles, vertices, shapes, lines_visited, tangents) ;
        

        System.out.println("Number of shapes formed: "+shapes.size());
        for(int i=0;i<shapes.size();i++)
        {
            System.out.println("\n"+shapes.get(i).id+" "+shapes.get(i).shape_name);
            
            System.out.println("Vertices:");
            for(int j=0;j<shapes.get(i).vertices.length;j++)
            {
                System.out.println(shapes.get(i).vertices[j]);
            }
            System.out.println("Components:");
            for(int j=0;j<shapes.get(i).line_components.size();j++)
            {
                System.out.println(shapes.get(i).line_components.get(j));
            }
            System.out.println("Angles:");
            for(int j=0;j<shapes.get(i).angles.size();j++)
            {
                System.out.println(shapes.get(i).angles.get(j).cid1+" "+shapes.get(i).angles.get(j).cid2+" "+shapes.get(i).angles.get(j).vid+" "+shapes.get(i).angles.get(j).angle);
            }
            System.out.println("Associations:");
            for(int j=0;j<shapes.get(i).associations.size();j++)
            {
                System.out.println(shapes.get(i).associations.get(j).line_id+" "+shapes.get(i).associations.get(j).description);
            }
        }

        System.setOut(console);

        
            
        
        //SVG_Generation svg_gen = new SVG_Generation();
        //svg_gen.generateSVG(shapes, svgfile, lines, circles);
        
        
    }
}
