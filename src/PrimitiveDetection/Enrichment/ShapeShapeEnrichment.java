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
import src.PrimitiveJunction.*;

public class ShapeShapeEnrichment{


	//check for remaining lines if they intersect in X or T type
	void remaining(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Shape shape1, Shape shape2, HashMap<Integer, Integer> junctionVisited, HashMap<Pair<Integer, Integer>, ArrayList<Junction>> primitive_wise, GroupWise group)
	{
		for(int i = 0; i<shapeJunction1.size() + shapeJunction2.size();i++)
		{
			Junction jn;
			String type1, type2;
			int id1, id2;
			//getting element from correct arraylist
			if(i < shapeJunction1.size())
			{
				jn = shapeJunction1.get(i);
				type1 = jn.type1;
				type2 = jn.type2;
				id1 = jn.id1;
				id2 = jn.id2;
			}
			else
			{
				jn = shapeJunction2.get(i - shapeJunction1.size());
				type1 = jn.type2;
				type2 = jn.type1; 
				id1 = jn.id2;
				id2 = jn.id1; 
			}

			if(junctionVisited.get(jn.id) == 1)
				continue;

			//if involved lines are part of same shape, then ignore
			if((shape1.line_components.contains(id1) && shape1.line_components.contains(id2)) || (shape2.line_components.contains(id1) && shape2.line_components.contains(id2)))
					continue;

			//sj1 and sj2 are primitive wise junction list of primitive line l and primitive shape-line/primitive shape-association
			ArrayList<Junction> sj1 = new ArrayList<Junction>();
			Pair<Integer, Integer> p = new Pair<Integer, Integer>(id1, id2); 
			if(primitive_wise.containsKey(p))
				sj1 = primitive_wise.get(p);

			ArrayList<Junction> sj2 = new ArrayList<Junction>();
			p = new Pair<Integer, Integer>(id2, id1); 
			if(primitive_wise.containsKey(p))
				sj2 = primitive_wise.get(p);


			Line l1 = Utilities.getLineWithId(all_data.lines, id1);
			Line l2 = Utilities.getLineWithId(all_data.lines, id2);
			//find lineLine intersection of the incvolved lines
			PrimitiveJunction.lineLine(all_data, sj1, sj2, l1, l2, shape1, shape2, group, junctionVisited);
		}
		return;
	}

	
	//check if shape1 is inscribed in shape2 or vice versa or none
	void inscribed(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Shape shape1, Shape shape2, HashMap<Integer, Integer> junctionVisited, GroupWise group)
	{
		int num_vertices1 = 0, num_vertices2 = 0, j=0, k=0;
		//j1 is junction visited list corresponding to shape1 being the inner shape, and j2 is the same list corresponding to shape2 being the inner shape
		int[] j1 = new int[shapeJunction1.size()+shapeJunction2.size()], j2 = new int[shapeJunction1.size()+ shapeJunction2.size()];
		HashMap<Integer, Integer> v1 = new HashMap<Integer, Integer>(), v2 = new HashMap<Integer, Integer>(); 

		String poly_name1 = shape1.shape_name+" "+Utilities.polyName(all_data, shape1), poly_name2= shape2.shape_name+" "+Utilities.polyName(all_data, shape2);

		//v1 and v2 are list of vertices of shape 1 and shape2 and all are marked "unvisited"
		for(int i = 0;i<shape1.vertices.length; i++)
		{
			v1.put(shape1.vertices[i], 0);
		}
		for(int i = 0;i<shape2.vertices.length; i++)
		{
			v2.put(shape2.vertices[i], 0);
		}

		//oj1 is <junction,map> list corresponding to shape1 being the inner shape, and oj2 is the same list corresponding to shape2 being the inner shape
		ArrayList<Pair<Junction, Pair<Integer, Integer>>> oj1 = new ArrayList<Pair<Junction, Pair<Integer, Integer>>>();
		ArrayList<Pair<Junction, Pair<Integer, Integer>>> oj2 = new ArrayList<Pair<Junction, Pair<Integer, Integer>>>();

		//iv1 and ov1 are the vertices inside and on shape 2 when shape1 is the inner shape
		ArrayList<Integer> iv1 = new ArrayList<Integer>();
		ArrayList<Integer> ov1 = new ArrayList<Integer>();
		//iv2 and ov2 are the vertices inside and on shape 1 when shape2 is the inner shape
		ArrayList<Integer> iv2 = new ArrayList<Integer>();
		ArrayList<Integer> ov2 = new ArrayList<Integer>();

		//find the number of vertices involved in intersection for shape1 and shape2
		for(int i = 0; i < shapeJunction1.size() + shapeJunction2.size(); i++)
		{
			Junction jn;
			int map1, map2, id1, id2;//id1 always correspond to shape1 and id2 to shape2
			//getting element from the correct arraylist
			if(i < shapeJunction1.size())
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
			//for inscription, the junction must be a vertex(of the inner shape)
			if(jn.vid != -1)
			{
				//if jn is a vertex of shape1, intersecting edge of shape2
				if(Utilities.arrayContains(shape1.vertices, jn.vid) && Utilities.is_edge(id2, shape2))
				{
					v1.put(jn.vid, 1);//mark this vertex visited in shape1-vertices
					j1[j++] = jn.id;//mark this junction visited in shape1 jn list
					oj1.add(new Pair<Junction, Pair<Integer, Integer>>(jn, new Pair<Integer, Integer>(map2, map1)));//add this junction with correct mapping in shape1 jn list
					// System.out.println("shapeJunction.get(i).id: "+shapeJunction.get(i).id+" j: "+j);
				}
				//if jn is a vertex of shape2, intersecting edge of shape1
				if(Utilities.arrayContains(shape2.vertices, jn.vid) && Utilities.is_edge(id1, shape1))
				{
					v2.put(jn.vid, 1);//mark this vertex visited in shape2-vertices
					j2[k++] = jn.id;//mark this junction visited in shape2 jn list
					oj2.add(new Pair<Junction, Pair<Integer, Integer>>(jn, new Pair<Integer, Integer>(map1, map2)));//add this junction with correct mapping in shape2 jn list
					// System.out.println("shapeJunction.get(i).id: "+shapeJunction.get(i).id+" k: "+k);
				}
			}
		}
		//fill ov1 and iv1 with vertices that lie on and inside shape2 when shape1 is inner shape
		for (Map.Entry<Integer, Integer> entry : v1.entrySet())  
		{
			if(entry.getValue() == 1)
			{
				ov1.add(entry.getKey());
				num_vertices1+=1;
			}
			else
			{
				iv1.add(entry.getKey());
			}
		}
		//fill ov2 and iv2 with vertices that lie on and inside shape1 when shape2 is inner shape
		for (Map.Entry<Integer, Integer> entry : v2.entrySet())  
		{
			if(entry.getValue() == 1)
			{
				ov2.add(entry.getKey());
				num_vertices2+=1;
			}
			else
			{
				iv2.add(entry.getKey());
			}
		}

		// System.out.println("num_vertices1: "+num_vertices1+" num_vertices2: "+num_vertices2);

		//inner shape can be non-convex, but not the outer shape
		//if all shape1 vertices lie on shape2, then shape1 is inscribed in shape 2
		if(num_vertices1 == shape1.vertices.length && !shape2.shape_name.equals("Non_Convex_Shape"))
		{
			//mark visited for all marked junctions in j1
			for(int i = 0 ;i<j; i++)
			{
				junctionVisited.put(j1[i], 1);
			}
			//if this combination of shape1 inside shape2 is not already present then add
			if(!Utilities.inscribeContains(group.inscribe, shape2.id, shape1.id))
				group.inscribe.add(new InscribeType(shape2.id, shape1.id, oj1));
		}
		//if all shape2 vertices lie on shape1, then shape2 is inscribed in shape 1
		else if (num_vertices2 == shape2.vertices.length && !shape1.shape_name.equals("Non_Convex_Shape"))
		{
			//mark visited for all marked junctions in j2
			for(int i = 0 ;i<k; i++)
			{
				junctionVisited.put(j2[i], 1);
			}

			//if this combination of shape2 inside shape1 is not already present then add
			if(!Utilities.inscribeContains(group.inscribe, shape1.id, shape2.id))
				group.inscribe.add(new InscribeType(shape1.id, shape2.id, oj2));
		}
		else 
		{
			//if all shape1 vertices dont lie on shape2 and other vertices are inside shape2, then shape1 is contained inside shape2
			
			if(num_vertices1 < shape1.vertices.length && !shape2.shape_name.equals("Non_Convex_Shape"))
			{
				//check if other vertices are inside shape2
				if(Utilities.is_inside(all_data, iv1, shape2))
				{
					if(!Utilities.containedContains(group.contain, shape2.id, shape1.id))
					{
						// System.out.println("1: Shape1: "+shape1.id+" "+"shape2 "+shape2.id+" contain "+group.contain.size());
						for(int i = 0 ;i<j; i++)
						{
							junctionVisited.put(j1[i], 1);
						}
						group.contain.add(new ContainType(shape2.id, shape1.id, oj1, iv1, ov1));
					}
				}
			}
			//if all shape2 vertices dont lie on shape1 and other vertices are inside shape1, then shape2 is contained inside shape1
			if (num_vertices2 < shape2.vertices.length && !shape1.shape_name.equals("Non_Convex_Shape"))
			{
				// System.out.println("is_inside2");
				if(Utilities.is_inside(all_data, iv2, shape1))
				{
					if(!Utilities.containedContains(group.contain, shape1.id, shape2.id))
					{
						for(int i = 0 ;i<k; i++)
						{
							junctionVisited.put(j2[i], 1);
						}
						// System.out.println("Shape1: "+shape1.id+" "+"shape2 "+shape2.id+" contain "+group.contain.size());
						group.contain.add(new ContainType(shape1.id, shape2.id, oj2, iv2, ov2));
					}
				}
			}
		}
		return;
	}

	//finds common lines between shape1 and shape2
	void commonLine(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Shape shape1, Shape shape2, HashMap<Integer, Integer> junctionVisited, GroupWise group)
	{
		//array list of line and vertex ids
		ArrayList<Integer> lid = new ArrayList<Integer>();
		ArrayList<Integer> vid = new ArrayList<Integer>();
		
		for(int i = 0;i< shape1.line_components.size();i++)
		{
			//for all lines of shape1, check if they are present in shape2
			if(shape2.line_components.contains(shape1.line_components.get(i)))
			{
				Line l = Utilities.getLineWithId(all_data.lines, shape1.line_components.get(i));
				//if line is common, its vertices are also: add them in lid and vid
				lid.add(l.id);
				vid.add(l.v1);
				vid.add(l.v2);

				CommonComponent c = new CommonComponent();
				//if an entry for common line already exists, just add these shapes 
				if(Utilities.commonComponentContains(group.common, l.id, "Line", c))
				{
					if(!c.shape_id.contains(shape1.id))
					{
						c.shape_id.add(shape1.id);
						c.type_id.add("Shape");//this line id is a line component and not an association
					}
					if(!c.shape_id.contains(shape2.id))
					{
						c.shape_id.add(shape2.id);
						c.type_id.add("Shape");
					}
				}
				else//else create a new entry
				{		
					ArrayList<Integer> sid = new ArrayList<Integer>();
					ArrayList<String> tp = new ArrayList<String>();
					sid.add(shape1.id);
					tp.add("Shape");
					sid.add(shape2.id);
					tp.add("Shape");
					group.common.add(new CommonComponent(l.id, "Line", sid, tp));
				}
			}
		}
		//if no line is common, return
		if(lid.size()==0)
			return;

		//else find all junctions that contain the above found common vid and mark them visited
		for(int i = 0; i<shapeJunction1.size() + shapeJunction2.size();i++)
		{
			Junction jn;
			if(i < shapeJunction1.size())
				jn = shapeJunction1.get(i);
			else
				jn = shapeJunction2.get(i - shapeJunction1.size());

			if(jn.vid != -1)
			{
				if(vid.contains(jn.vid))
				{
					junctionVisited.put(jn.id, 1);
				}
			}
		}
		return;
	}
	
	//finds all vertices that are common in shape1 and shape2
	void commonVertex(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Shape shape1, Shape shape2, HashMap<Integer, Integer> junctionVisited, GroupWise group)
	{

		ArrayList<Integer> sid = new ArrayList<Integer>();
		ArrayList<String> tp = new ArrayList<String>();
		

		for(int i = 0; i<shapeJunction1.size() + shapeJunction2.size();i++)
		{
			Junction jn;
			//getting element from correct arraylist
			if(i < shapeJunction1.size())
				jn = shapeJunction1.get(i);
			else
				jn = shapeJunction2.get(i - shapeJunction1.size());

			//for all junctions that coincide with vertices, check if they are vertices of shape1 and shape2
			if(jn.vid != -1)
			{
				if(junctionVisited.get(jn.id) != 1 && Utilities.arrayContains(shape2.vertices, jn.vid) && Utilities.arrayContains(shape1.vertices, jn.vid))
				{
					//mark the jn visited
					junctionVisited.put(jn.id, 1);
					CommonComponent c = new CommonComponent();
					//if there is already an entry for this vertex, just add these shapes
					if(Utilities.commonComponentContains(group.common, jn.vid, "Vertex", c))
					{
						if(!c.shape_id.contains(shape1.id))
						{
							c.shape_id.add(shape1.id);
							c.type_id.add("Shape");
						}
						if(!c.shape_id.contains(shape2.id))
						{
							c.shape_id.add(shape2.id);
							c.type_id.add("Shape");
						}
					}
					else//else create a new entry
					{
						sid.add(shape1.id);
						tp.add("Shape");
						sid.add(shape2.id);
						tp.add("Shape");

						group.common.add(new CommonComponent(jn.vid, "Vertex", sid, tp));
					}
				}
			}
		}

		return;
	}

	void enrich(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Shape shape1, Shape shape2, HashMap<Pair<Integer, Integer>, ArrayList<Junction>> primitive_wise, GroupWise group, HashMap<Integer, Integer> junctionVisited)
	{

		inscribed(all_data, shapeJunction1, shapeJunction2, shape1, shape2, junctionVisited, group);
		commonLine(all_data, shapeJunction1, shapeJunction2, shape1, shape2, junctionVisited, group);
		commonVertex(all_data, shapeJunction1, shapeJunction2, shape1, shape2, junctionVisited, group);
		remaining(all_data, shapeJunction1, shapeJunction2, shape1, shape2, junctionVisited, primitive_wise, group);
		// System.out.println("Shape1 "+shape1.shape_name+" shape2: "+shape2.shape_name+" des: "+ des);
		return;
	}
	
}