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

public class ContainedEnrichment{

	//checks if line is contained inside circle
	void lineCircleContained(Extracted_Information all_data, Shape s1, Shape s2)
	{
		//shape1 is always Line and shape2 is circle
		Line l = Utilities.getLineWithId(all_data.lines, s1.line_components.get(0));
		Vertex v1 = Utilities.getVertexWithId(all_data.vertices, l.v1);
		Vertex v2 = Utilities.getVertexWithId(all_data.vertices, l.v2);
		Circle c = Utilities.getCircleWithId(all_data.circles, s2.circle_components.get(0));

		//distance of vertex v1 and v2 from circle centers
		double d1 = Math.sqrt((c.x - v1.x)*(c.x - v1.x) + (c.y - v1.y)* (c.y - v1.y));
		double d2 = Math.sqrt((c.x - v2.x)*(c.x - v2.x) + (c.y - v2.y)* (c.y - v2.y));
		ArrayList<Integer> iv = new ArrayList<Integer>();
		iv.add(v1.id);
		iv.add(v2.id);
		//if less than radius, line contained inside circle
		if(d1 <= c.r && d2 <= c.r)
			all_data.contain.add(new ContainType(s2.id, s1.id, new ArrayList<Pair<Junction, Pair<Integer, Integer>>>(), iv, new ArrayList<Integer>()));
		return;
	}

	//checks if line is contained inside shape
	void lineShapeContained(Extracted_Information all_data, Shape s1, Shape s2)
	{
		// line is always shape1
		Vertex v0 = Utilities.getVertexWithId(all_data.vertices, s1.vertices[0]);
		Vertex v1 = Utilities.getVertexWithId(all_data.vertices, s1.vertices[1]);
		//if both vertices are contained inside the shape then line is inside the shape
		if(Utilities.pointContains(all_data, s2, v0.x, v0.y) && Utilities.pointContains(all_data, s2, v1.x, v1.y))
		{
			ArrayList<Integer> iv = new ArrayList<Integer>();
			iv.add(v0.id);
			iv.add(v1.id);
			all_data.contain.add(new ContainType(s2.id, s1.id, new ArrayList<Pair<Junction, Pair<Integer, Integer>>> (), iv, new ArrayList<Integer> ()));
		}
		return;
	}

	//check if one shape is contained inside another
	void shapeShapeContained(Extracted_Information all_data, Shape s1, Shape s2)
	{
		Boolean r = true;
		//check if all vertices of s1 are inside s2
		for(int i = 0; i < s1.vertices.length; i++)
		{
			Vertex v = Utilities.getVertexWithId(all_data.vertices, s1.vertices[i]);
			r = r && Utilities.pointContains(all_data, s2, v.x, v.y);
			if(r == false)
				break;

		}
		//if all vertices of s1 are inside s2 and s2 is non-convex then s1 inside s2
		if(r== true && !s2.shape_name.equals("Non_Convex_Shape"))
		{
			ArrayList<Integer> vert = new ArrayList<Integer>();
			for(int j = 0; j<s1.vertices.length;j++)
				vert.add(s1.vertices[j]);
			all_data.contain.add(new ContainType(s2.id, s1.id, new ArrayList<Pair<Junction, Pair<Integer, Integer>>> (), vert, new ArrayList<Integer> ()));
		}
		else// else if all vertices of s2 are inside s1 and s1 is non-convex then s2 inside s1
		{
			r = true;
			for(int i = 0; i < s2.vertices.length; i++)
			{
				Vertex v = Utilities.getVertexWithId(all_data.vertices, s2.vertices[i]);
				r = r && Utilities.pointContains(all_data, s1, v.x, v.y);
				if(r == false)
					break;
			}
		}
		if(r== true && !s1.shape_name.equals("Non_Convex_Shape"))
		{
			ArrayList<Integer> vert = new ArrayList<Integer>();
			for(int j = 0; j<s2.vertices.length;j++)
				vert.add(s2.vertices[j]);
			all_data.contain.add(new ContainType(s1.id, s2.id, new ArrayList<Pair<Junction, Pair<Integer, Integer>>> (), vert, new ArrayList<Integer> ()));
		}
		return;
		
	}

	//check concentric circles
	void circleCircleContained(Extracted_Information all_data, Shape s1, Shape s2)
	{
		Circle c1 = Utilities.getCircleWithId(all_data.circles, s1.circle_components.get(0));
		Circle c2 = Utilities.getCircleWithId(all_data.circles, s2.circle_components.get(0));

		//if circle centers are same, check which circle is inside another: Concentric
		if(c1.x == c2.x && c1.y == c2.y)
		{
			if(c1.r < c2.r) //c1 inside c2
				all_data.contain.add(new ContainType(s2.id, s1.id, new ArrayList<Pair<Junction, Pair<Integer, Integer>>> (), new ArrayList<Integer>(), new ArrayList<Integer> (), true));
			else //c2 inside c1
				all_data.contain.add(new ContainType(s1.id, s2.id, new ArrayList<Pair<Junction, Pair<Integer, Integer>>> (), new ArrayList<Integer>(), new ArrayList<Integer> (), true));
		}
		return;
	}

	//check if circle is contained inside shape or vice versa
	void circleShapeContained(Extracted_Information all_data, Shape s1, Shape s2)
	{
		//s1 is circle
		
		Circle c1 = Utilities.getCircleWithId(all_data.circles, s1.circle_components.get(0));
		Boolean r1 = true, r2 = true; //r1 keeps a check if shape is inside circle and r2 keeps a check if circle is inside shape
		for(int i = 0 ;i<s2.vertices.length; i++)
		{
			Vertex v = Utilities.getVertexWithId(all_data.vertices, s2.vertices[i]);
			double d = Math.sqrt((v.x - c1.x)*(v.x - c1.x) + (v.y - c1.y)*(v.y - c1.y));
			//if vertex-center distance is less than radius then shape is inside circle
			if(d < c1.r)
			{
				r1 = r1 && true;
				r2 = r2 && false;
			}
			else//else circle is inside shape
			{
				r1 = r1 && false;
				r2 = r2 && true;
			}
		}
		if(r1 == true) //if shape  vertices are inside circle, shape in circle
		{
			ArrayList<Integer> vert = new ArrayList<Integer>();
			for(int j = 0; j<s2.vertices.length;j++)
				vert.add(s2.vertices[j]);
			all_data.contain.add(new ContainType(s1.id, s2.id, new ArrayList<Pair<Junction, Pair<Integer, Integer>>> (), vert, new ArrayList<Integer> ()));
			// return s2.shape_name+" "+Utilities.polyName(all_data, s2)+" is contained in Circle C"+s1.circle_components.get(0)+". ";
		}
		else if(r2 == true && !s2.shape_name.equals("Non_Convex_Shape")) //if shape vertices outside circle and shape is non-convex, then check if circle center is inside shape and all shape lines are outside
		{
			if(Utilities.pointContains(all_data, s2, c1.x, c1.y)) //checks circle center inside shape
			{
				r2 = true;
				//check if all lines are outside the circle
				for(int i = 0 ;i<s2.line_components.size(); i++)
				{
					Line l = Utilities.getLineWithId(all_data.lines, s2.line_components.get(i));
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
					all_data.contain.add(new ContainType(s1.id, s2.id, new ArrayList<Pair<Junction, Pair<Integer, Integer>>> (), new ArrayList<Integer>(), new ArrayList<Integer> ()));
				}
			}
		}
	
		return ;
	}

	void enrich(Extracted_Information all_data, Shape shape1, Shape shape2)
	{
		System.out.println("Contained called for "+shape1.id+" "+shape2.id);
		if(shape1.shape_name == "Line" && shape2.shape_name == "Line")
		{
			return;
		}
		else if (shape1.shape_name == "Line" && shape2.shape_name.equals("Circle")) 
		{
			System.out.println("lineCircleContained");
			lineCircleContained(all_data, shape1, shape2);
		}
		else if((shape2.shape_name == "Line")&& shape1.shape_name.equals("Circle"))
		{
			System.out.println("lineCircleContained");
			lineCircleContained(all_data, shape2, shape1);
		}
		else if ((shape1.shape_name == "Line" && !shape2.shape_name.equals("Non_Convex_Shape")))
		{
			System.out.println("lineShapeContained");
			lineShapeContained(all_data, shape1, shape2);
		}
		else if ((shape2.shape_name == "Line")&& !shape1.shape_name.equals("Non_Convex_Shape"))
		{
			System.out.println("lineShapeContained");
			lineShapeContained(all_data, shape2, shape1);
		}
		else if (shape1.shape_name == "Circle" && shape2.shape_name == "Circle")
		{
			circleCircleContained(all_data, shape1, shape2);
		}
		else if (shape1.shape_name == "Circle")
		{
			circleShapeContained(all_data, shape1, shape2);
		}
		else if (shape2.shape_name == "Circle")
		{
			circleShapeContained(all_data, shape2, shape1);
		}
		else
		{
			shapeShapeContained(all_data, shape1, shape2);
		}
		return;
	}

}