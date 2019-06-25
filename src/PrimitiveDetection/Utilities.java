package src;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;
import java.util.HashMap; 
import java.util.Map; 

import src.All_class.*;
import src.Utilities.*;

public class Utilities
{
    //****Tried using hashmap to store <index, component> whenever the component is first retreived but error becuase the component might not be completely filled at the time of first retrieval

    //****hashmap to store <index, list_index> whenever the component is first retreived but error becuase the list_index might change if there is changes in the list

	//gets the Vertex in ArrayList which has vertex.id=index
    static Vertex getVertexWithId(ArrayList<Vertex> l, int index)
    {
        for(int i = 0; i<l.size(); i++)
        {
            if(l.get(i).id == index)
            {
                return l.get(i);
            }
        }
        return new Vertex();
    }
    //gets the Junction in ArrayList which has junction.id=index
    static Junction getJunctionWithId(ArrayList<Junction> l, int index)
    {
        
        for(int i = 0; i<l.size(); i++)
        {
            if(l.get(i).id == index)
            {
                return l.get(i);
            }
        }
        return new Junction();
    }

    //gets the Line in ArrayList which has line.id=index
    static Line getLineWithId(ArrayList<Line> l, int index)
    {
        for(int i = 0; i<l.size(); i++)
        {
            if(l.get(i).id == index)
            {
                return l.get(i);
            }
        }
        return new Line();
    }

    //gets the Circle in ArrayList which has circle.id=index
    static Circle getCircleWithId(ArrayList<Circle> l, int index)
    {
        for(int i = 0; i<l.size(); i++)
        {
            if(l.get(i).id == index)
            {
                return l.get(i);
            }
        }
        return new Circle();
    }

    //gets the Label in ArrayList which has label.id=index
    static Label getLabelWithId(ArrayList<Label> l, int index)
    {
        for(int i = 0; i<l.size(); i++)
        {
            if(l.get(i).id == index)
            {
                return l.get(i);
            }
        }
        return new Label();
    }

	//gets the Shape in ArrayList which has shape.id=index
    static Shape getShapeWithId(ArrayList<Shape> l, int index)
    {
        for(int i = 0; i<l.size(); i++)
        {
            if(l.get(i).id == index)
            {
                return l.get(i);
            }
        }
        return new Shape();
    }
    
    //returns line name as a combination of labels and/or vertices
	static String line_name(Line ln, Extracted_Information all_data)
	{
        //if label name is already set, return this
        if(!ln.label_name.equals(""))
            return ln.label_name;

		String l = "";
        //get the vertices of the line
        Vertex v1 = getVertexWithId(all_data.vertices, ln.v1), v2 = getVertexWithId(all_data.vertices, ln.v2);
        //get vertex names
        l = vertex_name(v1, all_data)+vertex_name(v2, all_data);
        //store name in line component for fast retreival next time
        ln.label_name = l;
		return l;
	}

    //returns junction name as label or junction id
    static String junction_name(Junction jn, Extracted_Information all_data)
    {
        //if junction name is already set, return this
        if(!jn.label_name.equals(""))
            return jn.label_name;

        String l = "";

        if(jn.vid!=-1)
        {
            //if jn coincides with vertex, extract vertex and its name and save this as the junction's name
            Vertex v = getVertexWithId(all_data.vertices, jn.vid);
            l = "Vertex "+vertex_name(v, all_data);
            jn.label_name = l;

            return l;
        }

        //else find if any label is associated to this junction
        Pair<Integer, String> p = new Pair<Integer, String>(jn.id, "name");
        if(all_data.junctionAssociation.containsKey(p))
        {
            //if so extract that label
            JunctionLabel lbl = all_data.junctionAssociation.get(p);
            l = getLabelWithId(all_data.labels, lbl.label_id).c;
        }
        else//else auto-generate a name from jn's id
        {
            l = "J"+(jn.id)+"";
        }

        //store the label name in junction component
        l = "Junction "+l;
        jn.label_name = l;

        return l;
    }

    //returns vertex name as label or vertex id
    static String vertex_name(Vertex v, Extracted_Information all_data)
    {
        //if vertex name is already set, return this
        if(!v.label_name.equals(""))
            return v.label_name;

        String l = "";
        //else check if any label was associated to it in diagram
        Pair<Integer, String> p = new Pair<Integer, String>(v.id, "name");
        if(all_data.vertexAssociation.containsKey(p))
        {
            //if so extract that label
            VertexLabel lbl = all_data.vertexAssociation.get(p);
            l = getLabelWithId(all_data.labels, lbl.label_id).c;
        }
        else
        {
            //else auto generate a name from vertex's id
            l = "V"+(v.id)+"";
        }
        //store label name in vertex component
        v.label_name = l;

        return l;
    }

    //returns name of this shape
    static String polyName(Extracted_Information all_data, Shape s)
    {
        //if name of this polygon is already set, return this
        if(!s.label_name.equals(""))
            return s.label_name;

        //if shape is circle return circle id as its name 
        String l = "";
        if(s.shape_name =="Circle")
            l = "C"+s.circle_components.get(0);
        else if(s.shape_name == "Line") //if shape is line, return line name
            l = Utilities.line_name(Utilities.getLineWithId(all_data.lines, s.line_components.get(0)), all_data); 
        else
        {
            //else concatenate the name of all its vertices and return that
            for(int i = 0; i<s.vertices.length; i++)
            {
                Vertex v = Utilities.getVertexWithId(all_data.vertices, s.vertices[i]);
                l += Utilities.vertex_name(v, all_data);
            }
        }
        //store shape name in the component
        s.label_name = l;

        return l;
    }

    //True if vertex vid is an end point of more than one line
    static Boolean vertexOfMoreThanOne(ArrayList<Vertex_Edge> table, int vid)
    {
    	int count = 0;
        //if vid is an endpoint of more than one line, it will be present in mpore than 1 entry in Vertex-Edge table
    	for(int i = 0; i< table.size(); i++)
    	{
    		if(table.get(i).v1 == vid || table.get(i).v2 == vid)
    			count++;
    		if(count > 1)
    			return true;
    	}
    	return false;
    }

    //check if the common component cc is already seen
    static Boolean commonComponentContains(ArrayList<CommonComponent> cc, int id, String t, CommonComponent c)
    {
        for(int i = 0; i<cc.size();i++)
        {
            //check by its id and type("line"/"vertex")
            if(cc.get(i).component_id == id && cc.get(i).type == t)
            {
                c = cc.get(i);
                return true;
            }
        }
        return false;
    }

    //check if "iid inside oid" is  already found 
    static Boolean containedContains(ArrayList<ContainType> ins, int oid, int iid)
    {
        // System.out.println("called");
        for(int i = 0; i<ins.size();i++)
        {
            //check by comparing iid and oid with ids of inner and outer shape
            if(ins.get(i).out_id == oid && ins.get(i).in_id == iid)
            {
                return true;
            }
        }
        return false;
    }

    //check if "iid inscribed in oid" is  already found 
    static Boolean inscribeContains(ArrayList<InscribeType> ins, int oid, int iid)
    {
        for(int i = 0; i<ins.size();i++)
        {
            //check by comparing iid and oid with ids of inner and outer shape
            if(ins.get(i).circum_id == oid && ins.get(i).in_id == iid)
            {
                return true;
            }
        }
        return false;
    }

    //check if "k" is present in integer array "arr"
    static Boolean arrayContains(int[] arr, int k)
    {
        for(int i = 0;i<arr.length;i++)
        {
            if(arr[i] == k)
                return true;
        }
        return false;
    }

    //check if the vertex "vid" is end point of an s=association of shape1, if so, return associated line id in aid
    static Boolean isAssociationVertex(Shape shape1, int vid, Extracted_Information all_data, Integer aid)
    {
        for(int i = 0; i < all_data.table.size(); i++)
        {
            Vertex_Edge ve = all_data.table.get(i);
            //check each entry in vertex edge table, if it involved vertex "vid"
            if(ve.v1 == vid || ve.v2 == vid)
            {
                //if yes, check if the corresponding edge is an association of shape1
                if(shape1.associations.contains(ve.e))
                {
                    aid = ve.e;
                    return true;
                }
            }
        }
        return false;
    }

    //calculate ratio in which junction "jn" with coordinates "(x, y)" divides Line "l" with endpoints "v1, v2"
    static String junction_ratio(Junction jn, Line l, Vertex v1, Vertex v2, double x, double y)
    {
        //calculate distance of v1 and v2 from junction
        double d1 = Math.sqrt((v1.x - x)*(v1.x - x) + (v1.y - y)*(v1.y - y));
        double d2 = Math.sqrt((v2.x - x)*(v2.x - x) + (v2.y - y)*(v2.y - y));     
        //normalize distances to bring the total out of 100, i.e, ratio is m:(100-m)
        double p = (d1*100)/(d1 + d2), q;
        p = Math.round(p);
        q = 100 - p;
        //if any of the distance is 0, it means junction coincides with vertex, so return an empty string
        if(p ==0 || q == 0)
            return "";
        //else return the ratio
        return (int)p+":"+(int)q;
    }

    //calculate ratio in which junction "jn" divides Line "l" 
    static String junction_ratio(Junction jn, Line l, Extracted_Information all_data)
    {
        //extract vertices of line l
        Vertex v1 = getVertexWithId(all_data.vertices, l.v1);
        Vertex v2 = getVertexWithId(all_data.vertices, l.v2);

        double d1, d2;
        //if junction coincides with a vertex, calculate ratio w.r.t. vertex coordinates
        if(jn.vid != -1)
        {
            Vertex v = getVertexWithId(all_data.vertices, jn.vid);
            return junction_ratio(jn, l, v1, v2, v.x, v.y);
        }
        else //else calculate ratio w.r.t. junction coordinates
        {       
            return junction_ratio(jn, l, v1, v2, jn.x, jn.y);
        }
    }

    //check in which quadrant does (x,y) lie for circle c
    static int getQuadrant(Circle c, double x, double y)
    {
        // System.out.println("getQuadrant: "+c.x+" "+c.y+" "+x+" "+y);
        // 1st quadrant 
        if (x >= c.x-6 && y <= c.y+6) 
            return 1; 
      
        // 2nd quadrant 
        if (x < c.x && y <= c.y) 
            return 2; 
      
        // 3rd quadrant 
        if (x < c.x && y > c.y) 
            return 3; 
      
        // 4th quadrant 
        if (x >= c.x && y > c.y) 
            return 4; 
        return -1;
    }

    //prints all information for lines
    static void printLine(ArrayList<Line> line)
    {
        System.out.println("Lines: ");
        for(int i = 0;i < line.size(); i++)
        {
            Line l = line.get(i);
            System.out.println("Id: "+l.id+"\t"+l.points[0]+" "+l.points[1]+" "+l.points[2]+" "+l.points[3]+" slope: "+l.slope+" length "+l.length+" v1: "+l.v1+" v2 "+l.v2+" label_name "+l.label_name+" label_length: "+l.label_length+" label_slope:"+l.label_slope);
        }
    }

    //prints all information for vertices
    static void printVertex(ArrayList<Vertex> vertex)
    {
        System.out.println("Vertices: ");
        for(int i = 0;i < vertex.size(); i++)
        {
            Vertex l = vertex.get(i);
            System.out.println("Id: "+l.id+"\t"+l.x+" "+l.y+" label_name "+l.label_name);
        }
    }

    //return names of all vertex present in the list in one string
    static String vertex_names(Extracted_Information all_data, ArrayList<Integer> vertices)
    {
        String des = "";
        for(int j = 0 ;j <vertices.size(); j++)
        {
            if(j!=0)
            {
                if(j < vertices.size()-1)
                    des += ", ";
                else if( j == vertices.size()-1)
                    des += " and ";
            }
            Vertex l = Utilities.getVertexWithId(all_data.vertices, vertices.get(j));
            des += Utilities.vertex_name(l, all_data);
        }
        return des;
    }

    //check if a ray starting from outside the polygon(before xmin) and ending at (x,y) intersects shape line "L"
    static Boolean ray_intersects(Extracted_Information all_data, double x, double y, Line l, double xmin)
    {
        //coordinates of the ray (x,y) to point outside polygon
        double v1x1 = x, v1y1 = y, v1x2 = xmin - (1/y), v1y2 = y;
        //vertices of the shape line
        Vertex v1 = getVertexWithId(all_data.vertices, l.v1), v2 = getVertexWithId(all_data.vertices, l.v2);

        //equation of "infinite" ray in the form Ax+By+c
        double a1 = v1y2 - v1y1;
        double b1 = v1x1 - v1x2;
        double c1 = (v1x2 * v1y1) - (v1x1 * v1y2);

        //checking if v1 and v2 lies on ray
        double d1 = (a1 * v1.x) + (b1 * v1.y) + c1;
        double d2 = (a1 * v2.x) + (b1 * v2.y) + c1;

        //if d1 and d2 have same sign: v1 and v2 lies on the same side of the ray and thus no intersection
        if (d1 > 0 && d2 > 0) return false;
        if (d1 < 0 && d2 < 0) return false;

        //if here, it means line intersected "infinite" ray. to check if it intersceted "finite" ray, check if "finite" ray intersected "infinite" shape line
        //equation of "infinite" shape_line in the form Ax+By+c
        double a2 = v2.y - v1.y;
        double b2 = v1.x - v2.x;
        double c2 = (v2.x * v1.y) - (v1.x * v2.y);

        d1 = (a2 * v1x1) + (b2 * v1y1) + c2;
        d2 = (a2 * v1x2) + (b2 * v1y2) + c2;

        if (d1 > 0 && d2 > 0) return false;
        if (d1 < 0 && d2 < 0) return false;

        //if here, then they either intersect or are collinear
        //if collinear, return false
        if ((a1 * b2) - (a2 * b1) == 0.0f) return false;
        //else intersect and return true
        return true;

    }

    static Boolean pointContains(Extracted_Information all_data, Shape s, double x, double y)
    {
        double xmin = Double.MAX_VALUE, xmax = Double.MIN_VALUE, ymin = Double.MAX_VALUE, ymax = Double.MIN_VALUE;
        //get coordinates of minimum rectangle covering the shape
        for (int i = 0; i < s.vertices.length; i++)
        {
            Vertex v1 = getVertexWithId(all_data.vertices, s.vertices[i]);
            if(xmin>v1.x)
                xmin = v1.x;
            if(xmax < v1.x)
                xmax = v1.x;
            if(ymin > v1.y)
                ymin = v1.y;
            if(ymax < v1.y)
                ymax = v1.y;

        }
        //if the vertex is outside the minimum rectangle covering the shape, then it is outside
        if(x <xmin || x > xmax || y < ymin || y > ymax)
            return false;

        //for a polygon, a ray from outside the polygon to the vertex will intersect the polygon odd number of times if the vertex is inside the polygon
        int num = 0;
        //for all shape lines, check if the ray intersects the line
        for (int i = 0; i < s.line_components.size(); i++) {
            if(ray_intersects(all_data, x, y, getLineWithId(all_data.lines, s.line_components.get(i)), xmin))
                num++;
        }
        //intersect odd number of times
        if ((num & 1) == 1) {
            return true;
        } else {
            return false;
        }
    }

    //checks if all vertices in "vlist" are inside Shape s
    static Boolean is_inside(Extracted_Information all_data, ArrayList<Integer> vlist, Shape s)
    {
        Boolean flag = true;
        for(int i = 0;i <vlist.size();i++)
        {
            Vertex v = Utilities.getVertexWithId(all_data.vertices, vlist.get(i));
            if(!Utilities.pointContains(all_data, s, v.x, v.y))
            {
                flag = false;
                break;
            }
        }
        return flag;
    }

    //replace "_" with " " in string "v"
    static String format_shape_name(String v)
    {
        String[] n = v.split("_", -1);
        v = String.join(" ", n);
        return v;
    }

    //check if "id" is an edge of shape s
    static Boolean is_edge(int id, Shape s)
    {
        for(int i = 0; i<s.line_components.size(); i++)
        {
            if(s.line_components.get(i)==id)
                return true;
        }
        return false;
    }

    /// Shortest distance between p0 and p1-----p2
    static double point_distance( Line l, double x, double y, Extracted_Information all_data){

        Vertex p1 = Utilities.getVertexWithId(all_data.vertices, l.v1);
        Vertex p2 = Utilities.getVertexWithId(all_data.vertices, l.v2);
        double vx = p2.x - p1.x, vy = p2.y - p1.y; // line vector
        double cpx = x - p1.x, cpy = y - p1.y; // vector from center to p1
        double dot = (cpx * vx)+(cpy * vy);
        double len_sq = (vx*vx + vy*vy);
        double t = -1; // t is used later for determining which case applies- (x,y) is between p1 and p2 OR (x,y) is near p1 OR (x,y) is near p2
        if (len_sq != 0) // check for division by zero 
            t = dot / len_sq; // 

        double xx, yy;

        if (t < 0){ // if (x,y) is near p1 and not between p1p2, find distance with p1
            xx = p1.x; yy = p1.y;
        } else if (t > 1){ // if (x,y) is near p and not between p1p2, find distance with p2
            xx = p2.x; yy = p2.y;
        } else { // if (x,y) is between p1p2, find distance with the point where perpendicular is dropped
            xx = p1.x + t * vx;
            yy = p1.y + t * vy;
        }

        double dx = x - xx;
        double dy = y - yy;
        return Math.sqrt(dx * dx + dy * dy); //return distance

    }

}