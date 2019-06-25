package src;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import java.io.OutputStreamWriter;

import javafx.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Comparator;
import java.lang.*;
import src.All_class.*;//Line;
import src.Utilities.*;
import java.util.HashMap; 
import java.util.Map; 

public class Average_Junction
{
    double min_dist = 14;
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
            ///System.out.println("x= "+x+"  y= "+y);
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

    double[] dimension_wise_distance_between_points(double[] p0, double[] p1){
        //returns [x1-x2, y1-y2]
        return new double[] {Math.abs(p0[0]-p1[0]), Math.abs(p0[1]-p1[1])};
    }

    double distance_between_points(double[] p0, double[] p1){
        //finds Eucledian distance between p0 and p1
        double[] a = dimension_wise_distance_between_points(p0, p1);
        double norm= a[0]*a[0] + a[1]*a[1];
        return Math.sqrt(norm);
    }

    double line_length(Line line){
        // finds length of line by finding distance between its end points
        return distance_between_points(new double[]{line.points[0], line.points[1]},  new double[] {line.points[2], line.points[3]});
    }

    double[] line_vector(Line line){
        //returns [x1-x0, y1-y0]
        double[] array = new double [] {line.points[2] - line.points[0], line.points[3]- line.points[1]};
        return array;
    }


    double[] line_unit_vector(Line line){
        //divides the vector by the line length
        double[] array = line_vector(line);
        double ll = line_length(line);
        array[0]/=ll;
        array[1]/=ll;
        return array;
    }


    double[] line_normal_vector(Line line){
        //vector perpendicular to the line unit vector in x-y plane
        double[] unit_vector = line_unit_vector(line);
        return new double[] {unit_vector[1], -unit_vector[0]};
    }

    double distance_between_line_and_point(Line line, double[] point)
    {    
        // intr is midpoint of line "line"
        double[] intr = new double [] {(line.points[0] + line.points[2])/2.0, (line.points[1] + line.points[3])/2.0};
        
        double[] vector = new double [] {point[0] - intr[0], point[1] - intr[1]};
        
        double[] u = line_unit_vector(line);
        double[] n = line_normal_vector(line);
    
        //perpendicular length of the triangle formed using point and midpoint of line
        double perpendicular_distance = Math.abs(vector[0]*n[0]+vector[1]*n[1]);
        //base length of the triangle
        double parallel_distance  = Math.abs(vector[0]*u[0]+vector[1]*u[1]);
        
        //if the point lies in between the endpoints of the line, return perpendicular distance
        if (parallel_distance <= line_length(line)/2.0)
            return perpendicular_distance;
        else//return the distance with the line endpoint closest to the "point"
            return Math.min(distance_between_points(point, new double[] {line.points[0], line.points[1]}), distance_between_points(point, new double[] {line.points[2], line.points[3]}));
    }

    double distance_between_lineends_and_point(Line line, double[] point)
    {    
        return Math.min(distance_between_points(point, new double[] {line.points[0], line.points[1]}), distance_between_points(point, new double[] {line.points[2], line.points[3]}));
    }

    double[] intersection_between_lines(Line l1, Line l2, double eps)
    {
        double x1 = l1.points[0]-l2.points[0];
        double x2 = l1.points[1]-l2.points[1];
        
        double d11 = l2.points[2]-l2.points[0];
        double d12 = l2.points[3]-l2.points[1];
        
        double d21 = l1.points[2]-l1.points[0];
        double d22 = l1.points[3]-l1.points[1];
        
        double cross = d11*d22 - d12*d21; //determinant of the coefficients of the two lines
        
            
        if (Math.abs(cross) < eps) //if determinant is 0 then lines are parallel
            return new double[] {};
            
        double t1 = (x1*d22 - x2*d21)/cross;
        
        double r1 = l2.points[0] + d11*t1;
        double r2 = l2.points[1] + d12*t1;
        
        double[] intr = new double [] {r1, r2};
        //checking if intersection point is on the line segments
        if (distance_between_line_and_point(l2, intr) < eps && distance_between_line_and_point(l1, intr) < eps)
        {
            
            return intr;
        }
        else
            return new double[] {};

    }

    ArrayList<Line> JunctionAveraging(ArrayList<Line> lines)
    {

        //detecting junctions among lines
        //"junctions" will contain all the junction objects detected from intersection of 2 lines
        ArrayList<Junction> junctions = new ArrayList<Junction>();
        for(int i = 0; i < lines.size()-1; i++)
        {
            double [] intr;
            for(int j = i ;j < lines.size(); j++)
            {
                
                intr = intersection_between_lines(lines.get(i), lines.get(j), min_dist);

                if(intr.length >= 2)
                {
                    ///System.out.println("intr: "+intr[0]+" "+intr[1]);
                    junctions.add(new Junction(lines.get(i).id, lines.get(j).id, intr[0], intr[1], "Line", "Line"));
                }
                
            }
        }

        for(int i = 0; i < junctions.size(); i++)
        {
            System.out.println(junctions.get(i).id+ " "+ junctions.get(i).x+ " "+ junctions.get(i).y);
        }

        
        // empty junction set
        DisjointUnionSets junc_sets = new DisjointUnionSets(junctions.size());

        System.out.println("junc size= "+junctions.size());

        // add junctions in the set, while also grouping them according to distance
        for(int i = 0; i < junctions.size()-1; i++)
        {
            
            for(int j = i+1 ;j < junctions.size(); j++)
            {
                double [] f1 = new double[] {junctions.get(i).x, junctions.get(i).y};
                double [] f2 = new double[] {junctions.get(j).x, junctions.get(j).y};
                
                
                if(distance_between_points(f1, f2)<min_dist)
                {
                    ///System.out.println("juncs "+i+" "+j);
                    junc_sets.union(i, j);
                }

            }

        }

        // print number of junction groups formed
        System.out.println("\n#parents "+junc_sets.parent.length);
        
        HashMap<Integer, ArrayList<Integer>> hashed_junc_groups = new HashMap<Integer, ArrayList<Integer>>();

        
        for(int i = 0; i<junc_sets.parent.length; i++)
        {
            hashed_junc_groups.put(junc_sets.parent[i], new ArrayList<Integer>());
            System.out.println(junc_sets.parent[i]);
        }

       System.out.println("\n#hashed_junc_groups "+hashed_junc_groups.size());

        for(int i = 0;i<junctions.size();i++)
        {
            int jid = junctions.get(i).id;
            int x = junc_sets.find(jid);
            hashed_junc_groups.get(x).add(jid);
        }

        System.out.println("\nhashed_junc_groups "+hashed_junc_groups);

        ArrayList<AvgJunction> avjunc = new ArrayList<AvgJunction>();

        //populating avgjunction arraylist with junction-id list for each junction set
        for (Map.Entry<Integer, ArrayList<Integer>> entry : hashed_junc_groups.entrySet())
        {
            avjunc.add(new AvgJunction(entry.getValue()));
        }

        System.out.println("\n#avjunc "+avjunc.size());
        

        //adding lineids participating in each junction set
        for(int k = 0; k < avjunc.size(); k++)
        {
            
            ArrayList<Integer> t = new ArrayList<Integer>();
            t = avjunc.get(k).junc_ids;
            
            //for each junction, add its tlines to the avgjunction object's junc_line if lineid not already present
            for(int i=0; i<t.size();i++)
            {
                int a1=0, a2=0;
                int curr_jid = t.get(i);
                Junction cur_junc = Utilities.getJunctionWithId(junctions, curr_jid);
                int lineid1= cur_junc.id1;
                int lineid2 = cur_junc.id2;

                for(int j =0; j<avjunc.get(k).junc_lines.size(); j++)
                {
                    if(avjunc.get(k).junc_lines.get(j)== lineid1)
                        a1=1;
                    if(avjunc.get(k).junc_lines.get(j) == lineid2)
                        a2=1;
                }
                if(a1==0)
                {
                    if(distance_between_lineends_and_point(Utilities.getLineWithId(lines, lineid1), new double[]{cur_junc.x, cur_junc.y})<min_dist)// checking if t
                    {
                        avjunc.get(k).tlines.add(new Integer(lineid1));
                        avjunc.get(k).tc++;
                        avjunc.get(k).junc_lines.add(new Integer(lineid1));
                    }
                    
                }
                if(a2==0)
                {
                    if(distance_between_lineends_and_point(Utilities.getLineWithId(lines, lineid2), new double[]{cur_junc.x, cur_junc.y})<min_dist)// checking if t
                    {
                        avjunc.get(k).tlines.add(new Integer(lineid2));
                        avjunc.get(k).tc++;
                        avjunc.get(k).junc_lines.add(new Integer(lineid2));
                    }
                                        
                }

            }

            //for each junction, add its xlines to the avgjunction object's junc_line if lineid not already present
            for(int i=0; i<t.size();i++)
            {
                int a1=0, a2=0;
                int curr_jid = t.get(i);
                Junction cur_junc = Utilities.getJunctionWithId(junctions, curr_jid);
                int lineid1= cur_junc.id1;
                int lineid2 = cur_junc.id2;

                for(int j =0; j<avjunc.get(k).junc_lines.size(); j++)
                {
                    if(avjunc.get(k).junc_lines.get(j)== lineid1)
                        a1=1;
                    if(avjunc.get(k).junc_lines.get(j) == lineid2)
                        a2=1;
                }
                if(a1==0)
                {
                    
                    {
                        avjunc.get(k).xlines.add(new Integer(lineid1));
                        avjunc.get(k).xc++;
                    }
                    avjunc.get(k).junc_lines.add(new Integer(lineid1));
                }
                if(a2==0)
                {
                    
                    {
                        avjunc.get(k).xlines.add(new Integer(lineid2));
                        avjunc.get(k).xc++;
                    }
                    avjunc.get(k).junc_lines.add(new Integer(lineid2));
                }

            }

            
        }

        for(int k = 0; k < avjunc.size(); k++)
        {
            System.out.println("\navjunc id: "+avjunc.get(k).id+" linesids: "+avjunc.get(k).junc_lines+" tlines: "+avjunc.get(k).tlines+" xlines: "+avjunc.get(k).xlines);
        }

            
        for(int k = 0; k < avjunc.size(); k++)
        {
            int xc=avjunc.get(k).xc, tc=avjunc.get(k).tc;
            double [] avgintr = new double[]{0,0};
            if ( xc + tc > 2 )
            {
                if(xc==2)
                {
                    int p = avjunc.get(k).xlines.get(0), q = avjunc.get(k).xlines.get(1);
                    //avgintr = intersection of both x lines
                    avgintr = intersection_between_lines(Utilities.getLineWithId(lines,p), Utilities.getLineWithId(lines,q), min_dist);

                    // for all tlines
                    // find which end is avgintr and change it to avgintr
                    System.out.println("avg intersection: "+avgintr[0]+" "+avgintr[1]);
                    for(int j =0; j<tc; j++)
                    {
                        int m = avjunc.get(k).tlines.get(j);
                        System.out.println("checking 0 1 with : "+Utilities.getLineWithId(lines,m).points[0]+ " "+Utilities.getLineWithId(lines,m).points[1]);
                        System.out.println("checking 2 3 with : "+Utilities.getLineWithId(lines,m).points[2]+ " "+Utilities.getLineWithId(lines,m).points[3]);
                        if(distance_between_points(avgintr, new double[] {Utilities.getLineWithId(lines,m).points[0], Utilities.getLineWithId(lines,m).points[1]})<min_dist)
                        {
                            System.out.println("match with : "+Utilities.getLineWithId(lines,m).points[0]+ " "+Utilities.getLineWithId(lines,m).points[1]);
                            Utilities.getLineWithId(lines,m).points[0] = avgintr[0];
                            Utilities.getLineWithId(lines,m).points[1] = avgintr[1];
                        }
                        else
                        {
                            System.out.println("match with : "+Utilities.getLineWithId(lines,m).points[2]+ " "+Utilities.getLineWithId(lines,m).points[3]);
                            Utilities.getLineWithId(lines,m).points[2] = avgintr[0];
                            Utilities.getLineWithId(lines,m).points[3] = avgintr[1];
                        }
                    }
                }
                if(xc==1)
                {
                    int p = avjunc.get(k).xlines.get(0);
                    avgintr[0] = (double)0.0;
                    avgintr[1] = (double)0.0;
                    double [] temp;
                    // for each tline, find where it'll intersect the xline, store all intersections in an array
                    for(int j =0; j<tc; j++)
                    {
                        int m = avjunc.get(k).tlines.get(j);
                        temp = intersection_between_lines(Utilities.getLineWithId(lines,p), Utilities.getLineWithId(lines,m), min_dist);
                        avgintr[0]= (avgintr[0]+temp[0]);
                        avgintr[1]= (avgintr[1]+temp[1]);

                    }
                    avgintr[0]=avgintr[0]/tc;
                    avgintr[1]=avgintr[1]/tc;

                    // System.out.println("avg intersection for tlines: "+avgintr[0]+" "+avgintr[1]);

                    // avgintr = avg of all intersections

                    // for all tlines
                    // find which end is intr and change it to avg intersection
                    for(int j =0; j<tc; j++)
                    {
                        int m = avjunc.get(k).tlines.get(j);
                        if(distance_between_points(avgintr, new double[] {Utilities.getLineWithId(lines,m).points[0], Utilities.getLineWithId(lines,m).points[1]})<min_dist)
                        {
                            System.out.println("match with : "+Utilities.getLineWithId(lines,m).points[0]+ " "+Utilities.getLineWithId(lines,m).points[1]);
                            Utilities.getLineWithId(lines,m).points[0] = avgintr[0];
                            Utilities.getLineWithId(lines,m).points[1] = avgintr[1];
                        }
                        else
                        {
                            System.out.println("match with : "+Utilities.getLineWithId(lines,m).points[2]+ " "+Utilities.getLineWithId(lines,m).points[3]);
                            Utilities.getLineWithId(lines,m).points[2] = avgintr[0];
                            Utilities.getLineWithId(lines,m).points[3] = avgintr[1];
                        }
                    }
                    
                }
                /*if(xc>2)
                {
                    //a full-proof algo yet to be conceptualized

                    // the following is crude possibility but it will not consider xlines with both ends fixed beforehand

                    //find all xlines whose atleast 1 end point is unattached, set S
                    // choose 1st remaining line or if none remains then choose 1st line from S, find intersection of all other S-lines with it
                    //find avg of the intersection
                    //for all S-lines 
                    //   if both ends free, make it pass through avg intersection keeping same slope
                    //   if one end free, change slope and make it pass

                    //need a recursive approach which can improve fixed xlines and also adjust lines attached to those fixed xlines
                }*/

                System.out.println("avg intersection: "+avgintr[0]+" "+avgintr[1]);
            }


        }

        Junction.count=0;

        return lines;
    }
}