package src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import src.All_class.*;
import src.Utilities.*;
import java.util.HashMap; 
import java.util.Map; 

public class ShapeIntersectionAssociation {

    class DisjointUnionSets 
    { 
        int[] rank, parent; 
        int n; 

        // Constructor 
        public DisjointUnionSets(int n) 
        { 
            rank = new int[n]; 
            parent = new int[n]; 
            this.n = n; 
            makeSet(); 
        } 

        // Creates n sets with single item in each 
        void makeSet() 
        { 
            for (int i=0; i<n; i++) 
            { 
                // Initially, all elements are in 
                // their own set. 
                parent[i] = i; 
            } 
        } 

        // Returns representative of x's set 
        int find(int x) 
        { 
            // Finds the representative of the set 
            // that x is an element of 
            if (parent[x]!=x) 
            { 
                // if x is not the parent of itself 
                // Then x is not the representative of 
                // his set, 
                parent[x] = find(parent[x]); 

                // so we recursively call Find on its parent 
                // and move i's node directly under the 
                // representative of this set 
            } 

            return parent[x]; 
        } 

        // Unites the set that includes x and the set 
        // that includes x 
        void union(int x, int y) 
        { 
            // Find representatives of two sets 
            int xRoot = find(x), yRoot = find(y); 

            // Elements are in the same set, no need 
            // to unite anything. 
            if (xRoot == yRoot) 
                return; 

            // If x's rank is less than y's rank 
            if (rank[xRoot] < rank[yRoot]) 

                // Then move x under y so that depth 
                // of tree remains less 
                parent[xRoot] = yRoot; 

            // Else if y's rank is less than x's rank 
            else if (rank[yRoot] < rank[xRoot]) 

                // Then move y under x so that depth of 
                // tree remains less 
                parent[yRoot] = xRoot; 

            else // if ranks are the same 
            { 
                // Then move y under x (doesn't matter 
                // which one goes where) 
                parent[yRoot] = xRoot; 

                // And increment the the result tree's 
                // rank by 1 
                rank[xRoot] = rank[xRoot] + 1; 
            } 
        } 
    }   

    //returns list of shapes in which id is a line/circle
    ArrayList<Integer> find_shape(int id, ArrayList<Shape> shapes)
    {
        ArrayList<Integer> sid = new ArrayList<Integer>();
        for(int i = 0; i < shapes.size(); i++)
        {
            for(int j = 0 ; j<shapes.get(i).line_components.size(); j++)
            {
                // if found return the shape id
                if(id == shapes.get(i).line_components.get(j))
                    sid.add(shapes.get(i).id);
            }
            for(int j = 0 ; j<shapes.get(i).circle_components.size(); j++)
            {
                // if found return the shape id
                if(id == shapes.get(i).circle_components.get(j))
                    sid.add(shapes.get(i).id);
            }

            for(int j = 0 ; j<shapes.get(i).associations.size(); j++)
            {
                // System.out.println("Association: "+shapes.get(i).associations.get(j).line_id);
                // if found return the shape id
                if(id == shapes.get(i).associations.get(j).line_id)
                    sid.add(shapes.get(i).id);
            }
        }
        return sid;
    }

    // checks if the intersection point is a line end point- when 2 lines intersect
    void isvertex(ArrayList<Line> lines, double x, double y, int id1, int id2, String t1, String t2, int[] v, int isVertexThresh)
    {
        if(t1 == "Line")
        {
            Line l1 = Utilities.getLineWithId(lines, id1);
            double dist1=Math.sqrt(Math.abs(x-l1.points[0])*Math.abs(x-l1.points[0])+Math.abs(y-l1.points[1])*Math.abs(y-l1.points[1]));
            double dist2=Math.sqrt(Math.abs(x-l1.points[2])*Math.abs(x-l1.points[2])+Math.abs(y-l1.points[3])*Math.abs(y-l1.points[3]));
            if(dist1<isVertexThresh)//if (x==l1.points[0] && y ==l1.points[1])
                v[0] = l1.v1;
            else if(dist2<isVertexThresh)//if (x==l1.points[2] && y ==l1.points[3])
                v[0] = l1.v2;
        }
        if(t2 == "Line")
        {
            Line l2 = Utilities.getLineWithId(lines, id2);
            double dist3=Math.sqrt(Math.abs(x-l2.points[0])*Math.abs(x-l2.points[0])+Math.abs(y-l2.points[1])*Math.abs(y-l2.points[1]));
            double dist4=Math.sqrt(Math.abs(x-l2.points[2])*Math.abs(x-l2.points[2])+Math.abs(y-l2.points[3])*Math.abs(y-l2.points[3]));
            
            
            if(dist3<isVertexThresh)//if (x==l2.points[0] && y ==l2.points[1])
                v[1] = l2.v1;
            else if(dist4<isVertexThresh)//if (x==l2.points[2] && y ==l2.points[3])
                v[1] = l2.v2;
        }
    }

    ArrayList<Junction> intrShapeAssoc(ArrayList<Line> lines, ArrayList<Circle> circles, ArrayList<Shape> shapes, ArrayList<Vertex> vertices, ArrayList<Vertex_Edge> table, ArrayList<Junction> intersections, String fname, HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> tangents, HashMap<Pair<Integer, Integer>, ArrayList<Junction>> shape_wise, HashMap<Pair<Integer, Integer>, Integer> shape_wise_visited, HashMap<Pair<Integer, Integer>, ArrayList<Junction>> primitive_wise, ArrayList<GroupWise> group_wise) throws IOException
    {

        int isVertexThresh = 6;
        PrintStream o2 = new PrintStream(new File(fname));
        System.setOut(o2);

        ArrayList<Junction> junctions = new ArrayList<Junction>(); 
        
        for(int i = 0; i < intersections.size(); i++)
        {
            Junction current = intersections.get(i);
            ArrayList<Integer> sh1_list = find_shape(current.id1, shapes);
            ArrayList<Integer> sh2_list = find_shape(current.id2, shapes);
            String s1=current.type1, s2=current.type2;
            // System.out.println("intersection id "+intersections.get(i).id);

            int[] v = new int []{-1, -1};
            isvertex(lines, current.x, current.y, current.id1, current.id2, s1, s2, v, isVertexThresh);
            int vid = -1;

            // if v[0] != -1 then it is an end point of line0
            if(v[0] != -1)
                vid = v[0];
            // if v[1] != -1 then it is an end point of line1                        
            if(v[1] != -1)
                vid = v[1];

            // id of vertex/ line inolved in intersection id1, id2; intersection point's x-y coordinate
            // s1, s2- "line" or "circle" corresponding to id1, id2; sh1, sh2- ids of shapes involved  
            junctions.add(new Junction(current.id1, current.id2, current.x, current.y, s1, s2, sh1_list, sh2_list, vid, current.junction_ratio1, current.junction_ratio2));

               
        }

        ArrayList<Junction> temp = new ArrayList<Junction>(); 

        DisjointUnionSets dus = new DisjointUnionSets(shapes.size());
        // removing duplicate junctions
        for(int i=0;i<junctions.size();i++)
        {
            Junction current  = junctions.get(i);
            int f = 0;
            for(int j = 0; j<i; j++)
            {
                if(current.x==junctions.get(j).x && current.y==junctions.get(j).y) 
                {
                    if((current.id1==junctions.get(j).id1 && current.id2==junctions.get(j).id2) || (current.id1==junctions.get(j).id2 && current.id2==junctions.get(j).id1))
                     {
                        f=1;
                        break;
                     }   
                }
            }
            if(f!=1)
            {
                temp.add(current);
                // System.out.println("hey there " +current.shapeid1.size()+" "+current.shapeid2.size());
                //filling shape_wise
                for(int j = 0; j< current.shapeid1.size(); j++)
                {
                    for(int k = 0; k < current.shapeid2.size(); k++)
                    {
                        dus.union(current.shapeid1.get(j), current.shapeid2.get(k));
                        Pair<Integer, Integer> p = new Pair<Integer, Integer>(current.shapeid1.get(j), current.shapeid2.get(k));
                        if(shape_wise.containsKey(p))
                        {
                            shape_wise.get(p).add(current);
                        }
                        else
                        {
                            ArrayList<Junction> arr = new ArrayList<Junction>();
                            arr.add(current);
                            shape_wise.put(p, arr);
                            shape_wise_visited.put(p, 0);
                        }
                    }
                }

                //filling primitive wise
                Pair<Integer, Integer> p = new Pair<Integer, Integer>(current.id1, current.id2);
                if(primitive_wise.containsKey(p))
                {
                    primitive_wise.get(p).add(current);
                }
                else
                {
                    ArrayList<Junction> arr = new ArrayList<Junction>();
                    arr.add(current);
                    primitive_wise.put(p, arr);
                }
            }
        }

        HashMap<Integer, ArrayList<Integer>> disconnected = new HashMap<Integer, ArrayList<Integer>>();

        // System.out.println("Disconnected: ");
        for(int i = 0;i<dus.parent.length; i++)
        {
            disconnected.put(dus.parent[i], new ArrayList<Integer>());
            // System.out.println(dus.parent[i]);
        }

        for(int i = 0;i<shapes.size();i++)
        {
            int sid = shapes.get(i).id;
            int x = dus.find(sid);
            disconnected.get(x).add(sid);
        }

        for (Map.Entry<Integer, ArrayList<Integer>> entry : disconnected.entrySet())
        {
            group_wise.add(new GroupWise(entry.getValue()));
        }
        
        for (Map.Entry<Pair<Integer, Integer>, ArrayList<Junction>> entry : shape_wise.entrySet())  
        {
            int s1 = entry.getKey().getKey(), s2 = entry.getKey().getValue();
            for(int i = 0; i<group_wise.size();i++)
            {
                GroupWise group = group_wise.get(i);
                if(group.shape_id.contains(s1) || group.shape_id.contains(s2))
                {
                    group.shape_wise.put(entry.getKey(), entry.getValue());
                }

            }
        }
        // System.out.println("GroupWise: "+group_wise.size());

        System.out.println("intersections.size(): "+temp.size());
        for(int i=0;i<temp.size();i++)
        {
            Junction jn = temp.get(i);
            if(jn.vid == -1)
            {
                for(int j = 0;j < vertices.size(); j++)
                {
                    Vertex v = vertices.get(j);
                    double d = Math.sqrt((v.x-jn.x)*(v.x-jn.x) + (v.y-jn.y)*(v.y-jn.y));
                    if(d < isVertexThresh)
                        jn.vid = v.id;
                }
            }

            if(jn.type1 == "Line")
            {
                Line l1 = Utilities.getLineWithId(lines, jn.id1);
                Vertex v1 = Utilities.getVertexWithId(vertices, l1.v1);
                Vertex v2 = Utilities.getVertexWithId(vertices, l1.v2);
                if(jn.vid!=-1)    
                {
                    Vertex v = Utilities.getVertexWithId(vertices, jn.vid);
                    jn.junction_ratio1 = Utilities.junction_ratio(jn, l1, v1, v2, v.x, v.y);
                }
                else
                {
                    jn.junction_ratio1 = Utilities.junction_ratio(jn, l1, v1, v2, jn.x, jn.y);
                }
            }
            if(jn.type2 == "Line")
            {
                Line l2 = Utilities.getLineWithId(lines, jn.id2);
                
                Vertex v1 = Utilities.getVertexWithId(vertices, l2.v1);
                Vertex v2 = Utilities.getVertexWithId(vertices, l2.v2);
                if(jn.vid!=-1)    
                {
                    Vertex v = Utilities.getVertexWithId(vertices, jn.vid);
                    jn.junction_ratio2 = Utilities.junction_ratio(jn, l2, v1, v2, v.x, v.y);
                }
                else
                {
                    jn.junction_ratio2 = Utilities.junction_ratio(jn, l2, v1, v2, jn.x, jn.y);
                }
            }


            // System.out.println(jn.type1+" "+jn.type2+" "+tangents.containsKey(jn.id1));
            if(jn.type1 == "Circle" && jn.type2 == "Line")
            {
                Pair<Integer, Integer> p = new Pair<Integer, Integer>(jn.id1, jn.id2);
                if(tangents.containsKey(p))
                {
                    // System.out.println("inside tangents");
                    Shape s = Utilities.getShapeWithId(shapes, tangents.get(p).getKey());
                    Associated_Line a = s.associations.get(tangents.get(p).getValue());
                    a.v1_map = jn.id;
                    a.type1 = "Junction";
                }
            }

             // storing info in template file
            System.out.println("JunctionID: "+jn.id+" "+jn.type1+" ID1: "+jn.id1+" "+jn.type2+" ID2: "+jn.id2+" x: "+jn.x+" y: "+jn.y+" vid: "+jn.vid+" ratio1: "+jn.junction_ratio1+" ratio2: "+jn.junction_ratio2);
        }
        return temp;
    }
}
