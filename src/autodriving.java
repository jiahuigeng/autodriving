/**
 * Created by gengjiahui on 4/3/2017.
 */
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import static org.opencv.imgproc.Imgproc.CV_DIST_L2;
import static org.opencv.imgproc.Imgproc.Canny;
import static org.opencv.imgproc.Imgproc.fitLine;


public class autodriving
{
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private JFrame frame;
    private JLabel imageLabel;

    public static void main(String[] args) throws InterruptedException {
        autodriving app = new autodriving();
             app.initGUI();
        app.runMainLoop(args);
    }

    private void initGUI() {
        frame = new JFrame("Video Playback Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //    frame.setSize(400,400);
        imageLabel = new JLabel();
        frame.add(imageLabel);
        frame.setVisible(true);
    }

    private void runMainLoop(String[] args) throws InterruptedException {
     //   JFrame frame=new JFrame();
        ImageProcessor imageProcessor = new ImageProcessor();
        Image tempImage;
        Mat webcamMatImage = new Mat();
        VideoCapture capture = new VideoCapture("src/LabCarsStraight.mp4"); //"src/LabCarsCurves.mp4"
        if( capture.isOpened()){
            while (true){
                capture.read(webcamMatImage);
                if( !webcamMatImage.empty() ){
              //      tempImage= imageProcessor.toBufferedImage(webcamMatImage);

                    Mat gray = new Mat(webcamMatImage.rows(), webcamMatImage.cols(), CvType.CV_8SC1);
                    Imgproc.cvtColor(webcamMatImage, gray, Imgproc.COLOR_RGB2GRAY);
                    //  Point p1=new Point(750,324);
                    MatOfPoint mp=new MatOfPoint(new Point(750,324),new Point(429,324), new Point(0,493),new Point(0,493),new  Point(0,0),new  Point(1280,0), new Point(1280,493));
                    List<MatOfPoint> pts=new ArrayList<MatOfPoint>();// new Point(750,324),new Point(429,324),
                    pts.add(mp);
                    Imgproc.fillPoly(gray,pts,new Scalar(0));
                    Canny(gray,gray,40,120);
                    Mat lines = new Mat();
                    Imgproc.HoughLinesP(gray, lines, 1, Math.PI/180, 50, 100 ,50);
                    double[] data;
                    double slope=0;
                    List<Point> left=new ArrayList<>();
                    List<Point> right= new ArrayList<>();
                    Point pt1 = new Point();
                    Point pt2 = new Point();
                    for (int i = 0; i<lines.rows() ; i++){
                        data = lines.get( i,0);
                        pt1.x = data[0];
                        pt1.y = data[1];
                        pt2.x = data[2];
                        pt2.y = data[3];
                        slope=((pt2.y-pt1.y)/(pt2.x-pt1.x));

                        if (slope>=0.2 && pt2.x>webcamMatImage.cols()/2 )
                        {
                            right.add(new Point(pt2.x,pt2.y).clone());
                            right.add(new Point(pt1.x,pt1.y).clone());
                        }
                        else if(slope <=-0.2 && pt2.x<webcamMatImage.cols()/2)
                        {

                            left.add(new Point(pt2.x,pt2.y).clone());
                            left.add(new Point(pt1.x,pt1.y).clone());

                        }
                        //      Imgproc.line(im, pt1, pt2, new Scalar(0, 0, 200), 3);
                    }
                    MatOfPoint right_p=new MatOfPoint();
                    right_p.fromList(right);
                    MatOfPoint left_p=new MatOfPoint();
                    left_p.fromList(left);
                    Mat right_line=new Mat();
                    Mat left_line=new Mat();

                    fitLine(right_p,right_line,CV_DIST_L2,0,0.01,0.01);
                    fitLine(left_p,left_line,CV_DIST_L2,0,0.01,0.01);


                    double []r=new double[4];
                    double []l=new double[4];
                    for (int i=0;i<4;i++)
                    {
                        r[i]=right_line.get(i,0)[0];
                        l[i]=left_line.get(i,0)[0];
                    }



                    Imgproc.line(webcamMatImage, new Point(r[2],r[3]), new Point(r[2]+100*r[0], r[3]+100*r[1]), new Scalar(0,0,200), 10);
                    Imgproc.line(webcamMatImage, new Point(l[2],l[3]), new Point(l[2]+100*l[0], l[3]+100*l[1]), new Scalar(0,0,200), 10);

                    tempImage= imageProcessor.toBufferedImage(webcamMatImage);
                    ImageIcon imageIcon = new ImageIcon(tempImage, "lane tracking");
                    imageLabel.setIcon(imageIcon);
                    frame.pack();  //this will resize the window to fit the image
                    Thread.sleep(50);

                }
                else{
                    System.out.println(" Frame not captured or video has finished");
                    break;
                }
            }
        }
        else{
            System.out.println("Couldn't open video file.");
        }



    }
}