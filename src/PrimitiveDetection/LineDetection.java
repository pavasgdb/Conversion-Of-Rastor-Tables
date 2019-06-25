package src;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import java.io.OutputStreamWriter;

import javafx.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.lang.*;
import src.All_class.Line;

public class LineDetection
{

	Mat LinePreProcess(Mat src, String linepp, String pathblur, String path_median) throws IOException
	{
                
                ///~~~~~~~~~~~~~~~~~ smoothes the image to eliminate noise and overwrites src with its smoothened version
        		/*GaussianBlur( src, src, new Size(3,3), 2, 2 ); //blurs the edges, all parameter values are standard 
        		Imgcodecs.imwrite(pathblur,src);*/ 

        		//Find median of pixel intensities. Need it as the Canny Edge Detector upper and lower thresholds are set to 1.33*median and 0.66*median for best results
                /*Process p1 = Runtime.getRuntime().exec("python3 "+path_median+ " "+ pathblur);
		        try 
		        {
		            p1.waitFor();
		        } 
		        catch(InterruptedException e)
		        {
		            System.out.println("Exception raised in median finding");
		        }*/

                //The python script stores computed median value in a file. It is accessed in the following try-catch block and the value is read into a string "npmedian"
        		/*String npmedian="";
        		 try{
        		File file = new File("./src/temp.txt");
			    Scanner input = new Scanner(file); 
			    npmedian  = input.next();
			    } catch (IOException e) {
        		e.printStackTrace();
    			}*/

    			//System.out.println("medianstring "+ npmedian);
/*    			float pixel_median = Float.parseFloat(npmedian);
*/    			//System.out.println("median "+ pixel_median);

                //Arguments for Canny in order:
                
                Mat dst = new Mat(); //output edge map; single channels 8-bit image, which has the same size as image
                /*int threshold2 = Math.min(255,(int)Math.round(pixel_median*1.33)); // second threshold for the hysteresis procedure. 50
                int threshold1 = Math.max(0,(int)Math.round(pixel_median*0.66)); // first threshold for the hysteresis procedure. 20*/
                int apertureSize = 5; // aperture size for the Sobel operator.
                Boolean l2gradient = true; // L2gradient: a flag, indicating whether a more accurate L2 norm =√((dI/dx)^2+(dI/dy)^2) should be used to calculate the image gradient magnitude ( L2gradient=true ), or whether the default L1 norm =|dI/dx|+|dI/dy| is enough ( L2gradient=false )
                int threshold2 = 50;
                int threshold1 = 20;
                System.out.println(threshold2 + " "+ threshold1);
                Canny(src, dst, threshold1, threshold2, apertureSize, l2gradient);// src: grayscale (ie. 8-bit) input image.
                //dst now contains the output of canny edge detector

                //storing canny output in an image
                //~~~~~~~~~~~~Imgcodecs.imwrite(linepp,dst);

                return dst;
	}


        ArrayList<Line> LineDetect(String imname, String linepp, String pathHL, String pathblur, String path_median)
        {
                Mat src = Imgcodecs.imread(imname, Imgcodecs.IMREAD_GRAYSCALE); //converts image specified by imname to 0-255 grayscale.

                //Arguments in order for HoughLinesP:
                Mat dst = src; // need to initialize dst to a Mat object because actual initialization happens inside try-catch block
                try{
                dst = LinePreProcess(src, linepp, pathblur, path_median); // Output of the edge detector. It should be a grayscale image (although in fact it is a binary one)
            	}
            	catch(IOException e){}
                // dst now contains the output of line preprocessing, it is the desired input for HoughLines operation

                Mat lineMatrix = new Mat();//A vector that will store the parameters (xstart,ystart,xend,yend) of the detected lines
                int rho = 1; // rho : The resolution of the parameter r in pixels.
                double theta = Math.PI/180; // theta: The resolution of the parameter θ in radians. 1 degree
                int threshold = 10;  // threshold: The minimum number of intersections to "*detect*" a line     18
                int minLinLength = 10; // minLinLength: The minimum number of points that can form a line. Lines with less than this number of points are disregarded.   15
                int maxLineGap = 5; // maxLineGap: The maximum gap between two points to be considered in the same line.
                HoughLinesP( dst, lineMatrix, rho, theta, threshold, minLinLength, maxLineGap );  //call the probabilistic Hough Lines transform
                //LineMatrix now contains endpoints of each line detected


                // ArrayList stores information of all lines detected in Line object format
                ArrayList<Line> lines = new ArrayList<>();
                
                //src1 loads empty image on which houghlines output will be drawn
                Mat src1 = Imgcodecs.imread(pathHL, Imgcodecs.IMREAD_COLOR);
                
                for(int j=0;j<lineMatrix.rows();j++){
                    double[] points = lineMatrix.get(j,0);
                    // add lines to the ArrayList which stores information of all lines detected
                    lines.add(new Line((int)points[0],(int)points[1],(int)points[2],(int)points[3], "normal"));
                    //add lines to the output image
                    Imgproc.line(src1, new Point((int)points[0], (int)points[1]),new Point((int)points[2], (int)points[3]), new Scalar(100,100,100), 1) ;
                }
                //save HoughLines output as an image file
                Imgcodecs.imwrite(pathHL,src1);

                
                return lines;

        }

}
