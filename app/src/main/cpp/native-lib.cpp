#include <jni.h>
#include <string>
#include "opencv2/opencv.hpp"
#include "opencv2/stitching.hpp"
#include "opencv2/highgui.hpp"
#include "opencv2/imgcodecs.hpp"

using namespace std;
using namespace cv;

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_vrapp_CreatePanoActivity_stitch(JNIEnv *env, jobject thiz,
                                                 jobjectArray image_names) {
    bool divide_images = false;
    Stitcher::Mode mode = Stitcher::PANORAMA;
    vector<Mat> imgs;
    string result_name = "storage/emulated/0/Photos/result_.jpg";
    int stringCount = env->GetArrayLength(image_names);

    for (int i = 0; i < stringCount; i++) {
        jstring instring = (jstring) (env->GetObjectArrayElement(image_names, i));
        string rawString = env->GetStringUTFChars(instring, 0);
        //char fullname[rawString.length() + 5];
        //sprintf(fullname, "storage/emulated/0/Photos/pano%d.jpg", i);
        Mat img = imread(samples::findFile(rawString));
        if (img.empty()) {
            //cout << "Can't read image '" << argv[i] << "'\n";
            return false;
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
        //env->ReleaseStringUTFChars(instring, 0);
    }
    Mat pano;
    Ptr<Stitcher> stitcher = Stitcher::create(mode);
    Stitcher::Status status = stitcher->stitch(imgs, pano);
    if (status != Stitcher::OK)
    {
        //cout << "Can't stitch images, error code = " << int(status) << endl;
        return false;
    }
    imwrite(result_name, pano);
    //cout << "stitching completed successfully\n" << result_name << " saved!";
    return true;
}