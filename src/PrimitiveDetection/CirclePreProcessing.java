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

import src.All_class.Circle;

public class CirclePreProcessing
{

    void printtoTextFile(Mat d, File file_g){
       Size size = d.size();
       byte[] a = new byte[(int) (d.width() * d.height())];
       d.get(0, 0, a);//I get byte array here for the whole image
       FileOutputStream fos_g = null;
        OutputStreamWriter ow = null;
        BufferedWriter fwriter = null;
        try {
            fos_g = new FileOutputStream(file_g);
            ow = new OutputStreamWriter(fos_g);
            fwriter = new BufferedWriter(ow);
             for (int x = 0; x < size.height; x++){
                for (int y = 0; y < size.width; y++){
                    fwriter.write(String.valueOf(a[(int) (x * size.width + y)]));
                    ow.flush();
                    fwriter.write(","); 
                    ow.flush();
                }
                fwriter.write("\n");
                ow.flush();
                //fos_g.flush();
            }
        fos_g.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
      
        try {
            fos_g.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    void remove_duplicate_circles(ArrayList<double[]> cir, String pathHC, Mat fin_img, ArrayList<Circle> circles)
    {
        //Mat cir = new Mat(tempCircles.rows(), tempCircles.cols(), CvType.CV_32F);
        int k = 0;
        // System.out.println("Final set of circles: ");
        for(int i = 0; i < cir.size(); i++)
        {
            int flag = 0;
            for(int j = i+1; j < cir.size(); j++)
            {
                double dx = cir.get(j)[0] - cir.get(i)[0];
                double dy = cir.get(j)[1] - cir.get(i)[1];
                double centre_dist = Math.sqrt(dx * dx + dy * dy);
                /// if duplicate found then add(update) the circle with max radius to final list
                // System.out.println("i: "+i+" j: "+j+" centre_dist: "+centre_dist+" radius_diff: "+Math.abs(tempCircles.get(0,i)[2] - tempCircles.get(0,j)[2]));
                if(centre_dist <= 15 && Math.abs(cir.get(i)[2] - cir.get(j)[2]) <= 12)
                {
                    // System.out.println("matched");
                    cir.get(j)[0]=(cir.get(j)[0]+cir.get(i)[0])/2;
                    cir.get(j)[1]=(cir.get(j)[1]+cir.get(i)[1])/2;
                    cir.get(j)[2]=(cir.get(j)[2]+cir.get(i)[2])/2;
                    flag += 1;
                    // break;
                }
                // System.out.println("updated: "+tempCircles.get(0,j)[0]+" "+tempCircles.get(0,i)[0]);
            }
            if(flag == 0)
            {
                // if no duplicate found, then add it to final list
                
                // System.out.println(cir.get(i)[0]+" "+cir.get(i)[1]+" "+cir.get(i)[2]);
                circles.add(new Circle(cir.get(i)[0], cir.get(i)[1], cir.get(i)[2], "normal", 1));
                k += 1;

                Point center = new Point(Math.round(cir.get(i)[0]), Math.round(cir.get(i)[1]));
                int radius = (int)Math.round(cir.get(i)[2]);
                // circle center
                circle( fin_img, center, 1, new Scalar(0,255,0), -1, 8, 0 );
                // circle outline
                circle( fin_img, center, radius, new Scalar(0,0,255), 1, 8, 0 );
            }
            Imgcodecs.imwrite(pathHC,fin_img);
        }
    }
    void max_votes_circles(Mat tempCircles, ArrayList<double[]> cir)
    {
        int k = 0;
        for(int i = 0; i < tempCircles.cols(); i++)
        {
            int flag = 0;
            double[] data = tempCircles.get(0,i);
            for(int j = i+1; j < tempCircles.cols(); j++)
            {
                double[] data1 = tempCircles.get(0,j);
                double dx = tempCircles.get(0,j)[0] - tempCircles.get(0,i)[0];
                double dy = tempCircles.get(0,j)[1] - tempCircles.get(0,i)[1];
                double centre_dist = Math.sqrt(dx * dx + dy * dy);
                /// if duplicate found then add(update) the circle with max radius to final list
                // System.out.println("i: "+i+" j: "+j+" centre_dist: "+centre_dist+" radius_diff: "+Math.abs(tempCircles.get(0,i)[2] - tempCircles.get(0,j)[2]));
                if(centre_dist <= 15 && Math.abs(tempCircles.get(0,i)[2] - tempCircles.get(0,j)[2]) <= 11)
                {
                    // System.out.println("matched");
                    tempCircles.put(0,j, new double[] {0, 0, 0});
                    flag += 1;
                    // break;
                }
                // System.out.println("updated: "+tempCircles.get(0,j)[0]+" "+tempCircles.get(0,i)[0]);
            }
        }

        for(int i = 0; i < tempCircles.cols(); i++)
        {
            if(tempCircles.get(0, i)[2]!=0)
            {
                cir.add(new double[] {tempCircles.get(0,i)[0], tempCircles.get(0,i)[1], tempCircles.get(0,i)[2]});
            }
        }
        tempCircles.release();
    }
    void CirclePreProcess(String imname, String circlepp, String pathHC, ArrayList<Circle> circles)
    {

        Mat src = Imgcodecs.imread(imname, Imgcodecs.IMREAD_COLOR);
        Mat dst = src.clone();
        Mat src_gray = new Mat();
        int i = 29;
        // bilateralFilter(src,dst,i,i*2,i/2); //edge preserving smoothening function
        // src = dst;
        /// Convert it to gray

        // Mat element = getStructuringElement(MORPH_CROSS,new Size(7,7), new Point(3,3));
        // erode(src,src,element);

        cvtColor( src, src_gray, COLOR_BGR2GRAY );

        /// blur will help avoid false circle detection by reducing noise, because openCV uses a gradient version of the hough circle detection algorithm

        // printtoTextFile(src_gray, new File("./output/before_gaussian_blur.csv"));
        GaussianBlur( src_gray, src_gray, new Size(3,3), 2, 2 ); //blurs the edges
        // printtoTextFile(src_gray, new File("./output/gaussian_blur.csv"));
        

        Mat dst1 = new Mat();
        Canny(src_gray, dst1, 10, 20, 3, false);

        // int scale = 1;
        // int delta = 0;
        // int ddepth = CvType.CV_16S;
        // Mat grad_x = new Mat(), grad_y = new Mat();
        // Mat abs_grad_x = new Mat(), abs_grad_y = new Mat(), grad = new Mat();
        //Imgproc.Scharr( src_gray, grad_x, ddepth, 1, 0, scale, delta, Core.BORDER_DEFAULT );
        // Imgproc.Sobel( src_gray, grad_x, ddepth, 1, 0, 3, scale, delta, Core.BORDER_REPLICATE );
        //Imgproc.Scharr( src_gray, grad_y, ddepth, 0, 1, scale, delta, Core.BORDER_DEFAULT );
        // Imgproc.Sobel( src_gray, grad_y, ddepth, 0, 1, 3, scale, delta, Core.BORDER_REPLICATE );
        // converting back to CV_8U
        // Core.absdiff(grad_x, Scalar.all(0), abs_grad_x);
        // Core.absdiff(grad_y, Scalar.all(0), abs_grad_y);
        // Core.convertScaleAbs( grad_x, abs_grad_x );
        // Core.convertScaleAbs( grad_y, abs_grad_y );

        // Core.add( grad_x, grad_y, grad );
        // grad.convertTo( grad, CvType.CV_8UC3);
        // printtoTextFile(grad, new File("./output/total_gradient.csv"));
        // printtoTextFile(dst1, new File("./output/Canny_4.csv"));




        //~~~~~~~~~~Imgcodecs.imwrite(circlepp, dst1);
        //~~~~~~~~~Imgcodecs.imwrite(pathHC, dst1);
        // Imgcodecs.imwrite(circlepp, src_gray);

        int diag=(int)Math.sqrt(src_gray.rows()*src_gray.rows()+src_gray.cols()*src_gray.cols())/2;
        Mat tempCircles =  new Mat();
        Mat src1 = Imgcodecs.imread(circlepp, Imgcodecs.IMREAD_COLOR);

        Mat fin_img = Imgcodecs.imread(pathHC, Imgcodecs.IMREAD_COLOR);
        // Parameters for HoughCircles:
          //   src_gray: Input image (grayscale)
          //   circles: A vector that stores sets of 3 values: x_{c}, y_{c}, r for each detected circle.
          //   CV_HOUGH_GRADIENT: Define the detection method. Currently this is the only one available in OpenCV
          //   dp = 1: The inverse ratio of resolution
          //   min_dist = Minimum distance between detected centers
          //   param_1 = 200: Upper threshold for the internal Canny edge detector
          //   param_2 = 100*: Threshold for center detection.
          //   min_radius = 0: Minimum radio to be detected. If unknown, put zero as default.
          //   max_radius = 0: Maximum radius to be detected. If unknown, put zero as default

        int bucket_size= diag/10;
        int k = 9*3, num_loop = diag/bucket_size-2;
        int gap=k/num_loop;

        ArrayList<double[]> cir = new ArrayList<double[]>();
        for (i = num_loop+1; i>0; i--)
        {	
        	// System.out.println("Radius: "+i*(bucket_size)+" gradient: "+(20)+" votes: "+(2*22*bucket_size*i)/(7*k)+" k: "+k+" gap: "+gap);
	       	HoughCircles( src_gray, tempCircles, HOUGH_GRADIENT, 1, 10, 20, (2*22*bucket_size*i)/(7*k), i*bucket_size, (i+1)*bucket_size+2);
	       	k-=gap;
	        
	        for(int j = 0; j < tempCircles.cols();j++ )
	        {
	            Point center = new Point(Math.round(tempCircles.get(0, j)[0]), Math.round(tempCircles.get(0,j)[1]));
	            int radius = (int)Math.round(tempCircles.get(0,j)[2]);
	            // System.out.println(center.x+" "+center.y+" "+radius);
	            // circle center
	            circle( src1, center, 1, new Scalar(0,255,0), -1, 8, 0 );
	            // circle outline
	            circle( src1, center, radius, new Scalar(0,0,255), 1, 8, 0 );
	        }
            Imgcodecs.imwrite(circlepp,src1);

            //tempCircles.release();
            max_votes_circles(tempCircles, cir);
            // System.out.println("cir.size(): "+cir.size());
    	}
        /*System.out.println("cir:");
        for(i = 0; i < cir.size(); i++)
        {
            System.out.println(cir.get(i)[0]+" "+cir.get(i)[1]+" "+cir.get(i)[2]);
            Point center = new Point(Math.round(cir.get(i)[0]), Math.round(cir.get(i)[1]));
                int radius = (int)Math.round(cir.get(i)[2]);
                // circle center
                circle( fin_img, center, 1, new Scalar(0,255,0), -1, 8, 0 );
                // circle outline
                circle( fin_img, center, radius, new Scalar(0,0,255), 1, 8, 0 );
        }
        Imgcodecs.imwrite(pathHC, fin_img);*/
        remove_duplicate_circles(cir, pathHC, fin_img, circles);
    }

}