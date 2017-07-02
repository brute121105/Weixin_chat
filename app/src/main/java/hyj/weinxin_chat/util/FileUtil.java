package hyj.weinxin_chat.util;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;



import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2017/5/15.
 */

public class FileUtil {
    boolean successFlag = false;
    //创建路径和文件
    public static void createFile2Path(String filePathName,String fileName){
        /**
         * 创建路径
         */
        File filePath = null;
        try {
            filePath = new File(filePathName);
            if (!filePath.exists()) {
                filePath.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e+"");
        }
        /**
         * 创建文件
         */
        File file = null;
        try {
            file = new File(filePathName + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            Log.i("error:", e+"");
        }
    }
    //内容写入文件
    public static void writeContent2File(String filePathName,String fileName,String strcontent){
        createFile2Path(filePathName,fileName);
        String strFilePath = filePathName+fileName;
        // 每次写入时，换行
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }
  /*  //文件上传到服务器
    public static void uploadMultiFile(String url, String data, File file, final String fileName) {
        //开启子线程执行上传，避免主线程堵塞
      *//*  new Thread(new Runnable() {
            @Override
            public void run() {*//*
                //File file = new File(filePath, fileName);
                //if(file==null) file = new File("");
          if(file==null) file = new File("");
          RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM);
                       *//* .addFormDataPart("file",fileName, fileBody)
                        .addFormDataPart("name",fileName)
                        .addFormDataPart("data",data);*//*//name是对方接收的另一个参数，文件名
              if(fileName!=null){
                  System.out.println("fileName--->"+fileName);
                  builder. addFormDataPart("file",fileName, fileBody)
                          .addFormDataPart("name",fileName);
              }

              if(data!=null){
                  builder. addFormDataPart("data",data);
              }

                RequestBody requestBody = builder.build();
                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();
                final okhttp3.OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
                OkHttpClient okHttpClient  = httpBuilder
                        //设置超时
                        .readTimeout(30, TimeUnit.SECONDS)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "uploadMultiFile() e=" + e);
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseStr = response.body().string();
                        Log.i(TAG, "uploadMultiFile() response=" +responseStr );
                    }
                });
            //}
        //}).start();
    }*/
}
