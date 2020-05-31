package com.example.vrapp;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_stitching.*;
import org.bytedeco.opencv.opencv_highgui.*;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

public class ImageStitch {
    static MatVector imgs = new MatVector();
    static String result_name = "/storage/emulated/0/Photos/result.jpg";

    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            Mat img = imread("/storage/emulated/0/Photos/" + i + ".jpg");
            if (img.empty()) {
                //System.out.println("Can't read image '" + i + "'");
            }
            imgs.resize(imgs.size() + 1);
            imgs.put(imgs.size() - 1, img);
        }
        Mat pano = new Mat();
        Stitcher stitcher = Stitcher.create(0);
        int status = stitcher.stitch(imgs, pano);

        if (status != Stitcher.OK) {
            //System.out.println("Can't stitch images, error code = " + status);
            System.exit(-1);
        }

        imwrite(result_name, pano);

        //System.out.println("Images stitched together to make " + result_name);
    }
}
