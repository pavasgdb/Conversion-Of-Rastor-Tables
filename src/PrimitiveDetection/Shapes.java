package src;


//import src.MathsExtraction.Vec4i;
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
import src.Utilities.*;


public class Shapes {



    //Calculates the length of the line with endpoints v1, v2
	public double edgelength(Vertex v1, Vertex v2)
    {  
        return Math.sqrt(Math.pow((v1.x - v2.x),2) + Math.pow((v1.y - v2.y),2));     
    }


    // given end points of 2 lines, finds their slope, subtracts it to find angle between lines in degrees
    public double findangle(Vertex v1, Vertex v2, Vertex v3, Vertex v4)
    {  
        double a1, a2, a;
        a1= Math.abs(Math.atan2((double)(v1.y - v2.y), (double)(v1.x - v2.x)));
        a2= Math.abs(Math.atan2((double)(v3.y - v4.y), (double)(v3.x - v4.x)));
        a = Math.abs(a1-a2)*(180/Math.PI);
        // if(a < 0)
        // 	a = a + 180;
        return a;
    }
    // given end points of 2 lines, finds their slope, subtracts it to find angle between lines in degrees
    public double findangle(Line l1, Line l2)
    {  
        double a1, a2, a;
        a1= l1.slope;
        a2= l2.slope;
        a = Math.abs(a1-a2);
        // if(a < 0)
        //  a = a + 180;
        return a;
    }

    //finds if the line with id = line_id and endpoints v1, v2 are associated with the triangle tri_cycle
    public boolean centroidVertexAssociation(Vertex v1, Vertex v2, float xm, float ym, int [] tri_cycle, int threshold, ArrayList<Associated_Line> associations, int line_id, HashMap<Integer, Integer> lines_visited)
    {
        //dist1 is the distance of vertex1 of the current line from centroid of the triangle
        double dist1= Math.sqrt(Math.pow((v1.x - xm), 2) + Math.pow((v1.y - ym),2));
        //dist2 is the distance of vertex2 of the current line from centroid of the triangle
        double dist2= Math.sqrt(Math.pow((v2.x - xm), 2) + Math.pow((v2.y - ym),2));
        int flag = 1;
        
        // if v1 of line is closer to the centroid then check if v2 coincides with any vertex of the triangle
        System.out.println("centroid: "+dist1+" "+dist2+" "+threshold);
        if(dist1 <= threshold || dist2 <= threshold)
        {
            if(dist2 <= threshold)
            {
                Vertex temp = v1;
                v1 = v2;
                v2 = temp;
                flag = 2;
            }

            int found = -2;
            for(int j=0;j<tri_cycle.length/2;j++)
            {
                if(v2.id == tri_cycle[j])
                {
                    found = tri_cycle[j];
                    break;
                }
            }
            // if v2 is a triangle vertex then v1v2 is an associated line for our triangle
            if(found!=-2)
            {
            	if(flag == 1)
                	associations.add(new Associated_Line(line_id, "CentroidVertex", -1, found, "Centroid", "Vertex" ));
            	else
            		associations.add(new Associated_Line(line_id, "CentroidVertex", found, -1, "Vertex", "Centroid" ));
                return true;
            }
        }
        return false;
        
    }

    //finds all lines associated with the triangle tri_cycle
    public ArrayList<Associated_Line> associated_lines_triangle(int [] tri_cycle, ArrayList<Line> lines, HashMap<Integer, Integer> lines_visited, ArrayList<Vertex> vertices)
    {

    	float xm=0, ym=0;
        // xm,ym will store centroid coordinates
        for(int i=0;i<tri_cycle.length/2;i++)
        {
            Vertex lt = Utilities.getVertexWithId(vertices, tri_cycle[i]);

            xm += lt.x;
            ym += lt.y;
        }
        xm /=3;
        ym /=3;

        //lines of the triangle are marked visited
        lines_visited.put(tri_cycle[3], 1);
        lines_visited.put(tri_cycle[4], 1);
        lines_visited.put(tri_cycle[5], 1);

        ArrayList<Associated_Line> associations = new ArrayList<Associated_Line>();
        double dist1, dist2;
        

        //lines joining vertex and centroid are found and marked visited
        for(int i=0; i<lines.size();i++)
        {
            if(lines_visited.get(lines.get(i).id)!=1)
            {
                Vertex v1 = Utilities.getVertexWithId(vertices, lines.get(i).v1);
                Vertex v2 = Utilities.getVertexWithId(vertices, lines.get(i).v2);

                if(centroidVertexAssociation(v1, v2, xm, ym, tri_cycle, 11, associations, lines.get(i).id, lines_visited))
					lines_visited.put(lines.get(i).id, 1);
                
            }
        }
        return associations;
    }


    public boolean isEquilateral(double len1, double len2, double len3, double equalSide)
    {
    	if(Math.abs(len1-len2) < equalSide && Math.abs(len2-len3) < equalSide)
    		return true;
    	return false;
    }

    public boolean isIsoceles(double len1, double len2, double len3, double equalSide, int[] ie)
    {
    	if(Math.abs(len1-len2) < equalSide)
        {
            ie[0] = 2;
            return true;
        }
        if(Math.abs(len3-len2) < equalSide)
        {
            ie[0] = 0;
            return true;
        }
        if(Math.abs(len3-len1) < equalSide)
        {
            ie[0] = 1;
            return true;
        }
        	
        return false;
    }

    //angle1, angle2, angle3 are in degrees
    public boolean isRight(ArrayList<Line> lines, ArrayList<AngleBetweenComponents>shape_angles, ArrayList<Integer> line_component, double angle90, int[] ids)
    {
        int hypotenuse = -1, perpendicular = -1, base = -1;
    	for(int i = 0; i < shape_angles.size(); i++)
    	{
            if(shape_angles.get(i).angle > 90 - angle90 && 
    			shape_angles.get(i).angle < 90 + angle90)
    		{
    			Line l1 = Utilities.getLineWithId(lines, shape_angles.get(i).cid1);
    			Line l2 = Utilities.getLineWithId(lines, shape_angles.get(i).cid2);
    			double slope1 = Math.atan2((-l1.points[3]+l1.points[1]), (l1.points[2]-l1.points[0]));//check the numerator. done because coordinate system is different in graphics than actual. simplifiees visualization
                slope1=slope1*(180/Math.PI);
                // if(slope1<0)
                //     slope1+=180;

                double slope2 = Math.atan2((-l2.points[3]+l2.points[1]),(l2.points[2]-l2.points[0]));//check the numerator. done because coordinate system is different in graphics than actual. simplifiees visualization
                slope2 = slope2*(180/Math.PI);
                // if(slope2<0)
                //     slope2 += 180;

                if(slope1 < slope2)
                {
                	base = l1.id;
                	perpendicular = l2.id;
                }
                else
                {
                	base = l2.id;
                	perpendicular = l1.id;
                }	
                break;
    		}
    	}
        if(perpendicular == -1 && base == -1)
            return false;
    	for(int i = 0; i < line_component.size(); i++)
    	{
    		if(line_component.get(i) != base && line_component.get(i) != perpendicular)
    		{
    			hypotenuse = line_component.get(i);
                ids[0] = hypotenuse;
                ids[1] = perpendicular;
                ids[2] = base;
    			break;
    		}
    	}
        return true;
    }


    public void isTriangle(int [] tri_cycle, ArrayList<Line> lines, ArrayList<Shape> shapes, int [] tri_vertex, ArrayList<Vertex> vertices, HashMap<Integer, Integer> lines_visited)
    { 
        // variables for storing edge lengths and angles of triangle

        Vertex v1 = Utilities.getVertexWithId(vertices, tri_cycle[0]);
        Vertex v2 = Utilities.getVertexWithId(vertices, tri_cycle[1]);
        Vertex v3 = Utilities.getVertexWithId(vertices, tri_cycle[2]);

        double len1 = edgelength(v1, v2);
        double len2 = edgelength(v2, v3);
        double len3 = edgelength(v3, v1);
        // double angle1 = findangle(v1, v2, v1, v3);
        // double angle2 = findangle(v1, v2, v2, v3);
        // double angle3 = findangle(v2, v3, v1, v3);
        double angle1 = findangle(Utilities.getLineWithId(lines, tri_cycle[5]), Utilities.getLineWithId(lines, tri_cycle[3]));
        double angle2 = findangle(Utilities.getLineWithId(lines, tri_cycle[3]), Utilities.getLineWithId(lines, tri_cycle[4]));
        double angle3 = findangle(Utilities.getLineWithId(lines, tri_cycle[4]), Utilities.getLineWithId(lines, tri_cycle[5]));

        // shape_component stores edges
        ArrayList<Integer> line_component = new ArrayList<Integer>();
        line_component.add(tri_cycle[3]);
        line_component.add(tri_cycle[4]);
        line_component.add(tri_cycle[5]);
        
        // shape_angles stores angles between the edges: edge1,edge2,vertex included, angle formed
        ArrayList<AngleBetweenComponents> shape_angles = new ArrayList<AngleBetweenComponents>();
        shape_angles.add(new AngleBetweenComponents(tri_cycle[5], tri_cycle[3], tri_cycle[0], angle1));
        shape_angles.add(new AngleBetweenComponents(tri_cycle[3], tri_cycle[4], tri_cycle[1], angle2));
        shape_angles.add(new AngleBetweenComponents(tri_cycle[4], tri_cycle[5], tri_cycle[2], angle3));

        // find associations
        ArrayList<Associated_Line> associations = associated_lines_triangle(tri_cycle, lines, lines_visited, vertices);



    	int equalSide = 5, hypotenuse=-1, base=-1, perpendicular=-1;
    	double angle90 = 5;
		if(isEquilateral(len1, len2, len3, equalSide))
           shapes.add(new Shape("Equilateral_Triangle", line_component, new ArrayList<Integer>(), shape_angles, tri_vertex, associations)); 
        else
        { 
            int[] ids_iso = new int[1];
            boolean iso = isIsoceles(len1, len2, len3, equalSide, ids_iso);
            int[] ids = new int[3];
            boolean right = isRight(lines, shape_angles, line_component, angle90, ids);
            hypotenuse = ids[0]; perpendicular = ids[1]; base = ids[2];

            // add the shape information found into a new shape object 
            if(right && iso)
            {
                shapes.add(new Shape("Right_Isosceles_Triangle", line_component, new ArrayList<Integer>(), shape_angles, tri_vertex, base, perpendicular, hypotenuse, associations));
                //System.out.println("figure is right iso triangle");
                return;
            }
            else if(iso)
            {
                int ie = tri_cycle[3];
                if(ids_iso[0] == 1)
                {
                    ie = tri_cycle[4];
                }
                else if (ids_iso[0] == 2)
                {
                    ie = tri_cycle[5];
                }
                shapes.add(new Shape("Isosceles_Triangle", line_component, new ArrayList<Integer>(), shape_angles, tri_vertex, associations, ie));
               // System.out.println("figure is iso triangle");
                return;
            }
            else if(right)
            {
                shapes.add(new Shape("Right_Triangle", line_component, new ArrayList<Integer>(), shape_angles, tri_vertex, base, perpendicular, hypotenuse, associations));
                //System.out.println("figure is right triangle");
                return;
            }
            else
            {
                shapes.add(new Shape("Triangle", line_component, new ArrayList<Integer>(), shape_angles, tri_vertex, associations));
                //System.out.println("figure is right triangle");
                return;
            }
        }
    }


    public ArrayList<Associated_Line> associated_lines_quadrilateral(int [] quad_cycle, ArrayList<Line> lines, HashMap<Integer, Integer> lines_visited, ArrayList<Vertex> vertices)
    {

        lines_visited.put(quad_cycle[4], 1);
        lines_visited.put(quad_cycle[5], 1);
        lines_visited.put(quad_cycle[6], 1);
        lines_visited.put(quad_cycle[7], 1);

        ArrayList<Associated_Line> associations = new ArrayList<Associated_Line>();
        
        //search for associations among all the non-visited lines
        for(int i=0; i<lines.size();i++)
        {
            // System.out.println("assoc_visit: "+lines.get(i).id+" "+lines_visited.get(lines.get(i).id));
            if(lines_visited.get(lines.get(i).id)!=1)
            {
                int found1=-2, found2=-2;
     			//for the current non-visited line, check if its vertex matches any of the quadrilateral's vertices
                for(int j=0;j<quad_cycle.length/2;j++)
                {
                    // if v1 of current line is a quadrilateral vertex, check if v2 is a quadrilateral vertex of quadrilateral center
                    if(lines.get(i).v1==quad_cycle[j])
                    {
                        found1 = quad_cycle[j];
                        //checking f v2 is quadrilateral vertex
                        for(int k=0;k<quad_cycle.length/2;k++)
                        {
                            if(lines.get(i).v2==quad_cycle[k])
                            {
                                found2= quad_cycle[k];
                                break;
                            }
                        }
                    }
                    // System.out.println("found: "+found1+" "+found2);
                    //if v1 and v2 of lines are quadrilateral vertices, then break
                    if(found1!=-2 && found2!=-2)
                        break;
                        
                }    

                //if v1 and v2 of lines are quadrilateral vertices, then association found
                if(found1!=-2 && found2!=-2)
                {
                    associations.add(new Associated_Line(lines.get(i).id, "Diagonal", found1, found2, "vertex", "vertex"));
                    // System.out.println("associated");
                    lines_visited.put(lines.get(i).id, 1);
                }
                
            }
        }
        return associations;
    }

    public ArrayList<Associated_Line> associate_diagonals(int [] cycle, ArrayList<Line> lines, HashMap<Integer, Integer> lines_visited, ArrayList<Vertex> vertices)
    {

        for(int i = cycle.length/2; i < cycle.length; i++)
            lines_visited.put(cycle[i], 1);

        ArrayList<Associated_Line> associations = new ArrayList<Associated_Line>();
        
        //search for associations among all the non-visited lines
        for(int i=0; i<lines.size();i++)
        {
            // System.out.println("assoc_visit: "+lines.get(i).id+" "+lines_visited.get(lines.get(i).id));
            if(lines_visited.get(lines.get(i).id)!=1)
            {
                int found1=-2, found2=-2;
                //for the current non-visited line, check if its vertex matches any of the quadrilateral's vertices
                for(int j=0;j<cycle.length/2;j++)
                {
                    // if v1 of current line is a quadrilateral vertex, check if v2 is a quadrilateral vertex of quadrilateral center
                    if(lines.get(i).v1==cycle[j])
                    {
                        found1 = cycle[j];
                        //checking f v2 is quadrilateral vertex
                        for(int k=0;k<cycle.length/2;k++)
                        {
                            if(lines.get(i).v2==cycle[k])
                            {
                                found2= cycle[k];
                                break;
                            }
                        }
                    }
                    // System.out.println("found: "+found1+" "+found2);
                    //if v1 and v2 of lines are quadrilateral vertices, then break
                    if(found1!=-2 && found2!=-2)
                        break;
                        
                }    

                //if v1 and v2 of lines are quadrilateral vertices, then association found
                if(found1!=-2 && found2!=-2)
                {
                    associations.add(new Associated_Line(lines.get(i).id, "Diagonal", found1, found2, "vertex", "vertex"));
                    // System.out.println("associated");
                    lines_visited.put(lines.get(i).id, 1);
                }
                
            }
        }
        return associations;
    }

    public String subtypeQuad(double len1, double len2, double len3, double len4, double angle1, double angle2, double angle3, double angle4)
    {
    	int equalSide = 5, angle90 = 5, equalAngle = 2;
    	if(Math.abs(len1-len3) < equalSide && Math.abs(len2-len4) < equalSide)
        {   
	        if(Math.abs(angle1 - angle3)< 2 && (angle1 > 90 - angle90 && angle1<90 + angle90) )
	        {
	            if(Math.abs(len1-len2) < equalSide)
		        {	
			        return "Square";
			    }
		        else
		        {	
			        return "Rectangle";
		        }
	        }
	        else
	        {    
	            if(Math.abs(len1-len2) < equalSide)
                {
                    return "Rhombus";
		        }
	            else
	            {
		            return "Parallelogram";
		        }
	        }
        }
        return "Quadrilateral";
    }


	public void isQuad(int [] quad_cycle, ArrayList<Line> lines, ArrayList<Shape> shapes, int [] quad_vertex, ArrayList<Vertex> vertices, HashMap<Integer, Integer> lines_visited)	
    {  
    	Vertex v1 = Utilities.getVertexWithId(vertices, quad_cycle[0]);
        Vertex v2 = Utilities.getVertexWithId(vertices, quad_cycle[1]);
        Vertex v3 = Utilities.getVertexWithId(vertices, quad_cycle[2]);
        Vertex v4 = Utilities.getVertexWithId(vertices, quad_cycle[3]);


        // storing length of edges and values of angles into variables
        double len1 = edgelength(v1, v2);
        double len2 = edgelength(v2, v3);
        double len3 = edgelength(v3, v4);
        double len4 = edgelength(v4, v1);
        
        // double angle1 = findangle(v1, v2, v1, v4);
        // double angle2 = findangle(v1, v2, v2, v3);
        // double angle3 = findangle(v2, v3, v4, v3);
        // double angle4 = findangle(v4, v3, v1, v4);
        
        double angle1 = findangle(Utilities.getLineWithId(lines, quad_cycle[7]), Utilities.getLineWithId(lines, quad_cycle[4]));
        double angle2 = findangle(Utilities.getLineWithId(lines, quad_cycle[4]), Utilities.getLineWithId(lines, quad_cycle[5]));
        double angle3 = findangle(Utilities.getLineWithId(lines, quad_cycle[5]), Utilities.getLineWithId(lines, quad_cycle[6]));
        double angle4 = findangle(Utilities.getLineWithId(lines, quad_cycle[6]), Utilities.getLineWithId(lines, quad_cycle[7]));

        // shape_component stores the edges
        ArrayList<Integer> line_component = new ArrayList<Integer>();
        line_component.add(quad_cycle[4]);
        line_component.add(quad_cycle[5]);
        line_component.add(quad_cycle[6]);
        line_component.add(quad_cycle[7]);
        
        //shape_angles stores angles between two edges: edge1, edge2, vertex included, angle
        ArrayList<AngleBetweenComponents> shape_angles = new ArrayList<AngleBetweenComponents>();
        shape_angles.add(new AngleBetweenComponents(quad_cycle[4], quad_cycle[5], v2.id, angle2));
        shape_angles.add(new AngleBetweenComponents(quad_cycle[5], quad_cycle[6], v3.id, angle3));
        shape_angles.add(new AngleBetweenComponents(quad_cycle[6], quad_cycle[7], v4.id, angle4));
        shape_angles.add(new AngleBetweenComponents(quad_cycle[7], quad_cycle[4], v1.id, angle1));
        
        
        //associations found
        ArrayList<Associated_Line> associations = associated_lines_quadrilateral(quad_cycle, lines, lines_visited, vertices);

        String shape_name = subtypeQuad(len1, len2, len3, len4, angle1, angle2, angle3, angle4);
        if(shape_name == "Rectangle")
        {
            if(len1 > len2)
                shapes.add(new Shape(shape_name, line_component, new ArrayList<Integer>(), shape_angles, quad_vertex, associations, quad_cycle[4], quad_cycle[5]));
            else
                shapes.add(new Shape(shape_name, line_component, new ArrayList<Integer>(), shape_angles, quad_vertex, associations, quad_cycle[5], quad_cycle[4]));
        }
        else
            shapes.add(new Shape(shape_name, line_component, new ArrayList<Integer>(), shape_angles, quad_vertex, associations));    
        return;
    }

    Boolean isRegular(ArrayList<AngleBetweenComponents> shape_angles)
    {
        double interiorAngle = 0, error = 0;
        if(shape_angles.size() == 5)
        {
            interiorAngle = 108;
            error = 4;
        }
        else if(shape_angles.size() == 6)
        {
            interiorAngle = 120;
            error = 5;
        }
        for(int i = 0; i<shape_angles.size();i++)
        {
            if(Math.abs(shape_angles.get(i).angle - interiorAngle) > error )
                return false;
        }
        return true;
    }

    public void identify_shape(int[] cur_cycle, ArrayList<Line> lines, ArrayList<Shape> shapes, ArrayList<Vertex> vertices, HashMap<Integer, Integer> lines_visited, int[] cycle_vertex, int cycle_length, Boolean isconvex)
    {
        // System.out.println("cur_cycle");
        // for(int i=0;i<cur_cycle.length;i++)
        //     System.out.println(cur_cycle[i]);
        // System.out.println("cycle_vertex");
        // for(int i=0;i<cycle_vertex.length;i++)
        //     System.out.println(cycle_vertex[i]);

        // if loop length is 3 check what kind of triangle is formed
        if (cycle_length == 3 && isconvex)
        {
            isTriangle(cur_cycle, lines, shapes, cycle_vertex, vertices, lines_visited);  
        }
        // if loop length is 4 check for quadrilateral
        else if (cycle_length == 4 && isconvex)
        {
            isQuad(cur_cycle, lines, shapes, cycle_vertex, vertices, lines_visited);
        }
        // for polygon with more sides

        else
        {
            int h=cycle_length;
            int f=cycle_length*2;
            ArrayList<Associated_Line> associations = associate_diagonals(cur_cycle, lines, lines_visited, vertices);
            
            // line_component storess edges
            ArrayList<Integer> line_component = new ArrayList<Integer>();
            for(int j=h; j<f; j++)
            {
                line_component.add(cur_cycle[j]);
                lines_visited.put(cur_cycle[j], 1);
            }
    
           
            
            if(!isconvex)
            {

                ArrayList<AngleBetweenComponents> shape_angles = new ArrayList<AngleBetweenComponents>();
                // finding the closing angle
                // Vertex v0 = Utilities.getVertexWithId(vertices, cur_cycle[0]);
                // double a = findangle( v0 ,Utilities.getVertexWithId(vertices, cur_cycle[1]), v0, 
                //  Utilities.getVertexWithId(vertices, cur_cycle[h-1]));
                // adding other angles
                for(int j=0; j<h; j++)
                {
                    int vp = (j==0)?((j-1)%h+h):((j-1)%h);
                    int vc = j%h, vn = (j+1)%h;
                    int lp = vp+h, ln = vc+h;
                    Line l1 = Utilities.getLineWithId(lines, cur_cycle[lp]), l2 =Utilities.getLineWithId(lines, cur_cycle[ln]);
                    
                    double a = findangle(l1, l2);

                    int v;
                    if((l1.v1 == l2.v2) || (l1.v1 == l2.v1))
                        v = l1.v1;
                    else v = l2.v2;


                    // double a = findangle(vj, Utilities.getVertexWithId(vertices, cur_cycle[vp]), vj, Utilities.getVertexWithId(vertices, cur_cycle[(vn)]));
                    // System.out.println(cur_cycle[lp]+" "+cur_cycle[ln]+" "+v+" "+ a);
                    shape_angles.add(new AngleBetweenComponents(cur_cycle[lp], cur_cycle[ln], v, a));
                }

                shapes.add(new Shape("Non_Convex_Shape", line_component, new ArrayList<Integer>(), shape_angles, cycle_vertex, new ArrayList<Associated_Line>()));
            }
            else
            { 
                // shape_angles stores all angles
                ArrayList<AngleBetweenComponents> shape_angles = new ArrayList<AngleBetweenComponents>();
                // finding the closing angle
                // Vertex v0 = Utilities.getVertexWithId(vertices, cur_cycle[0]);
                // double a = findangle( v0 ,Utilities.getVertexWithId(vertices, cur_cycle[1]), v0, 
                //  Utilities.getVertexWithId(vertices, cur_cycle[h-1]));
                // adding other angles
                for(int j=0; j<h; j++)
                {
                    int vp = (j==0)?((j-1)%h+h):((j-1)%h);
                    int vc = j%h, vn = (j+1)%h;
                    int lp = vp+h, ln = vc+h;
                    Vertex vj = Utilities.getVertexWithId(vertices, cur_cycle[vc]);

                    double a = findangle(Utilities.getLineWithId(lines, cur_cycle[lp]), Utilities.getLineWithId(lines, cur_cycle[ln]));

                    // double a = findangle(vj, Utilities.getVertexWithId(vertices, cur_cycle[vp]), vj, Utilities.getVertexWithId(vertices, cur_cycle[(vn)]));

                    shape_angles.add(new AngleBetweenComponents(cur_cycle[lp], cur_cycle[ln], cur_cycle[vc], a));
                }
                if (cycle_length == 5)
                    shapes.add(new Shape("Pentagon", line_component, new ArrayList<Integer>(), shape_angles, cycle_vertex, associations, isRegular(shape_angles)));
                else if(cycle_length == 6)
                    shapes.add(new Shape("Hexagon", line_component, new ArrayList<Integer>(), shape_angles, cycle_vertex, associations, isRegular(shape_angles)));
                else 
                    shapes.add(new Shape("Polygon", line_component, new ArrayList<Integer>(), shape_angles, cycle_vertex, associations));
            }
        }

    }

}