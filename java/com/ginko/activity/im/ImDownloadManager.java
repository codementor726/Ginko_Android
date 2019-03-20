package com.ginko.activity.im;

import com.ginko.vo.MultimediaMessageVO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class ImDownloadManager {

    private final int MAX_CONCURRENT_DOWNLOAD_THREAD_COUNT = 10;
    private int nCurrentDownloadThreadCount = 0;

    private Object lockObj = new Object();

    private Queue<MultimediaMessageVO> downloadQueue;
    private Queue<String> downloadFilePathQueue;

    private HashMap<Long , MultimediaMessageVO> downloadMessagesMap;

    public interface ImDownloadListener{
        public void onImMessageFileDownloaded();
    }

    private List<ImDownloadListener> downloadListeners;
    public ImDownloadManager()
    {

        downloadQueue = new LinkedList<MultimediaMessageVO>();
        downloadFilePathQueue = new LinkedList<String>();
        downloadMessagesMap = new HashMap<Long , MultimediaMessageVO>();

        downloadListeners = new ArrayList<ImDownloadListener>();

    }


    public synchronized void registerDownloadListener(ImDownloadListener _downloadListener)
    {
        //
        boolean alreadyExist = false;//block the double registration
        for(int i=0;i<downloadListeners.size();i++)
        {
            if(downloadListeners.get(i) == _downloadListener)
            {
                alreadyExist = true;
                break;
            }
        }
        if(alreadyExist == false)
            this.downloadListeners.add(_downloadListener);
    }

    public synchronized void unregisterDownloadListener(ImDownloadListener _downloadListener)
    {
        //
        for(int i=0;i<downloadListeners.size();i++)
        {
            if(downloadListeners.get(i) == _downloadListener)
            {
                this.downloadListeners.remove(i);
                break;
            }
        }
    }

    public synchronized void addMessageToDownloadQueue(MultimediaMessageVO message , String filePath)
    {
        synchronized (lockObj) {
            try {
                downloadQueue.offer(message);
                downloadFilePathQueue.offer(filePath);
                downloadMessagesMap.put(message.getMsgId(), message);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        launchDownloadThreadFromQueue(null);

    }

    private synchronized void launchDownloadThreadFromQueue(MultimediaMessageVO prevDownloadedMessage)
    {
        if(prevDownloadedMessage!=null) {
            nCurrentDownloadThreadCount--;
            if (nCurrentDownloadThreadCount < 0) nCurrentDownloadThreadCount = 0;


            if(downloadMessagesMap.containsKey(prevDownloadedMessage.getMsgId()))
                downloadMessagesMap.remove(prevDownloadedMessage.getMsgId());

        }
        if(nCurrentDownloadThreadCount<MAX_CONCURRENT_DOWNLOAD_THREAD_COUNT)
        {
            MultimediaMessageVO message = null;
            String filePath = null;
            if (downloadQueue.size() > 0){
                message = downloadQueue.poll();
                filePath = downloadFilePathQueue.poll();
            }
            if(message!=null)
            {
                nCurrentDownloadThreadCount++;
                new DownloadThread(message , filePath).start();
            }
        }
    }

    public synchronized boolean isMessageInDownloadQueue(MultimediaMessageVO msg)
    {
        return downloadMessagesMap.containsKey(msg.getMsgId());
    }


    //Download file from server and save
    public void DownloadFromUrl(String DownloadUrl, String filePath) throws Exception{

        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        URL url = new URL(DownloadUrl); //you can write here any link
        File file = new File(filePath);

        long startTime = System.currentTimeMillis();

       /* Open a connection to that URL. */
        connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        // expect HTTP 200 OK, so we don't mistakenly save error report
        // instead of the file
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            return;
        }
        int fileLength = connection.getContentLength();

        // download the file
        input = connection.getInputStream();
        output = new FileOutputStream(filePath);

        byte data[] = new byte[1024*64];
        long total = 0;
        int count;

        while ((count = input.read(data)) != -1) {
            // allow canceling with back button
            //if (isCancelled()) {
            //    input.close();
            //    return null;
            //}
            total += count;
            //System.out.println("----FileDownload Progress =" + String.valueOf(total)+ " ------");
            // publishing the progress....
            //if (fileLength > 0) // only if total length is known
            //    publishProgress((int) (total * 100 / fileLength));
            output.write(data, 0, count);
        }
        if (output != null)
            output.close();
        if (input != null)
            input.close();
        if (connection != null)
            connection.disconnect();
        /*
        // Define InputStreams to read from the URLConnection.
        InputStream is = ucon.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);

        // Read bytes to the Buffer until there is nothing more to read(-1).
        ByteArrayBuffer baf = new ByteArrayBuffer(5000);
        int current = 0;
        while ((current = bis.read()) != -1) {
            baf.append((byte) current);
        }

        // Convert the Bytes read to a String.
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(baf.toByteArray());
        fos.flush();
        fos.close();
        Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");*/

    }

    class DownloadThread extends Thread{
        private MultimediaMessageVO message = null;
        private String strDownloadSavePath = null;
        public DownloadThread(MultimediaMessageVO msg , String path)
        {
            this.message = msg;
            this.strDownloadSavePath = path;
        }

        @Override
        public void run() {

            try
            {
                DownloadFromUrl(message.getFileUrl() , strDownloadSavePath);
                message.setFile(strDownloadSavePath);
            }catch(Exception e)
            {
                System.out.println("----FileDownload Exception----");
                e.printStackTrace();
                message.setFile("");
            }

            if(downloadListeners != null && downloadListeners.size()>0) {
                for(ImDownloadListener downlodListener : downloadListeners) {
                    if(downlodListener!=null)
                        downlodListener.onImMessageFileDownloaded();
                }
            }
            //check another message from queue and relaunch thread
            launchDownloadThreadFromQueue(message);
        }
    }

}
