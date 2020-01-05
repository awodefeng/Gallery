package com.xiaoxun.smart.uploadfile;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import json.JSONArray;
import json.JSONObject;
import json.JSONValue;

/**
 * Created by guxiaolong on 2017/11/6.
 */

public class UploadFile {

    private Context mContext;
    private String mToken;
    private String AES_KEY;


    public UploadFile(Context context, String token, String aesKey) {
        mContext = context;
        mToken = token;
        AES_KEY = aesKey;
    }

    public String uploadFile(final String token, final String type, final String eid, final String gid, final String filePath, final String previewFilePath, final ProgressListener listener, final OnUploadResult onUploadResult) {

        long totalSize = 0;

        if (filePath != null && filePath.length() > 0) {
            File srcFile = new File(filePath);
            if (!srcFile.exists()) {
                return "资源文件不存在";
            }
            totalSize += srcFile.length();
        }

        if (previewFilePath != null && previewFilePath.length() > 0) {
            File previewFile = new File(previewFilePath);
            if (!previewFile.exists()) {
                return "预览图片不存在";
            }
            totalSize += previewFile.length();
        }


        /*if (totalSize > 10 * 1024 * 1024) {
            return "文件超过大小";
        }*/

        final String fileName;
        final String previewFileName;

        Date d = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String timestamp = format.format(d).toString();
        String time = getReversedOrderTime(timestamp);

        if (filePath != null && filePath.length() > 0 ) {
            String fileSuffix = filePath.substring(filePath.lastIndexOf(".") + 1);
            fileName = time + "." + fileSuffix;
            previewFileName = time + "_" + fileSuffix + previewFilePath.substring(previewFilePath.lastIndexOf("."));
        } else {
            fileName = null;
            previewFileName = time + "_" + "xxx" + previewFilePath.substring(previewFilePath.lastIndexOf("."));
        }

        AsyncTask<Long, Boolean, String> task = new AsyncTask<Long, Boolean, String>() {
            @Override
            protected String doInBackground(Long... params) {
                try {
                    final long size = params[0];

                    // 服务器端修改了证书，APP需要更改地址
                    // 做一下修改，用于Launcher
                    String url = "https://fdsfile.xunkids.com/upload";
                    JSONObject json = new JSONObject();

                    if (filePath != null && filePath.length() > 0) {
                        json.put("key", "GP/" + gid + "/MSG/SOURCE/" + fileName);
                        json.put("sid", token);

                        String fileUrl = runWithHttpsClient(json.toString(), url);
                        Log.e("UploadFile", "fileUrl: " + fileUrl);
                        if (fileUrl != null && fileUrl.startsWith("http")) {
                            boolean success = putFile(eid, fileUrl, filePath, new ProgressListener() {
                                @Override
                                public void transferred(long transferedBytes) {
                                    listener.transferred(transferedBytes);
                                }
                            });
                            if (!success) {
                                String deleteUrl = "https://fdsfile.xunkids.com/delsfile";
                                JSONArray array = new JSONArray();
                                array.add("GP/" + gid + "/MSG/SOURCE/" + fileName);

                                JSONObject deleteJson = new JSONObject();
                                deleteJson.put("keys", array);
                                deleteJson.put("sid", token);
                                runWithHttpsClient(deleteJson.toJSONString(), deleteUrl);
                                return "fail";
                            }
                        } else if (fileUrl != null && fileUrl.equals("-121")) {
                            return "storage";
                        } else {
                            return "fail";
                        }
                    }


                    json.put("key", "GP/" + gid + "/MSG/PREVIEW/" + previewFileName);
                    json.put("sid", mToken);
                    String fileUrl = runWithHttpsClient(json.toJSONString(), url);
                    if (fileUrl != null && fileUrl.startsWith("http")) {
                        boolean success = putFile(eid, fileUrl, previewFilePath, new ProgressListener() {
                            @Override
                            public void transferred(long transferedBytes) {
                                listener.transferred(transferedBytes);
                            }
                        });
                        if (!success) {
                            String deleteUrl = "https://fdsfile.xunkids.com/delsfile";
                            JSONArray array = new JSONArray();
                            if (filePath != null && filePath.length() > 0) {
                                array.add("GP/" + gid + "/MSG/SOURCE/" + fileName);
                            }
                            array.add("GP/" + gid + "/MSG/PREVIEW/" + previewFileName);
                            JSONObject deleteJson = new JSONObject();
                            deleteJson.put("keys", array);
                            deleteJson.put("sid", mToken);
                            runWithHttpsClient(deleteJson.toJSONString(), deleteUrl);
                            return "fail";
                        } else {
                            return "success";
                        }
                    } else if (fileUrl != null && fileUrl.equals("-121")) {
                        return "storage";
                    } else {
                        return "fail";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (result != null && result.equals("success")) {
                    try {
                        String content = "GP/" + gid + "/MSG/PREVIEW/" + previewFileName;
                        onUploadResult.onResult(content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (result != null && result.equals("storage")) {
                    onUploadResult.onResult("空间存储不足");
                } else {
                    onUploadResult.onResult("文件上传失败");
                }
            }

            @Override
            protected void onProgressUpdate(Boolean... values) {
                super.onProgressUpdate(values);
            }
        };
        task.execute(totalSize);
        return null;
    }

    public String runWithHttpsClient(String postData, String postUrl) throws IOException {
        try {
            InputStream in = mContext.getAssets().open("dxclient_t.bks");
            KeyStore keystore = KeyStore.getInstance("BKS");
            keystore.load(in, "123456".toCharArray());

            SSLSocketFactory socketFactory = new SSLSocketFactory(keystore);

            int timeOut = 30 * 1000;
            HttpParams param = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(param, timeOut);
            HttpConnectionParams.setSoTimeout(param, timeOut);
            HttpConnectionParams.setTcpNoDelay(param, true);

            Log.e("UploadFile", "postData: " + postData + " postUrl " + postUrl);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", socketFactory, 443));

            ClientConnectionManager manager = new ThreadSafeClientConnManager(param, registry);
            DefaultHttpClient client = new DefaultHttpClient(manager, param);

            HttpPost request = new HttpPost(postUrl);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Charset", "UTF-8");
            String encrytData = BASE64Encoder.encode(AESUtil.encryptAESCBC(postData, AES_KEY, AES_KEY));
            StringEntity postEntity = new StringEntity(encrytData + mToken);
            request.setEntity(postEntity);
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            Log.e("UploadFile", "result.toString(): " + result.toString());
            String out = new String(AESUtil.decryptAESCBC(Base64.decode(result.toString()), AES_KEY, AES_KEY));
            JSONObject resultJson = (JSONObject) JSONValue.parse(out);
            Log.e("UploadFile", "resultJson: " + resultJson.toJSONString());
            int code = (Integer) resultJson.get("code");
            if (code == 0) {
                return (String) resultJson.get("url");
            } else {
                return code + "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private boolean putFile(String eid, String url, String filePath, ProgressListener listener) {
        try {
            byte[] encryptByte = AESUtil.encryptFile(filePath, eid.substring(0, 16), eid.substring(0, 16));
            InputStream inputStream = new ByteArrayInputStream(encryptByte);
            InputStreamEntity requestEntity = getInputStreamRequestEntity(inputStream, inputStream.available());
            ProgressOutHttpEntity progressEntity = new ProgressOutHttpEntity(requestEntity, listener);
            HttpPut httpPut = new HttpPut(url);
            httpPut.setEntity(progressEntity);

            DefaultHttpClient dhc = new DefaultHttpClient();
            HttpResponse response = dhc.execute(httpPut);

            Log.e("UploadFile", "StatusCode: " + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() == 200) {
                Log.i("http", "httpclient: " + response.getEntity().toString());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private InputStreamEntity getInputStreamRequestEntity(InputStream input, long inputStreamLength) {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(input);
        InputStreamEntity entity = new InputStreamEntity(bufferedInputStream, inputStreamLength);
        return entity;
    }

    private static String getReversedOrderTime(String time) {
        StringBuilder timeStamp = new StringBuilder();
        String test = null;
        if (time != null) {
            test = time;
        } else {
            test = getTimeStampGMT();
        }
        timeStamp.append(String.format("%1$08d", 99999999 - Integer.parseInt(test.substring(0, 8))));
        timeStamp.append(String.format("%1$09d", 999999999 - Integer.parseInt(test.substring(8, 17))));
        return timeStamp.toString();
    }

    private static String getTimeStampGMT() {
        Date d = new Date();
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(d);
    }
}
