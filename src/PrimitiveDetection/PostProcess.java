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

public class PostProcess
{
    // following are used in nearly_same_remdup
    double thresh_min_line_dist = 10; 

    int angle_s = 5;

    // used in combine_end_points, nearly_same_merge
    double thresh_end_point_combined = 15.0;
    double thresh_perpendicular_dist = 8;

    ///subtract x,y coordinates of two points
    Point subtract(Point p1,Point p2){
        return new Point(p1.x-p2.x,p1.y-p2.y);
    }
    ///add x,y coordinates of two points
    Point add(Point p1,Point p2){
        return new Point(p1.x+p2.x,p1.y+p2.y);
    }
      ///multiply a scalar value to x,y coordinates of a point
    Point scalar(double d,Point p){
        return new Point(d*p.x,d*p.y);
    }

    /// Shortest distance between p0 and p1-----p2
    double point_distance( Point p0, Point p1, Point p2){

        Point A = subtract(p0,p1); // vector from point p0 to p1
        Point B = subtract(p2,p1); // line vector p2p1

        double dot = A.x*B.x + A.y*B.y; // dot product of A and B
        double len_sq = B.x*B.x + B.y*B.y; // squared length of line p1p2
        double param = -1; // param is used later for determining which case applies- p0 is between p1 and p2 OR p0 is near p1 OR p0 is near p2
        if (len_sq != 0) // check for division by zero 
            param = dot / len_sq; // 

        double xx, yy;

        if (param < 0){ // if p0 is near p1 and not between p1p2, find distance with p1
            xx = p1.x; yy = p1.y;
        } else if (param > 1){ // if p0 is near p and not between p1p2, find distance with p2
            xx = p2.x; yy = p2.y;
        } else { // if p0 is between p1p2, find distance with the point where perpendicular is dropped
            xx = p1.x + param * B.x;
            yy = p1.y + param * B.y;
        }

        double dx = p0.x - xx;
        double dy = p0.y - yy;
        return Math.sqrt(dx * dx + dy * dy); //return distance
    }

    //checks if the lines are in order L1L2(p0p1p2p3, p0p1p3p2. p1p0p2p3, p1p0p3p2), L2L1(p2p3p0p1, p2p3p1p0 , p3p2p0p1, p3p2p1p0) and extends
    //used in nearly_same, merge_lines
    Boolean check_order_extend(Point p0, Point p1, Point p2, Point p3, int[] v){
        
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

    /// check if nearly same lines p0p1 and p2p3 are in order p0p2p1p3 and p0p1 can be extended till p3
    Point extend(Point p0, Point p1, Point p2, Point p3){
        double dy = p1.y - p0.y;
        double dx = p1.x - p0.x;
        double slope = dy/dx;
        double angle = Math.abs((Math.atan(slope)*180)/Math.PI);

        int flag = 0;
        if (angle<45){
            if(p0.x<p2.x && p2.x<=p1.x && p1.x<p3.x)
            {
                //System.out.println("mergelies if 1");
                flag = 1;

            }
            if(p0.x>p2.x && p2.x>=p1.x && p1.x>p3.x)
            {
                //System.out.println("mergelies if 1");
                flag = 1;

            }
            
        } else {
            
            if(p0.y<p2.y && p2.y<=p1.y && p1.y<p3.y){
                flag = 2;
                //System.out.println("mergelies if 2");

                
            }
            if(p0.y>p2.y && p2.y>=p1.y && p1.y>p3.y)
            {
                //System.out.println("mergelies if 2");
                flag = 2;

            }
          
        }
        
        Point pnew = new Point(0, 0);
        if(flag == 1){
            pnew.x = p3.x;
            pnew.y = p0.y + (slope*(pnew.x - p0.x));
        }
        if(flag == 2){
            pnew.y = p3.y;
            pnew.x = p0.x + ((pnew.y - p0.y)/slope);
        }
        if(flag == 0)
            pnew = p1;

        return pnew;
    }

    double dist_point_line(Point p0, Point p1, Point p2)
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
    public void sortPoints(ArrayList<Pair<Integer,Point>> points)
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

    //check partial overlap between 2 parallel lines
    boolean check_line_partial_overlap(Point p0, Point p1, Point p2, Point p3)
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

    //check full overlap between 2 parallel lines
    boolean check_line_full_overlap(Point p0, Point p1, Point p2, Point p3)
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

    /// check if line segments p0p1 and p2p3 are nearly same (non intersecting but same)
    // used in merge_lines
    int nearly_same_merge(Point p0, Point p1, Point p2, Point p3){

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
            if ( n<thresh_end_point_combined) 
            {
                int[] v = new int[4];
                // if it can be extended, true will be obtained and 1 is returned
                if(check_order_extend(p0, p1, p2, p3, v)) 
                    return 1;

                else return 2;

                
            }
            // if it can't be extended in order- they are either overlapping lines or very far apart
            //if they are overlapping return 2
            if(check_line_partial_overlap(p0,p1,p2,p3)) // checking partial overlap
                return 2;

            if(check_line_full_overlap(p0,p1,p2,p3)) // checking partial overlap
                return 3;



        }
            

        // in all other cases return 0- slope different, slope same but side by side distance very large
        return 0;
    }

    /// Check if line segment p0---p1 overlaps with any circle detected
    Boolean line_overlaps_circle(Point p0, Point p1, ArrayList<Circle> circles, double thresh_line_overlap_circle){
        ArrayList<Point> p_arr = new ArrayList<>();

        /// take points along the line segment- p0 p3 p2 p4 p1
        Point p2 = scalar(0.5,add(p0,p1));
        Point p3 = scalar(0.5,add(p0,p2));
        Point p4 = scalar(0.5,add(p2,p1));
        p_arr.add(p0);
        p_arr.add(p1);
        p_arr.add(p2);
        p_arr.add(p3);
        p_arr.add(p4);

        int over=0;  //check
        int count=0;
        /*float count=0;

        for(int j=0; j<p_arr.size();j++){
            for(int i = 0; i < circles.size(); i++ )
            {
                Point C = new Point(circles.get(i).x, circles.get(i).y);
                double R = circles.get(i).r;
                Point A = subtract(p_arr.get(j),C);
                double len1 = A.x*A.x + A.y*A.y;
                double len = Math.sqrt(len1) - R;
                if(Math.abs(len)<=0.1*R){
                    if(j == 0 || j == p_arr.size()-1)
                    {
                        count += 12.5;
                    }
                    else
                        count += 25;
                    break;
                }
            }
        }
        System.out.println("Count: "+count);
        if(count >= 75)
            return true;
        else
            return false;*/
        // checking for all circles
        for(int i = 0; i < circles.size(); i++ )
        {
            Point C = new Point(circles.get(i).x, circles.get(i).y);
            double R = circles.get(i).r;
            
            /// check overlap of each point with circle- checking how close it is to circumference of the circle
            for(int j=0; j<p_arr.size();j++){
                Point A = subtract(p_arr.get(j),C);
                double len1 = A.x*A.x + A.y*A.y;
                double len = Math.sqrt(len1) - R;

                // if the point is closer to the circumference increment count
                if(Math.abs(len)>thresh_line_overlap_circle){
                    over = 0;
                    //break;
                } else {
                    over = 1;
                    count++;
                }
            }


            int mx=(3*circles.size())/4;
            // comparing with max(3, 0.75 * no_of_circles), min 3 at least
            if(3 >= mx)
                mx=3;
            if (count>mx){
                return true; // means ignore this line, it is overlapping
            }
        }
        return false;
    }

    //Sorts the Pair<Integer, Double> list in increasing order of second argument
    public void sortLines(ArrayList<Line> lines)
    {
        lines.sort(new Comparator<Line>() {
        @Override
        public int compare(Line o1, Line o2) {
            int result = Double.compare(Math.min(o1.points[0], o1.points[2]), Math.min(o2.points[0], o2.points[2]));
             if ( result == 0 ) {
                    result = Double.compare(Math.min(o1.points[1],o1.points[3]), Math.min(o2.points[1],o2.points[3]));
             } 
             return result;
        }
        
        });
    }

    /// Merge similar lines detected by Hough Transform to elongate them
    ArrayList<Line> merge_lines(ArrayList<Line> lines, ArrayList<Circle> circles, double thresh_line_overlap_circle){
        ArrayList<Line> min_lines = new ArrayList<>();

        ///Remove lines on circle boundaries
        for( int i = 0; i < lines.size(); i++ ){
            Point a = new Point(lines.get(i).points[0], lines.get(i).points[1]);
            Point b = new Point(lines.get(i).points[2], lines.get(i).points[3]);
            double dist_AB = Math.sqrt((a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y));
            if(!line_overlaps_circle(a,b,circles,thresh_line_overlap_circle)  ){
                // if not overlapping, then add to min_lines
                min_lines.add(lines.get(i));
            }
        }
        // update lines to new set min_lines
        lines.clear();
        for(int i = 0; i < min_lines.size(); i++){
            lines.add(min_lines.get(i));
        }

        sortLines(lines);

        for( int i = 0; i < lines.size(); i++ ){
            for(int j = 0; j < lines.size(); j++ ){
                if(i != j){
                    Point a = new Point(lines.get(i).points[0], lines.get(i).points[1]);
                    Point b = new Point(lines.get(i).points[2], lines.get(i).points[3]);
                    Point c = new Point(lines.get(j).points[0], lines.get(j).points[1]);
                    Point d = new Point(lines.get(j).points[2], lines.get(j).points[3]);
                    Point e = new Point(0,0);
                    int flag = nearly_same_merge(a,b,c,d);

                    ///if they are nearly same but not one after the other, extend one line 
                    if(flag == 2){
                        /// check if nearly same lines p0p1 and p2p3 are in order p0p2p1p3 and p0p1 can be extended till p3
                        b = extend(a,b,c,d);
                        lines.get(i).points[2] = (int)b.x;
                        lines.get(i).points[3] = (int)b.y;

                        /*lines.get(j).points[2] = (int)b.x;
                        lines.get(j).points[3] = (int)b.y;*/

                        /*ArrayList<Pair<Integer, Point> > pointslist = new ArrayList<Pair<Integer, Point>>();
                        pointslist.add(new Pair<Integer, Point>(0, a));
                        pointslist.add(new Pair<Integer, Point>(1, b));
                        pointslist.add(new Pair<Integer, Point>(2, c));
                        pointslist.add(new Pair<Integer, Point>(3, d));

                        sortPoints(pointslist);

                        lines.get(i).points[0] =(int)pointslist.get(0).getValue().x;
                        lines.get(i).points[1] =(int)pointslist.get(0).getValue().y;
                        lines.get(i).points[2] =(int)pointslist.get(3).getValue().x;
                        lines.get(i).points[3] =(int)pointslist.get(3).getValue().y;*/

                    }
                    else if(flag == 1){ ///if they are nearly same but one after the other
                        int[] v = new int[4];
                        // v will contain the order of points after this function call
                        check_order_extend(a, b, c, d, v);
                        // update one of the lines with the new coordinate order v
                        for(int k = 0; k < 4; k++)
                            lines.get(i).points[k]=v[k];
                    }
                    align_ends(lines);
                }
            }
        }
        return lines;
    }

    /// check if line segments p0p1 and p2p3 intersect
    // used in remove_duplicates
    Boolean line_intersection(Point p0, Point p1, Point p2, Point p3, Point r){ //r pass by pointer
        
        Point s1,s2,s3;
        s1 = subtract(p1,p0);
        s2 = subtract(p3,p2);
        s3 = subtract(p0,p2);
        double s, t;

        s = ((s1.x * s3.y) - (s1.y * s3.x))/ (s1.x*s2.y - s2.x*s1.y);
        t = ((s2.x * s3.y) - (s2.y * s3.x))/ (s1.x*s2.y - s2.x*s1.y);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1){
            // Collision detected and r updated with the intersection point
            r.x = p0.x + (t * s1.x);
            r.y = p0.y + (t * s1.y);
            return true;
        }
        // No collision, lines may be close together but non intersecting
        return false;
    }

    /// Find angle between line segment p1---O and p2---O
    // used in remove_duplicates
    double angle_between(Point p1, Point p2, Point O){
        
        Point s1, s2;
        s1 = subtract(p1, O);
        s2 = subtract(p2,O);

        double m12 = Math.sqrt(s1.x*s1.x + s1.y*s1.y);
        double m13 = Math.sqrt(s2.x*s2.x + s2.y*s2.y);

        double theta = Math.acos( (s1.x*s2.x + s1.y*s2.y) / (m12 * m13) );
        return (theta*180)/ Math.PI;
    }

    /// Check line segment p0p1 is shorter than p2p3
    // used in remove_duplicates
    int is_shorter(Point p0, Point p1, Point p2, Point p3){

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
    }



        /// check if line segments p0p1 and p2p3 are nearly same (non intersecting but same)
    // used in merge_lines
    int nearly_same_remdup(Point p0, Point p1, Point p2, Point p3){

        // calculating distance of each end point of a line with the other line's end-point
        double a = Math.min(point_distance(p0,p2,p3), point_distance(p1,p2,p3));
        double b = Math.min(point_distance(p2,p0,p1), point_distance(p3,p0,p1));

        // finding slopes of both lines
        double slope1 = (p1.y - p0.y)/(p1.x - p0.x);
        double slope2 = (p3.y - p2.y)/(p3.x - p2.x);
        slope1 = (Math.atan(slope1)*180)/Math.PI;
        slope2 = (Math.atan(slope2)*180)/Math.PI;

        // taking min of distance between lines
        double m = Math.min(a,b);

        // if distance and slope difference within threshold, then check order for extension
        if (m<thresh_min_line_dist && (Math.abs(slope1-slope2)<angle_s || Math.abs(slope1-slope2)>180 - angle_s ))
        {
            int[] v = new int[4];
            // if it can be extended, true will be obtained and 1 is returned
            if(check_order_extend(p0, p1, p2, p3, v)) 
                return 1;
            // if it can't be extended- they are nearly same but not one after the other
            else return 2;
        }
        else
            return 0;
    }


    /// Remove extra lines detected by Hough Transform
    ArrayList<Line> remove_duplicates(ArrayList<Line> lines){
        ///removing duplicates
        int coun=0, lsz=lines.size();
        ArrayList<Line> min_lines = new ArrayList<>();
        for( int i = 0; i <lsz ; i++ ){ 
            int flag = 0;
            for(int j = 0; j < lsz; j++ ){ 
                if(i!=j){
                    Point a = new Point(lines.get(i).points[0], lines.get(i).points[1]);
                    Point b = new Point(lines.get(i).points[2], lines.get(i).points[3]);
                    Point c = new Point(lines.get(j).points[0], lines.get(j).points[1]);
                    Point d = new Point(lines.get(j).points[2], lines.get(j).points[3]);
                    Point e = new Point(0,0);
                    // ans is true if the lines intersect and e stores the intersection point
                    Boolean ans = line_intersection(a,b,c,d,e);
                    // if(lines.get(i).id ==2)
                    // System.out.println("lineid: "+lines.get(i).id+" and "+lines.get(j).id+" ans: "+ ans+" num: "+lines.size()+" c = "+coun); coun++;
                    /// intersecting lines, check angle
                    if (ans){
                        double angle;
                        /// find angles between three unique points
                        if(a.x == e.x && a.y == e.y)
                        {
                            if(c.x == e.x && c.y == e.y)
                                angle = angle_between(b,d,e);
                            else 
                                angle = angle_between(b,c,e);    
                        }    
                        else
                        {
                            if(c.x == e.x && c.y == e.y)
                                angle = angle_between(a,d,e);
                            else 
                                angle = angle_between(a,c,e);    
                        }
                        if((angle >=0 && angle<6) || (angle<=180 && angle>174))
                        {
                            if((angle < angle_s || angle > (180-angle_s))){

                                /// if len(ab)<len(cd) then -1 is returned, if equal then 0, if greater than 1 len(ab)>len(cd)
                                int temp = is_shorter(a,b,c,d);
                                if (temp<0 || (temp==0 && i<j))
                                {
                                    flag = 1;
                                    break;
                                }
                            }
                        }
                    } 
                    else {
                        /// non intersecting lines, check if nearly parallel
                        if(nearly_same_remdup(a,b,c,d)==2){
                            int temp = is_shorter(a,b,c,d);
                            if (temp<0 || (temp==0 && i<j))
                            {
                                flag = 1;
                                break;
                            }
                        }
                    }
                }
            }
            // add lines which do not have any duplicate
            if (flag == 0){
                min_lines.add(new Line(lines.get(i)));
            }
        }
        return min_lines;
    }



    ///Combining points of two connected lines
    ArrayList<Line> combining_end_points(ArrayList<Line> lines ){

        int size = lines.size();
        int[][] array = new int [2*size][2*size];
        // construct a 2D array containing all line-end point distances from other end points
        for( int i = 0; i < size; i++ ){
            Point a = new Point(lines.get(i).points[0], lines.get(i).points[1]);
            Point b = new Point(lines.get(i).points[2], lines.get(i).points[3]);
            double dist_AB = Math.sqrt((a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y));
            int m = 2*i;
            array[m][m+1]  = array[m+1][m]  = (int)dist_AB; //Extra


            for(int j = i+1; j < size; j++ ){
                
                Point c = new Point(lines.get(j).points[0], lines.get(j).points[1]);
                Point d = new Point(lines.get(j).points[2], lines.get(j).points[3]);

                double dist_AC = Math.sqrt((a.x-c.x)*(a.x-c.x) + (a.y-c.y)*(a.y-c.y));
                double dist_BD = Math.sqrt((b.x-d.x)*(b.x-d.x) + (b.y-d.y)*(b.y-d.y));
                double dist_AD = Math.sqrt((a.x-d.x)*(a.x-d.x) + (a.y-d.y)*(a.y-d.y));
                double dist_BC = Math.sqrt((b.x-c.x)*(b.x-c.x) + (b.y-c.y)*(b.y-c.y));

                int k = 2*j;
                array[m][k]  = array[k][m]  = (int)dist_AC;
                array[m][k+1]  = array[k+1][m]  = (int)dist_AD;
                array[m+1][k]  = array[k][m+1]  = (int)dist_BC;
                array[m+1][k+1]  = array[k+1][m+1]  = (int)dist_BD;
            }
        }

        ArrayList<Integer> line_no = new ArrayList<Integer>();
        ArrayList<Integer> point_no = new ArrayList<Integer>();
        // constructing a set of points which are near a line-end point according  to the distance matrix
        for(int p=0;p<2*size;p++){
            int alpha = 0;
            int r=0;
            int line_first = (p/2);
            int mod = p%2;
            if(mod == 1){r=2;}
            double sum_x = lines.get(line_first).points[r];
            double sum_y = lines.get(line_first).points[r+1];
            line_no.add(line_first);

            point_no.add(mod);

            // finding all end-points which are close to the point being currently explored
            for(int q=p+1;q<2*size;q++){
                if(array[p][q]<thresh_end_point_combined){
                    alpha++;
                    int s=0;
                    int mod2 = q%2;
                    if(mod2 == 1){s=2;}
                    int line_second = (q/2);
                    line_no.add(line_second);
                    point_no.add(mod2);
                    sum_x = sum_x+lines.get(line_second).points[s];
                    sum_y = sum_y+lines.get(line_second).points[s+1];
                }
            }

            // alpha stores the count of all points close to our 'under-exploration' point
            if(alpha>0){
                // finding avg of all these close points to get final average x-y coordinate
                double x = sum_x / (alpha+1) ;
                double y = sum_y / (alpha+1) ;
                while(alpha>=0){
                    // removing all these points from the exhaustive set of line-points
                    int l_n = line_no.get(line_no.size()-1);
                    line_no.remove(line_no.size()-1);
                    int p_n = point_no.get(point_no.size()-1);
                    point_no.remove(point_no.size()-1);
                    lines.get(l_n).points[2*p_n] = (int)x;
                    lines.get(l_n).points[2*p_n+1] =(int)y;
                    alpha--;
                }
            }
            line_no.clear();point_no.clear();
        }

        return lines;
    }


    /// Check is the line is very small and can be discarded
    Boolean lineispoint(Line l, double thresh) //thresh=2
    {
        if(l.points[0] < l.points[2] + thresh && l.points[0] > l.points[2] - thresh && l.points[1] < l.points[3] + thresh && l.points[1] > l.points[3] - thresh)
            return true;
        return false;
    }


    /// Check if end points of l and l1 are exactly equal
    Boolean lineisequal(Line l, Line l1)
    {
        if((l.points[0] == l1.points[0] && l.points[1] == l1.points[1] && l.points[2] == l1.points[2] && l.points[3] == l1.points[3]) || (l.points[0] == l1.points[2] && l.points[1] == l1.points[3] && l.points[2] == l1.points[0] && l.points[3] == l1.points[1]))
            return true;
        return false;
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

    //filter lines by length
    ArrayList<Line> filter_lines_on_length(ArrayList<Line> lines)
    {
        ArrayList<Line> final_lines = new ArrayList<>();
        int max =0;
        for(int i = 0; i < lines.size(); i++)
        {
            Line l = lines.get(i);
            l.length = Math.sqrt(((l.points[3]-l.points[1]) * (l.points[3]-l.points[1])) + ((l.points[2]-l.points[0])*(l.points[2]-l.points[0])));
            if((int)l.length>max)
                max = (int)l.length;
        }
        max = (int)(max*0.2);
        System.out.println("min length= "+max);
        for(int i = 0; i < lines.size(); i++)
        {
            Line l = lines.get(i);
            l.length = Math.sqrt(((l.points[3]-l.points[1]) * (l.points[3]-l.points[1])) + ((l.points[2]-l.points[0])*(l.points[2]-l.points[0])));
            if((int)l.length>max)
            {
                System.out.println("id= "+lines.get(i).id);
                final_lines.add(new Line(lines.get(i)));
            }
            else
            {
                System.out.println("id= "+lines.get(i).id+"smalllen= "+l.length);
            }
        }

        return final_lines;

    }

    
    ArrayList<Line> postOperations(ArrayList<Line> lines, ArrayList<Circle> circles, String imname){
        
        String path_ML = "./output/"+imname+"_1mergelines.png";
        String path_RD = "./output/"+imname+"_2removedup.png";
        String path_CE = "./output/"+imname+"_3combineend.png";
        int thresh_line_overlap_circle=5;

        Mat src1 = Imgcodecs.imread(path_ML, Imgcodecs.IMREAD_COLOR);

        align_ends(lines);
        System.out.println("align_ends()---"+lines.size());
        for(int i=0;i<lines.size();i++)
        {
            System.out.println(lines.get(i).id+"\t"+lines.get(i).points[0]+" "+lines.get(i).points[1]+" "+lines.get(i).points[2]+" "+lines.get(i).points[3]);
        }

        lines= merge_lines(lines, circles, thresh_line_overlap_circle);

        // print output of merge_lines, ie. all line endpoints
        System.out.println("merge_lines()---"+lines.size());
        for(int i=0;i<lines.size();i++)
        {
            System.out.println(lines.get(i).id+"\t"+lines.get(i).points[0]+" "+lines.get(i).points[1]+" "+lines.get(i).points[2]+" "+lines.get(i).points[3]);
            Imgproc.line(src1, new Point(lines.get(i).points[0], lines.get(i).points[1]),new Point(lines.get(i).points[2], lines.get(i).points[3]), new Scalar(0,0,0), 1) ;    
        }

        //save mergelines output as an image file
        Imgcodecs.imwrite(path_ML,src1);


        src1 = Imgcodecs.imread(path_RD, Imgcodecs.IMREAD_COLOR);


        lines = remove_duplicates(lines);
        System.out.println("remove duplicates output---"+lines.size());
        for(int i=0;i<lines.size();i++)
        {
            System.out.println(lines.get(i).id+"\t"+lines.get(i).points[0]+" "+lines.get(i).points[1]+" "+lines.get(i).points[2]+" "+lines.get(i).points[3]);
            Imgproc.line(src1, new Point(lines.get(i).points[0], lines.get(i).points[1]),new Point(lines.get(i).points[2], lines.get(i).points[3]), new Scalar(0,0,0), 1) ;    
        }

        //save removeduplicates output as an image file
        Imgcodecs.imwrite(path_RD,src1);


        src1 = Imgcodecs.imread(path_CE, Imgcodecs.IMREAD_COLOR);

        for (int t=0;t<2*lines.size();t++)
        {
            lines = combining_end_points(lines);////////////

            /////////////////
            if(t==0)
            {
                System.out.println("combining end points output---"+lines.size());
                for(int i=0;i<lines.size();i++)
                {
                    System.out.println(lines.get(i).id+"\t"+lines.get(i).points[0]+" "+lines.get(i).points[1]+" "+lines.get(i).points[2]+" "+lines.get(i).points[3]);  
                }
            }

            ArrayList<Line> l = new ArrayList<Line>();
            for(int i = 0; i < lines.size(); i++)
                l.add(lines.get(i));
            lines.clear();

            //Remove the detected points
            for(int i = 0; i < l.size(); i++) 
            {
                if( lineispoint(l.get(i), 2) )
                    continue;
                lines.add(l.get(i));
            }

            lines = remove_duplicates(lines);
        }

        // filter lines on length;
        lines = filter_lines_on_length(lines);

        /////////////////
        System.out.println("combining & remove & filter output---"+lines.size());
        for(int i=0;i<lines.size();i++)
        {
            System.out.println(lines.get(i).id+"\t"+lines.get(i).points[0]+" "+lines.get(i).points[1]+" "+lines.get(i).points[2]+" "+lines.get(i).points[3]);
            Imgproc.line(src1, new Point(lines.get(i).points[0], lines.get(i).points[1]),new Point(lines.get(i).points[2], lines.get(i).points[3]), new Scalar(0,0,0), 1) ;    
        }

        //save combine end point output as an image file
        Imgcodecs.imwrite(path_CE,src1);


        ArrayList<Line> line_set = new ArrayList<Line>();
        for(int i = 0; i < lines.size(); i++)//remove lines that are exactly same
        {
            line_set.add(new Line(lines.get(i).points[0], lines.get(i).points[1], lines.get(i).points[2], lines.get(i).points[3], lines.get(i).id, -1, -1, "normal", 1));
        }

        return line_set;
    }
}
    