package src;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileDescriptor;

import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import src.All_class.*;
import src.Utilities.*;

import java.lang.Double;
import java.util.HashMap; 
import java.util.Map; 
import java.util.regex.Pattern; 
import java.util.regex.Matcher; 

public class LabelAssociation {


    //reads labels from the csv file and create Label objects
    void read_label(String filename, ArrayList<Label> labels) throws FileNotFoundException, IOException
    {
        String line = null;// This will reference one line at a time     
        FileReader fr = new FileReader(filename);
        BufferedReader br = new BufferedReader(fr);
        line = br.readLine();
        if(line == null)
            return;
        String[] heading = line.split(","); //stores the headings of the csv file
        int h=0, w=1, x=2, y=3, t=4; //index of height, width, x, y, text
        for (int i=0;i<heading.length; i++) //this loop stores the indices of the various headings
        {
            // System.out.println("heading["+ i+"]: "+heading[i]);
            if(heading[i].equals("height"))
                h = i;
            else if(heading[i].equals("width"))
                w = i;
            else if(heading[i].equals("left"))
                x = i;
            else if(heading[i].equals("top"))
                y = i;
            else if(heading[i].equals("text"))
                t = i;

        }
        // System.out.println(" h, w, x, y, t "+h+" "+w+" "+x+" "+y+" "+t);
        line = br.readLine();
        //this loop reads the file line by line
        while(line != null) {
            String[] data = line.split(",");
            line = br.readLine();
            //creating new label object
            labels.add(new Label(data[t], Float.parseFloat(data[x]), Float.parseFloat(data[y])+Float.parseFloat(data[h]), Float.parseFloat(data[h]), Float.parseFloat(data[w]), -1));
        }   
        br.close();   
        
    }
    
    //finds distance of label from junction, returns  min_distance and junction id
    Pair<Double, Integer> label_junction_distance(Label label, ArrayList<Junction> junctions)
    {
        //center of the label
        double centerX = label.x +  label.width/2;
        double centerY = label.y -  label.height/2;

        double min_junction_distance = Double.MAX_VALUE; //double max_value and thus the import

        double a, b, d;
        int vid = 0;
        for(int i = 0; i < junctions.size(); i++)
        {
            a = (junctions.get(i).x - centerX) * (junctions.get(i).x - centerX);
            b = (junctions.get(i).y - centerY) * (junctions.get(i).y - centerY);
            d = Math.sqrt(a + b); //d is distance

            if(d < min_junction_distance) //if d is less than minimum than store d and corresponding junction index
            {
                vid = junctions.get(i).id;  
                min_junction_distance = d;
            } 
        }
        return new Pair<Double, Integer>(min_junction_distance, vid);
    }

    //finds distance of label from vertex, returns  min_distance and vertex id
    Pair<Double, Integer> label_vertex_distance(Label label, ArrayList<Vertex> vertices, String type, ArrayList<Vertex_Edge> table)
    {
        double min_vertex_distance = Double.MAX_VALUE; //double max_value and thus the import
        
        //label center
        double centerX = label.x +  label.width/2;
        double centerY = label.y -  label.height/2;

        double a, b, d;
        int vid =0;
        // System.out.println("vertices.size(): "+vertices.size());
        for(int i = 0; i < vertices.size(); i++)
        {

            //if the label is of "length" type, then check its association only if an angle is formed at that vertex, i.e, the vertex is an end point of 2 or more lines
            if(type == "name" || (type == "length" && Utilities.vertexOfMoreThanOne(table, vertices.get(i).id)) )
            {
                a = (vertices.get(i).x - centerX) * (vertices.get(i).x - centerX);
                b = (vertices.get(i).y - centerY) * (vertices.get(i).y - centerY);
                d = Math.sqrt(a + b);

                if(d < min_vertex_distance)
                {
                    vid = vertices.get(i).id;  
                    min_vertex_distance = d;
                }
            }
        }
        return new Pair<Double, Integer>(min_vertex_distance, vid);
    }

    //finds distance of label from line mid_point, returns min_distance and line id
    Pair<Double, Integer> label_line_distance(Label label, ArrayList<Line> lines, ArrayList<Vertex> vertices)
    {
        //label center
        double centerX = label.x +  label.width/2;
        double centerY = label.y -  label.height/2;

        double min_line_distance = Double.MAX_VALUE; //double max_value and thus the import

        double a, b=-1, c, num, den, d;
        int lid = 0;

        Vertex v1, v2;
        for(int i = 0; i < lines.size(); i++)
        {
            // System.out.println("v1: "+lines.get(i).v1+ " v2: "+lines.get(i).v2);
            v1 = Utilities.getVertexWithId(vertices, lines.get(i).v1);
            v2 = Utilities.getVertexWithId(vertices, lines.get(i).v2);
            //mid point of line
            double mid_x = (v1.x + v2.x)/2, mid_y = (v1.y + v2.y)/2;
            
            a = (mid_x - centerX) * (mid_x - centerX);
            b = (mid_y - centerY) * (mid_y - centerY);
            d = Math.sqrt(a + b);

            if(d < min_line_distance)
            {
                lid = i;  
                min_line_distance = d;
            }
        }
        return new Pair<Double, Integer>(min_line_distance, lid);
    }


    // find which vertex of line lv1-lv2 is closer to the label with center: (centerX, centerY)
    int min_distance(int lv1, int lv2, double centerX, double centerY, ArrayList<Vertex> vertices)
    {
        Vertex v1 = Utilities.getVertexWithId(vertices, lv1), v2 = Utilities.getVertexWithId(vertices, lv2);
        double d1 = Math.sqrt((v1.x - centerX)*(v1.x - centerX) + (v1.y - centerY)*(v1.y - centerY));
        double d2 = Math.sqrt((v2.x - centerX)*(v2.x - centerX) + (v2.y - centerY)*(v2.y - centerY));
        if(d1<d2)
            return lv1;
        else return lv2;
    }

    //if label is "length" type and is getting associated to junction, find which angle is it representing (which 1 out of 4 of "X" or which one out of 3 of "T")
    Pair<Integer, Integer> find_junction_details(Label label, Junction junction, ArrayList<Vertex> vertices, ArrayList<Line> lines)
    {
        int l1 = junction.id1, l2 = junction.id2;
        String t1 = junction.type1, t2 = junction.type2;

        //line end points
        int l1v1 = Utilities.getLineWithId(lines, l1).v1, 
        l1v2 = Utilities.getLineWithId(lines, l1).v2, 
        l2v1 = Utilities.getLineWithId(lines, l2).v1, 
        l2v2 = Utilities.getLineWithId(lines, l2).v2;

        //label center
        double centerX = label.x +  label.width/2;
        double centerY = label.y +  label.height/2;

        int vid1=-1, vid2=-1;


        //find which vertices of the intersecting lines are closer to the label
        //Handles X and T-shape intersection
        if(t1 == "Line" && t2 == "Line")
        {
            if(junction.vid!=-1) // for T-shape intersection
            {
                if(junction.vid == l1v1 || junction.vid == l1v2)
                {
                    vid1 = (junction.vid == l1v1)? l1v2: l1v1;
                    vid2 = min_distance(l2v1, l2v2, centerX, centerY, vertices);
                }
                else 
                {
                    vid1 = min_distance(l1v1, l1v2, centerX, centerY, vertices);
                    vid2 = (junction.vid == l2v1)? l2v2: l2v1;
                }
            }
            else //for cross intersection
            {
                vid1 = min_distance(l1v1, l1v2, centerX, centerY, vertices);
                vid2 = min_distance(l2v1, l2v2, centerX, centerY, vertices);
            }
        }

        return new Pair<Integer, Integer>(vid1, vid2);

    }

    //checks whether the label is "name" type or "length" type
    String check_type(Label label)
    {
        if(label.c.equals("0")) //a "0" (zero) cannot occur on its own in an image (possibly an O)
            return "None";
      

        Pattern length = Pattern.compile("\\d+[.]*\\d*[a-z]*"); 
        Pattern variable = Pattern.compile("[a-z]+");
        Pattern angle = Pattern.compile("[0-9]+"+"\u00B0");
        Pattern name = Pattern.compile("[a-zA-Z]+");

      
        Matcher m = length.matcher(label.c); 
        Matcher m3 = angle.matcher(label.c); 
        Matcher m1 = variable.matcher(label.c);
        Matcher m2 = name.matcher(label.c);

        if(m.matches() || m1.matches() || m3.matches())
            return "length";
        else if(m2.matches()) 
            return "name";
        else return "None"; //to handle mis-detected characters
    }

    //Associate label with line
    void associateLine(String type, Line line, Label label, ArrayList<LengthLabel> ll, HashMap<Pair<Integer, String> , LineLabel> la)
    {
        // if(type == "name") //if label is "name" type
        //    la.put(new Pair<Integer, String>(line.id, type), new LineLabel(line.id, label.id, -1, "name_single"));
        // else // if label is "length" type
        {
            // System.out.println("Line vertices: "+line.id+" "+line.v1+" "+line.v2);
            LengthLabel lbl = new LengthLabel(line.v1, line.v2, "Vertex", "Vertex");
            ll.add(lbl);
            label.associated_length_id = ll.size()-1;
            la.put(new Pair<Integer, String>(line.id, type), new LineLabel(line.id, label.id, -1, type));
            line.label_length = label.c;
            // line.reference_line_id = -1;
        }
    }

    //associate label to the junction
    void associateJunction(String type, HashMap<Pair<Integer, String>, JunctionLabel> ja, Label label, Junction junction, ArrayList<Line> lines, ArrayList<Vertex> vertices)
    {
        if(type == "name")
            ja.put(new Pair<Integer, String>(junction.id, type), new JunctionLabel(junction.id, label.id, type));
        else
        {
            Pair<Integer, Integer> p = find_junction_details(label, junction, vertices, lines);
            ja.put(new Pair<Integer, String>(junction.id, type), new JunctionLabel(junction.id, label.id, type, p.getKey(), p.getValue(), "Vertex", "Vertex"));
        }
    }

    //associate label with one of junction, vertex or distance 
    void associate(Label label, Line line, Vertex vertex, Junction junction, double min_line_distance, double min_vertex_distance, double min_junction_distance, HashMap<Pair<Integer, String> , LineLabel> la, HashMap<Pair<Integer, String> , VertexLabel> va, HashMap<Pair<Integer, String> , JunctionLabel> ja, ArrayList<LengthLabel> ll, ArrayList<Vertex> vertices, ArrayList<Line> lines, String type)
    {
        // System.out.println("Label: "+label.c+" l_dist: "+min_line_distance+" j_dist "+min_junction_distance+" v_dist "+min_vertex_distance);
        if(min_line_distance > min_junction_distance) 
        {
            if(min_line_distance >= min_vertex_distance) 
            {
            	if(min_junction_distance >= min_vertex_distance) //line, junction, vertex
                {
                    va.put(new Pair<Integer, String>(vertex.id, type), new VertexLabel(vertex.id, label.id, type));
                }
                else //line, vertex, junction
                {
                    if(junction.vid == -1)
                        associateJunction(type, ja, label, junction, lines, vertices);
                    else
                        va.put(new Pair<Integer, String>(junction.vid, type), new VertexLabel(junction.vid, label.id, type));
                }
            }
            else //vertex, line, junction
            {
                if(junction.vid == -1)
                    associateJunction(type, ja, label, junction, lines, vertices);
                else
                    va.put(new Pair<Integer, String>(junction.vid, type), new VertexLabel(junction.vid, label.id, type));
            }

        }
        else 
        {
            if(min_line_distance >= min_vertex_distance) // junction, line, vertex
            {
                va.put(new Pair<Integer, String>(vertex.id, type), new VertexLabel(vertex.id, label.id, type));
            }
            else 
            {
            	if(min_junction_distance >= min_vertex_distance) // junction, vertex, line
                {
                	if(Math.abs(min_vertex_distance - min_line_distance) <= 6 || type.equals("name"))
                	{
                		va.put(new Pair<Integer, String>(vertex.id, type), new VertexLabel(vertex.id, label.id, type));
                	}
                	else
                	{
                        associateLine(type, line, label, ll, la);
                	}
                }
                else //vertex, junction, line
                {
                	if(Math.abs(min_junction_distance - min_line_distance) <= 6|| type.equals("name"))
                	{
                        // System.out.println("junction associated");
                        if(junction.vid == -1)
                            associateJunction(type, ja, label, junction, lines, vertices);
                        else
                            va.put(new Pair<Integer, String>(junction.vid, type), new VertexLabel(junction.vid, label.id, type));
                	}
                	else
                	{
                		associateLine(type, line, label, ll, la);
                	}
                }
            }
        }
        return;
        // System.out.println("Label: "+label.c+" associated with: "+la.size()+" "+va.size()+" "+ja.size());
    }


    //find the first line that will be described and set that as reference line. Give length of all other lines with respect to this reference line
    void setLineLength(Extracted_Information all_data)
    {
        int flag_id = -1;
        if(all_data.lines.size()!=0)
        {
            // System.out.println("group_wise.size(): "+all_data.group_wise.size());
            for(int i = 0;i<all_data.group_wise.size();i++)
            {
                ArrayList<Integer> shape = all_data.group_wise.get(i).shape_id;
                for(int j = 0;j< shape.size(); j++)
                {
                    Shape s = Utilities.getShapeWithId(all_data.shapes, shape.get(j));
                    if(s.shape_name.equals("Circle"))
                    {
                        // System.out.println("Circle");
                        if(s.associations.size()!=0)
                        {
                            flag_id = s.associations.get(0).line_id;
                        }
                    }
                    else
                    {
                        flag_id = s.line_components.get(0);
                        break;
                    }
                }
                if(flag_id!=-1)
                    break;
            }
        }
    
        all_data.reference_line_id = flag_id;
        Line l_flag = Utilities.getLineWithId(all_data.lines, flag_id);

        for(int i = 0; i<all_data.lines.size(); i++)
        {
            Line ln = all_data.lines.get(i);

            if(ln.id == flag_id)
            {
                ln.length_ratio = "x";
            }
            else if(flag_id!=-1)
            {
                double ratio = Math.round((ln.length/l_flag.length)*10.0)/10.0;
                ln.length_ratio = Double.toString(ratio)+"x";
            }
            // System.out.println("Line "+ln.id+" label_length "+ln.label_length);
        }
    }

    //associate labels with components
    void associate_labels(String filename, Extracted_Information all_data) throws IOException
    {

        ArrayList<Label> labels = new ArrayList<Label>();
        HashMap<Pair<Integer, String> , LineLabel> la = new HashMap<>(); //hashmap where key is <Line_id, "name"/"length"> and value is label id 
        HashMap<Pair<Integer, String> , VertexLabel> va = new HashMap<>(); //hashmap where key is <Vertex_id, "name"/"length"> and value is label id 
        HashMap<Pair<Integer, String> , JunctionLabel> ja = new HashMap<>(); //hashmap where key is <Junction_id, "name"/"length"> and value is label id 
        ArrayList<LengthLabel> ll = new ArrayList<LengthLabel>(); //array list storing all length labels
        
        try {
            read_label(filename, labels);   
        }
        catch(FileNotFoundException ex) {     
            System.out.println("Unable to open file '" + filename + "'");  
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + filename + "'");                  
        }

        if(labels.size()!=0)
        {
            for(int i = 0; i<labels.size(); i++)
            {
                String type = check_type(labels.get(i));
                System.out.println(labels.get(i).c+" "+type);
                if(type.equals("None"))
                    continue;
                double min_line_distance=Double.MAX_VALUE, min_vertex_distance=Double.MAX_VALUE, min_junction_distance=Double.MAX_VALUE;
                
                Line line = new Line();
                Pair<Double, Integer> p;
                p = label_line_distance(labels.get(i), all_data.lines, all_data.vertices);
                min_line_distance = p.getKey();
                line = Utilities.getLineWithId(all_data.lines, p.getValue());

                Vertex vertex = new Vertex();
                p = label_vertex_distance(labels.get(i), all_data.vertices, type, all_data.table);
                min_vertex_distance =p.getKey();
                vertex = Utilities.getVertexWithId(all_data.vertices, p.getValue());

                Junction junction = new Junction();
                p= label_junction_distance(labels.get(i), all_data.intersections);
                min_junction_distance = p.getKey();
                junction = Utilities.getJunctionWithId(all_data.intersections, p.getValue());

                associate(labels.get(i), line, vertex, junction, min_line_distance, min_vertex_distance, min_junction_distance, la, va, ja, ll, all_data.vertices, all_data.lines, type);

            }
        }

        all_data.labels = labels;
        all_data.length_labels = ll;
        all_data.lineAssociation = la;
        all_data.vertexAssociation = va;
        all_data.junctionAssociation = ja;
        setLineLength(all_data);
        return;
    }
}
