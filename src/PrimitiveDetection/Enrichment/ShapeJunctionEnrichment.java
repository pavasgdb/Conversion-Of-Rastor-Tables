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

public class ShapeJunctionEnrichment{


	void lineCircleShape(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Shape shape1, Shape shape2, HashMap<Pair<Integer, Integer>, ArrayList<Junction>> primitive_wise, GroupWise group, HashMap<Integer, Integer> junctionVisited)
	{
		// Shape1 is always line and Shape2 is always circle
		// a line can intersect a convex polygon maximum twice

		//Line l and Circle c intersect each other
		Line l = Utilities.getLineWithId(all_data.lines, shape1.line_components.get(0));
		Circle c = Utilities.getCircleWithId(all_data.circles, shape2.circle_components.get(0));

		// ArrayList<Integer> junctionVisited = new ArrayList<Integer>();
		for(int i = 0; i<shapeJunction1.size()+shapeJunction2.size();i++)
		{
			Junction jn;
			String type1, type2;
			int id1, id2;
			//getting element from the correct arraylist
			if(i < shapeJunction1.size())
			{
				jn = shapeJunction1.get(i);
				type1 = jn.type1;
				type2 = jn.type2; //Check the type of shape involved in intersection with line l, here Line l is type1 and circle/association is type2
				id1 = jn.id1;
				id2 = jn.id2; //find which association/circle is involved in junction with Line l, here Line l is id1 and associated line/circle is id2
			}
			else
			{
				jn = shapeJunction2.get(i - shapeJunction1.size());
				type1 = jn.type2;
				type2 = jn.type1; //Check the type of shape involved in intersection with line l, here Line l is type2 and circle/association is type1
				id1 = jn.id2;
				id2 = jn.id1; //find which association/circle is involved in junction with Line l, here Line l is id2 and associated line/circle is id1
			}

			if(junctionVisited.get(jn.id) == 1)
				continue;

			//sj1 and sj2 are primitive wise junction list of primitive line l and primitive circle c/primitive association l2
			ArrayList<Junction> sj1 = new ArrayList<Junction>();
			Pair<Integer, Integer> p = new Pair<Integer, Integer>(l.id, id2); 
			if(primitive_wise.containsKey(p))
				sj1 = primitive_wise.get(p);

			ArrayList<Junction> sj2 = new ArrayList<Junction>();
			p = new Pair<Integer, Integer>(id2, l.id); 
			if(primitive_wise.containsKey(p))
				sj2 = primitive_wise.get(p);

			if(type2 == "Line")//intersection of line l and association of circle
			{
				Associated_Line as = PrimitiveJunction.findAssociatedLine(shape2, id2);
				Line l2 = Utilities.getLineWithId(all_data.lines, id2);
				PrimitiveJunction.lineLine(all_data, sj1, sj2, l, l2, shape1, shape2, group, junctionVisited);
			}
			else //intersection of line l and circle c
			{
				PrimitiveJunction.lineCircle(all_data, sj1, sj2, l, c, shape1, shape2, group, junctionVisited);
			}
			//mark junctions in primitive list visited
			for(int j = 0; j<sj1.size()+sj2.size(); j++)
			{
				if(j<sj1.size())
					junctionVisited.put(sj1.get(j).id, 1);
				else
					junctionVisited.put(sj2.get(j-sj1.size()).id, 1);
			}
		}
		return;
	}

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
			int map1, map2, id1, id2; //id1 always correspond to shape1 and id2 to shape2
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
				// System.out.println("is_inside1");
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


	void lineShape(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Shape shape1, Shape shape2, HashMap<Pair<Integer, Integer>, ArrayList<Junction>> primitive_wise, GroupWise group, HashMap<Integer, Integer> junctionVisited)
	{
		//Line is shape1 and shape is shape2 always
		Line l = Utilities.getLineWithId(all_data.lines, shape1.line_components.get(0));

		// ArrayList<Integer> junctionVisited = new ArrayList<Integer>();
		
		for(int i = 0; i<shapeJunction1.size() + shapeJunction2.size();i++)
		{
			Junction jn;
			String type1, type2;
			int id1, id2;
			//getting element from the correct arraylist
			if(i < shapeJunction1.size())
			{
				jn = shapeJunction1.get(i);
				type1 = jn.type1;
				type2 = jn.type2; //here Line l is type1 and shape is type2
				id1 = jn.id1;
				id2 = jn.id2; //here Line l is id1 and shape-line/association is id2
			}
			else
			{
				jn = shapeJunction2.get(i - shapeJunction1.size());
				type1 = jn.type2;
				type2 = jn.type1; // here Line l is type2 and shape is type1
				id1 = jn.id2;
				id2 = jn.id1; // here Line l is id2 and shape-line/association is id1
			}

			if(junctionVisited.get(jn.id) ==1)
				continue;

			//check if line is inscribed in shape
			inscribed(all_data, shapeJunction1, shapeJunction2, shape1, shape2,junctionVisited, group);

			//sj1 and sj2 are primitive wise junction list of primitive line l and primitive shape-line/primitive shape-association
			ArrayList<Junction> sj1 = new ArrayList<Junction>();
			Pair<Integer, Integer> p = new Pair<Integer, Integer>(l.id, id2); 
			if(primitive_wise.containsKey(p))
				sj1 = primitive_wise.get(p);

			ArrayList<Junction> sj2 = new ArrayList<Junction>();
			p = new Pair<Integer, Integer>(id2, l.id); 
			if(primitive_wise.containsKey(p))
				sj2 = primitive_wise.get(p);

			if(type2 == "Line") //if "Line"(component/association) of shape is involved in intersection
			{
				//Line l1 is involved in intersection with line l1
				Line l1 = Utilities.getLineWithId(all_data.lines, id2);

				PrimitiveJunction.lineLine(all_data, sj1, sj2, l, l1, shape1, shape2, group, junctionVisited);
			}
			else //if "Arc"(component/association) of shape is involved in intersection
			{}
			//mark junctions in primitive list visited
			for(int j = 0; j<sj1.size()+sj2.size(); j++)
			{
				if(j<sj1.size())
					junctionVisited.put(sj1.get(j).id, 1);
				else
					junctionVisited.put(sj2.get(j-sj1.size()).id, 1);
			}
		}
		return;
	}

	void circleCircleShape(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Shape shape1, Shape shape2, HashMap<Pair<Integer, Integer>, ArrayList<Junction>> primitive_wise, GroupWise group, HashMap<Integer, Integer> junctionVisited)
	{
		String des = "";

		//c1 and c2 are the primitive circles
		Circle c1 = Utilities.getCircleWithId(all_data.circles, shape1.circle_components.get(0));
		Circle c2 = Utilities.getCircleWithId(all_data.circles, shape2.circle_components.get(0));

		// ArrayList<Integer> junctionVisited = new ArrayList<Integer>();
		
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
				type2 = jn.type2; //here circle1/assoc. is type1 and circle2/association is type2
				id1 = jn.id1;
				id2 = jn.id2; // here circle1/assoc. is id1 and circle2/assoc is id2
			}
			else
			{
				jn = shapeJunction2.get(i - shapeJunction1.size());
				type1 = jn.type2;
				type2 = jn.type1; // here circle1/assoc. is type2 and circle2/association is type1
				id1 = jn.id2;
				id2 = jn.id1; //here circle1/assoc. is id2 and circle2/assoc.is id1
			}

			if(junctionVisited.get(jn.id) == 1)
				continue;

			//sj1 and sj2 are primitive wise junction list of primitive line l and primitive circle c/primitive association l2
			ArrayList<Junction> sj1 = new ArrayList<Junction>();
			Pair<Integer, Integer> p = new Pair<Integer, Integer>(id1, id2); 
			if(primitive_wise.containsKey(p))
				sj1 = primitive_wise.get(p);

			ArrayList<Junction> sj2 = new ArrayList<Junction>();
			p = new Pair<Integer, Integer>(id2, id1); 
			if(primitive_wise.containsKey(p))
				sj2 = primitive_wise.get(p);

			//association of shape1 and circle of shape2
			if(type1 == "Line" && type2 == "Circle")
			{
				Line l1 = Utilities.getLineWithId(all_data.lines, id1);
				Associated_Line as = PrimitiveJunction.findAssociatedLine(shape1, id1);
				PrimitiveJunction.lineCircle(all_data, sj1, sj2, l1, c2, shape1, shape2, group, junctionVisited);
			}
			//association of shape2 and circle of shape1
			else if(type2 == "Line" && type1 == "Circle")
			{
				Line l2 = Utilities.getLineWithId(all_data.lines, id2);
				Associated_Line as = PrimitiveJunction.findAssociatedLine(shape2, id2);
				PrimitiveJunction.lineCircle(all_data, sj2, sj1, l2, c1, shape2, shape1, group, junctionVisited);
			}
			//association of shape1 and shape2
			else if(type1 == "Line" && type2 == "Line")
			{
				Line l1 = Utilities.getLineWithId(all_data.lines, id1);
				Associated_Line as1 = PrimitiveJunction.findAssociatedLine(shape1, id1);

				Line l2 = Utilities.getLineWithId(all_data.lines, id2);
				Associated_Line as2 = PrimitiveJunction.findAssociatedLine(shape2, id2);

				PrimitiveJunction.lineLine(all_data, sj1, sj2, l1, l2, shape1, shape2, group, junctionVisited);
			}
			//circle of shape1 and shape2
			else if(type1 == "Circle" && type2 == "Circle")
			{
				PrimitiveJunction.circleCircle(all_data, sj1, sj2, c1, c2, shape1, shape2, group, junctionVisited);
			}


			for(int j = 0; j<sj1.size()+sj2.size(); j++)
			{
				if(j<sj1.size())
					junctionVisited.put(sj1.get(j).id, 1);
				else
					junctionVisited.put(sj2.get(j-sj1.size()).id, 1);
			}
		}
		return;
	}

	void connect(Extracted_Information all_data, ArrayList<Junction> shapeJunction1, ArrayList<Junction> shapeJunction2, Shape shape1, Shape shape2, HashMap<Pair<Integer, Integer>, ArrayList<Junction>> primitive_wise, GroupWise group)
	{
		HashMap<Integer, Integer> junctionVisited = new HashMap<Integer, Integer>();

		//insert all junctions in junctionVisited and mark them "unvisited"
		for(int i = 0;i<shapeJunction1.size()+shapeJunction2.size();i++)
		{
			if(i < shapeJunction1.size())
				junctionVisited.put(shapeJunction1.get(i).id, 0);
			else
				junctionVisited.put(shapeJunction2.get(i - shapeJunction1.size()).id, 0);
		}
		System.out.println("Enrich Shape1: "+shape1.shape_name+" Shape2: "+shape2.shape_name);
		//if both shapes are lines, call corresponding fn
		if(shape1.shape_name.equals("Line") && shape2.shape_name.equals("Line"))
		{
			System.out.println("lineLine");
			Line l1 = Utilities.getLineWithId(all_data.lines, shape1.line_components.get(0)), l2 = Utilities.getLineWithId(all_data.lines, shape2.line_components.get(0));

			PrimitiveJunction.lineLine(all_data, shapeJunction1, shapeJunction2, l1, l2, shape1, shape2, group, junctionVisited);
		}
		else if (shape1.shape_name.equals("Line") && shape2.shape_name.equals("Circle"))
		{
			System.out.println("lineCircle");
			lineCircleShape(all_data, shapeJunction1, shapeJunction2, shape1, shape2, primitive_wise, group, junctionVisited);
		}
		else if (shape2.shape_name.equals("Line") && shape1.shape_name.equals("Circle"))
		{
			System.out.println("lineCircle");
			lineCircleShape(all_data, shapeJunction2, shapeJunction1, shape2, shape1, primitive_wise, group, junctionVisited);
		}
		else if (shape1.shape_name.equals("Line"))
		{
			System.out.println("lineShape1");
			lineShape(all_data, shapeJunction1, shapeJunction2, shape1, shape2, primitive_wise, group, junctionVisited);
		}
		else if (shape2.shape_name.equals("Line"))
		{
			System.out.println("lineShape2");
			lineShape(all_data, shapeJunction2, shapeJunction1, shape2, shape1, primitive_wise, group, junctionVisited);
		}
		else if (shape1.shape_name.equals("Circle") && shape2.shape_name.equals("Circle"))
		{
			System.out.println("circleCircle");
			circleCircleShape(all_data, shapeJunction1, shapeJunction2, shape1, shape2, primitive_wise, group, junctionVisited);
		}
		else if (shape1.shape_name.equals("Circle"))
		{
			System.out.println("CircleShape");
			CircleShapeEnrichment s = new CircleShapeEnrichment();
			s.enrich(all_data, shapeJunction1, shapeJunction2, shape1, shape2, primitive_wise, group, junctionVisited);
		}
		else if (shape2.shape_name.equals("Circle"))
		{
			System.out.println("CircleShape");
			CircleShapeEnrichment s = new CircleShapeEnrichment();
			s.enrich(all_data, shapeJunction2, shapeJunction1, shape2, shape1, primitive_wise, group, junctionVisited);
		}
		else
		{
			System.out.println("shapeShape");
			ShapeShapeEnrichment s = new ShapeShapeEnrichment();
			s.enrich(all_data, shapeJunction1, shapeJunction2, shape1, shape2, primitive_wise, group, junctionVisited);
		}
		return;
	}

	//remove duplicates in primitives involved in XT junction
	ArrayList<XnT_Type> xt_postprocess(ArrayList<XnT_Type> xt_type, Extracted_Information all_data)
	{
		//new XT list
		ArrayList<XnT_Type> xt_new = new ArrayList<XnT_Type>();
		Boolean flag;

		for(int i = 0 ;i<xt_type.size(); i++)
		{
			XnT_Type x1 = xt_type.get(i);
			Junction j1 = Utilities.getJunctionWithId(all_data.intersections, x1.junction_id);
        	flag = true;
        	//taking component ids involved in junction in correct order
        	int j11=j1.id1, j12=j1.id2;
        	if(x1.map1 == 2)
        	{
        		j11 = j1.id2;
        		j12 = j1.id1;
        	}

			
			for(int j = i+1; j<xt_type.size(); j++)
			{
				XnT_Type x2 = xt_type.get(j);
            	Junction j2 = Utilities.getJunctionWithId(all_data.intersections, x2.junction_id);
            	//taking component ids involved in junction in correct order
            	int j21=j2.id1, j22=j2.id2;
	        	if(x2.map1 == 2)
	        	{
	        		j21 = j2.id2;
	        		j22 = j2.id1;
	        	}
	        	//if both junctions are of the same type(X/T) check if they represnt the same junction
	      		//For X junction: compare both the line ids involved
	      		//For T junction: its importatnt to check the vertex involved, also check the component that contributes for "-"
		        if(j11 == j21 && x1.type == x2.type)
		        {
		        	if((x1.type=="X" && j12 == j22 )||(x1.type == "T" && j1.vid == j2.vid))
	            	{
	            		flag = false;
	            		break;
	            	}	
				}
			}
			//if flag is true, means no duplicate of x1 is found thus add it in new list
			if(flag)
			{
				xt_new.add(x1);
			}
		}
		return xt_new;
	}

	void enrich(Extracted_Information all_data)
	{
		String des = "";
		ContainedEnrichment cd = new ContainedEnrichment();
		//loop on all groups
		for(int i = 0; i<all_data.group_wise.size(); i++)
		{
			GroupWise group = all_data.group_wise.get(i);
			for (Map.Entry<Pair<Integer, Integer>, ArrayList<Junction>> row : group.shape_wise.entrySet())
			{
				//shape_wise has <shape1, shape2, arraylist of jn> if all of these jn are visited then continue
				if(all_data.shape_wise_visited.get(row.getKey()) == 1)
					continue;
				//else take this arraylist of jn in intersection1
				int s1 = row.getKey().getKey(), s2 = row.getKey().getValue();
				ArrayList<Junction> intersection1 = row.getValue(), intersection2 = new ArrayList<Junction>();

				//finding is there exists <shape2, shape1, list of jn1>. If yes then take in intersection2
				Pair<Integer, Integer> p = new Pair<Integer, Integer>(s2, s1);
				Boolean flag = group.shape_wise.containsKey(p);
				if(flag)
					intersection2 = group.shape_wise.get(p);

				//**Note: intersection1 are those junctions where shape1 contains id1 of all the jns. intersection2 are those junctions where shape1 contains id2 of all the jns.

				//if shape1 and shape2 are not same
				if(s1 != s2)
				{
					//enrich these intersection1 and intersection2
					connect(all_data, intersection1, intersection2, Utilities.getShapeWithId(all_data.shapes, s1), Utilities.getShapeWithId(all_data.shapes, s2), all_data.primitive_wise, group);	
					
					//mark these shape_wise rows visited
					if(flag)
						all_data.shape_wise_visited.put(p, 1);
					all_data.shape_wise_visited.put(row.getKey(), 1);
				}
			}
			//postprocess the X, T type intersections of this group
			group.xt_type = xt_postprocess(group.xt_type, all_data);
			

		}

		//for all pairs of shapes first check if they intersect, if not check if they are contained inside one another
		for(int k = 0; k<all_data.shapes.size(); k++)
			{
				for(int j = k+1; j <all_data.shapes.size(); j ++)
				{
					// int i1 = group.shape_id.get(k), i2 = group.shape_id.get(j);
					Shape sh1 = all_data.shapes.get(k), sh2 = all_data.shapes.get(j);
					if(all_data.shape_wise.containsKey(new Pair<Integer, Integer>(sh1.id, sh2.id)) ||all_data.shape_wise.containsKey(new Pair<Integer, Integer>(sh2.id, sh1.id)) )
						continue;
					else
					{
						System.out.println("Contained called for "+sh1.id+" "+sh2.id);
						cd.enrich(all_data, sh1, sh2);
					}

				}
			}

		System.out.println("GroupWise: ");
        for(int k = 0; k<all_data.group_wise.size();k++)
        {
        	GroupWise group = all_data.group_wise.get(k);
            System.out.println("Group"+k);
            for(int j = 0;j<group.shape_id.size();j++)
            {
                System.out.print(group.shape_id.get(j)+", ");
            }
            System.out.println("\nXnT:");
            for(int j = 0; j<group.xt_type.size();j++)
            {
            	Junction current = Utilities.getJunctionWithId(all_data.intersections, group.xt_type.get(j).junction_id);
            	System.out.println("Line1/Circle: "+current.id1+" Line2/Circle: "+current.id2+" type: "+group.xt_type.get(j).type+" jn_id: "+group.xt_type.get(j).junction_id);
            }
            System.out.println("\ncommon:");
            for(int j = 0; j<group.common.size();j++)
            {
            	System.out.println("Common: "+group.common.get(j).component_id+" Type: "+group.common.get(j).type+" Size of shapes "+group.common.get(j).shape_id.size());
            }
            System.out.println("\nvertexAtCenter:");
            for (Map.Entry<Integer, Pair<Integer, ArrayList<Integer>>> entry : group.vertexAtCenter.entrySet())
            {
            	System.out.println("Circle id: "+entry.getKey()+" Vertex id: "+entry.getValue().getKey()+" Number of lines: "+entry.getValue().getValue());
            }
            System.out.println("\nContain:");
            for (int j = 0; j< group.contain.size(); j++)
            {
            	System.out.println("Out id: "+group.contain.get(j).out_id+" In id: "+group.contain.get(j).in_id+" junction size "+group.contain.get(j).junctions.size()+" in vertices size: "+group.contain.get(j).in_vertices.size()+" on vertices size: "+group.contain.get(j).on_vertices.size());
            }
            System.out.println("\nInscribe:");
            for (int j = 0; j< group.inscribe.size(); j++)
            {
            	System.out.println("Circum id: "+group.inscribe.get(j).circum_id+" In id: "+group.inscribe.get(j).in_id+" junction size "+group.inscribe.get(j).junctions.size());
            }
            
        }

		return;
	}
}