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

public class CircleShapeEnrichment{

	//checks if v is inside c
	Boolean is_inside_circle(Extracted_Information all_data, Vertex v, Circle c)
	{
		double d = Math.sqrt((v.x - c.x)*(v.x - c.x) + (v.y - c.y)*(v.y - c.y));
		//if distance of v from center of c <= radius then inside c
		if( c.r >= d )
			return true;
		return false;
	}

	//checks if shape is contained in shape
	Boolean shape_inside_circle(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Circle c, Shape shape2, HashMap<Integer, Integer> junctionVisited, int[] junc, HashMap<Integer, Integer> v_map, ArrayList<Pair<Junction, Pair<Integer, Integer>>> oj, ArrayList<Integer> iv, ArrayList<Integer> ov, GroupWise group, Shape shape1)
	{
		int f = 0;
		for (Map.Entry<Integer, Integer> entry : v_map.entrySet())  
		{
			if(entry.getValue() != 1) //if the vertex is not on cirumference, check if it is inside the circle
			{
				Vertex v = Utilities.getVertexWithId(all_data.vertices, entry.getKey());
				if(is_inside_circle(all_data, v, c))
				{
					iv.add(entry.getKey());
					v_map.put(entry.getKey(), 1);
				}
				else return false;
			}
		}
		group.contain.add(new ContainType(shape1.id, shape2.id, oj, iv, ov));
		return true;
	}

	
    //checks if circle is contained in shape
	Boolean circle_inside_shape(Extracted_Information all_data, Circle c1, Shape shape2, Shape shape1)
	{
		Boolean r2 = true;
		//check if all vertices are outside circle by comparing its distance from the center of circle with the radius of circle
		for(int i = 0 ;i<shape2.vertices.length; i++)
		{
			Vertex v = Utilities.getVertexWithId(all_data.vertices, shape2.vertices[i]);
			double d = Math.sqrt((v.x - c1.x)*(v.x - c1.x) + (v.y - c1.y)*(v.y - c1.y));
			if(d < c1.r)
			{
				r2 = false;
				break;
			}
		}
		// r2 is true means there is "no" vertex which is inside the circle
		if(r2 == true)
		{
			//check if circle center is within the shape
			if(Utilities.pointContains(all_data, shape2, c1.x, c1.y))
			{
				r2 = true;
				//check if all lines are outside the circle
				for(int i = 0 ;i<shape2.line_components.size(); i++)
				{
					Line l = Utilities.getLineWithId(all_data.lines, shape2.line_components.get(i));
					//check perpenndicular distance of line "segment" from circle center
					double d = Utilities.point_distance(l, c1.x, c1.y, all_data);
					if(d < c1.r-1)
					{
						r2 = false;
						break;
					}
				}
				//r2 is true means all lines are outside the circle
				if(r2 == true)
				{
					return true;
					// group.contain.add(new ContainType(shape2.id, shape1.id, oj, iv, ov));
				}
			}
		}
		return false;
	}

	//check if shape is inscribed in circle
	void shapeInscribed(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Shape shape1, Shape shape2, HashMap<Integer, Integer> junctionVisited, GroupWise group)
	{
		//shape1 is circle always
		int num_vertices = 0, j=0, k=0;
		int[] junc = new int[shapeJunction1.size() + shapeJunction2.size()];
		HashMap<Integer, Integer> v_map = new HashMap<Integer, Integer>(); 
		
		Circle c = Utilities.getCircleWithId(all_data.circles, shape1.circle_components.get(0));	

		for(int i = 0;i<shape2.vertices.length; i++)
		{
			v_map.put(shape2.vertices[i], 0); //stores which vertex has already been seen
		}
		//oj is <junction, map> list, iv and ov are list of vertices that are inside and on the circle
		ArrayList<Pair<Junction, Pair<Integer, Integer>>> oj = new ArrayList<Pair<Junction, Pair<Integer, Integer>>>() ;
		ArrayList<Integer> iv = new ArrayList<Integer>() ;
		ArrayList<Integer> ov = new ArrayList<Integer>() ;
		//marks all vertices of shapes that are intersection of shape and circle
		for(int i = 0; i < shapeJunction1.size()+shapeJunction2.size(); i++)
		{
			Junction jn;
			int map1, map2, id1, id2;
			String type1, type2;
			//getting element from the correct arraylist
			if( i <shapeJunction1.size())
			{
				jn = shapeJunction1.get(i);
				map1 = 1;
				map2 = 2;
				id1 = jn.id1;
				id2 = jn.id2;
				type1 = jn.type1;
				type2 = jn.type2;
			}
			else
			{
				jn = shapeJunction2.get(i - shapeJunction1.size());
				map1 = 2;
				map2 = 1;
				id1 = jn.id2;
				id2 = jn.id1;
				type1 = jn.type2;
				type2 = jn.type1;
			}
			// System.out.println("jn.id: "+jn.id+" jn.type1: "+jn.type1+" jn.type2: "+jn.type2+" jn.vid: "+jn.vid);

			//inscription holds only when junction coincides with vertices
			if(jn.vid != -1)
			{
				//if jn.vid is shape vertex and intersceting with circle
				if(Utilities.arrayContains(shape2.vertices, jn.vid) && type1 == "Circle")
				{
					//mark this vertex visited
					v_map.put(jn.vid, 1);
					junc[k++] = jn.id;
					// System.out.println("shapeJunction.get(i).id: "+jn.id+" vid: "+jn.vid+" k: "+k);
					oj.add(new Pair<Junction, Pair<Integer, Integer>>(jn, new Pair<Integer, Integer>(map2, map1)));
				}
			}
		}

		//count number of vertices of shape that lies on the circle
		for (Map.Entry<Integer, Integer> entry : v_map.entrySet())  
		{
			if(entry.getValue() == 1)
			{
				num_vertices+=1;
				ov.add(entry.getKey());
			}
			else
				iv.add(entry.getKey());
		}
		// System.out.println(num_vertices+" "+shape2.vertices.length+" insccription ");
		//if equal: inscribed
		if (num_vertices == shape2.vertices.length)
		{
			for(int i = 0 ;i<k; i++)//mark those junctions visited
			{
				junctionVisited.put(junc[i], 1);
			}
			group.inscribe.add(new InscribeType(shape1.id, shape2.id, oj));
		}
		else if(num_vertices > 0) // if some vertices are inside and some on the circumference of the circle: contain
		{
			if(shape_inside_circle(all_data, shapeJunction1, shapeJunction2, c, shape2, junctionVisited, junc, v_map, oj, iv, ov, group, shape1))
			{
				for(int i = 0 ;i<k; i++)
				{
					junctionVisited.put(junc[i], 1);
				}
				group.contain.add(new ContainType(shape2.id, shape1.id, oj, iv, ov));
			}
		}
		return;
	}

	//check if circle is inscribed in shape
	void circleInscribed(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Shape shape1, Shape shape2, HashMap<Integer, Integer> junctionVisited, GroupWise group)
	{
		//shape1 is circle
		int num_lines = 0, j=0, k=0;
		int[] junc = new int[shapeJunction1.size()+shapeJunction2.size()];

		Circle c = Utilities.getCircleWithId(all_data.circles, shape1.circle_components.get(0));
		

		HashMap<Integer, Integer> l_map = new HashMap<Integer, Integer>(); 
		for(int i = 0;i<shape2.line_components.size(); i++) //stores all lines whom the circle intersect 
		{
			l_map.put(shape2.line_components.get(i), 0);
		}

		//<junction, map>list of all junctions where circle intersect shape
		ArrayList<Pair<Junction, Pair<Integer, Integer>>> oj = new ArrayList<Pair<Junction, Pair<Integer, Integer>>>() ;
		//ov and iv are list of all lines of outer shape that are touched and not touched respectively by the circle
		ArrayList<Integer> iv = new ArrayList<Integer>() ;
		ArrayList<Integer> ov = new ArrayList<Integer>() ;

		for(int i = 0; i < shapeJunction1.size()+shapeJunction2.size(); i++)
		{
			Junction jn;
			int map1, map2, id1, id2;
			//getting element from correct array list
			if( i <shapeJunction1.size())
			{
				map1 = 1;
				map2 = 2;
				jn = shapeJunction1.get(i);
				id1 = jn.id1;
				id2 = jn.id2;
			}
			else
			{
				map1 = 2;
				map2 = 1;
				jn = shapeJunction2.get(i - shapeJunction1.size());
				id1 = jn.id2;
				id2 = jn.id1;
			}

			//in case of circle inscription, junction should "not" coincide with vertex, and should intersect a line of the outer shape
			if(shape2.line_components.contains(id2) && jn.vid == -1)
			{
				//mark this line visited
				l_map.put(jn.id2, 1);
				junc[k++] = jn.id; //mark this junction visited
				oj.add(new Pair<Junction, Pair<Integer, Integer>>(jn, new Pair<Integer, Integer>(map2, map1)));//add the correct <junction, map> pair in oj
			}
		}

		//add all the lines involved in intersection with the circle in ov
		for (Map.Entry<Integer, Integer> entry : l_map.entrySet())  
		{
			// System.out.println(entry.getKey()+" "+entry.getValue());
			if(entry.getValue() == 1)
			{
				num_lines +=1;

				ov.add(entry.getKey());	
			}
		}
		if (num_lines == shape2.line_components.size()) //if circle touches all_lines: circle is inscribed
		{
			for(int i = 0 ;i<k; i++)
			{
				junctionVisited.put(junc[i], 1);
			}
			group.inscribe.add(new InscribeType(shape2.id, shape1.id, oj));
		}
		else if(num_lines > 0) //else check if circle is inside the shape
		{
			if(circle_inside_shape(all_data, c, shape2, shape1))
			{
				group.contain.add(new ContainType(shape2.id, shape1.id, oj, iv, ov));
				for(int i = 0 ;i<k; i++)
				{
					junctionVisited.put(junc[i], 1);
				}
			}
		}
		return;
	}

	//checks if a vertex of the shape is at the circle center
	void vertexAtCenter(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Shape shape1, Shape shape2, HashMap<Integer, Integer> junctionVisited, GroupWise group)
	{
		Circle c = Utilities.getCircleWithId(all_data.circles, shape1.circle_components.get(0));	
		
		//for each vertex, check its distance from circle center
		for(int i = 0; i<shape2.vertices.length; i++)
		{
			//calculate distance of vertex v from circle center
			Vertex v = Utilities.getVertexWithId(all_data.vertices, shape2.vertices[i]);
			double d = Math.sqrt((v.x - c.x)*(v.x - c.x) + (v.y - c.y)*(v.y - c.y));
			// System.out.println("Vertex "+Utilities.vertex_name(v, all_data)+" distance: "+d+" radius: "+c.r);

			//if near center
			if(d <= 6)
			{
				int lid1 = -1, lid2 = -1;
				//get lines involving the vertex in the shape(note: vertex-edge table is not used because it may be the case that even non-shape edges include this vertex, we want the shape edges)
				for(int j = 0; j<shape2.vertices.length; j++)
				{
					if(shape2.vertices[j] == v.id)
					{
						//for each vertex, find the two lines that include the vertex
						if(j!=0)
						{
							lid1 = shape2.line_components.get(j-1);
							lid2 = shape2.line_components.get(j);
						}
						else
						{
							lid1 = shape2.line_components.get(shape2.line_components.size()-1);
							lid2 = shape2.line_components.get(0);
						}
						break;
					}
				}
				//shapeJunction1, shapeJunction2 will contain intesections that include circle and shape: Not shape and shape. Thus this will never be satisfied
				/*//find the intersection that includes the two lines found above and mark it visited
				for(int j = 0; j < shapeJunction.size(); j++)
				{
					if(shapeJunction.get(j).id1 == lid1 || shapeJunction.get(j).id1 == lid2 || shapeJunction.get(j).id2 == lid1 || shapeJunction.get(j).id2 == lid2)
					{
						junctionVisited.put(shapeJunction.get(j).id, 1);
					}
				}*/

				//add them as vertex at center
				ArrayList<Integer> temp = new ArrayList<Integer>();
				temp.add(lid1);
				temp.add(lid2);
				group.vertexAtCenter.put(c.id, new Pair<Integer, ArrayList<Integer>>(v.id, temp));
			}
		}
		return;
	}

	//for remaining junctions, classify them as X or T type
	void remaining(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Shape shape1, Shape shape2, HashMap<Integer, Integer> junctionVisited, GroupWise group)
	{
		for(int i = 0;i < shapeJunction1.size()+ shapeJunction2.size(); i++)
		{
			Junction jn;
			int map1, map2;
			//getting element from correct arraylist
			if( i <shapeJunction1.size())
			{
				map1 = 1;
				map2 = 2;
				jn = shapeJunction1.get(i);
			}
			else
			{
				map1 = 2;
				map2 = 1;
				jn = shapeJunction2.get(i - shapeJunction1.size());
			}
			//for all unvisited jn, check if it is a T type or Xtype interscetion
			if(junctionVisited.get(jn.id)!=1)
			{
				if(jn.vid != -1)//T type
				{
					group.xt_type.add(new XnT_Type(shape1.id, shape2.id, map1, map2, jn.id, "T"));
				}
				else//Xtype
				{
					group.xt_type.add(new XnT_Type(shape1.id, shape2.id, map1, map2, jn.id, "X"));	
				}
				//mark jn visited
				junctionVisited.put(jn.id, 1);
			}
		}
		return;
	}

	void enrich(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Shape shape1, Shape shape2, HashMap<Pair<Integer, Integer>, ArrayList<Junction>> primitive_wise, GroupWise group, HashMap<Integer, Integer> junctionVisited)
	{
		//Shape1 is always circle
		
		shapeInscribed(all_data, shapeJunction1, shapeJunction2, shape1, shape2, junctionVisited, group);
		if(!(shape1.shape_name.equals("Non_Convex_Shape") || (shape1.shape_name.equals("Non_Convex_Shape"))))
			circleInscribed(all_data, shapeJunction1, shapeJunction2, shape1, shape2, junctionVisited, group);
		vertexAtCenter(all_data, shapeJunction1, shapeJunction2, shape1, shape2, junctionVisited, group);
		remaining(all_data, shapeJunction1, shapeJunction2, shape1, shape2, junctionVisited, group);
		return;
	}
}