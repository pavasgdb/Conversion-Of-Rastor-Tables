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
import java.util.Locale; 

import java.util.HashMap; 
import java.util.Map; 
import src.All_class.*;
import src.Utilities.*;

public class OverviewShapeEnrichment {

	enum name //name of all shapes(should be in caps and one word each)
	{ 
		LINE, POINT, CIRCLE, SQUARE, RECTANGLE, RHOMBUS, PARALLELOGRAM, QUADRILATERAL, EQUILATERAL_TRIANGLE, RIGHT_ISOSCELES_TRIANGLE, ISOSCELES_TRIANGLE, RIGHT_TRIANGLE, TRIANGLE, PENTAGON, HEXAGON, POLYGON, NON_CONVEX_SHAPE;
	} 

	int get_enum_index(String s)//get enum index of string s
	{
		return name.valueOf(s.toUpperCase(Locale.ENGLISH)).ordinal();
	}

	//overview of the whole image
	ArrayList<ImageOverview> overview(Extracted_Information all_data)
	{
		int[] count_array = new int[name.values().length]; //stores count of each type of shape

		//shape_names stores name labels for shapes of each type
		ArrayList<ArrayList<String>> shape_names = new ArrayList<ArrayList<String>>();
		for(int i = 0;i< name.values().length;i++)
		{
			shape_names.add(new ArrayList<String>());
		}

		//count occurence and store name labels of each shape type
		for(int i = 0; i < all_data.shapes.size(); i++)
		{
			int j = get_enum_index(all_data.shapes.get(i).shape_name);
			count_array[j] += 1;
			shape_names.get(j).add(Utilities.polyName(all_data, all_data.shapes.get(i)));
		}
		
		//store the data in ImageOverview Object
		ArrayList<ImageOverview> overview = new ArrayList<ImageOverview>();
		for(int i=0; i<count_array.length; i++)
		{
			if(count_array[i] != 0)
			{
				String v = name.values()[i].toString();
				String[] n = v.split("_", -1);
				v = String.join(" ", n); //shape name e.g, Right Isosceles triangle
				overview.add(new ImageOverview(count_array[i], v, shape_names.get(i)));
			}
		}
		return overview;
	}


	//same as the above function "overview", except that loop is on the shapes of a particular group(above loop was on all shapes)
	ArrayList<ImageOverview> groupWiseOverview(Extracted_Information all_data, GroupWise group)
	{
		int[] count_array = new int[name.values().length];
		ArrayList<ArrayList<String>> shape_names = new ArrayList<ArrayList<String>>();

		for(int i = 0;i< name.values().length;i++)
		{
			shape_names.add(new ArrayList<String>());
		}

		for(int i = 0; i < group.shape_id.size(); i++)
		{
			Shape sh = Utilities.getShapeWithId(all_data.shapes, group.shape_id.get(i));
			int j = get_enum_index(sh.shape_name);
			count_array[j] += 1;
			shape_names.get(j).add(Utilities.polyName(all_data, sh));
		}
		
		ArrayList<ImageOverview> overview = new ArrayList<ImageOverview>();
		// System.out.println("Count_array: ");
		for(int i=0; i<count_array.length; i++)
		{
			if(count_array[i] != 0)
			{
				String v = name.values()[i].toString();
				String[] n = v.split("_", -1);
				v = String.join(" ", n);
				overview.add(new ImageOverview(count_array[i], v, shape_names.get(i)));
			}
		}
		return overview;
	}


	//calculate relative length of radius of all circles w.r.t. reference line
	void radiusLength(Extracted_Information all_data)
	{
		//l is the reference line
		Line l = Utilities.getLineWithId(all_data.lines, all_data.reference_line_id);
		double len;
		//if there is no line in the image, pick reference "length" as radius of the first circle
		if(all_data.reference_line_id == -1)
			len = all_data.circles.get(0).r;
		else
			len =l.length;

		//for all circle radius, calculate its relative length
		for(int i = 0; i<all_data.circles.size();i++)
		{
			Circle c = all_data.circles.get(i);
			// System.out.println(all_data.reference_line_id+" "+c.r+" here");
			//if there is no reference line and this the first circle whose radius is the reference "length"
			if(all_data.reference_line_id == -1 && i == 0)
				 c.label_radius_length = /*Math.round(c.r*100.0)/100.0+*/"x";
			else // if this is not the reference "length"
			{
				double ratio = Math.round((c.r/len)*10.0)/10.0;        
				c.label_radius_length = Double.toString(ratio)+"x"/*" times of Line "+Utilities.line_name(l, all_data)*/;
				// System.out.println(l.v1+" "+l.v2+" here1");
			}

		}
	}

	void enrich(Extracted_Information all_data)
	{
		all_data.imageOverview = overview(all_data); //generate overview of whole image
		radiusLength(all_data); //calculate radius length w.r.t reference line
		for(int i = 0; i<all_data.group_wise.size();i++) // generate overview of each disconnected component
		{
			GroupWise group = all_data.group_wise.get(i);
			group.imageOverview = groupWiseOverview(all_data, group);
		}
	}
}