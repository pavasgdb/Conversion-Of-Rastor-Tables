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
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import src.All_class.*;
import src.Utilities.*;

public class GroupShapeDescription{


	//for each polygon generate: "Base <name> of <shape> <name> is at an angle x and has length = y. Line <name> is at an angle x1 and has length y1..."
	String lengthAngle(Extracted_Information all_data, Shape s, Boolean angle_flag, Boolean length_flag)
	{
		String des = "";
		String shape_name = Utilities.format_shape_name(s.shape_name)+" "+Utilities.polyName(all_data, s);
		Line ln = Utilities.getLineWithId(all_data.lines, s.line_components.get(0));
		//if angle flag == false, angles wont be described
		//if length flag == false, lengths wont be described

		//describe base line
		if(angle_flag || length_flag)
		{	
			des += "Base "+Utilities.line_name(ln, all_data)+ " of "+shape_name;
			if(angle_flag)
				des += ln.label_slope; //slope(ln.slope);
			if(length_flag)
			{
				if(angle_flag)
					des += " and";
				des += " has length = "+ln.length_ratio;
			}
			des += ". ";
		}

		//describe all other lines
		for(int i = 1; i < s.line_components.size(); i++)
		{
			ln = Utilities.getLineWithId(all_data.lines, s.line_components.get(i));
			if(i!=1)
				des+=", ";
			if(angle_flag || length_flag)
			{	
				des += "Line "+Utilities.line_name(ln, all_data);
				if(angle_flag)
					des += ln.label_slope; //slope(ln.slope);
				if(length_flag)
				{
					if(angle_flag)
						des += " and";
					des += " has length = "+ln.length_ratio;
				}
			}
		}
		des += ". ";
		return des;
	}

	//describes associations as: 
	//"<Shape> <name> has diagonal <name> joining vertex a and vertex b"
	//OR "<Shape> <name> has Line joining center of <shape> <name> to <vertex>"
	String association_des(Extracted_Information all_data, Shape s, String shape_name, Boolean angle_flag, Boolean length_flag)
	{
		String des = "";
		if(s.associations.size()!=0)
		{
			des += shape_name+" has";
			for(int i = 0; i < s.associations.size(); i++)
			{
				if(i!=0)
					des+=",";
				//description of diagonal
				if(s.associations.get(i).description.equals("Diagonal"))
				{
					Associated_Line as = s.associations.get(i);
					Vertex v1 = Utilities.getVertexWithId(all_data.vertices, as.v1_map), v2 = Utilities.getVertexWithId(all_data.vertices, as.v2_map);
					des += " "+as.description+" joining "+as.type1+" "+Utilities.vertex_name(v1, all_data)+" and "+as.type2+" "+Utilities.vertex_name(v2, all_data);
				}
				//description of line joining centroid to a vertex
				else if(s.associations.get(i).description.equals("CentroidVertex"))
				{
					Associated_Line as = s.associations.get(i);
					String v_name = "";
					if(as.v1_map == -1)
					{
						Vertex v = Utilities.getVertexWithId(all_data.vertices, as.v2_map);
						v_name = as.type2+" "+Utilities.vertex_name(v, all_data);
					}
					else
					{ 
						Vertex v = Utilities.getVertexWithId(all_data.vertices, as.v1_map);
						v_name = as.type1+" "+Utilities.vertex_name(v, all_data);
					}
					des += " Line joining center of "+shape_name+" to "+v_name;
				}
			}
			des += ". ";
		}
		return des;
	}

	//describe line as: "Line <name> is at an angle x1 and has length y1."
	String describe_line(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){ 
		String des = "";
		if(angle_flag || length_flag)
		{
			Line l = Utilities.getLineWithId(all_data.lines, s.line_components.get(0));
			
			des = "Line "+Utilities.line_name(l, all_data);
			
			if(angle_flag)
				des += l.label_slope; //slope(l.slope);
			
			if(length_flag)
			{
				if(angle_flag)
					des += " and";
				des += " has length = "+l.length_ratio;
				
			}
			des += ". ";
		}
		return des; 
	}

	String describe_point(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){
		return "";
	}

	//describe circle as : "Circle <name> has radius = x and has radius<name>, tangent<name>, diameter<name>, chord<name>. Radius/Diameter/Chord <name> is at an angle x and has length y and its end points lies in Quadrant 1 and Quadrant 2. Radius/Diameter/Chord <name> is at an angle x and has length y and both its end points lies in Quadrant 1. Tangent <name> touches circle at <junction name>, is at an angle x and has length = y. (Vertex <name> lies in Quadrant 1)/(Junction <name> divides <tangent_name> in ration <ratio> and lies in Quadrant 1)"
	String describe_circle(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){
		Circle cir = Utilities.getCircleWithId(all_data.circles, s.circle_components.get(0));
		String circle_name = "Circle "+Utilities.polyName(all_data, s);
		String des = circle_name + " has radius = "+cir.label_radius_length;

		String t_slope = "";
		if(s.associations.size() != 0)
		{
			// des += "Circle C"+s.circle_components.get(0);
			des += " and has ";
			for(int i = 0; i < s.associations.size(); i++)
			{
				Associated_Line as = s.associations.get(i);
				if(i!=0)
					des+=", ";
				Line lt=Utilities.getLineWithId(all_data.lines, as.line_id);
				des += as.description+" "+Utilities.line_name(lt, all_data);
				// System.out.println(lt.v1+", "+lt.v2+", "+lt.slope+" here2");
			}
			des+=". ";

			for(int i = 0; i < s.associations.size(); i++)
			{
				if(i!=0)
				{
					t_slope += ". ";
				}
				Associated_Line as = s.associations.get(i);
				Line t = Utilities.getLineWithId(all_data.lines, as.line_id);
				Vertex v1 = Utilities.getVertexWithId(all_data.vertices, t.v1), v2 = Utilities.getVertexWithId(all_data.vertices, t.v2);
				String t_name = as.description+" "+Utilities.line_name(t, all_data);

				//description for radius/diameter/chord
				if(as.description.equals("Radius")||as.description.equals("Diameter")||as.description.equals("Chord"))
				{
					// String c_name = as.description+" of circle C"+cir.id+" joins "+as.type1+" "+Utilities.vertex_name(v1, all_data)+" and "+as.type2+" "+Utilities.vertex_name(v2, all_data);
										
					if(angle_flag || length_flag)
					{
						t_slope += t_name;
						if(angle_flag)
							t_slope += t.label_slope; //slope(t.slope);
						if(length_flag)
						{
							if(angle_flag)
								t_slope += ",";
							t_slope += " has length = "+t.length_ratio;
							
						}
					}
					int q1 = Utilities.getQuadrant(cir, v1.x, v1.y), q2 = Utilities.getQuadrant(cir, v2.x, v2.y);

					if(t.slope > 0)
					{
						int qt = q1;
						q1 = q2;
						q2 = qt;
					}

					if(q1!=q2)
						t_slope += " and its endpoints lies in Quadrants "+ q1+" and "+q2;
					else
						t_slope += " and both its end points lie in Quadrant "+q1;
					
				}
				//description for tangent
				else if(as.description.equals("Tangent"))
				{
					Junction j1 = Utilities.getJunctionWithId(all_data.intersections, as.v1_map);
					String name= "", ratio = "";
					if(j1.vid!=-1)
					{
						Vertex v = Utilities.getVertexWithId(all_data.vertices, j1.vid);
						name = "Vertex "+Utilities.vertex_name(v, all_data);
						ratio = name+" lies in Quadrant "+Utilities.getQuadrant(cir, j1.x, j1.y);
					}
					else
					{
						name = Utilities.junction_name(j1, all_data);
						ratio = name+" divides "+t_name+" in ratio ";
						if(t.id == j1.id1)
							ratio += j1.junction_ratio1;
						else
							ratio += j1.junction_ratio2;
						ratio += " and lies in Quadrant "+Utilities.getQuadrant(cir, j1.x, j1.y);
					}

					// des += " "+t_name+" touching circle at "+name;

					if(angle_flag || length_flag)
					{
						t_slope += t_name+" touches "+circle_name+" at "+name;
						if(angle_flag)
							t_slope += ","+t.label_slope; //slope(t.slope);
						if(length_flag)
						{
							if(angle_flag)
								t_slope += " and";
							t_slope += " has length = "+t.length_ratio;
						}
					}
					t_slope+=". "+ratio;
				}
				// System.out.println("t_slope: "+t_slope);
			}

			des += t_slope;
		}
		des +=". ";
		return des;
	}
  
	
	//"BAse <name> of Square <name> is at an angle x. All sides have length = y." + association_description
	String describe_square(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){

		String des = "";

		String square_name = "Square "+Utilities.polyName(all_data, s);

		Line ln = Utilities.getLineWithId(all_data.lines, s.line_components.get(0));
		if(angle_flag)
		{
			des += "Base "+Utilities.line_name(ln, all_data)+ " of "+square_name+ln.label_slope/*slope(ln.slope)*/+". ";
		}
		if(length_flag)
		{
			des += "All sides have length = "+ ln.length_ratio+". ";
		}

		des += association_des(all_data, s, square_name, angle_flag, length_flag);
		return des;
	}

	//"Base <name> of Rectangle <name> is at an angle x and has length y. Rectangle <name> has length = x and breadth = y" + association_description
	String describe_rectangle(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){

		String des = "";

		String rect_name = "Rectangle "+Utilities.polyName(all_data, s);

		Line ln = Utilities.getLineWithId(all_data.lines, s.line_components.get(0));

		if(angle_flag)
			des += "Base "+Utilities.line_name(ln, all_data)+ " of "+rect_name+ln.label_slope+" and has length = "+ln.length_ratio+". ";
	
		ln = Utilities.getLineWithId(all_data.lines, s.length_id);
		Line lb = Utilities.getLineWithId(all_data.lines, s.breadth_id);

		if(length_flag)
		{
			des += rect_name+" has length = "+ln.length_ratio+" and breadth = " + lb.length_ratio+". ";
		}
		des += association_des(all_data, s, rect_name, angle_flag, length_flag);
		return des;
	}

	//base and line description  + association_description
	String describe_rhombus(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){
		
		String des = lengthAngle(all_data, s, angle_flag, length_flag);

		des += association_des(all_data, s, "Rhombus "+ Utilities.polyName(all_data, s), angle_flag, length_flag);
		return des;
	}

	//base and line description  + association_description
	String describe_parallelogram(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){

		String des = lengthAngle(all_data, s, angle_flag, length_flag);

		des += association_des(all_data, s, "Parallelogram "+ Utilities.polyName(all_data, s), angle_flag, length_flag);
		return des;
	}
	
	//base and line description  + association_description
	String describe_quadrilateral(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){
		String des = lengthAngle(all_data, s, angle_flag, length_flag);

		des += association_des(all_data, s, "Quadrilateral "+ Utilities.polyName(all_data, s), angle_flag, length_flag);
		return des;
	}

	//"Base <name< of equilateral triangle <name> is at an angle x. All lines have length = y."+ association_description
	String describe_equilateral_triangle(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){
		
		String des = "";

		Line ln = Utilities.getLineWithId(all_data.lines, s.line_components.get(0));
		if(angle_flag || length_flag)
		{	
			des += "Base "+Utilities.line_name(ln, all_data)+ " of Equilateral Triangle "+Utilities.polyName(all_data, s)+" ";
			if(angle_flag)
				des += ln.label_slope; //slope(ln.slope);
			if(length_flag)
			{
				des += ". All lines have length = "+ ln.length_ratio+". ";
			}
		}

		des += association_des(all_data, s, "Triangle "+ Utilities.polyName(all_data, s), angle_flag, length_flag);
		return des;
	}

	//"Triagle<name> is right isosceles triangle. Base <name> is at an angle x and length = y. Perpendicular  <name> is at an angle x and length = y. Hypotenuse <name> is at an angle x and length = y."  + association_description
	String describe_right_isosceles_triangle(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){

		String tri_name = "Triangle "+Utilities.polyName(all_data, s);
		String des = tri_name + " is a right isocesles triangle. ";
		
		Line p = Utilities.getLineWithId(all_data.lines, s.perpendicular_id), b =Utilities.getLineWithId(all_data.lines, s.base_id), h = Utilities.getLineWithId(all_data.lines, s.hypotenuse_id);
		
		String pname = "Perpendicular "+Utilities.line_name(p, all_data), bname = "Base "+Utilities.line_name(b, all_data), hname = "Hypotenuse "+Utilities.line_name(h, all_data);

		if(angle_flag || length_flag)
		{	
			des += bname;
			if(angle_flag)
				des += b.label_slope; //slope(ln.slope);
			if(length_flag)
			{
				if(angle_flag)
					des += " and";
				des += " has length = "+b.length_ratio;
			}
		}
		if(angle_flag || length_flag)
		{	
			des += ", "+pname;
			if(angle_flag)
				des += p.label_slope; //slope(ln.slope);
			if(length_flag)
			{
				if(angle_flag)
					des += " and";
				des += " has length = "+p.length_ratio;
			}
		}
		if(angle_flag || length_flag)
		{	
			des += " and "+hname;
			if(angle_flag)
				des += h.label_slope; //slope(ln.slope);
			if(length_flag)
			{
				if(angle_flag)
					des += " and";
				des += " has length = "+h.length_ratio;
			}
		}
		des += ". ";
		des += association_des(all_data, s, tri_name, angle_flag, length_flag);
		return des;
	}

	//"Triagle<name> is right triangle. Base <name> is at an angle x and length = y. Perpendicular  <name> is at an angle x and length = y. Hypotenuse <name> is at an angle x and length = y."  + association_description
	String describe_right_triangle(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){

		String tri_name = "Triangle "+Utilities.polyName(all_data, s);
		String des = tri_name + " is a right triangle. ";

		Line p = Utilities.getLineWithId(all_data.lines, s.perpendicular_id), b =Utilities.getLineWithId(all_data.lines, s.base_id), h = Utilities.getLineWithId(all_data.lines, s.hypotenuse_id);
		String pname = "Perpendicular "+Utilities.line_name(p, all_data), bname = "Base "+Utilities.line_name(b, all_data), hname = "Hypotenuse "+Utilities.line_name(h, all_data);

		if(angle_flag || length_flag)
		{	
			des += bname;
			if(angle_flag)
				des += b.label_slope; //slope(ln.slope);
			if(length_flag)
			{
				if(angle_flag)
					des += " and";
				des += " has length = "+b.length_ratio;
			}
		}
		if(angle_flag || length_flag)
		{	
			des += ", "+pname;
			if(angle_flag)
				des += p.label_slope; //slope(ln.slope);
			if(length_flag)
			{
				if(angle_flag)
					des += " and";
				des += " has length = "+p.length_ratio;
			}
		}
		if(angle_flag || length_flag)
		{	
			des += " and "+hname;
			if(angle_flag)
				des += h.label_slope; //slope(ln.slope);
			if(length_flag)
			{
				if(angle_flag)
					des += " and";
				des += " has length = "+h.length_ratio;
			}
		}
		des += ". ";
		des += association_des(all_data, s, tri_name, angle_flag, length_flag);
		return des;
	}

	//"Triagle<name> is an isosceles triangle with inequal side as <name>."+ base and line description + association_description
	String describe_isosceles_triangle(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){

		String tri_name = "Triangle "+Utilities.polyName(all_data, s);
		String des = tri_name+" is an isosceles triangle with inequal side as "+Utilities.line_name(Utilities.getLineWithId(all_data.lines, s.inequal_side), all_data)+". " ;

		des += lengthAngle(all_data, s, angle_flag, length_flag);

		des += association_des(all_data, s, tri_name, angle_flag, length_flag);

		return des;
	}

	//base and line description + association_description
	String describe_triangle(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){

		String des = lengthAngle(all_data, s, angle_flag, length_flag);

		des += association_des(all_data, s, "Triangle "+Utilities.polyName(all_data, s), angle_flag, length_flag);

		return des;
	}

	//"Pentagin<name> is regular/irregular." + base and line description + association_description
	String describe_pentagon(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){
	
		String pent_name = "Pentagon "+Utilities.polyName(all_data, s);
		
		String des = pent_name+" is ";
		des +=(s.regularity == true)?"regular. ":"irregular. ";
		
		des += lengthAngle(all_data, s, angle_flag, length_flag);

		des += association_des(all_data, s, pent_name, angle_flag, length_flag);
		return des;
	}
	
	//"Hexagon <name> is regular/irregular." + base and line description + association_description
	String describe_hexagon(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){
		
		String hex_name = "Hexagon "+Utilities.polyName(all_data, s);
		String des = hex_name+" is ";
		des +=(s.regularity == true)?"regular. ":"irregular. ";
		
		des += lengthAngle(all_data, s, angle_flag, length_flag);

		des += association_des(all_data, s, hex_name, angle_flag, length_flag);
		return des;
	}

	//"<name> is a n-sided polygon." + base and line description + association_description
	String describe_polygon(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){
		String nc_name = Utilities.polyName(all_data, s);

		String des = nc_name+" is a "+s.line_components.size()+"-sided polygon. ";

		des += lengthAngle(all_data, s, angle_flag, length_flag);

		des += association_des(all_data, s, "Polygon "+nc_name, angle_flag, length_flag);

		return des;
	}

	//"<name> is a n-sided non-convex shape." + base and line description + association_description
	String describe_non_convex_shape(Extracted_Information all_data, GroupWise group, Shape s, Boolean angle_flag, Boolean length_flag){
		String nc_name = Utilities.polyName(all_data, s);

		String des = nc_name+" is a "+s.line_components.size()+"-sided non-convex shape. ";

		des += lengthAngle(all_data, s, angle_flag, length_flag);
		des += association_des(all_data, s, nc_name, angle_flag, length_flag);

		return des;
	}


	String describe_shape(Extracted_Information all_data, GroupWise group)
	{
		String des = "";
		Boolean angle_flag = true, length_flag =true;
		for(int i = 0; i < group.shape_id.size(); i++)
		{
			//for each shape, call the method "describe_<shape_name>"(e.g., for square, the following lines will call describe_square)
			Shape sh = Utilities.getShapeWithId(all_data.shapes, group.shape_id.get(i));
			String method_name = "describe_"+sh.shape_name.toLowerCase(Locale.ENGLISH);
			// System.out.println(method_name);
			Method method;
			try {
				//this tells the name and type of arguments
			  	method = GroupShapeDescription.class.getDeclaredMethod(method_name, Extracted_Information.class, GroupWise.class, Shape.class, Boolean.class, Boolean.class);
			  	//this calls the method with the given arguments
				des += method.invoke(new GroupShapeDescription(), all_data, group, sh, angle_flag, length_flag);
			} 
			catch (SecurityException e) { System.out.println("exception 1"); }
			catch (NoSuchMethodException e) {System.out.println("exception 2 "+e);}
			catch (IllegalArgumentException e) {System.out.println("exception 3 "+e);}
			catch (IllegalAccessException e) {System.out.println("exception 4");}
			catch (InvocationTargetException e) {System.out.println("exception 5");}
		}
		return des;
	}
}