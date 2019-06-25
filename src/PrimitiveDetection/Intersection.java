package src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.FileWriter;
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
import java.util.Comparator;

import src.All_class.*;

public class Intersection {

    
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

    //Sorts the Pair<Double, Point> list according to x-y coordinate of points
    public void sortPoints(ArrayList<Pair<Double,Point>> points)
    {
        points.sort(new Comparator<Pair<Double,Point>>() {
        @Override
        public int compare(Pair<Double,Point> o1, Pair<Double,Point> o2) {
            int result = Double.compare(o1.getValue().x, o2.getValue().x);
             if ( result == 0 ) {
               // both X are equal -> compare Y too
                result = Double.compare(o1.getValue().y, o2.getValue().y);
             } 
             return result;
        }
        
        });
    }


    
    double distance_between_line_and_point(Line line, double[] point)
    {    
        // intr is midpoint of line "line"
        double[] intr = new double [] {(line.points[0] + line.points[2])/(double)2.0, (line.points[1] + line.points[3])/(double)2.0};
        
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

    int point_in_extended_line(Line l, double[] f)
    {
        // intr is midpoint of line "line"
        double[] intr = new double [] {(l.points[0] + l.points[2])/(double)2.0, (l.points[1] + l.points[3])/(double)2.0};
        
        double[] vector = new double [] {f[0] - intr[0], f[1] - intr[1]};
        
        double[] u = line_unit_vector(l);
        double[] n = line_normal_vector(l);
    
        //perpendicular length of the triangle formed using point and midpoint of line
        double perpendicular_distance = Math.abs(vector[0]*n[0]+vector[1]*n[1]);
        //base length of the triangle
        double parallel_distance  = Math.abs(vector[0]*u[0]+vector[1]*u[1]);
        
        //if the point lies in between the endpoints of the line, return -1
        if (parallel_distance <= line_length(l)/2.0)
            return -1;

        if(((int)distance_between_points(f, new double[] {l.points[0], l.points[1]}))<((int)distance_between_points(f, new double[] {l.points[2], l.points[3]})))
            return 0;

        else
            return 1;

        /*ArrayList<Pair<Double, Point> > pointslist = new ArrayList<Pair<Double, Point>>();
        Point a = new Point(l.points[0], l.points[1]);
        Point b = new Point(l.points[2], l.points[3]);
        Point j = new Point((int)f[0], (int)f[1]);
        pointslist.add(new Pair<Double, Point>(0, a));
        pointslist.add(new Pair<Double, Point>(1, b));
        pointslist.add(new Pair<Double, Point>(2, j));
        sortPoints(pointslist);

        if((pointslist.get(0).getKey()==0 && pointslist.get(1).getKey()==1 && pointslist.get(2).getKey()==2)|| (pointslist.get(0).getKey()==2 && pointslist.get(1).getKey()==1 && pointslist.get(2).getKey()==0))
            return 1;

        if((pointslist.get(0).getKey()==2 && pointslist.get(1).getKey()==0 && pointslist.get(2).getKey()==1) || (pointslist.get(0).getKey()==1 && pointslist.get(1).getKey()==0 && pointslist.get(2).getKey()==2))
            return 0;

        return -1;*/
    }

    
    //returns coordinates of intersection point [x, y] if line intersects else an empty array
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
    
    ArrayList<double[]> intersection_between_circle_and_line(Circle circle, Line line, int eps){
        // arraylist is returned- will have 1 entry in case of 1 intersection point and 2 entries in case of 2

        int min_angle = 40;
        ArrayList<double[]> temp_sln = new ArrayList<double[]>();
        double[] normal_vector = line_normal_vector(line);
        double[] parallel_vector = line_unit_vector(line);
        
        //midpoint
        double[] intr = new double [] {(line.points[0] + line.points[2])/(double)2.0, (line.points[1] + line.points[3])/(double)2.0};
        // vector p1 from line midpoint to circle centre
        double[] p1 = new double [] {intr[0]-(double)circle.x, intr[1]-(double)circle.y};
        

        double d = p1[0]*normal_vector[0] + p1[1]*normal_vector[1];
        double[] perp_vector = new double[] {d*normal_vector[0], d*normal_vector[1]};

        // Tangency
        double D = (double)Math.pow(circle.r,2) - d*d;

        // Error tolerant step
        // Intersection of a line at only one point with a circle - geometric equation for intersection of circle and line
        if (D < 0){
            D = (double)Math.pow((circle.r + eps),2) - d*d;
            if (D >= 0){
                double [] par_vector = new double[] {(double)Math.sqrt(D) * parallel_vector[0], (double)Math.sqrt(D) * parallel_vector[1]};
                double [] pt = new double[] {((double)circle.x + perp_vector[0]), ((double)circle.y + perp_vector[1])};
                temp_sln.add(pt);
            }
        }
        // intersection of a line at 2 points with a circle
        else{
            double[] par_vector = new double[] {(double)Math.sqrt(D) * parallel_vector[0],(double) Math.sqrt(D) * parallel_vector[1]};
            double[] pt = new double[] {(double)circle.x + perp_vector[0] + par_vector[0], (double)circle.y + perp_vector[1] + par_vector[1]};
            temp_sln.add(pt);
            pt = new double[] {(double)circle.x + perp_vector[0] - par_vector[0], (double)circle.y + perp_vector[1] - par_vector[1]};
            temp_sln.add(pt);
        }

        //Check if the point is on the line
        ArrayList<double[]> sln = new ArrayList<double[]>();
        for(double[] point: temp_sln)
            if(distance_between_line_and_point(line, point) < eps){
                sln.add(point);
             }

        /*if len(sln) == 2:
            angle = instantiators['angle'](sln[0], circle.center, sln[1])
            if angle_in_degree(angle, True) < min_angle:
                return [midpoint(sln[0], sln[1])]*/
        return sln;
    }
    
    ArrayList<double[]> intersections_between_circles(Circle circle0, Circle circle1){
        //http://www.ambrsoft.com/TrigoCalc/Circles2/circle2intersection/CircleCircleIntersection.htm#TwoCirclesTangency
        ArrayList<double[]> intr = new ArrayList<double[]>();
     	double a=circle0.x, b=circle0.y, c=circle1.x, d=circle1.y, r0=circle0.r, r1=circle1.r, x1, y1, x2, y2, delta;
        double norm= (a-c)*(a-c) + (b-d)*(b-d);
     	double D = Math.sqrt(norm);
        //condition of intersection of two circles- there will be two intersection points
        if(r0+r1>D && d> Math.abs(r0-r1))
        {
        	delta = Math.sqrt((D+r0+r1) * (D+r0-r1) * (D-r0+r1) * (-D+r0+r1))/4;
        	x1 = ((a+c)/2) + ((c-a) * (r0*r0-r1*r1))/(2*D*D) + (2*delta*(b-d))/(D*D);
        	y1 = ((b+d)/2) + ((d-b) * (r0*r0-r1*r1))/(2*D*D) - (2*delta*(a-c))/(D*D);
        	x2 = ((a+c)/2) + ((c-a) * (r0*r0-r1*r1))/(2*D*D) - (2*delta*(b-d))/(D*D);
        	y2 = ((b+d)/2) + ((d-b) * (r0*r0-r1*r1))/(2*D*D) + (2*delta*(a-c))/(D*D);
        	intr.add(new double [] {x1, y1});
        	intr.add(new double [] {x2, y2});
        }
        //condition of inner and outer tangency of two circles-there will be a single intersection point
        else if( r0+r1==d || Math.abs(r0-r1)==d )
        {
        	delta = (c-a)*(c-a) + (d-b)*(d-b);
        	x1 = (((a-c)*(r0*r0-r1*r1))/(2*delta)) - (a+c)/2;
        	y1 = (((b-d)*(r0*r0-r1*r1))/(2*delta)) - (b+d)/2;
        	intr.add(new double [] {x1, y1});
        }
        return intr;
     }


    ArrayList<Pair <Double, Double>> linecord = new ArrayList<Pair <Double, Double>>(); //~~~~~~~~~~~~~~~~~~~~~~~~~~


    /// Searches if vertex p is near endpoint of another line
    public int searchind(ArrayList<Pair <Double, Double>> vertex_list, Pair <Double, Double> p)
    {
        // find vertex which is near the point p
        for (int j = 0; j < vertex_list.size(); j++)
        {
            double dist = Math.sqrt(
                        Math.abs(vertex_list.get(j).getKey() - p.getKey()) * Math.abs(vertex_list.get(j).getKey() - p.getKey()) 
                        + Math.abs(vertex_list.get(j).getValue() - p.getValue()) * Math.abs(vertex_list.get(j).getValue() - p.getValue()) );
            
            if(dist<8)//distance between two vertices to consider them the same;
               // return the index of vertex found close to our point
               return j;
        }
        return -1;
    }


     /// vertices of all lines
    public void fix_end_point(ArrayList<Line> lines)
    {
        for (int i = 0; i < lines.size(); i++)
        {   
            Pair<Double,Double> p1 = new Pair <Double, Double>(lines.get(i).points[0],lines.get(i).points[1]);
            Pair<Double,Double> p2 = new Pair <Double, Double>(lines.get(i).points[2],lines.get(i).points[3]);
            // search index of close points
            int index1 = searchind(linecord,p1); 
            int index2 = searchind(linecord,p2); 
            if (index1 == -1)
            {
                //if no close point found then add the current point
                linecord.add(p1);
            }
            else  // update the current point to the closest point found
            {
                lines.get(i).points[0] = linecord.get(index1).getKey();
                lines.get(i).points[1] = linecord.get(index1).getValue();
            }
            // similarly for the other end point
            if (index2 == -1)
            {
                linecord.add(p2);
            }
            else
            {
                lines.get(i).points[2] = linecord.get(index2).getKey();
                lines.get(i).points[3] = linecord.get(index2).getValue();
            }
        }
    }


     /// store vertices and create vertex-edge table
    public void storeVertexEdge(ArrayList<Line> lines, ArrayList<Vertex> vertices, ArrayList<Vertex_Edge> table)
    {   
        for (int i = 0; i < linecord.size(); i++)
        {   
            Pair<Double,Double> p1 = new Pair <Double, Double>(linecord.get(i).getKey(),linecord.get(i).getValue());
            int index1 = searchind(linecord,p1);
            //storing info of vertices 
            vertices.add(new Vertex(linecord.get(i).getKey(),linecord.get(i).getValue(), index1));
        }
        
        for (int i = 0; i < lines.size(); i++)
        {   
            Pair<Double,Double> p1 = new Pair <Double, Double>(lines.get(i).points[0],lines.get(i).points[1]);
            Pair<Double,Double> p2 = new Pair <Double, Double>(lines.get(i).points[2],lines.get(i).points[3]);
            int index1 = searchind(linecord,p1);
            int index2 = searchind(linecord,p2);
            //table stores lineid and corresp vertex id
            table.add(new Vertex_Edge(index1, index2, lines.get(i).id));
        }
    }


    // updating vertex id in the lines datastructure
    void update_vertices(ArrayList<Line> lines, ArrayList<Vertex_Edge> table)
    {
        for(int i=0;i<lines.size();i++)
        {
            for(int j=0;j<table.size();j++)
            {
                if(table.get(j).e==lines.get(i).id)
                {
                    lines.get(i).v1=table.get(j).v1;
                    lines.get(i).v2=table.get(j).v2;
                    break;
                }
            }
        }
    }
    
    int is_horizontal(Line l)
    {
        if(l.points[3] == l.points[1])
            return 0;
        if(Math.abs(l.points[3] - l.points[1]) <= 5)
            return 1;
        return -1;
    }

    int is_vertical(Line l)
    {
        if(l.points[2] == l.points[0])
            return 0;
        if(Math.abs(l.points[2] - l.points[0]) <= 5)
            return 1;
        return -1;
    }

    void update(ArrayList<Line> lines, ArrayList<Junction> intersections, Line l, double xc, double yc)
    {
        double x1 = l.points[0], y1 = l.points[1], x2 = l.points[2], y2 = l.points[3];
        if(xc == -1)
        {
            Line l1;
            for(int i = 0; i<lines.size(); i ++)
            {
                l1 = lines.get(i);
                if((l1.points[0] == x1 && l1.points[1] == y1) || (l1.points[0] == x2 && l1.points[1] == y2))
                {
                    l1.points[1] = yc;
                }
                if((l1.points[2] == x1 && l1.points[3] == y1) || (l1.points[2] == x2 && l1.points[3] == y2))
                {
                    l1.points[3] = yc;
                }

            }
            for(int i = 0; i<intersections.size(); i ++)
            {
                if((intersections.get(i).x == x1 && intersections.get(i).y == y1) || (intersections.get(i).x == x2 && intersections.get(i).y == y2))
                {
                    intersections.get(i).y = yc;
                }
            }
        }
        else
        {
            Line l1;
            for(int i = 0; i<lines.size(); i ++)
            {
                l1 = lines.get(i);
                if((l1.points[0] == x1 && l1.points[1] == y1) || (l1.points[0] == x2 && l1.points[1] == y2))
                {
                    l1.points[0] = xc;
                }
                if((l1.points[2] == x1 && l1.points[3] == y1) || (l1.points[2] == x2 && l1.points[3] == y2))
                {
                    l1.points[2] = xc;
                }

            }
            for(int i = 0; i<intersections.size(); i ++)
            {
                if((intersections.get(i).x == x1 && intersections.get(i).y == y1) || (intersections.get(i).x == x2 && intersections.get(i).y == y2))
                {
                    intersections.get(i).x = xc;
                }
            }
        }
    }

    void end_points_horizontal_vertical(ArrayList<Line> lines, ArrayList<Junction> intersections)
    {
        double xc, yc;
        Line l;
        for(int i=0; i< lines.size(); i++)
        {
            l = lines.get(i);
            int x = is_horizontal(l);
            if(x < 0) //not horizontal
            {
                int y = is_vertical(l);
                if( y > 0)//vertical but incorrect
                {
                    xc = (l.points[0] + l.points[2])/2;
                    yc = -1;
                    update(lines, intersections, l, xc, yc);
                }
                else//vertical but correct: y=0, or not vertical : y<0
                {}
            }
            else if(x > 0) // horizontal but incorrect
            {
                yc = (l.points[1] + l.points[3])/2;
                xc = -1;
                update(lines, intersections, l, xc, yc);
            }
            else //horizonatl but correct 
            {}
        }
    }
    public static void align_ends(ArrayList<Line> lines)
    {
        for(int i=0;i<lines.size();i++)
        {
            if(lines.get(i).points[0]>lines.get(i).points[2])
            {
                double a = lines.get(i).points[2];
                double b = lines.get(i).points[3];
                lines.get(i).points[2] = lines.get(i).points[0];
                lines.get(i).points[3] = lines.get(i).points[1];
                lines.get(i).points[0] = a;
                lines.get(i).points[1] = b;
            }
            if(lines.get(i).points[0]==lines.get(i).points[2])
            {
                if(lines.get(i).points[1]<lines.get(i).points[3])
                {            
                    double a = lines.get(i).points[2];
                    double b = lines.get(i).points[3];
                    lines.get(i).points[2] = lines.get(i).points[0];
                    lines.get(i).points[3] = lines.get(i).points[1];
                    lines.get(i).points[0] = a;
                    lines.get(i).points[1] = b;
                }
            }
        }
    }



    ArrayList<Junction> intersections(ArrayList<Line> lines, ArrayList<Circle> circles,  ArrayList<Vertex> vertices, ArrayList<Vertex_Edge> table) throws IOException
    {

        //detecting junctions among lines
        ArrayList<Junction> intersections = new ArrayList<Junction>();
        for(int i = 0; i < lines.size()-1; i++)
        {
            /*double [] intr;
            for(int j = i ;j < lines.size(); j++)
            {
                intr = intersection_between_lines(lines.get(i), lines.get(j), 10);
                if(intr.length >= 2)
                {    
                    if (point_in_extended_line(lines.get(i),intr)==0)
                    {
                        lines.get(i).points[0]= (int)intr[0];
                        lines.get(i).points[1]= (int)intr[1];
                    }
                    if (point_in_extended_line(lines.get(i),intr)==1)
                    {
                        lines.get(i).points[2]= (int)intr[0];
                        lines.get(i).points[3]= (int)intr[1];
                    }
                    if (point_in_extended_line(lines.get(j),intr)==0)
                    {
                        lines.get(j).points[0]= (int)intr[0];
                        lines.get(j).points[1]= (int)intr[1];
                    }
                    if (point_in_extended_line(lines.get(j),intr)==1)
                    {
                        lines.get(j).points[2]= (int)intr[0];
                        lines.get(j).points[3]= (int)intr[1];
                    }
                    //if lines intersect, store the intersection point
                    intersections.add(new Junction(lines.get(i).id, lines.get(j).id, intr[0], intr[1], "Line", "Line"));
                }
            }*/

             //ET_PMT, above section is only PMT handling
            double [] intr;
            for(int j = i ;j < lines.size(); j++)
            {
                intr = intersection_between_lines(lines.get(i), lines.get(j), 10);
                if(intr.length >= 2)
                {    
                    double d1 = distance_between_points(intr, new double[]{lines.get(i).points[0], lines.get(i).points[1]});
                    double d2 = distance_between_points(intr, new double[]{lines.get(i).points[2], lines.get(i).points[3]});
                    double d3 = distance_between_points(intr, new double[]{lines.get(j).points[0], lines.get(j).points[1]});
                    double d4 = distance_between_points(intr, new double[]{lines.get(j).points[2], lines.get(j).points[3]});



                    if (d1>0 && d1<10)
                    {
                        lines.get(i).points[0]= intr[0];
                        lines.get(i).points[1]= intr[1];
                    }
                    else if (d2>0 && d2<10)
                    {
                        lines.get(i).points[2]= intr[0];
                        lines.get(i).points[3]= intr[1];
                    }
                    else if (d3>0 && d3<10)
                    {
                        lines.get(j).points[0]= intr[0];
                        lines.get(j).points[1]= intr[1];
                    }
                    else if (d4>0 && d4<10)
                    {
                        lines.get(j).points[2]= intr[0];
                        lines.get(j).points[3]= intr[1];
                    }
                    //if lines intersect, store the intersection point
                    intersections.add(new Junction(lines.get(i).id, lines.get(j).id, intr[0], intr[1], "Line", "Line"));
                }
            }
            
        }


        // detecting intersection between circles and lines
        for(int i=0;i<circles.size();i++)
        {
            ArrayList<double[]> intr;
            for(int j=0;j<lines.size(); j++)
            {
                // intr is an arraylist of doubles. each element is a x-y coordinate
                intr=intersection_between_circle_and_line(circles.get(i), lines.get(j), 3);
                
                // loop on all the intersection points between the circle and the line
                for(double[] tr: intr)
                {    
                    // if both x-y coordinate are present                
                    if(tr.length>=2)
                        // if they are intersecting, store the intersection point
                        intersections.add(new Junction(circles.get(i).id, lines.get(j).id, tr[0], tr[1], "Circle", "Line"));
                }
                
            }
        }

        //detecting intersections between circles
        for(int i=0;i<circles.size();i++)
        {
            ArrayList<double[]> intr;
            for(int j=i+1;j<circles.size(); j++)
            {
                intr=intersections_between_circles(circles.get(i), circles.get(j));
                for(double[] tr: intr)
                {                    
                    if(tr.length>=2)
                        // if they are intersecting, store the intersection point
                        intersections.add(new Junction(circles.get(i).id, circles.get(j).id, (double)tr[0], (double)tr[1], "Circle", "Circle"));
                }
                
            }
        }

        //////////////////////

        
        /*System.out.println("final processing before---"+lines.size());
        for(int i=0;i<lines.size();i++)
        {
            System.out.println(lines.get(i).id+"\t"+lines.get(i).points[0]+" "+lines.get(i).points[1]+" "+lines.get(i).points[2]+" "+lines.get(i).points[3]+" slope: "+lines.get(i).slope+" length "+lines.get(i).length);
        }

        System.out.println("intersections.size() before shape association: "+intersections.size());
        for(int i=0;i<intersections.size();i++)
        {
            System.out.println(intersections.get(i).id+": "+intersections.get(i).type1+" ID1: "+intersections.get(i).id1+" "+intersections.get(i).type2+" ID2: "+intersections.get(i).id2+" x: "+intersections.get(i).x+" y: "+intersections.get(i).y);
        }*/

        end_points_horizontal_vertical(lines, intersections);    

        align_ends(lines);

        for(int i=0;i<lines.size();i++)
        {
            Line l = lines.get(i);
            l.length = Math.sqrt(((l.points[3]-l.points[1]) * (l.points[3]-l.points[1])) + ((l.points[2]-l.points[0])*(l.points[2]-l.points[0])));
            l.slope = Math.atan2((double)(l.points[3]-l.points[1]),(double)(l.points[2]-l.points[0]))*(180/Math.PI);
            // System.out.println(l.slope+" here3");
            //for genral coordinate system
            if(l.slope<=0 || Math.abs(l.slope)==90 || Math.abs(l.slope)==180)
            {
                l.slope = Math.abs(Math.round(l.slope));
            }
            else
            {
                l.slope =  Math.round(180-l.slope);
                double temp = l.points[0];
                l.points[0] = l.points[2];
                l.points[2] = temp;
                temp = l.points[1];
                l.points[1] = l.points[3];
                l.points[3] = temp;
            }

        
            if((l.slope >= 85 && l.slope <= 95)||(l.slope >= 265 && l.slope <= 275))
                l.label_slope += " is vertical";
            else if (l.slope<=5 || l.slope>=355 ||(l.slope>=175 && l.slope<=185))
                l.label_slope += " is horizontal";
            else
            {
                l.label_slope += " is at an angle of "+Double.toString((int)l.slope)+" degrees";
            }

        }


        fix_end_point(lines);    

        // storing all line info        
        storeVertexEdge(lines, vertices, table);
        // updating vertex id in the lines datastructure
        update_vertices(lines, table);
        ///////////////////

        for(int i=0;i<intersections.size();i++)
        {
            Junction jn = intersections.get(i);
            // Line l1 = Utilities.getLineWithId(lines, jn.id1);
            // Line l2 = Utilities.getLineWithId(lines, jn.id2);
            // Vertex v1 = Utilities.getVertexWithId(vertices, l1.v1);
            // Vertex v2 = Utilities.getVertexWithId(vertices, l1.v2);
            // if(jn.vid!=-1)    
            // {
            //     Vertex v = Utilities.getVertexWithId(vertices, jn.vid);
            //     jn.junction_ratio1 = Utilities.junction_ratio(jn, l1, v1, v2, v.x, v.y);
            // }
            // else
            // {
            //     jn.junction_ratio1 = Utilities.junction_ratio(jn, l1, v1, v2, jn.x, jn.y);
            // }
            // v1 = Utilities.getVertexWithId(vertices, l2.v1);
            // v2 = Utilities.getVertexWithId(vertices, l2.v2);
            // if(jn.vid!=-1)    
            // {
            //     Vertex v = Utilities.getVertexWithId(vertices, jn.vid);
            //     jn.junction_ratio2 = Utilities.junction_ratio(jn, l2, v1, v2, v.x, v.y);
            // }
            // else
            // {
            //     jn.junction_ratio2 = Utilities.junction_ratio(jn, l2, v1, v2, jn.x, jn.y);
            // }
            
        }

        
        System.out.println("final processing output---"+lines.size());
        for(int i=0;i<lines.size();i++)
        {
            System.out.println(lines.get(i).id+"\t"+lines.get(i).points[0]+" "+lines.get(i).points[1]+" "+lines.get(i).points[2]+" "+lines.get(i).points[3]+" slope: "+lines.get(i).slope+" length "+lines.get(i).length);
        }

		String File_Output_decoder="./output/lines.csv";
		
		File file = new File(File_Output_decoder);
		FileWriter filewriter = new FileWriter(file);
		
        for(int i=0;i<lines.size();i++)
        {
			filewriter.write(lines.get(i).points[0]+","+lines.get(i).points[1]+","+lines.get(i).points[2]+","+lines.get(i).points[3]+"\n");
        }
		
		filewriter.flush();
		filewriter.close();
		
        return intersections;
    }
}
