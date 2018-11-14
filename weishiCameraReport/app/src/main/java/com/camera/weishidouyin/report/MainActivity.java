package com.camera.weishidouyin.report;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG="yilia";

    private List<Map<String, Object>> aList; //摄像头采集文件
    private List<Map<String, Object>> bList; //MP4文件
    public String aBaseFile; //摄像头采集文件存放的文件夹
    public String bBaseFile; //weishimp4文件存放的文件夹
    public String cBaseFile; //douyinmp4文件存放的文件夹

    public String platform="android";
    public String app_name = "weishi";
    public String app_version = "4.8.0.588";
    public String file_name = "";
    public String device_type = "";

    private Information information=new Information();
    private MicInformation micInformation=new MicInformation();
    private CameraData cameraData=new CameraData();

    private String camerainfoUrl="http://123.207.110.59/camerainformation";
    private String mp4Url="http://123.207.110.59/record_video_files";


    private JSONObject objectCamera = new JSONObject();//创建一个总的对象，这个对象对整个json串
    private JSONObject jsonObjInformation = new JSONObject();//对象，json形式
    private JSONObject jsonObjMicInformation = new JSONObject();//对象，json形式

    private String appName;
    private String afileName;
    private String bfileName;
    private String cfileName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //防止Android阻止httpget请求
//                .detectDiskReads()
//                .detectDiskWrites()
//                .detectNetwork()   // or .detectAll() for all detectable problems
//                .penaltyLog()
//                .build());
//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                .detectLeakedSqlLiteObjects()
//                .detectLeakedClosableObjects()
//                .penaltyLog()
//                .penaltyDeath()
//                .build());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aList=new ArrayList<Map<String,Object>>();
        bList=new ArrayList<Map<String,Object>>();
        aBaseFile=GetFileUtils.getInstance().getaBasePath();
        bBaseFile=GetFileUtils.getInstance().getbBasePath();
        cBaseFile=GetFileUtils.getInstance().getcBasePath();

        Button camerainfo_report = (Button)findViewById(R.id.camerainfo_report);
        final Button weishimp4_report = (Button)findViewById(R.id.weishimp4_report);
        final Button douyinmp4_report = (Button)findViewById(R.id.douyinmp4_report);
        final EditText device_type_input = (EditText)findViewById(R.id.device_type_input) ;



        camerainfo_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    aList=loadFolderList(aBaseFile);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                for (int i=0;i<aList.size();i++) {
                    afileName=aList.get(i).get("fPath").toString().replace("/storage/emulated/0/","");
                    FileToSend(afileName); //从文件中获取信息并上报
                    SystemClock.sleep(2000);
                }
            }
        });

        weishimp4_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                device_type = device_type_input.getText().toString();
                if (device_type.equals("")){
                    Toast.makeText(getApplicationContext(),"请先输入设备型号！",Toast.LENGTH_SHORT ).show();
                }
                else {
                    try {
                        bList=loadFolderList(bBaseFile);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    for (int i=0;i<bList.size();i++) {
                        bfileName=bList.get(i).get("fPath").toString().replace("/storage/emulated/0/","");
                        //FileToSend(bfileName); //从文件中获取信息并上报
//                    file_name = bfileName.replace("weishimp4/","");
                        file_name = device_type+bfileName.replace("CameraData/weishi/","")+".mp4";
                        mp4Url = mp4Url+"/"+platform+"/"+app_name+"/"+app_version+"/"+file_name;
                        Log.d(TAG, "onClick: mp4url="+mp4Url);
                        mSendMp4(bfileName);
                    }
                }
            }
        });

        douyinmp4_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                device_type = device_type_input.getText().toString();
                if (device_type.equals("")){
                    Toast.makeText(getApplicationContext(),"请先输入设备型号！",Toast.LENGTH_SHORT ).show();
                }
                else {
                    app_name = "douyin";
                    app_version = "3.2.1";
                    try {
                        bList=loadFolderList(cBaseFile);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    for (int i=0;i<bList.size();i++) {
                        cfileName=bList.get(i).get("fPath").toString().replace("/storage/emulated/0/","");
                        file_name = device_type+cfileName.replace("CameraData/weishi/","")+".mp4";
                        mp4Url = mp4Url+"/"+platform+"/"+app_name+"/"+app_version+"/"+file_name;
                        Log.d(TAG, "onClick: mp4url="+mp4Url);
                        mSendMp4(cfileName);
                    }
                }
            }
        });
    }

    /**
     * 获取文件中的有效数据并发送到服务器
     * @param fileName
     */

    public void FileToSend(String fileName) {
        File file = new File(Environment.getExternalStorageDirectory(),fileName);
        List<Map<String,String>> mMapList= new ArrayList<Map<String,String>>();
        int flag=0; //
        //判断是否挂载 Sdcard 卡
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            FileInputStream inputStream = null;
            BufferedReader reader = null;
            try {
                inputStream = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                int i = 0;
                int j=0;
                Map<String,String> mMap=new HashMap<String,String >();
                while ((line = reader.readLine()) != null) {
                    if ((line.contains("Camera")&&line.contains(" information:"))||line.contains("Camera traces (0):")||line.contains("CameraParameters::dump:")
                            ||(line.contains("Camera device")&&line.contains("dynamic info:"))) {

                        if (i!=0&&mMap.size()>0&&flag==1){
                            setInformation(mMap);//有效数据
                        }
                        mMap.clear();
                        flag=1;
                        j=i;
                        i++;
                        continue;
                    }
                    if (line.contains("Device")&&line.contains("is closed, no client instance")) {  //关闭的摄像头数据不发送
                        mMap.clear(); //清除无效数据
                        flag=0;
                        continue;
                    }
                    if (!line.equals("")) {
                        String[] temp;
                        temp=line.split(":",2);
                        if (temp.length>1) {
                            temp[0]=temp[0].trim(); //去掉前后空格
                            temp[1]=temp[1].trim();
                            if (temp[0].equals("Focusing areas")){
                                temp[1]=reader.readLine().trim();
                            }
                            mMap.put(temp[0],temp[1]);
                        }
                    }
                }
                setInformation(mMap);
                mMap.clear();
                inputStream.close(); //内存泄漏
                cameraData.information=information;
                buildJson(); //构建成json格式
                mSendString(objectCamera); //上传到服务器
            } catch (IOException e) {
                // TODO: handle exception
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        inputStream.close();
                        reader.close();
                    } catch (Exception e2) {
                        // TODO: handle exception
                        e2.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 设置Informatica的参数
     * 如果有些参数获取不到，可以修改这里，可能有些机型的参数不一样
     * @param mMap
     */

    public void setInformation(Map<String ,String>mMap) {
        String flTemp1="",flTemp2="";
        String foTemp1="",foTemp2="";
        String scTemp1="",scTemp2="";
        String foaTemp1="",foaTemp2="";
        String wmTemp1="",wmTemp2="";
        String fpsTemp1="",fpsTemp2="";
        String bsTemp1="",bsTemp2="";
        for (Map.Entry<String, String> entry : mMap.entrySet()) {
            //Log.d(TAG, "setInformation: entry.getvalue="+entry.getValue());
            if (entry.getKey().equals("flash-mode")) {
                flTemp1=entry.getValue();
            }else if (entry.getKey().equals("Flash mode")) {
                flTemp2=entry.getValue();
            }
            else if (entry.getKey().equals("focus-mode")) {
                foTemp1=entry.getValue();
            } else if (entry.getKey().equals("Focus mode")) {
                foTemp2=entry.getValue();
            }
            else if (entry.getKey().equals("scene-mode")) {
                scTemp1=entry.getValue();
            }else if (entry.getKey().equals("Scene mode")) {
                scTemp2=entry.getValue();
            }
            else if (entry.getKey().equals("exposure-compensation")) {
                information.exposureCompensation=entry.getValue();
                information.exposureMode=entry.getValue();
            }else if (entry.getKey().equals("exposure-compensation-step")) {
                information.exposureCompensationStep=entry.getValue();
            }
            else if (entry.getKey().equals("whitebalance")) {
                wmTemp1=entry.getValue();
            }else if (entry.getKey().equals("White balance mode")) {
                wmTemp2=entry.getValue();
            }
            else if (entry.getKey().equals("auto-exposure-lock")) {
                information.autoExposureLock=entry.getValue();
            }else if (entry.getKey().equals("auto-exposure-lock-supported")) {
                information.autoExposureLock=entry.getValue();
            }
            else if (entry.getKey().equals("auto-whitebalance-lock")) {
                information.autoWhiteBalanceLock=entry.getValue();
            }else if (entry.getKey().equals("auto-whitebalance-lock-supported")) {
                information.autoWhiteBalanceLock=entry.getValue();
            }
            else if (entry.getKey().equals("focal-length")) {
                information.focalLength=entry.getValue();
            }
            else if (entry.getKey().equals("zoom")) {
                information.zoom=entry.getValue();
            }
            else if (entry.getKey().equals("video-stabilization")) {
                information.videoStabilization=entry.getValue();
            } else if (entry.getKey().equals("video-stabilization-supported")) {
                information.videoStabilization=entry.getValue();
            }
            else if (entry.getKey().equals("focus-areas")) {
                foaTemp1=entry.getValue();
            }else if (entry.getKey().equals("Focusing areas")) {
                foaTemp2=entry.getValue();
            }
            else if (entry.getKey().equals("preview-size-values")) {
                information.cameraPreview=entry.getValue();
            }
            else if (entry.getKey().equals("preview-fps-range")) {
                fpsTemp1=entry.getValue();
            }else if (entry.getKey().equals("Selected still capture FPS range")) {
               fpsTemp2=entry.getValue();
            }
            else if (entry.getKey().equals("effect")) {
                information.colorEffect=entry.getValue();
            }
            else if (entry.getKey().equals("preferred-preview-size-for-video")) {
                bsTemp1=entry.getValue();
            }else if (entry.getKey().equals("Preview size")) {
                bsTemp2=entry.getValue();
            }else if (entry.getKey().equals("preview-size")) {
                bsTemp1=entry.getValue();
            }
            else if (entry.getKey().equals("auto-hdr-enable")) {
                information.HighHDR=entry.getValue();
            }
            else if (entry.getKey().equals("os_version")) {
                cameraData.osVersion=entry.getValue();
            }
            else if (entry.getKey().equals("device_model")) {
                cameraData.deviceModel=entry.getValue();
            }
            else if (entry.getKey().equals("version")) {
                cameraData.version=entry.getValue();
            }
            else if (entry.getKey().equals("cameraPosition")) {
                cameraData.cameraPosition=entry.getValue();
            }
            else if (entry.getKey().equals("appName")) {
                cameraData.appName=entry.getValue();
            }
        }
        
        
        if (!flTemp2.equals("")){
            information.flashMode=flTemp2;
        }
        else if (!flTemp1.equals("")){
            information.flashMode=flTemp1;
        }

        if (!foTemp2.equals("")){
            information.focusMode=foTemp2;
        }
        else if (!foTemp1.equals("")){
            information.focusMode=foTemp1;
        }

        if (!scTemp2.equals("")){
            information.sceneMode=scTemp2;
        }
        else if (!scTemp1.equals("")){
            information.sceneMode=scTemp1;
        }

        if (!foaTemp2.equals("")){
            information.focusAreas=foaTemp2;
        }
        else if (!foaTemp1.equals("")){
            information.focusAreas=foaTemp1;
        }

        if (!wmTemp2.equals("")){
            information.whiteBalanceMode=wmTemp2;
        }
        else if (!wmTemp1.equals("")){
            information.whiteBalanceMode=wmTemp1;
        }

        if (!bsTemp2.equals("")){
            information.bestSize=bsTemp2;
        }
        else if (!bsTemp1.equals("")){
            information.bestSize=bsTemp1;
        }

        if (!fpsTemp2.equals("")){
            String[] temp;
            temp=fpsTemp2.split("-");
            temp[0]=temp[0].trim();
            temp[1]=temp[1].trim();
            information.minFrame=temp[0];
            information.maxFrame=temp[1];

        }
        else if (!fpsTemp1.equals("")){
            String[] temp;
            temp=fpsTemp1.split(",");
            temp[0]=temp[0].trim();
            temp[1]=temp[1].trim();
            information.minFrame=String.valueOf(Double.valueOf(temp[0])/1000);
            information.maxFrame=String.valueOf(Double.valueOf(temp[1])/1000);
        }

    }


    /**
     * 转成json格式的字符串
     */

    public void buildJson() {
        try {
            jsonObjInformation.put("auto_exposureLock",cameraData.information.autoExposureLock);
            jsonObjInformation.put("auto_whiteBalance_lock",cameraData.information.autoWhiteBalanceLock);
            jsonObjInformation.put("best_size",cameraData.information.bestSize);
            jsonObjInformation.put("camera_preview",cameraData.information.cameraPreview);
            jsonObjInformation.put("color_effect",cameraData.information.colorEffect);
            jsonObjInformation.put("exposure_compensation",cameraData.information.exposureCompensation);
            jsonObjInformation.put("exposure_compensation_step",cameraData.information.exposureCompensationStep);
            jsonObjInformation.put("exposure_mode",cameraData.information.exposureMode);
            jsonObjInformation.put("flash_mode",cameraData.information.flashMode);
            jsonObjInformation.put("focal_length",cameraData.information.focalLength);
            jsonObjInformation.put("focus_areas",cameraData.information.focusAreas);
            jsonObjInformation.put("focus_mode",cameraData.information.focusMode);
            jsonObjInformation.put("High_HDR",cameraData.information.HighHDR);
            jsonObjInformation.put("max_frame",cameraData.information.maxFrame);
            jsonObjInformation.put("min_frame",cameraData.information.minFrame);
            jsonObjInformation.put("scene_mode",cameraData.information.sceneMode);
            jsonObjInformation.put("video_stabilization",cameraData.information.videoStabilization);
            jsonObjInformation.put("white_balanceMode",cameraData.information.whiteBalanceMode);
            jsonObjInformation.put("zoom",cameraData.information.zoom);


            jsonObjMicInformation.put("AVEncodeBitRateKey",cameraData.micInformation.AVEncodeBitRateKey);
            jsonObjMicInformation.put("AVEncoderBitRatePerChannelKey",cameraData.micInformation.AVEncodeBitRatePerChannelKey);
            jsonObjMicInformation.put("AVFormatIDKey",cameraData.micInformation.AVFormatIDKey);
            jsonObjMicInformation.put("AVNumberOfChannelsKey",cameraData.micInformation.AVNumberOfChannelsKey);
            jsonObjMicInformation.put("AVSampleRateKey",cameraData.micInformation.AVSampleRateKey);

            objectCamera.put("information",jsonObjInformation);
            objectCamera.put("appName",cameraData.appName);
            objectCamera.put("iosVersion",cameraData.osVersion);
            objectCamera.put("platform",cameraData.platform);
            objectCamera.put("micInformation",jsonObjMicInformation);
            objectCamera.put("version",cameraData.version);
            objectCamera.put("cameraPosition",cameraData.cameraPosition);
            objectCamera.put("iphoneType",cameraData.deviceModel);
            objectCamera.put("deviceModel",cameraData.deviceModel);

            Log.d(TAG, "buildJson: "+objectCamera.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 发送数据到服务器
     * @param jsonObject
     */

    private void mSendString(JSONObject jsonObject) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        client.setTimeout(20000); //设置超时为20s
        String strGBK=jsonObject.toString();
        params.put("data",strGBK);
        //发送一个post请求
        client.post(camerainfoUrl,params,new AsyncHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                Log.d(TAG, "onSuccess: +"+statusCode);
             }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                super.onProgress(bytesWritten, totalSize);
                int count = (int) ((bytesWritten * 1.0 / totalSize) * 100);
                Log.e("上传 Progress>>>>>", bytesWritten + " / " + totalSize);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                //未正确响应时调用：statusCode=401\403\404\...
                Log.d(TAG, "onFailure: "+statusCode);
            }
            @Override
            public void onRetry(int retryNo){
                //重试请求时调用
            }
        });
    }


    /**
     * 发送mp4文件到服务器
     * @param
     */

    private void mSendMp4(String fileName) {
        File mp4File = new File(Environment.getExternalStorageDirectory(),fileName);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        client.setTimeout(20000); //设置超时为20s
        try {
            params.put("file",mp4File);
        }catch (FileNotFoundException e) {

        }
        //发送一个post请求
        client.post(mp4Url,params,new AsyncHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                Log.d(TAG, "onSuccess: +"+statusCode);
             }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                super.onProgress(bytesWritten, totalSize);
                int count = (int) ((bytesWritten * 1.0 / totalSize) * 100);
                Log.e("上传 Progress>>>>>", bytesWritten + " / " + totalSize);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                //未正确响应时调用：statusCode=401\403\404\...
                Log.d(TAG, "onFailure: "+statusCode);
            }
            @Override
            public void onRetry(int retryNo){
                //重试请求时调用
            }
        });

    }


    /**
     * 获取指定文件夹中的文件
     * @param file
     * @throws IOException
     */
    private List<Map<String, Object>> loadFolderList(String file) throws IOException{

        List<Map<String, Object>> xList;
        xList=new ArrayList<Map<String,Object>>();

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            PermissionsUtil.requestPermission(MainActivity.this, new PermissionListener() {
                @Override
                public void permissionGranted(@NonNull String[] permission) {
                }
                @Override
                public void permissionDenied(@NonNull String[] permission) {

                }
            },new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
        }

                List < Map < String, Object >> list = GetFileUtils.getInstance().getSonNode(file);
        if(list!=null){
            Collections.sort(list, GetFileUtils.getInstance().defaultOrder());
            xList.clear();
            for(Map<String, Object> map:list){
                String fileType=(String) map.get(GetFileUtils.FILE_INFO_TYPE);
                Map<String,Object> gMap=new HashMap<String, Object>();
                if(map.get(GetFileUtils.FILE_INFO_ISFOLDER).equals(true)){
                    gMap.put("fIsDir", true);
                    gMap.put("fInfo", map.get(GetFileUtils.FILE_INFO_NUM_SONDIRS)+"个文件夹和"+
                            map.get(GetFileUtils.FILE_INFO_NUM_SONFILES)+"个文件");
                }else{
                    gMap.put("fIsDir", false);
                    if(fileType.equals("txt")||fileType.equals("text")){
                    }else{
                    }
                    gMap.put("fInfo","文件大小:"+GetFileUtils.getInstance().getFileSize(map.get(GetFileUtils.FILE_INFO_PATH).toString()));
                }
                gMap.put("fName", map.get(GetFileUtils.FILE_INFO_NAME));
                gMap.put("fPath", map.get(GetFileUtils.FILE_INFO_PATH));
                xList.add(gMap);
            }
        }else{
            xList.clear();
        }
        return xList;
    }

}
