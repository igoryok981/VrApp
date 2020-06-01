#include <jni.h>
#include <string>
#include "opencv2/opencv.hpp"
#include "opencv2/stitching.hpp"
#include "opencv2/highgui.hpp"
#include "opencv2/imgcodecs.hpp"

using namespace std;
using namespace cv;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_vrapp_CreatePanoActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_vrapp_CreatePanoActivity_stitch(JNIEnv *env, jobject thiz,
                                                 jlongArray image_address_array,
                                                 jlong output_address
                                                 ) {
    bool divide_images = false;
    Stitcher::Mode mode = Stitcher::PANORAMA;
    vector<Mat> imgs;
    string result_name = "storage/emulated/0/Photos/result2.jpg";
    string folder = "storage/emulated/0/Photos/";

    for (int i = 0; i < 4; i++) {
        char fullname[folder.length() + 5];
        sprintf(fullname, "storage/emulated/0/Photos/s%d.jpg", i);
        Mat img = imread(samples::findFile(fullname));
        if (img.empty()) {
            //cout << "Can't read image '" << argv[i] << "'\n";
            return;
        }
        if (divide_images) {
            Rect rect(0, 0, img.cols / 2, img.rows);
            imgs.push_back(img(rect).clone());
            rect.x = img.cols / 3;
            imgs.push_back(img(rect).clone());
            rect.x = img.cols / 2;
            imgs.push_back(img(rect).clone());
        } else
            imgs.push_back(img);
        //delete[] fullname;
    }
    Mat pano;
    Ptr<Stitcher> stitcher = Stitcher::create(mode);
    Stitcher::Status status = stitcher->stitch(imgs, pano);
    if (status != Stitcher::OK)
    {
        //cout << "Can't stitch images, error code = " << int(status) << endl;
        return;
    }
    imwrite(result_name, pano);
    //cout << "stitching completed successfully\n" << result_name << " saved!";
    //return EXIT_SUCCESS;
}