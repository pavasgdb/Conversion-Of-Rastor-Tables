package src;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import java.io.File;
import java.io.PrintStream;

import src.All_class.*;

public class SVG_Generation
{
	static Line getLineWithId(ArrayList<Line> l, int index)
    {
        for(int i = 0; i<l.size(); i++)
        {
            if(l.get(i).id == index)
                return l.get(i);
        }
        return new Line();
    }

    static Circle getCircleWithId(ArrayList<Circle> c, int index)
    {
        for(int i = 0; i<c.size(); i++)
        {
            if(c.get(i).id == index)
                return c.get(i);
        }
        return new Circle();
    }

	static void addShape(String shape_name, Shape shape_object, String svgfile, ArrayList<Line> lines, ArrayList<Circle> circles) throws FileNotFoundException
	{



		if(shape_name.equals("Circle"))
		{
			int circleid = shape_object.circle_components.get(0);
			Circle c = getCircleWithId(circles,circleid);
			System.out.println("<circle id='"+circleid+"' cx='"+c.x+"' cy='"+c.y+"' r='"+c.r+"' stroke='black' stroke-width='2' fill='none' />");

		}
		else if(shape_name.equals("Line"))
		{
			int lineid = shape_object.line_components.get(0);
			Line l = getLineWithId(lines,lineid);
			System.out.println("<line id='"+lineid+"' x1='"+l.points[0]+"' y1 = '"+l.points[1]+"' x2= '"+l.points[2]+"' y2='"+l.points[3]+"' stroke-width='2' stroke='black' />");
		}
		else
		{
			for(int j=0;j<shape_object.line_components.size();j++)
            {
                int lineid = shape_object.line_components.get(j);
				Line l = getLineWithId(lines,lineid);
				System.out.println("<line id='"+lineid+"' x1='"+l.points[0]+"' y1 = '"+l.points[1]+"' x2= '"+l.points[2]+"' y2='"+l.points[3]+"' stroke-width='2' stroke='black' />");
            }
		}
		
	}

	void generateSVG(ArrayList<Shape> shapes, String svgfile, ArrayList<Line> lines, ArrayList<Circle> circles, ArrayList<Label> labels) throws FileNotFoundException
	{

		//prinitng information to template file
        PrintStream o1 = new PrintStream(new File(svgfile));
        // Store current System.out before assigning a new value
        PrintStream console = System.out;
        // Assign o1 to output stream
        System.setOut(o1);
        System.out.println("<?xml version='1.0' standalone='no'?>\n<svg width='1000' height='1000'>");
		for(int i=0;i<shapes.size();i++)
	        {
				//add shape to svg svg.add(shape_name,shapes.get(i), svgfile) //draw shape acc to shape_name- component lines and associated lines
	            SVG_Generation svg_writer = new SVG_Generation();
	            addShape(shapes.get(i).shape_name, shapes.get(i), svgfile, lines, circles);

	            // add associated lines
	            for(int j=0;j<shapes.get(i).associations.size();j++)
	            {
	                int lineid = shapes.get(i).associations.get(j).line_id;
					Line l = getLineWithId(lines,lineid);
					System.out.println("<line id='"+lineid+"' x1='"+l.points[0]+"' y1 = '"+l.points[1]+"' x2= '"+l.points[2]+"' y2='"+l.points[3]+"' stroke-width='2' stroke='black' />");
	            }
	        }

	    for(int i=0;i<labels.size();i++)
	    {
	    	System.out.println("<text x='"+labels.get(i).x+"' y='"+labels.get(i).y+"' fill='black'>"+labels.get(i).c+"</text>");
	    }
	    System.out.println("</svg>");
	    System.setOut(console);
    }

}