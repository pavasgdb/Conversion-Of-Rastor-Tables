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

public class PrimitiveJunction{

	//return associated_line id for shape s
	static Associated_Line findAssociatedLine(Shape s, int id)
	{
		//traverse on all associated lines and return line having line_id same as id
		for(int j = 0; j < s.associations.size(); j++)
		{
			if(s.associations.get(j).line_id == id)
			{
				return s.associations.get(j);
			}
		}
		return new Associated_Line();
	}

	//intersection of primitive line with another primitive line
	static void lineLine(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Line l1, Line l2, Shape s1, Shape s2, GroupWise group, HashMap<Integer, Integer> junctionVisited)
	{
		//two lines can intersect each other only at a single point
		//loop over both arraylist<Junction>
		for(int i = 0; i<shapeJunction1.size()+shapeJunction2.size(); i++)
		{
			Junction current;
			int map1, map2;
			//getting element from the correct arraylist
			if(i<shapeJunction1.size()) 
			{
				//shape1 always maps to id1 of shapeJunction1
				current = shapeJunction1.get(i);
				map1 = 1;
				map2 = 2;
			}
			else
			{
				//shape1 always maps to id2 of shapeJunction2
				current = shapeJunction2.get(i - shapeJunction1.size());
				map1 = 2;
				map2 = 1;
			}

			if(current.vid == -1) //if junction is not at a vertex, it is an X-intersection
			{
				//if jn is not visited, then store x-type intersection and mark the jn visited
				if(junctionVisited.get(current.id)!=1)
				{
					group.xt_type.add(new XnT_Type(s1.id, s2.id, map1, map2, current.id, "X"));
					junctionVisited.put(current.id, 1);
				}
			}
			else //T-type or V-type
			{
				Boolean a = (l1.v1 == current.vid || l1.v2 == current.vid);
				Boolean b = (l2.v1 == current.vid || l2.v2 ==current.vid); 

				if(a && b)//if both l1 and l2 involves the intersecting vertex, it is V-intersection(i.e., common component)
				{
					int i1 = s1.id, i2 = s2.id;
					String t1 = "Shape", t2 = "Shape";
					//If shape is circle, then type is "association", else type is "shape"
					if(s1.shape_name == "Circle")
					{
						i1 = l1.id;
						t1 = "Association";
					}
					if(s2.shape_name == "Circle")
					{
						i2 = l2.id;
						t2 = "Association";
					}
					
					// System.out.println(current.vid+ " Vertex Common");
					CommonComponent comp = new CommonComponent();
					//if there is already an entry of this common component
					if(Utilities.commonComponentContains(group.common, current.vid, "Vertex", comp))
					{
						//add shape1 and its type if not already present
						if(!comp.shape_id.contains(i1))
						{
							comp.shape_id.add(i1);	
							comp.type_id.add(t1);
						}
						//add shape2 and its type if not already present
						if(!comp.shape_id.contains(i2))
						{		
							comp.shape_id.add(i2);
							comp.type_id.add(t2);	
						}
					}
					else //if this is new entry
					{
						ArrayList<Integer> temp = new ArrayList<Integer>();
						temp.add(i1);
						temp.add(i2);
						ArrayList<String> tp = new ArrayList<String>();
						tp.add(t1);
						tp.add(t2);
						group.common.add(new CommonComponent(current.vid, "Vertex", temp, tp));
					}
				}
				else // if either l1 or l2, but not both, involves vertex, it is a T-intersection(Always store shape involving "-" first and the "|")
				{
					if(a)
					{
						//Shape2 always maps to map2(Shape2 is "-" and shape1 is "|")
						if(junctionVisited.get(current.id)!=1)
						{
							group.xt_type.add(new XnT_Type(s2.id, s1.id, map2, map1, current.id, "T"));
							junctionVisited.put(current.id, 1);
						}
					}
					else if(b)
					{
						//Shape1 always maps to map1(Shape1 is "-" and shape2 is "|")
						if(junctionVisited.get(current.id)!=1)
						{
							group.xt_type.add(new XnT_Type(s1.id, s2.id, map1, map2, current.id, "T"));
							junctionVisited.put(current.id, 1);
						}
					}
				}
			}
		}
		return;
	}

	//intersection of primitive line with primitive(not shape) circle: shape circle will hav associations
	static void lineCircle(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Line l1, Circle c, Shape shape1, Shape shape2, GroupWise group, HashMap<Integer, Integer> junctionVisited)
	{
		//Shape1 is line Shape2 is Circle
		
		for(int i = 0; i<shapeJunction1.size()+shapeJunction2.size();i++)
		{
			Junction jn;
			String type1, type2;
			int id1, id2, map1, map2;
			//getting element from the correct arraylist
			if(i < shapeJunction1.size())
			{
				jn = shapeJunction1.get(i);
				type1 = jn.type1;
				type2 = jn.type2; // line is type1 and circle is type2
				id1 = jn.id1;
				id2 = jn.id2; // here Line l is id1 and associated line is id2
				map1 = 1;
				map2 = 2;
			}
			else
			{
				jn = shapeJunction2.get(i - shapeJunction1.size());
				type1 = jn.type2;
				type2 = jn.type1; //here Line l is type2 and circle is type1
				id1 = jn.id2;
				id2 = jn.id1; //here Line l is id2 and associated line is id1
				map1 = 2;
				map2 = 1;
			}

			if(jn.vid == -1) //if junction is not vertex: X-type interscetion of line and circle 
			{
				if(junctionVisited.get(jn.id) != 1)
				{
					group.xt_type.add(new XnT_Type(shape1.id, shape2.id, map1, map2, jn.id, "X"));
					junctionVisited.put(jn.id, 1);
				}
			}
			else//T-type of line and circle 
			{
				if(junctionVisited.get(jn.id) != 1)
				{
					group.xt_type.add(new XnT_Type(shape2.id, shape1.id, map2, map1, jn.id, "T"));
					junctionVisited.put(jn.id, 1);
				}
			}
		}

		//vertex v1 and v2 of line
		Vertex v1 = Utilities.getVertexWithId(all_data.vertices, l1.v1);
		Vertex v2 = Utilities.getVertexWithId(all_data.vertices, l1.v2);
		//distances of v1 and v2 from center of circle
		double d1 = Math.sqrt((v1.x - c.x)*(v1.x - c.x) + (v1.y - c.y)*(v1.y - c.y));
		double d2 = Math.sqrt((v2.x - c.x)*(v2.x - c.x) + (v2.y - c.y)*(v2.y - c.y));
		//if  v1 close to center of circle, add vertex at center
		if(d1 <= 6)
		{
			//if entry for this circle is present, just add this vertex
			if(group.vertexAtCenter.containsKey(c.id)) 
				group.vertexAtCenter.get(c.id).getValue().add(l1.id);
			else //create a new entry for this circle
				group.vertexAtCenter.put(c.id, new Pair<Integer, ArrayList<Integer>>(v1.id, new ArrayList<Integer>(l1.id)));
		}
		//same as above for v2
		if(d2 <= 6)
		{
			if(group.vertexAtCenter.containsKey(c.id))
				group.vertexAtCenter.get(c.id).getValue().add(l1.id);
			else
				group.vertexAtCenter.put(c.id, new Pair<Integer, ArrayList<Integer>>(v2.id, new ArrayList<Integer>(l1.id)));
		}

		return;
	}

	//intersection of primitive circle with primitive(not shape) circle: shape circle will hav associations
	static void circleCircle(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Circle c1, Circle c2, Shape s1, Shape s2, GroupWise group, HashMap<Integer, Integer> junctionVisited)
	{
		int j_cnt = 0;
		//getting element from the correct arraylist
		for(int i = 0 ; i < shapeJunction1.size()+ shapeJunction2.size(); i++)
		{
			Junction jn;
			int map1, map2;
			if(i < shapeJunction1.size())
			{
				jn = shapeJunction1.get(i);
				map1 = 1;
				map2 = 2;
			}
			else
			{
				jn = shapeJunction2.get(i - shapeJunction1.size());
				map1 = 2;
				map2 = 1;
			}
			//two circles can intersect as "X" or "8": both stored as "X"-type
			if(junctionVisited.get(jn.id)!=1)
			{
				group.xt_type.add(new XnT_Type(s1.id, s2.id, map1, map2, jn.id, "X"));
				junctionVisited.put(jn.id, 1);
			}
		}
		return;
	}

}