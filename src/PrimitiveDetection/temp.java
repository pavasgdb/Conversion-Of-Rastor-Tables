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


public class temp
{
        // following are used in nearly_same, remove_duplicates
    /*static double thresh_min_line_dist = 10; 
    static double thresh_perpendicular_dist = 8;
    static int angle_s = 6;*/

    /*static class Line{
        int [] points = new int[4]; //x1y1 x2y2 of a line
        String type; //normal, dashed, arrowed
        int thickness; //pixel thickness
        int v1, v2; //vertex ids of line end points
        int id; //line id
        static int count = 0;
        Line(){}
        
        Line(int a,int b,int c,int d, int idn, int vr1, int vr2, String t, int th){
            points[0] = a;
            points[1] = b;
            points[2] = c;
            points[3] = d;
            v1 = vr1;
            v2 = vr2;
            id = count++;
            type=t;
            thickness=th;
        }

        Line(int a,int b,int c,int d, String t){
            points[0] = a;
            points[1] = b;
            points[2] = c;
            points[3] = d;
            v1 = -1;
            v2 = -1;
            id = count++;
            type=t;
            thickness= -1;
        }

        Line(Line v){
            System.arraycopy(v.points,0,points,0,4);
            id = v.id;
            v1 = v.v1;
            v2 = v.v2;
            type = v.type;
            thickness = v.thickness;
        }
    }*/

	/*public static double dist_point_line(Point p0, Point p1, Point p2)
    {
        double a = (p1.y-p2.y);
        double b = (p2.x-p1.x);
        double c = p1.x*(p2.y-p1.y)-p1.y*(p2.x-p1.x);
        double num = Math.abs((p0.x*a)+(p0.y*b)+c);
        double den = Math.sqrt((a*a)+(b*b));
        double d = num/den;
        return d;
    }
    //Sorts the Pair<Integer, Point> list according to x-y coordinate of points
    public static void sortPoints(ArrayList<Pair<Integer,Point>> points)
    {
        points.sort(new Comparator<Pair<Integer,Point>>() {
        @Override
        public int compare(Pair<Integer,Point> o1, Pair<Integer,Point> o2) {
            int result = Double.compare(o1.getValue().x, o2.getValue().x);
             if ( result == 0 ) {
               // both X are equal -> compare Y too
                result = Double.compare(o1.getValue().y, o2.getValue().y);
             } 
             return result;
        }
        
        });
    }

    //check overlap between 2 parallel lines
    public static boolean checkoverlap(Point p0, Point p1, Point p2, Point p3)
    {
        ArrayList<Pair<Integer, Point> > pointslist = new ArrayList<Pair<Integer, Point>>();
        pointslist.add(new Pair<Integer, Point>(0, p0));
        pointslist.add(new Pair<Integer, Point>(1, p1));
        pointslist.add(new Pair<Integer, Point>(2, p2));
        pointslist.add(new Pair<Integer, Point>(3, p3));
        sortPoints(pointslist);

        if((pointslist.get(0).getKey()==0 && pointslist.get(1).getKey()==2 && pointslist.get(2).getKey()==1 && pointslist.get(3).getKey()==3)|| (pointslist.get(0).getKey()==0 && pointslist.get(1).getKey()==3 && pointslist.get(2).getKey()==1 && pointslist.get(3).getKey()==2) || (pointslist.get(0).getKey()==1 && pointslist.get(1).getKey()==2 && pointslist.get(2).getKey()==0 && pointslist.get(3).getKey()==3) || (pointslist.get(0).getKey()==1 && pointslist.get(1).getKey()==3 && pointslist.get(2).getKey()==0 && pointslist.get(3).getKey()==2))
            return true;

        return false;
        
    }

    ///subtract x,y coordinates of two points
    public static Point subtract(Point p1,Point p2){
        return new Point(p1.x-p2.x,p1.y-p2.y);
    }

    /// Shortest distance between p0 and p1-----p2
    public static double point_distance( Point p0, Point p1, Point p2){

        Point A = subtract(p0,p1);
        Point B = subtract(p2,p1);

        double dot = A.x*B.x + A.y*B.y;
        double len_sq = B.x*B.x + B.y*B.y;
        double param = -1;
        if (len_sq != 0) //in case of zero length
            param = dot / len_sq;

        double xx, yy;

        if (param < 0){
            xx = p1.x; yy = p1.y;
        } else if (param > 1){
            xx = p2.x; yy = p2.y;
        } else {
            xx = p1.x + param * B.x;
            yy = p1.y + param * B.y;
        }

        double dx = p0.x - xx;
        double dy = p0.y - yy;
        //System.out.println("dx= "+dx+" dy= "+dy);
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static void sortLines(ArrayList<Line> lines)
    {
        lines.sort(new Comparator<Line>() {
        @Override
        public int compare(Line o1, Line o2) {
            int result = Double.compare(Math.min(o1.points[0], o1.points[2]), Math.min(o2.points[0], o2.points[2]));
             if ( result == 0 ) {
               // both X are equal -> compare Y too
                //if(o1.points[0]==o1.points[2] && o1.points[0]==o2.points[2])
                    result = Double.compare(Math.min(o1.points[1],o1.points[3]), Math.min(o2.points[1],o2.points[3]));
             } 
             return result;

        /*
            if (o1.points[0] > o2.points[0]) {
                return 1;
            } else if (o1.getValue().equals(o2.getValue())) {
                return 0; 
            } else {
                return -1;//
        }
        
        });
    }

    public static void align_ends(ArrayList<Line> lines)
    {
        for(int i=0;i<lines.size();i++)
        {
            if(lines.get(i).points[0]>lines.get(i).points[2])
            {
                int a = lines.get(i).points[2];
                int b = lines.get(i).points[3];
                lines.get(i).points[2] = lines.get(i).points[0];
                lines.get(i).points[3] = lines.get(i).points[1];
                lines.get(i).points[0] = a;
                lines.get(i).points[1] = b;
            }
            if(lines.get(i).points[0]==lines.get(i).points[2])
            {
                if(lines.get(i).points[1]>lines.get(i).points[3])
                {            
                    int a = lines.get(i).points[2];
                    int b = lines.get(i).points[3];
                    lines.get(i).points[2] = lines.get(i).points[0];
                    lines.get(i).points[3] = lines.get(i).points[1];
                    lines.get(i).points[0] = a;
                    lines.get(i).points[1] = b;
                }
            }
        }
    }

    //check overlap between 2 parallel lines
    public static boolean check_line_overlap(Point p0, Point p1, Point p2, Point p3)
    {
        ArrayList<Pair<Integer, Point> > pointslist = new ArrayList<Pair<Integer, Point>>();
        pointslist.add(new Pair<Integer, Point>(0, p0));
        pointslist.add(new Pair<Integer, Point>(1, p1));
        pointslist.add(new Pair<Integer, Point>(2, p2));
        pointslist.add(new Pair<Integer, Point>(3, p3));
        sortPoints(pointslist);

        if((pointslist.get(0).getKey()==0 && pointslist.get(1).getKey()==2 && pointslist.get(2).getKey()==1 && pointslist.get(3).getKey()==3)|| (pointslist.get(0).getKey()==0 && pointslist.get(1).getKey()==3 && pointslist.get(2).getKey()==1 && pointslist.get(3).getKey()==2) || (pointslist.get(0).getKey()==1 && pointslist.get(1).getKey()==2 && pointslist.get(2).getKey()==0 && pointslist.get(3).getKey()==3) || (pointslist.get(0).getKey()==1 && pointslist.get(1).getKey()==3 && pointslist.get(2).getKey()==0 && pointslist.get(3).getKey()==2))
            return true;

        return false;
        
    }

    //checks if the lines are in order L1L2(p0p1p2p3, p0p1p3p2. p1p0p2p3, p1p0p3p2), L2L1(p2p3p0p1, p2p3p1p0 , p3p2p0p1, p3p2p1p0) and extends
    //used in nearly_same, merge_lines
    public static Boolean check_order_extend(Point p0, Point p1, Point p2, Point p3, int[] v){
        
        double dy = p1.y - p0.y, dx = p1.x - p0.x;
        double slope = dy/dx;
        double angle = Math.abs((Math.atan(slope)*180)/Math.PI);

        int flag = 0;
        double p0p, p1p, p2p, p3p;
        Point t;
        // if angle < 45, compare x-coordinates of points, 
        if(angle<45)
        {
            p0p=p0.x; 
            p1p=p1.x;
            p2p=p2.x; 
            p3p=p3.x;

            // make p0 as the lowest x-coordinate
            if(p0.x>p1.x)
            {
                p0p=p1.x;
                p1p=p0.x;
                t=p0; p0=p1; p1=t;
            }
            // make p2 as the lowest x coordinate
            if(p2.x>p3.x)
            {
                p2p=p3.x;
                p3p=p2.x;
                t=p2; p2=p3; p3=t;
            }
            
        }
        // else compare along y-coordinates
        else
        {
            p0p=p0.y; 
            p1p=p1.y; 
            p2p=p2.y; 
            p3p=p3.y;

            // p0 - lowest y
            if(p0.y>p1.y)
            {
                p0p=p1.y;
                p1p=p0.y;
                t=p0; p0=p1; p1=t;
            }
            // p2- lowest y
            if(p2.y>p3.y)
            {
                p2p=p3.y;
                p3p=p2.y;
                t=p2; p2=p3; p3=t;
            }
        }


        Point maxp=p1, minp=p0;
        Boolean ans=false;

        // if line order is p0p1p2p3
        if(p1p<=p2p)
        {
            maxp=p3; minp=p0; ans=true;
        }
        // order p2p3p0p1
        else if(p3p<=p0p)
        {
            maxp=p1; minp=p2; ans=true;
        }

        // if none of the above orders, then returns false

        v[0] = (int)minp.x;
        v[1] = (int)minp.y;
        v[2] = (int)maxp.x;
        v[3] = (int)maxp.y;
        return ans;
        
    }

    //check full overlap between 2 parallel lines
    public static boolean check_line_full_overlap(Point p0, Point p1, Point p2, Point p3)
    {
        ArrayList<Pair<Integer, Point> > pointslist = new ArrayList<Pair<Integer, Point>>();
        pointslist.add(new Pair<Integer, Point>(0, p0));
        pointslist.add(new Pair<Integer, Point>(1, p1));
        pointslist.add(new Pair<Integer, Point>(2, p2));
        pointslist.add(new Pair<Integer, Point>(3, p3));
        sortPoints(pointslist);

        if((pointslist.get(0).getKey()==0 && pointslist.get(1).getKey()==2 && pointslist.get(2).getKey()==3 && pointslist.get(3).getKey()==1)|| (pointslist.get(0).getKey()==0 && pointslist.get(1).getKey()==3 && pointslist.get(2).getKey()==2 && pointslist.get(3).getKey()==1) || (pointslist.get(0).getKey()==1 && pointslist.get(1).getKey()==2 && pointslist.get(2).getKey()==3 && pointslist.get(3).getKey()==0) || (pointslist.get(0).getKey()==1 && pointslist.get(1).getKey()==3 && pointslist.get(2).getKey()==2 && pointslist.get(3).getKey()==0))
            return true;

        return false;
        
    }

    public static int nearly_same(Point p0, Point p1, Point p2, Point p3){

        //calculating perpendicular distance of end-points from other line
        double a = Math.min(dist_point_line(p0,p2,p3), dist_point_line(p1,p2,p3));
        double b = Math.min(dist_point_line(p2,p0,p1), dist_point_line(p3,p0,p1));

        // taking min of distance between lines
        double m = Math.min(a,b);

        // finding slopes of both lines
        double slope1 = (p1.y - p0.y)/(p1.x - p0.x);
        double slope2 = (p3.y - p2.y)/(p3.x - p2.x);
        slope1 = (Math.atan(slope1)*180)/Math.PI;
        slope2 = (Math.atan(slope2)*180)/Math.PI;

        //if slope difference within threshold & perpendicular distance within threshold
        if((Math.abs(slope1-slope2)<angle_s || Math.abs(slope1-slope2)>180 - angle_s) && m<thresh_perpendicular_dist )
        {
            //calculating distance of each end point of a line with the other line's end-point
            double side_dist_a = Math.min(point_distance(p0,p2,p3), point_distance(p1,p2,p3));
            double side_dist_b = Math.min(point_distance(p2,p0,p1), point_distance(p3,p0,p1));

            // taking min of distance between lines
            double n = Math.min(side_dist_a,side_dist_b);

            // if endpoint-endpoint distance within threshold, then check order for extension
            if ( n<thresh_min_line_dist) 
            {
                int[] v = new int[4];
                // if it can be extended, true will be obtained and 1 is returned
                if(check_order_extend(p0, p1, p2, p3, v)) 
                    return 1;

                
            }
            // if it can't be extended in order- they are either overlapping lines or very far apart
            //if they are overlapping return 2
            if(check_line_overlap(p0,p1,p2,p3))
                return 2;

            if(check_line_full_overlap(p0,p1,p2,p3)) // checking partial overlap
                return 3;


        }
            

        // in all other cases return 0- slope different, slope same but side by side distance very large
        return 0;
    }

    public static int is_shorter(Point p0, Point p1, Point p2, Point p3){

        Point A = subtract(p0,p1);
        Point B = subtract(p2,p3);

        double len1 = A.x*A.x + A.y*A.y;
        double len2 = B.x*B.x + B.y*B.y;

        if (len1<len2)
            return -1;
        else if (len1 == len2)
            return 0;
        else
            return 1;
    }*/

    public static void main(String args[])
    {
    	/*temp t = new temp();
    	Point p0 = new Point(0,0);
    	Point p1 = new Point(3,0);
    	Point p2 = new Point(5,0);
    	Point p3 = new Point(8,0); 

    	double d = t.dist_point_line(p0,p2,p3);
        double e = t.point_distance(p0,p2,p3);

    	System.out.println(d+" "+e);

    	System.out.println(t.checkoverlap(p0,p1,p2,p3));*/

        ArrayList<Line> line_set = new ArrayList<>();

        line_set.add(new Line(2,20,60,20, "normal"));
        line_set.add(new Line(6,2,19,22, "normal"));
        line_set.add(new Line(20,1,20,100, "normal"));
        line_set.add(new Line(21,22,35,2, "normal"));
        line_set.add(new Line(42,60,120,60, "normal"));
        line_set.add(new Line(46,42,59,62, "normal"));
        line_set.add(new Line(60,62,75,42, "normal"));
        //line_set.add(new Line(198,59,88,171, "normal"));



        /*align_ends(line_set);
        sortLines(line_set);*/

        for(int i=0;i<line_set.size();i++)
        {
            System.out.println(line_set.get(i).id+"\t"+line_set.get(i).points[0]+" "+line_set.get(i).points[1]+" "+line_set.get(i).points[2]+" "+line_set.get(i).points[3]);
        }


        Average_Junction aj = new Average_Junction();
        line_set = aj.JunctionAveraging(line_set);


        System.out.println("After avg junc---------");
        for(int i=0;i<line_set.size();i++)
        {
            System.out.println(line_set.get(i).id+"\t"+line_set.get(i).points[0]+" "+line_set.get(i).points[1]+" "+line_set.get(i).points[2]+" "+line_set.get(i).points[3]);
        }

         


        /*Point a1 = new Point(198,59);
        Point a2 = new Point(84,177);
        Point a3 = new Point(154,108);
        Point a4 = new Point(167,95);
        Point a5 = new Point(182,79);
        Point a6 = new Point(242,16);
        Point a7 = new Point(209,48);
        Point a8 = new Point(223,34);

        Point b1 = new Point(84,177);
        Point b2 = new Point(241,16);
        Point b3 = new Point(154,108);
        Point b4 = new Point(167,95);
        Point b5 = new Point(209, 48);
        Point b6 = new Point(223,34);
*/


        /*System.out.println(nearly_same(a1,a2,a3,a4));
        System.out.println(nearly_same(a1,a2,a5,a6));
        System.out.println(nearly_same(a1,a2,a7,a8));
        System.out.println(nearly_same(a5,a6,a7,a8));*/

        /*System.out.println(nearly_same(b1,b2,b3,b4));
        System.out.println(nearly_same(b1,b2,b5,b6));

        System.out.println(is_shorter(b3,b4,b1,b2));
        System.out.println(is_shorter(b5,b6,b1,b2));*/


        



    }
}