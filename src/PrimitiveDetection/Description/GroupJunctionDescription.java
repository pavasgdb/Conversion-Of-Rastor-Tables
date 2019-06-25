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

public class GroupJunctionDescription{

	//"<shape> <name> is inscribed in <shape> <name>
	String describe_inscribe(Extracted_Information all_data, GroupWise group)
	{
		String des = "";
		for(int i = 0; i < group.inscribe.size(); i++)
		{
			InscribeType it = group.inscribe.get(i);

			Shape circum_shape = Utilities.getShapeWithId(all_data.shapes, it.circum_id);
			Shape in_shape = Utilities.getShapeWithId(all_data.shapes, it.in_id);

			String circum_name = Utilities.format_shape_name(circum_shape.shape_name)+" "+Utilities.polyName(all_data, circum_shape);
			String in_name = Utilities.format_shape_name(in_shape.shape_name)+" "+Utilities.polyName(all_data, in_shape);

			des += in_name+" is inscribed in "+circum_name+". ";
		}
		return des;
	}

	//"<shape1> <name> and <shape2> <name> are concentric and <shape2> <name> lies inside <shape1> <name>" / "Circle <name> is contained inside <shape> <name> with circle touching Line<name>, Line<name>...."/ "<shape> <name> is contained inside circle<name> with vertex <name>, <name> inside and vertex <name>, <name> on the circumference of the circle"/ "<shape> <name> is contained inside <shape2> <name> with vertex <name>, <name> inside and vertex <name>, <name> on the <shape2> <name>"
	String describe_contains(Extracted_Information all_data, GroupWise group)
	{
		String des = "";
		for(int i = 0; i < group.contain.size(); i++)
		{
			ContainType it = group.contain.get(i);

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
							else if( j == it.on_vertices.size()-1 && it.on_vertices.size()!=1)
								des += " and ";
							Line l = Utilities.getLineWithId(all_data.lines, it.on_vertices.get(j));
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

	//"Vertex <name> lies at the center of Circle<name>"
	String decsribe_vertexAtCenter(Extracted_Information all_data, GroupWise group)
	{
		String des = "";
		for(Map.Entry<Integer, Pair<Integer, ArrayList<Integer>>> entry: group.vertexAtCenter.entrySet())//Key: circle_id (not shape_id), Pair.key: Vertex Id, Pair.value: ArrayList of lines that end at vid
		{
			int cid = entry.getKey();
			Pair<Integer, ArrayList<Integer>> p = entry.getValue();
			int vid = p.getKey();
			ArrayList<Integer> line_ids = p.getValue();

			Circle c = Utilities.getCircleWithId(all_data.circles, cid);
			Vertex v = Utilities.getVertexWithId(all_data.vertices, vid);

			String c_name = "Circle C"+c.id;
			String v_name = "Vertex "+Utilities.vertex_name(v, all_data);

			des += v_name +" lies at the center of "+c_name+". ";
		}
		return des;
	}

	//"<Shape> <name>, <Shape> <name>, <Shape> <name> have common Line/Vertex <name>."
	String describe_common(Extracted_Information all_data, GroupWise group)
	{
		String des = "";

		for(int i = 0; i < group.common.size(); i++)
		{
			String name = "";
			CommonComponent cc = group.common.get(i);
			if(cc.type == "Vertex")
			{
				Vertex v = Utilities.getVertexWithId(all_data.vertices, cc.component_id);
				name = "Vertex "+Utilities.vertex_name(v, all_data);
			}
			else if(cc.type =="Line")
			{
				Line l = Utilities.getLineWithId(all_data.lines, cc.component_id);
				name = "Line "+Utilities.line_name(l, all_data);
			}
			for(int j = 0; j <cc.shape_id.size(); j ++)
			{
				if(j !=0 && j< cc.shape_id.size()-1)
					des +=", ";
				else if(j !=0 && j== cc.shape_id.size()-1)
					des+=" and ";
				if(cc.type_id.get(j) == "Shape")
				{
					Shape s  = Utilities.getShapeWithId(all_data.shapes, cc.shape_id.get(j));
					des += Utilities.format_shape_name(s.shape_name)+" "+Utilities.polyName(all_data, s);
				}
				else
				{
					Line s  = Utilities.getLineWithId(all_data.lines, cc.shape_id.get(j));
					des += "Line "+Utilities.line_name(s, all_data);
				}
			}
			des += " have common "+name+". ";
		}
		return des;
	}

	//returns name of the component "id" of "type"
	String comp_name(Extracted_Information all_data, int id, String type)
	{
		if(type == "Line")
		{
			Line l1 = Utilities.getLineWithId(all_data.lines, id);
			return "Line "+Utilities.line_name(l1, all_data);
		}
		else
		{
			Circle c = Utilities.getCircleWithId(all_data.circles, id);
			return "Circle C"+c.id;
		}
	}

	//"Line/Circle <name> intersects Line/Circle <name2> at Junction <name>. Junction <name> divides Line <name1> in ratio x:y. Junction <name> divides Line <name2> in ratio x:y."/ "Junction name lies on Line/Circle <name> (in Quadrant1 / and divides it in ratio x:y)"
	String describe_xtType(Extracted_Information all_data, GroupWise group)
	{
		String des = "";

		for(int i = 0;i< group.xt_type.size(); i++)
		{
			XnT_Type xt = group.xt_type.get(i);
			Shape s1 = Utilities.getShapeWithId(all_data.shapes, xt.sh1);
			Shape s2 = Utilities.getShapeWithId(all_data.shapes, xt.sh2);
			String s1_name="", s2_name="";

			if(!(s1.shape_name == "Line" || s1.shape_name == "Circle"))
				s1_name = " of "+Utilities.format_shape_name(s1.shape_name)+" "+Utilities.polyName(all_data, s1);
			if(!(s2.shape_name == "Line" || s2.shape_name == "Circle"))
				s2_name = " of "+Utilities.format_shape_name(s2.shape_name)+" "+Utilities.polyName(all_data, s2);
			
			Junction jn = Utilities.getJunctionWithId(all_data.intersections, xt.junction_id);
			// System.out.println(jn.id+" "+jn.type1+" "+jn.junction_ratio1);
			String name1, name2, ratio1, ratio2;
			if(xt.map1 == 1)
			{
				name1 = comp_name(all_data, jn.id1, jn.type1);
				name2 = comp_name(all_data, jn.id2, jn.type2);
				ratio1 = jn.junction_ratio1;
				ratio2 = jn.junction_ratio2;
			}
			else
			{
				name2 = comp_name(all_data, jn.id1, jn.type1);
				name1 = comp_name(all_data, jn.id2, jn.type2);
				ratio1 = jn.junction_ratio2;
				ratio2 = jn.junction_ratio1;
			}
			// System.out.println(jn.junction_ratio1+" "+jn.junction_ratio2);
			String j_name = Utilities.junction_name(jn, all_data);
			if(xt.type == "X")
			{
				des += name1+/*s1_name+*/" intersects "+name2/*+s2_name*/+" at "+j_name+". ";
				if(ratio1 != "")
				{
					des += j_name+" divides "+name1+" in ratio "+ratio1+". ";
				}
				if(ratio2 != "")
				{
					des += j_name+" divides "+name2+" in ratio "+ratio2+". ";
				}

			}
			else if(xt.type == "T")
			{
				des += j_name+" lies on "+name1/*+s1_name*/;
				if(s1.shape_name == "Circle")
				{
					Circle c = Utilities.getCircleWithId(all_data.circles, s1.circle_components.get(0));
					des +=" in Quadrant "+Utilities.getQuadrant(c, jn.x, jn.y)+". ";
				}
				else
				{
					des += " and divides it in ratio "+ratio1+". ";
					
				}
				// des += name1+s1_name+" touches "+name2+s2_name+" at "+j_name+". ";
			}
			
		}

		return des;
	}

	String describe_intersections(Extracted_Information all_data, GroupWise group)
	{
		String description = "";

		description += describe_inscribe(all_data, group);

		description += decsribe_vertexAtCenter(all_data, group);

		description += describe_common(all_data, group);

		description += describe_contains(all_data, group);

		description += describe_xtType(all_data, group);

		return description;
	}
}