package com.ginko.api.request;

import com.ginko.context.ConstValues;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Transfer {
    public static final String CRLF = "\r\n";

    public static String HttpSend(String address , HashMap arg){
        Set set = arg.keySet();
        String parameter = "";
        Object []items = set.toArray();


        URL url;

        try {
            for(int i = 0; i < items.length; i++) {
                String name = URLEncoder.encode((String) items[i], "UTF-8");
                String value = URLEncoder.encode((String) arg.get((String) items[i]), "UTF-8");

                if (i == items.length - 1)
                    parameter += name + "=" + value;
                else
                    parameter +=  name + "=" + value + "&";
            }
            System.out.println(parameter);

            url = new URL(ConstValues.baseUrl + "/"+address);

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, ConstValues.connectionTimeOut);
            HttpConnectionParams.setSoTimeout(httpParams, ConstValues.connectionTimeOut);

            URLConnection connection = url.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection)connection;
            httpConnection.setDefaultUseCaches(false);
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            httpConnection.setReadTimeout(ConstValues.connectionTimeOut);
            httpConnection.setConnectTimeout(ConstValues.connectionTimeOut);
            httpConnection.setRequestProperty("Connection", "Keep-Alive");
            httpConnection.setRequestProperty("Accept-Encoding", "gzip,deflate");
            httpConnection.setRequestProperty("User-Agent", "Apache-HttpClient/4.1.1(java 1.5)");
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");


            PrintWriter pw = new PrintWriter(new OutputStreamWriter(httpConnection.getOutputStream(), "utf-8"));
            pw.write(parameter);
            pw.flush();

            int responseCode = httpConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputS = httpConnection.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputS , "utf-8"));
                StringBuffer buffer = new StringBuffer();

                int c;

                while((c=in.read()) != -1)
                    buffer.append((char)c);

                return buffer.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static String GetHttpSendResponse(String address , HashMap arg){
        Set set = arg.keySet();
        String parameter = "";
        Object []items = set.toArray();


        URL url;

        try {
            for(int i = 0; i < items.length; i++) {
                String name = URLEncoder.encode((String) items[i], "UTF-8");
                String value = URLEncoder.encode((String) arg.get((String) items[i]), "UTF-8");

                if (i == items.length - 1)
                    parameter += name + "=" + value;
                else
                    parameter +=  name + "=" + value + "&";
            }
            System.out.println(parameter);

            url = new URL(address+parameter);

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, ConstValues.connectionTimeOut);
            HttpConnectionParams.setSoTimeout(httpParams, ConstValues.connectionTimeOut);

            URLConnection connection = url.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection)connection;
            httpConnection.setDefaultUseCaches(false);
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            httpConnection.setReadTimeout(ConstValues.connectionTimeOut);
            httpConnection.setConnectTimeout(ConstValues.connectionTimeOut);
            httpConnection.setRequestProperty("Connection", "Keep-Alive");
            httpConnection.setRequestProperty("Accept-Encoding", "gzip,deflate");
            httpConnection.setRequestProperty("User-Agent", "Apache-HttpClient/4.1.1(java 1.5)");
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");


            PrintWriter pw = new PrintWriter(new OutputStreamWriter(httpConnection.getOutputStream(), "utf-8"));
            pw.write(parameter);
            pw.flush();

            int responseCode = httpConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputS = httpConnection.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputS , "utf-8"));
                StringBuffer buffer = new StringBuffer();

                int c;

                while((c=in.read()) != -1)
                    buffer.append((char)c);

                return buffer.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static String sendMultipartPost(String address, HashMap<String , Object> param) {
        URL targetURL;
        StringBuffer buffer = new StringBuffer();

        try{
            targetURL = new URL(ConstValues.baseUrl + "/" + address);

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, ConstValues.connectionTimeOut);
            HttpConnectionParams.setSoTimeout(httpParams, ConstValues.connectionTimeOut);

            HttpURLConnection conn = (HttpURLConnection)targetURL.openConnection();

            // Delimeter
            String delimeter = makeDelimeter();
            byte[] newLineBytes = CRLF.getBytes();
            byte[] delimeterBytes = delimeter.getBytes();
            byte[] dispositionBytes = "Content-Disposition: form-data; name=".getBytes();
            byte[] quotationBytes = "\"".getBytes();
            byte[] contentTypeBytes = "Content-Type: application/octet-stream".getBytes();
            byte[] fileNameBytes = "; filename=".getBytes();
            byte[] twoDashBytes = "--".getBytes();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + delimeter);
            conn.setReadTimeout(ConstValues.connectionTimeOut);
            conn.setConnectTimeout(ConstValues.connectionTimeOut);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            conn.setRequestProperty("User-Agent", "Apache-HttpClient/4.1.1(java 1.5)");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            BufferedOutputStream out = null;

            try {
                out = new BufferedOutputStream(conn.getOutputStream());
                Set set = param.keySet();
                Object []items = set.toArray();

                for(int i = 0; i < items.length; i++) {
                    String name = (String) items[i];

                    String strIsFile = name;
	    	    	/*if (name.length() >= 10)
	                	strIsFile = name.substring(0, 10);
	    	    	else
	    	    		strIsFile = "";*/

                    if (strIsFile.compareTo("pictures[]") != 0 && strIsFile.compareTo("thumb_photo")!=0)
                    {
                        // Delimeter 鞝勳啞
                        out.write(twoDashBytes);
                        out.write(delimeterBytes);
                        out.write(newLineBytes);

                        // parameter name
                        out.write(dispositionBytes);
                        out.write(quotationBytes);
                        out.write(name.getBytes());
                        out.write(quotationBytes);

                        // String
                        out.write(newLineBytes);
                        out.write(newLineBytes);

                        //
                        out.write(((String) param.get((String) items[i])).getBytes());
                        out.write(newLineBytes);
                    }
                    //put photo files
                    else {									//
                        List<String> values = (List<String>)param.get((String) items[i]);
                        for(int k = 0;k<values.size();k++)
                        {
                            // Delimeter 鞝勳啞
                            out.write(twoDashBytes);
                            out.write(delimeterBytes);
                            out.write(newLineBytes);

                            // parameter name
                            out.write(dispositionBytes);
                            out.write(quotationBytes);
                            out.write(name.getBytes());
                            out.write(quotationBytes);

                            String value = values.get(k);
                            if (value != null)
                            {

                                if (value.compareTo("") != 0)		//
                                {
                                    String fileName = "";
                                    fileName = value.substring(value.lastIndexOf("/")+1);
                                    // File鞚�臁挫灛頃橂姅 歆�瓴�偓頃滊嫟.
                                    out.write(fileNameBytes);
                                    out.write(quotationBytes);
                                    out.write(fileName.getBytes() );
                                    out.write(quotationBytes);
                                }
                                else {
                                    out.write(fileNameBytes);
                                    out.write(quotationBytes);
                                    out.write(quotationBytes);
                                }
                            }
                            else{
                                out.write(fileNameBytes);
                                out.write(quotationBytes);
                                out.write(quotationBytes);
                            }

                            out.write(newLineBytes);
                            out.write(contentTypeBytes);
                            out.write(newLineBytes);
                            out.write(newLineBytes);

                            // File 雿办澊韯半ゼ 鞝勳啞頃滊嫟.
                            if (value != null)
                            {
                                if (value.compareTo("") != 0) {
                                    String filePath = value;
                                    if(value.contains("file://"))
                                        filePath = value.substring(value.indexOf("file://") + 7);
                                    File file = new File(filePath);
                                    try{
                                        if(file.exists())
                                        {
                                            System.out.println("---Existing File--"+file.getAbsolutePath());
                                        }
                                        else
                                        {
                                            System.out.println("---File Not Exists--");
                                        }
                                    }catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    // file鞐�鞛堧姅 雮挫毄鞚�鞝勳啞頃滊嫟.
                                    BufferedInputStream is = null;

                                    try {
                                        FileInputStream fi = new FileInputStream(file);
                                        is = new BufferedInputStream(fi);
                                        byte[] fileBuffer = new byte[1024 * 8]; // 8k
                                        int len = -1;
                                        while ((len = is.read(fileBuffer)) != -1)
                                            out.write(fileBuffer, 0, len);
                                    }catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    finally {
                                        if (is != null) try { is.close(); } catch(IOException ex) {ex.printStackTrace();}
                                    }

                                    out.write(newLineBytes);
                                }
                                else
                                    out.write(newLineBytes);
                            }
                            else
                            {
                                out.write(newLineBytes);
                            }
                        }
                    } // 韺岇澕 雿办澊韯办潣 鞝勳啞 敫旊煭 雭�

                    if (i == items.length - 1)
                    {
                        // 毵堨毵�Delimeter 鞝勳啞
                        out.write(twoDashBytes);
                        out.write(delimeterBytes);
                        out.write(twoDashBytes);
                        out.write(newLineBytes);
                    }
                } // for 耄攧鞚�雭�

                out.flush();
            } finally {
                if (out != null) out.close();
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputS = conn.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputS, "utf-8"));

                int c;

                while((c=in.read()) != -1)
                    buffer.append((char)c);
            }
            else return ("");

        } catch(Exception e) {
            e.printStackTrace();
        }

        return buffer.toString();
    }

    private static String makeDelimeter() {
        return "---------------------------7d115d2a20060c";
    }
}
