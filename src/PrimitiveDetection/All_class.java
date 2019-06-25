package src;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap; 
import java.util.Map; 
import java.util.Set; 

public class All_class{

    static class Line{
        double [] points = new double[4]; //x1y1 x2y2 of a line
        String type; //normal, dashed, arrowed
        int thickness; //pixel thickness
        int v1, v2; //vertex ids of line end points
        double slope, length;
        int id; //line id
        static int count = 0;
        String label_name = "", label_length = "", label_slope="", length_ratio=""; //label_name is name of the line from labels(e.g, AB), label_length is the label given for length in image if any (e.g, 5cm), label_slope = "at an angle of x degrees"/"is horizontal"/"is vertical", length_ratio = ratio+"x"
        Line(){}
        
        Line(double a,double b,double c,double d, int idn, int vr1, int vr2, String t, int th){
            points[0] = a;
            points[1] = b;
            points[2] = c;
            points[3] = d;
            v1 = vr1;
            v2 = vr2;
            id = count++;
            type=t;
            thickness=th;
        }

        Line(double a,double b,double c,double d, String t){
            points[0] = a;
            points[1] = b;
            points[2] = c;
            points[3] = d;
            v1 = -1;
            v2 = -1;
            id = count++;
            type=t;
            thickness= -1;
        }

        Line(Line v){
            System.arraycopy(v.points,0,points,0,4);
            id = v.id;
            v1 = v.v1;
            v2 = v.v2;
            type = v.type;
            thickness = v.thickness;
        }
    }

    static class Circle{
        double x, y; // xy coordinate of center of circle
        double r; //circle radius
        int id; //circle id
        String type; //normal, dashed
        int thickness; //pixel thickness
        static int count = 1000;
        String label_name = "", label_radius_length = "";//label_name = "C1000", label_radius_length = ratio+"x"
        Circle(){}
        Circle(double a, double b, double c, String t, int th){
            x=a;
            y=b;
            r=c;
            id=count++;
            type=t;
            thickness=th;
        }
    }

    static class Vertex{
        double x, y; //x y coordinate of the vertex
        static int count = 0;
        int id; //vertex id
        String label_name = ""; //name of the vertex given in label
        Vertex(){}
        Vertex(double a,double b, int idn){
            x = a;
            y = b;
            id = idn;
        }
    }

    static class Label{
        static int count=0;
        int id, associated_length_id; // if label is length label, associated_length_id is the id of the LengthLabel that stores information anbout the end-points
        float x, y, height, width; //Label id, label bounding box bottom left coordinates x y, heigh, width , id of component label is associated with
        String c; //c: character
        Label(){}
        Label(String ch, float a, float b, float d, float e, int a1){
            c = ch;
            x = a;
            y = b;
            height = d;
            width = e;
            id = count++;
            associated_length_id = a1;
        }
    }

    static class Vertex_Edge{
        int v1, v2, e; //stores line id e, end point v1 vertex id, endpoint v2 vertex id
        static int count = 0;
        int id; //id of that entry of the table
        Vertex_Edge(){}
        Vertex_Edge(int a, int b, int c){
            v1 = a;
            v2 = b;
            e = c;
            id = count++;
        }
    }

    static class LengthLabel{
        static int count=0;
        int id, id1, id2; // id1, id2 are ids of the end-points that define this length
        String type1, type2; //type1, type2: "Vertex"/"Point"/"Junction" defines type of the end points
        LengthLabel(){}
        LengthLabel(int i1, int i2, String t1, String t2){
            id1 = i1;
            id2 = i2;
            type1 = t1;
            type2 = t2;
            id = count++;
        }
    }
    
    static class Shape
    {
        String shape_name; // name of the shape
        ArrayList<Integer> line_components, circle_components; //ids of lines and circles that forms this shape
        ArrayList<AngleBetweenComponents> angles; //interior angles of this shape
        int[] vertices; //set of vertices
        ArrayList<Associated_Line> associations; //associated lines of these shapes
        static int count = 0;
        int id;
        int base_id, perpendicular_id, hypotenuse_id; //in case of right angle triangle
        int inequal_side; //in case of isosceles triangle
        int length_id, breadth_id; //in case of rectangle
        Boolean regularity; // in case of pentagon and hexagon
        String label_name = "";//name of the shape given in labels
        Shape(){};
        Shape(String sname, ArrayList<Integer> lc, ArrayList<Integer> cc, ArrayList<AngleBetweenComponents> a, int[] v, ArrayList<Associated_Line> as)
        {
            shape_name = sname;
            line_components = lc;
            circle_components = cc;
            angles = a;
            vertices = v;
            id = count++;
            associations = as;
        }
        Shape(String sname, ArrayList<Integer> lc, ArrayList<Integer> cc, ArrayList<AngleBetweenComponents> a, int[] v, ArrayList<Associated_Line> as, int ie) //isosceles
        {
            shape_name = sname;
            line_components = lc;
            circle_components = cc;
            angles = a;
            vertices = v;
            id = count++;
            associations = as;
            inequal_side = ie;
        }
        Shape(String sname, ArrayList<Integer> lc, ArrayList<Integer> cc, ArrayList<AngleBetweenComponents> a, int[] v, ArrayList<Associated_Line> as, Boolean r) //Hexagon, pentagon
        {
            shape_name = sname;
            line_components = lc;
            circle_components = cc;
            angles = a;
            vertices = v;
            id = count++;
            associations = as;
            regularity = r;
        }
        Shape(String sname, ArrayList<Integer> lc, ArrayList<Integer> cc, ArrayList<AngleBetweenComponents> a, int[] v, int bid, int pid, int hid, ArrayList<Associated_Line> as) //right
        {
            shape_name = sname;
            line_components = lc;
            circle_components = cc;
            angles = a;
            vertices = v;
            id = count++;
            base_id = bid;
            perpendicular_id = pid;
            hypotenuse_id = hid;
            associations = as;
        }
        Shape(String sname, ArrayList<Integer> lc, ArrayList<Integer> cc, ArrayList<AngleBetweenComponents> a, int[] v, ArrayList<Associated_Line> as, int l, int b) //rectangle
        {
            shape_name = sname;
            line_components = lc;
            circle_components = cc;
            angles = a;
            vertices = v;
            id = count++;
            associations = as;
            length_id = l;
            breadth_id = b;
        }
    }
    
    static class Associated_Line{
        int line_id; //line id of the associated line
        String description; //description of the association
        int v1_map, v2_map; //to which vertex/edge id of the shape does v1 of line line_id maps to(in case of tangent, v1_map stores junction id)
        String type1, type2; //type1 tells v1_map is vertex_id/edge_id/centroid 
        //**If type is centroid, corresponding v_map = -1
        Associated_Line(){}
        Associated_Line(int l, String d, int v1, int v2, String t1, String t2)
        {
            line_id = l;
            description = d;
            v1_map = v1;
            v2_map = v2;
            type1 = t1;
            type2 = t2;
        }
    }

    static class AngleBetweenComponents
    {
        int cid1; //component id 1
        int cid2; // component id 2
        int vid; //id of the vertex included
        double angle; //angle value
        static int count = 0;
        int id; //angle id
        AngleBetweenComponents(){}
        
        AngleBetweenComponents(int c1, int c2, int v, double a)
        {
            cid1 = c1;
            cid2 = c2;
            angle = a;
            id = count++;
            vid = v;
        }
    }
    
    static class Junction{
        double x, y; //xy coordinate of intersection
        int id1, id2, vid;//id1, id2 are ids of lines/circles that are intersecting; vid = vertex id if intersection is at a vertex
        ArrayList<Integer>shapeid1, shapeid2; //shapeid1 shapeid2 are ids of the shapes that are associated with the intersecting lines
        
        int id;
        static int count=0;
        String type1, type2;//type1, typ2 = "Line"/ "Circle"
        String label_name = "", junction_ratio1 = "", junction_ratio2=""; //junction_ratio1 stores in which ratio id1 is divided by this junction. Same junction_ratio2
        Junction(){}
        
        Junction(int l1, int l2, double a, double b, String t1, String t2)
        {
            x=a;
            y=b;
            id1=l1;
            id2=l2;
            id = count++;
            type1 = t1;
            type2 = t2;
        }
        Junction(int l1, int l2, double a, double b, String t1, String t2, ArrayList<Integer> s1, ArrayList<Integer> s2, int v1)
        {
            x=a;
            y=b;
            id1=l1;
            id2=l2;
            id = count++;
            type1 = t1;
            type2 = t2;
            shapeid1 = s1;
            shapeid2 = s2;
            vid = v1;
        }
        Junction(int l1, int l2, double a, double b, String t1, String t2, ArrayList<Integer> s1, ArrayList<Integer> s2, int v1, String r1, String r2)
        {
            x=a;
            y=b;
            id1=l1;
            id2=l2;
            id = count++;
            type1 = t1;
            type2 = t2;
            shapeid1 = s1;
            shapeid2 = s2;
            vid = v1;
            junction_ratio1 = r1;
            junction_ratio2 = r2;
        }
    }
    
    static class LineLabel{
        int line_id, label_id1, label_id2; //line_id is associated with either a single name label_id1 or to two vertex name label_id1_label_id2 or to a length label label_id1
        String type; // = {name_double, name_single, length}
        LineLabel(){}
        LineLabel(int l1, int i1, int i2, String t){
            line_id = l1;
            label_id1 = i1;
            label_id2 = i2;
            type = t;
        }
    }

    static class VertexLabel{
        int vertex_id, label_id; // vertex_id is associated with either the name label_id or value(angle) label_id
        String type; // = {name, length}
        VertexLabel(){}
        VertexLabel(int l1, int i1, String t){
            vertex_id = l1;
            label_id = i1;
            type = t;
        }
    }

    static class JunctionLabel{ 
        int junction_id, label_id, pid1, pid2; // junction_id is associated with either the name label_id or value(angle) of <pid1, junction_id, pid2> is label_id: pid1, pid2 is id of vertex/point/junction
        String type, type1, type2; // type = {name, length}, type1, type2 = {Vertex, junction, point}
        JunctionLabel(){}
        JunctionLabel(int l1, int l2,  String t)
        {
            junction_id = l1;
            label_id = l2;
            type = t;
        }
        JunctionLabel(int l1, int l2,  String t, int i1, int i2, String t1, String t2){
            junction_id = l1;
            label_id = l2;
            pid1 = i1;
            pid2 = i2;
            type = t;
            type1 = t1;
            type2 = t2;
        }
    }

    
    static class AvgJunction{
        
        int id;
        ArrayList<Integer> xlines, tlines, junc_ids;
        //ArrayList<Point> junctions;
        ArrayList<Integer> junc_lines; //= new HashSet<Integer>();
        int xc =0, tc=0;
        double x, y;

        static int count=0;

        AvgJunction()
        {
            id = count++;
        }

        AvgJunction(ArrayList<Integer> jids)
        {
            id = count++;
            junc_ids = jids;
            junc_lines= new ArrayList<Integer>();
            xlines= new ArrayList<Integer>();
            tlines= new ArrayList<Integer>();
        }

        /*AvgJunction(ArrayList<Integer> xl, ArrayList<Integer> tl, int a, int b, ArrayList<Point> il)
        {
            id = count++;
            xlines = xl;
            tlines = tl;
            xc=a;
            tc=b;
            junctions=il;
        }*/
    }

    static class ImageOverview{
        int id;
        static int count = 0; 
        int num;//number of occurences of one type of shape 
        String shape_name; //name of that shape
        ArrayList<String> names;//name labels for all occurences of that shape
        ImageOverview(int n, String sn, ArrayList<String> nm){
            id = count++;
            num = n;
            shape_name = sn;
            names = nm;
        }
    }
    
    static class CommonComponent{ //common vertex/common line (V-type included)
        int component_id; //vertex id/line id
        String type; //"Line"/"Vertex"
        ArrayList<Integer> shape_id = new ArrayList<Integer>(); //ids of the shapes that have this component in common
        ArrayList<String> type_id = new ArrayList<String>(); //in case type_id = "Association", shape_id will have the associated line id, else type_id = "Shape"
        CommonComponent(){}
        CommonComponent(int cid, String t, ArrayList<Integer> sid, ArrayList<String> tid) 
        {
            component_id = cid;
            type = t;
            shape_id = sid;
            type_id = tid;
        }
    }

    static class XnT_Type{ // two lines intersect in X-type or T-type
        int sh1, sh2; // the two intersecting shapes (if T-type: sh1 is the shape containing "-" and sh2 is the shape conatining "|") (if Line-circle touch, sh1 is the circle and sh2 is line) NOTE: it will contain shape ids even if the shapes are lines. Handle it during description
        int map1, map2; // map1 stores ("1"/"2") whether sh1 has id1 as component or id2;
        int junction_id; //the junction id where sh1 and sh2 intersect
        String type; //"X"/"T"
        XnT_Type(int s1, int s2, int m1, int m2, int j, String t)
        {
            sh1 = s1;
            sh2 = s2;
            map1 = m1;
            map2 = m2;
            junction_id = j;
            type = t;
        }
    }

    static class InscribeType{ //one shape is inscribed inside another
        int circum_id, in_id; // id of the shapes that circumscribes and inscribed
        ArrayList<Pair<Junction, Pair<Integer, Integer>>> junctions; //for each junction, stores pair of ("1"/"2") whether out_id and in_id has id1 as component or id2; junctions that are involved in inscription
        InscribeType(int cid, int iid, ArrayList<Pair<Junction, Pair<Integer, Integer>>> j)
        {
            circum_id = cid;
            in_id = iid;
            junctions = j;
        }
    }

    static class ContainType{ //one shape contains the another: if touching on_vertices contains elements of outer shape that are touched. if completely contained, on_vertices is empty
        int out_id, in_id; //id of the shape that is outside and inside
        //on_junctions stores the junctions that correspond to the vertices of in_shape lying on the out_shape. There will be no such intersections therefore no in junctions (in_junctions stores junctions that correspond to the vertices of in_shape lying inside the out_shape)
        //in case when circle is inscribed in other shape, on_vertices contains line ids that the circle touches and in_vertices is empty
        ArrayList<Pair<Junction, Pair<Integer, Integer>>> junctions; //for each junction, stores pair of ("1"/"2") whether out_id and in_id has id1 as component or id2;
        // ArrayList<Pair<Junction, Pair<Integer, Integer>>> in_junctions; //for each junction, stores pair of ("1"/"2") whether out_id and in_id has id1 as component or id2;
        ArrayList<Integer> in_vertices, on_vertices;
        Boolean concentric;
        ContainType(int oid, int iid, ArrayList<Pair<Junction, Pair<Integer, Integer>>> j, ArrayList<Integer> iv, ArrayList<Integer> ov)
        {
            out_id = oid;
            in_id = iid;
            junctions = j;
            in_vertices = iv;
            on_vertices = ov;
            concentric = false;
        }
        ContainType(int oid, int iid, ArrayList<Pair<Junction, Pair<Integer, Integer>>> j, ArrayList<Integer> iv, ArrayList<Integer> ov, Boolean c)
        {
            out_id = oid;
            in_id = iid;
            junctions = j;
            in_vertices = iv;
            on_vertices = ov;
            concentric = c;
        }
    }

    static class GroupWise{
        int id; //group id
        static int count = 0;
        ArrayList<ImageOverview> imageOverview = new ArrayList<ImageOverview>();
        ArrayList<Integer> shape_id; //shape ids that contitute this group
        ArrayList<CommonComponent> common = new ArrayList<CommonComponent>(); //common vertices and lines between shapes of this group
        ArrayList<XnT_Type> xt_type = new ArrayList<XnT_Type>(); //X and T type intersections between shapes of this group
        ArrayList<InscribeType> inscribe = new ArrayList<InscribeType>(); //inscriptions between shapes of this group
        ArrayList<ContainType> contain = new ArrayList<ContainType>();// contained type between shapes of this group
        HashMap<Pair<Integer, Integer>, ArrayList<Junction>> shape_wise = new HashMap<Pair<Integer, Integer>, ArrayList<Junction>>(); //shape_wise intersections of shapes present in this group
        HashMap<Integer, Pair<Integer, ArrayList<Integer>>> vertexAtCenter = new HashMap<Integer, Pair<Integer, ArrayList<Integer>>>();//Key: circle_id (not shape_id), Pair.key: Vertex Id, Pair.value: ArrayList of lines that end at vid
        GroupWise(ArrayList<Integer> sid){
            id = count;
            count++;
            shape_id = sid;
        }
    }

    static class Extracted_Information{
        ArrayList<Line> lines;
        ArrayList<Circle> circles;
        ArrayList<Label> labels;
        ArrayList<Shape> shapes;
        ArrayList<Junction> intersections;
        ArrayList<Vertex> vertices;
        ArrayList<Vertex_Edge> table;
        ArrayList<LengthLabel> length_labels;
        HashMap<Pair<Integer, Integer>, ArrayList<Junction>> shape_wise;
        HashMap<Pair<Integer, Integer>, Integer> shape_wise_visited;
        HashMap<Pair<Integer, Integer>, ArrayList<Junction>> primitive_wise;
        ArrayList<GroupWise> group_wise;
        HashMap<Pair<Integer, String> , LineLabel> lineAssociation;
        HashMap<Pair<Integer, String> , VertexLabel> vertexAssociation;
        HashMap<Pair<Integer, String> , JunctionLabel> junctionAssociation;
        ArrayList<ImageOverview> imageOverview;
        ArrayList<ContainType> contain = new ArrayList<ContainType>();// contained type between disconnected shapes
        int reference_line_id;

        Extracted_Information(){}
        Extracted_Information(ArrayList<Line> l, ArrayList<Circle> c, ArrayList<Label> lb , ArrayList<Shape> s, ArrayList<Junction> i, ArrayList<Vertex> v, ArrayList<Vertex_Edge> t, ArrayList<LengthLabel> ll, HashMap<Pair<Integer, String> , LineLabel> la, HashMap<Pair<Integer, String> , VertexLabel> va, HashMap<Pair<Integer, String> , JunctionLabel> ja){
            lines = l;
            circles = c;
            labels = lb;
            shapes = s;
            intersections = i;
            vertices = v;
            table = t;
            length_labels = ll;
            lineAssociation = la;
            vertexAssociation = va;
            junctionAssociation = ja;
        }
        Extracted_Information(ArrayList<Line> l, ArrayList<Circle> c, ArrayList<Shape> s, ArrayList<Junction> i, ArrayList<Vertex> v, ArrayList<Vertex_Edge> t, HashMap<Pair<Integer, Integer>, ArrayList<Junction>> sw, HashMap<Pair<Integer, Integer>, Integer> swv, HashMap<Pair<Integer, Integer>, ArrayList<Junction>> pw){
            lines = l;
            circles = c;
            shapes = s;
            intersections = i;
            vertices = v;
            table = t;
            shape_wise = sw;
            shape_wise_visited = swv;
            primitive_wise = pw;
        }
    }
    
}

