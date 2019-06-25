package src;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.*;

import java.io.IOException;
import java.io.File;
import java.io.PrintStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import src.All_class.*;
import src.Utilities.*;
import java.util.HashMap; 
import java.util.Map; 
import javafx.util.Pair; 
import java.util.ArrayList;

public class Driver
{
    public static void main(String args[]) throws IOException{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // loads openCV library

        // image name as command line argument
        String imname = args[0];
        // directory paths used throughout the source code files
        String path_thin="./src/code/preprocess.py";   
        String circlepp ="./output/"+imname+"_circlepp.png";
        String linepp ="./output/"+imname+"_linepp.png";
        String path_output="./output/"; //path to thinned image obtained from preprocess.py
        String path_input_img = "./test_bench/ix/"; //path to input image sent to preprocess.py
        String pathHL = "./output/"+imname+"_01HoughLine.png";
        String pathHC = "./output/"+imname+"_02HoughCircle.png";
        String pathblur = "./output/"+imname+"_blur.png";
        String path_median = "./src/median.py";
        String pathPostProc = "./output/"+imname+"_PostProc.png";
        String pathTemplate = "./output/template_texts/"+imname;
        String pathSVG = "./output/SVGfiles/"+imname+"_SVG.svg";
        String fname = "./output/template_texts/Description";


        
        /// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Image preprocessing~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //call the python module for thinning and disconnection
        // Process p1 = Runtime.getRuntime().exec("python3 "+path_thin+ " "+ imname+" "+path_input_img+" "+path_output);
        // try 
        // {
        //     p1.waitFor();
        // } 
        // catch(InterruptedException e)
        // {
        //     System.out.println("Exception raised in thinning");
        // }


        /// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~Line Detection (preprocessing followed by Probabilistic HoughLines)~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // object of lineDetection class
        LineDetection ld = new LineDetection();
        
        ArrayList<Line> lines = new ArrayList<>();
        ArrayList<Vertex> vertices = new ArrayList<Vertex>();
        ArrayList<Vertex_Edge> table = new ArrayList<Vertex_Edge>();
        // calling linedetect function for preprocessing and HoughTransform
        lines = ld.LineDetect(path_output+imname+"_thinned.png", linepp, pathHL, pathblur, path_median);

        // print output of houghlines, ie. all line endpoints
        System.out.println("Hough_transform---"+lines.size());
        for(int i=0;i<lines.size();i++)
            System.out.println(lines.get(i).id+"\t"+lines.get(i).points[0]+" "+lines.get(i).points[1]+" "+lines.get(i).points[2]+" "+lines.get(i).points[3]);

        
        /// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~Circle Detection (preprocessing followed by HoughCircles)~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        CirclePreProcessing cpp = new CirclePreProcessing();
        ArrayList<Circle> circles = new ArrayList<Circle>();
        cpp.CirclePreProcess(path_output+imname+"_thinned.png", circlepp, pathHC, circles);

        
        /// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~PostProcessing (Lines)~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Line.count = 0; //reset the line ids count to 0
        PostProcess postproc = new PostProcess();
        lines = postproc.postOperations(lines, circles, imname);   //, circles, thresh_line_overlap_circle);
        
        
        /// ~~~~~~~~~~~~~~~~~~~~~~~~Detecting intersections among lines and circles~~~~~~~~~~~~~~~~~~

        Average_Junction aj = new Average_Junction();
        lines = aj.JunctionAveraging(lines);
        System.out.println("Lines after junction avging----------------*********** ");
        for (int i = 0; i < lines.size(); i++) {
            System.out.println("Line"+(i+1)+":");
            System.out.println("{ID, Type, End points, Thickness}:\t{"+lines.get(i).id +", "+lines.get(i).type+", (("+lines.get(i).points[0]+", "+lines.get(i).points[1]+"), ("+lines.get(i).points[2]+", "+lines.get(i).points[3]+")), 1px}");
              
        }
        //lines = aj.JunctionAveraging(lines);


        ArrayList<Junction> pre_junctions = new ArrayList<Junction>();
        Intersection inter = new Intersection();
        pre_junctions = inter.intersections(lines, circles, vertices, table);



/*
        /// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Storing info into document (text & image)~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Creating a File object that represents the text file on disk.
        PrintStream o1 = new PrintStream(new File(pathTemplate+"_Template1.txt"));
 
        // Store current System.out before assigning a new value
        PrintStream console = System.out;
 
        // Assign o1 (printstream for text file) to output stream
        System.setOut(o1);
*/
        //src1 loads empty image on which PostProcessing output will be drawn
        Mat src1 = Imgcodecs.imread(pathPostProc, Imgcodecs.IMREAD_COLOR);
        
        // writing the information on text file
        System.out.println("Number of lines: "+lines.size());
        System.out.println("Number of circles: "+circles.size());
        System.out.println(" ");
        System.out.println("\n\nLine properties:\n");

        for (int i = 0; i < lines.size(); i++) {
            System.out.println("Line"+(i+1)+":");
            System.out.println("{ID, Type, End points, Thickness}:\t{"+lines.get(i).id +", "+lines.get(i).type+", (("+lines.get(i).points[0]+", "+lines.get(i).points[1]+"), ("+lines.get(i).points[2]+", "+lines.get(i).points[3]+")), 1px}");
            // specifying lines to be drawn on output image file
            Imgproc.line(src1, new Point(lines.get(i).points[0], lines.get(i).points[1]),new Point(lines.get(i).points[2], lines.get(i).points[3]), new Scalar(0,0,255), 1) ;    
        }
        //save PostProcessing output as an image file
        Imgcodecs.imwrite(pathPostProc,src1);
/*
        Utilities.printVertex(vertices);

        if(circles.size()>0)
        {   
            System.out.println("");
            System.out.println("Circle Properties");
        }
        
        /// write circle details and Draw the circles detected
        for( int i = 0; i < circles.size(); i++ )
        {
            Point center = new Point(Math.round(circles.get(i).x), Math.round(circles.get(i).y));
            int radius = (int)Math.round(circles.get(i).r);
            // circle center
            //circle( src, center, 3, new Scalar(0,255,0), -1, 8, 0 );
            // circle outline
            circle( src1, center, radius, new Scalar(0,0,255), 1, 8, 0 );
            System.out.println("Circle"+(i+1)+":");
            System.out.println("{ID, Centre points, Radius, Thickness}:\t{"+circles.get(i).id+", ("+circles.get(i).x+", "+circles.get(i).y+"), "+circles.get(i).r+", 1px}");
        }
        //save PostProcessing output as an image file
        Imgcodecs.imwrite(pathPostProc,src1);

        System.setOut(console);
        
        // Mat src1 = Imgcodecs.imread("./output/"+imname+"_output.png", Imgcodecs.IMREAD_COLOR);
        // Imgcodecs.imwrite("./output/"+imname+"_output.png",src1);

        ArrayList<Shape> shapes = new ArrayList<Shape>();

        HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> tangents = new HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>>();

        DetectShape d = new DetectShape();
        d.detectShape(lines, circles, vertices, table, shapes, pathTemplate+"_Template2.txt", pathSVG, tangents);

        HashMap<Pair<Integer, Integer>, ArrayList<Junction>> shape_wise = new HashMap<Pair<Integer, Integer>, ArrayList<Junction>>();
        HashMap<Pair<Integer, Integer>, Integer> shape_wise_visited = new HashMap<Pair<Integer, Integer>, Integer>();
        HashMap<Pair<Integer, Integer>, ArrayList<Junction>> primitive_wise = new HashMap<Pair<Integer, Integer>, ArrayList<Junction>>();

        ArrayList<GroupWise> group_wise = new ArrayList<GroupWise>();
        Junction.count = 0; //reset the junction ids count to 0

        // System.out.println("pre_junctions.size() before shape association: "+pre_junctions.size());
        // for(int i=0;i<pre_junctions.size();i++)
        // {
        //     System.out.println(pre_junctions.get(i).id+": "+pre_junctions.get(i).type1+" ID1: "+pre_junctions.get(i).id1+" "+pre_junctions.get(i).type2+" ID2: "+pre_junctions.get(i).id2+" x: "+pre_junctions.get(i).x+" y: "+pre_junctions.get(i).y+" type1: "+pre_junctions.get(i).type1+" ratio1: "+pre_junctions.get(i).junction_ratio1+" ratio2: "+pre_junctions.get(i).junction_ratio2+" vid: "+pre_junctions.get(i).vid);
        // }

        ArrayList<Junction> junctions = new ArrayList<Junction>();

        ShapeIntersectionAssociation sia = new ShapeIntersectionAssociation();

        junctions = sia.intrShapeAssoc(lines, circles, shapes, vertices, table, pre_junctions, pathTemplate+"_Template3.txt", tangents, shape_wise, shape_wise_visited, primitive_wise, group_wise);


        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        
        Extracted_Information all_data = new Extracted_Information(lines, circles, shapes, junctions, vertices, table, shape_wise, shape_wise_visited, primitive_wise);


        System.out.println("intersections.size(): "+all_data.intersections.size());
        for(int i=0;i<all_data.intersections.size();i++)
        {
            System.out.println(all_data.intersections.get(i).id+": "+all_data.intersections.get(i).type1+" ID1: "+all_data.intersections.get(i).id1+" "+all_data.intersections.get(i).type2+" ID2: "+all_data.intersections.get(i).id2+" ("+all_data.intersections.get(i).x+", "+all_data.intersections.get(i).y+") ratio1: "+all_data.intersections.get(i).junction_ratio1+" ratio2: "+all_data.intersections.get(i).junction_ratio2+" vid: "+all_data.intersections.get(i).vid);
        }

        
        // Connected cnt = new Connected();
        // cnt.findConnections(all_data, group_wise);
        all_data.group_wise = group_wise;
        System.out.print("GroupWise: ");
        for(int i = 0; i<all_data.group_wise.size();i++)
        {
            System.out.println("Group"+i);
            for(int j = 0;j<all_data.group_wise.get(i).shape_id.size();j++)
            {
                System.out.print(all_data.group_wise.get(i).shape_id.get(j)+", ");
            }
            System.out.println("");
        
            for (Map.Entry<Pair<Integer, Integer>, ArrayList<Junction>> entry : all_data.group_wise.get(i).shape_wise.entrySet())  
            {
                System.out.println(entry.getKey().getKey()+" "+entry.getKey().getValue()+" "+entry.getValue().size());
            }
        }
*/        
        /*System.out.println("GroupWise: ");
        for(int i = 0; i<all_data.group_wise.size();i++)
        {
            System.out.println("Group"+i);
            for(int j = 0;j<all_data.group_wise.get(i).shape_id.size();j++)
            {
                System.out.print(all_data.group_wise.get(i).shape_id.get(j)+", ");
            }
        }*/
        
        // HashMap<Pair<Integer, String> , LineLabel> la = new HashMap<>(); 
        // HashMap<Pair<Integer, String> , VertexLabel> va = new HashMap<>(); 
        // HashMap<Pair<Integer, String> , JunctionLabel> ja = new HashMap<>(); 
        // ArrayList<LengthLabel> ll = new ArrayList<LengthLabel>();
        
/*
        LabelAssociation lba = new LabelAssociation();
        

        // all_data.labels = 
        lba.associate_labels(pathTemplate+"_label.csv", all_data);
        // all_data.length_labels = ll;
        // all_data.lineAssociation = la;
        // all_data.vertexAssociation = va;
        // all_data.junctionAssociation = ja;


        System.out.println("Labels: ");
        for(int i=0;i<all_data.labels.size(); i++)
            System.out.println("Label "+all_data.labels.get(i).c+" id: "+all_data.labels.get(i).id+" associated_length "+all_data.labels.get(i).associated_length_id);

        System.out.println("Length Labels: ");
        for(int i=0;i<all_data.length_labels.size(); i++)
        {
            LengthLabel lli = all_data.length_labels.get(i);
            System.out.println("id "+lli.id+" id1: "+lli.id1+" id2: "+lli.id2+" type1: "+lli.type1+" type2: "+lli.type2);
        }
        System.out.println("LineLabel: ");
        for (Map.Entry<Pair<Integer, String> , LineLabel> entry : all_data.lineAssociation.entrySet())  
        {
            System.out.println("Key = " + entry.getKey().getKey()+" Key1: "+ entry.getKey().getValue() + ", line_id = " + entry.getValue().line_id+" label_id1: "+ entry.getValue().label_id1+" label_id2: "+entry.getValue().label_id2+" type: "+entry.getValue().type); 
        }
        System.out.println("VertexLabel: ");
        for (Map.Entry<Pair<Integer, String>  , VertexLabel> entry : all_data.vertexAssociation.entrySet())  
            System.out.println("Key = " + entry.getKey().getKey()+" Key1: "+ entry.getKey().getValue() + ", vertex_id = " + entry.getValue().vertex_id+" label_id: "+ entry.getValue().label_id+" type: "+entry.getValue().type); 
        System.out.println("JunctionLabel: ");
        for (Map.Entry<Pair<Integer, String>  , JunctionLabel> entry : all_data.junctionAssociation.entrySet())  
            System.out.println("Key = " + entry.getKey().getKey()+" Key1: "+ entry.getKey().getValue() + ", junction_id = " + entry.getValue().junction_id+" label_id: "+ entry.getValue().label_id+" pid1: "+entry.getValue().pid1+" pid2: "+entry.getValue().pid2+" type: "+entry.getValue().type+" type1: "+entry.getValue().type1+" type2: "+entry.getValue().type2); 


        OverviewShapeEnrichment ose = new OverviewShapeEnrichment();
        ose.enrich(all_data);

        ShapeJunctionEnrichment sje = new ShapeJunctionEnrichment();
        sje.enrich(all_data);

        SVG_Generation svg_gen = new SVG_Generation();
        svg_gen.generateSVG(all_data.shapes, pathSVG, all_data.lines, all_data.circles, all_data.labels);

        // DescriptionGeneration dg = new DescriptionGeneration();
        // String description = dg.generate_description(all_data);
        // System.out.println("\n\nDescription: "+description);

        
        Description ds = new Description();
        String description = ds.describe(all_data);
        System.out.println("\n\nNew Description: "+description);


        File f = new File(fname);
        PrintWriter out = null;
        if ( f.exists() && !f.isDirectory() ) {
            out = new PrintWriter(new FileOutputStream(new File(fname), true));
        }
        else {
            out = new PrintWriter(fname);
        }
        out.append(imname+": "+description+"\n\n");
        out.close();

*/
	}
}
