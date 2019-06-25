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
import java.util.Locale; 

import src.All_class.*;
import src.Utilities.*;

public class Description{

	//describe the whole image overview: "There are x1 <shape_type> <shape_names>, x2 <shape_type> <shape_names>, ..."
	String wholeImageOverview(Extracted_Information all_data)
	{
		String description = "";
		description += "There ";
		if(all_data.imageOverview.size()>1)
			description += "are ";
		else
			description += "is ";

		for(int i = 0; i<all_data.imageOverview.size(); i++)
		{
			if(i == all_data.imageOverview.size()-1 && i!=0)
				description +=" and ";
			else if(i!=0)
				description += ", ";

			ImageOverview io = all_data.imageOverview.get(i);
			String shape_name = Utilities.format_shape_name(io.shape_name.toLowerCase(Locale.ENGLISH));
			if(io.num>1)
			{
				shape_name += "s";
			}
			description += io.num+" "+shape_name+" "+io.names.get(0);
			for(int j = 1; j < io.names.size(); j++)
			{
				if(j == io.names.size() - 1)
					description += " and ";
				else
					description += ", ";
				description += io.names.get(j);
			}
		}
		description += ". ";
		return description;
	}

	//Generates "Group i contains x1 <Shape_type> <shape_names>, x2 <Shape_type> <shape_names> ..."
	String group_overview(Extracted_Information all_data, GroupWise group, int index)
	{
		String description = "";
		description += "Group "+index+" contains ";

		for(int i = 0; i<group.imageOverview.size(); i++)
		{
			if(i == group.imageOverview.size()-1 && i!=0)
				description +=" and ";
			else if(i!=0)
				description += ", ";

			ImageOverview io = group.imageOverview.get(i);
			String shape_name = Utilities.format_shape_name(io.shape_name.toLowerCase(Locale.ENGLISH));
			if(io.num>1)
			{
				shape_name += "s";
			}
			description += io.num+" "+shape_name+" "+io.names.get(0);
			for(int j = 1; j < io.names.size(); j++)
			{
				if(j == io.names.size() - 1)
					description += " and ";
				else
					description += ", ";
				description += io.names.get(j);
			}
		}
		description += ". ";
		return description;
	}

	//describe each group: first its shapes, then its intersections
	String describeGroup(Extracted_Information all_data, GroupWise group)
	{
		String des = "";
		
		//describe each shape in the group
		GroupShapeDescription sd = new GroupShapeDescription();
		des += sd.describe_shape(all_data, group);

		//describe all kind of intersections in the group
		GroupJunctionDescription jd = new GroupJunctionDescription();
		des += jd.describe_intersections(all_data, group);

		return des;
	}

	//describe contain-relationship between shapes of different groups if any(i.e, between shapes of disconnected groups)
	String describe_contains(Extracted_Information all_data)
	{
		String des = "";
		for(int i = 0; i < all_data.contain.size(); i++)
		{
			ContainType it = all_data.contain.get(i);

			Shape out_shape = Utilities.getShapeWithId(all_data.shapes, it.out_id);
			Shape in_shape = Utilities.getShapeWithId(all_data.shapes, it.in_id);

			String out_name = Utilities.format_shape_name(out_shape.shape_name)+" "+Utilities.polyName(all_data, out_shape);
			String in_name = Utilities.format_shape_name(in_shape.shape_name)+" "+Utilities.polyName(all_data, in_shape);

			//if shapes are concentric
			if(it.concentric)
			{
				des += out_name + " and "+ in_name+" are concentric and "+in_name+" lies inside "+out_name;
			}
			else
			{
				//if inner shape is circle
				if(in_shape.shape_name == "Circle")
				{
					des += in_name+" is contained inside "+out_name;
					//if circle touches edges of the outer shape
					if(it.on_vertices.size()!=0)
					{
						des += " with circle touching ";
						for(int j = 0 ;j <it.on_vertices.size(); j++)
						{
							if(j < it.on_vertices.size()-1)
								des += ", ";
							else if( j == it.on_vertices.size()-1)
								des += " and ";
							Line l = Utilities.getLineWithId(all_data.lines, it.on_vertices.get(i));
							des += "Line "+Utilities.line_name(l, all_data);
						}
					}
					des+=". ";
				}
				//if outer shape is circle
				else if(out_shape.shape_name == "Circle")
				{
					des += in_name+" is contained inside "+out_name;
					//if inner shape vertices touches the circle
					if(it.in_vertices.size()!=0 && it.on_vertices.size()!=0)
					{
						des += " with ";
						if(it.in_vertices.size()!=0)
						{
							des += Utilities.vertex_names(all_data, it.in_vertices);
							des += " inside ";
						}	
						if(it.on_vertices.size()!=0)
						{
							des += "and ";
							des += Utilities.vertex_names(all_data, it.on_vertices);
							des += " on the circumference of";
						}
						des += "the circle";	
					}
					des+=". ";
				}
				else //for all other kind of shapes
				{
					des += in_name+" is contained inside "+out_name;
					//if inner shape touches outer shape
					if(it.in_vertices.size()!=0 && it.on_vertices.size()!=0)
					{
						des += " with ";
						if(it.in_vertices.size()!=0)
						{
							des += Utilities.vertex_names(all_data, it.in_vertices);
							des += " inside ";
						}	
						if(it.on_vertices.size()!=0)
						{
							des += "and ";
							des += Utilities.vertex_names(all_data, it.on_vertices);
							des += " on ";
						}
						des += out_name;	
					}
					des+=". ";
				}
			}

		}
		return des;
	}

	//describes length of line which is "given in the diagram"
	String line_length(Extracted_Information all_data)
	{
		String des = "";
		for(int i = 0;i< all_data.lines.size(); i++)
		{
			Line l = all_data.lines.get(i);
			if(!l.label_length.equals(""))
			{
				des += "Length of Line "+Utilities.line_name(l, all_data)+" is given to be "+l.label_length+". ";
			}
		}
		return des;
	}

	//describes angle at a vertex or junction which is "given in the diagram"
	//Angle <vertex_name> is given to be <value>
	//Angle included between <vname1>, <jname> and <vname2> is given to be <value>. <jname> divides Line <name1> in ratio x:y and Line <name2> in ratio a:b
	String vertexJunctionAngle(Extracted_Information all_data)
	{
		String des = "";

		for(Map.Entry<Pair<Integer, String>, VertexLabel> row : all_data.vertexAssociation.entrySet())  
		{
			if(row.getKey().getValue().equals("length"))
			{

				Vertex v = Utilities.getVertexWithId(all_data.vertices, row.getValue().vertex_id);
				Label lb =Utilities.getLabelWithId(all_data.labels, row.getValue().label_id);
				
				des += "Angle "+Utilities.vertex_name(v, all_data)+" is given to be "+lb.c+". ";
			}
		}

		for(Map.Entry<Pair<Integer, String>  , JunctionLabel> row : all_data.junctionAssociation.entrySet())  
		{
			if(row.getKey().getValue().equals("length"))
			{
				Junction v = Utilities.getJunctionWithId(all_data.intersections, row.getValue().junction_id);
				Label lb =Utilities.getLabelWithId(all_data.labels, row.getValue().label_id);
				String name1 = "", name2 = "";
				if(row.getValue().type1.equals("Vertex"))
				{
					Vertex v1 = Utilities.getVertexWithId(all_data.vertices, row.getValue().pid1);
					name1 = Utilities.vertex_name(v1, all_data);
				}
				if(row.getValue().type2.equals("Vertex"))
				{
					Vertex v2 = Utilities.getVertexWithId(all_data.vertices, row.getValue().pid2);
					name2 = Utilities.vertex_name(v2, all_data);
				}
				String j_name =Utilities.junction_name(v, all_data), ratio = "";
				if(v.type1 == "Line" && v.type2 =="Line")
				{
					Line l1 = Utilities.getLineWithId(all_data.lines, v.id1);
					Line l2 = Utilities.getLineWithId(all_data.lines, v.id2);
					ratio = j_name+" divides Line "+Utilities.line_name(l1, all_data)+" in ratio "+Utilities.junction_ratio(v, l1, all_data)+" and Line "+Utilities.line_name(l2, all_data)+" in ratio "+Utilities.junction_ratio(v, l2, all_data)+". ";
				}

				des += "Angle included between "+name1+", "+j_name+" and "+name2+" is given to be "+lb.c+". "+ratio;
			}
		}
		return des;
	}

	String describe(Extracted_Information all_data)
	{
		String description = "";

		//whole image overview is generated
		description += wholeImageOverview(all_data);

		//number of groups is described
		if(all_data.group_wise.size()>1)
		{
			description += "There are "+all_data.group_wise.size()+" groups of disconnected shapes. ";
		}

		for(int i = 0; i<all_data.group_wise.size(); i++)
		{
			GroupWise group = all_data.group_wise.get(i);
			//if there are more than one groups then give group overview of each group and then describe each group
			if(all_data.group_wise.size()>1)
			{
				description += group_overview(all_data, group, i);
			}
			description += describeGroup(all_data, group);

		}

		//describe contain-relationship between shapes of different groups if any(i.e, between shapes of disconnected groups)
		description += describe_contains(all_data);

		//describe lengths of line if given in the diagram
		description += line_length(all_data);
		description += vertexJunctionAngle(all_data);

		return description;
	}
}