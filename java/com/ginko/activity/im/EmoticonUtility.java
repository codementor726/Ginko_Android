package com.ginko.activity.im;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

import com.ginko.common.Dom4jParser;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;

import org.dom4j.Element;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lee on 4/20/2015.
 */
public class EmoticonUtility {


    public static EmoticonUtility getInstance(){return EmoticonUtility.instance;}


    public static final int[] EMOTICON_COUNTS = { 230, 100, 116, 189 };/* Objects , Places , Nature , People */
    public static final int[] EMOTICON_TYPE_START_INDEX = { 0, 230, 330, 446 };
    public static final int TOTAL_EMOTIONC_COUNTS = 635;
    private static EmoticonUtility instance = null;
    private AssetManager assetManager;
    private int emoticonHeight;
    private HashMap<String, Integer> emoticonIndexMap = null;
    private Bitmap[] emoticons = null;
    int end = 0;
    public boolean isLoading = false;
    public LoadEmoticonThread loadEmoticonThread = null;
    private Context mContext;
    private Pattern mPattern;
    int start = 0;
    private String[] strEmoticonCodes = null;
    private String[] strEmoticonUnicodes = null;

    public EmoticonUtility(Context context)
    {
        this.mContext = context;
        instance = this;
        this.assetManager = this.mContext.getAssets();
        this.emoticonHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.emoticon_height);
        this.emoticons = new Bitmap[TOTAL_EMOTIONC_COUNTS];

        this.loadEmoticonThread = new LoadEmoticonThread();
        this.isLoading = true;
        this.loadEmoticonThread.start();
    }

    public void releaseMemory()
    {
        if (this.emoticons != null)
        {
            int i = 0;
            while (i < TOTAL_EMOTIONC_COUNTS)
            {
                if (this.emoticons[i] != null)
                {
                    try {
                        this.emoticons[i].recycle();
                        this.emoticons[i] = null;
                        i++;
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                        this.emoticons[i] = null;
                    }
                    finally
                    {
                        this.emoticons[i] = null;
                    }
                }
            }
        }
        this.emoticons = null;
    }

    private void readEmoticonUnicodeFileNames()
    {
        if (this.strEmoticonUnicodes != null)
            return;
        this.strEmoticonUnicodes = new String[TOTAL_EMOTIONC_COUNTS];
        if (this.assetManager == null)
            this.assetManager = MyApp.getContext().getAssets();
        try
        {
            InputStream is = this.assetManager.open("emoji_filename_list.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(is);
            BufferedReader bufferdReader = new BufferedReader(inputStreamReader);
            int i = 0;
            do
            {
                String str = bufferdReader.readLine();
                if (str == null)
                    break;
                this.strEmoticonUnicodes[i] = str;
                i++;
            }
            while (i < TOTAL_EMOTIONC_COUNTS);
            return;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            this.strEmoticonUnicodes = null;
        }
    }


    private void readUnicodeHexCodeStrings()
    {
        if (this.strEmoticonCodes != null)
            return;
        this.strEmoticonCodes = new String[TOTAL_EMOTIONC_COUNTS];
        if (this.assetManager == null)
            this.assetManager = MyApp.getContext().getAssets();

        try
        {
            InputStream inputStream = this.assetManager.open("EmojisList.plist");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer strBuffer = new StringBuffer();
            byte[] arrayOfByte = new byte[4096];
            int readBytes = 0;
            String strLine = "";
            while((strLine = bufferedReader.readLine())!=null)
            {
                strBuffer.append(strLine+"\n");
            }
            inputStream.close();

            String strXMLContent = strBuffer.toString();
            Element rootElement = Dom4jParser.getRootElement(strXMLContent);
            Element dicElement = Dom4jParser.getElement(rootElement, "dict");
            int index = 0;
            for (Iterator<?> i = dicElement.elementIterator("array"); i.hasNext();) {
                Element emoticonArrayEle = (Element) i.next();

                if(index == 0)//People
                {
                    int numIndex = 0;
                    for(Iterator<?> j = emoticonArrayEle.elementIterator("string"); j.hasNext();)
                    {
                        Element strEle = (Element) j.next();
                        strEmoticonCodes[EMOTICON_TYPE_START_INDEX[3]+numIndex] = strEle.getText();
                        numIndex++;
                    }
                }
                else if(index == 1) //Places
                {
                    int numIndex = 0;
                    for(Iterator<?> j = emoticonArrayEle.elementIterator("string"); j.hasNext();)
                    {
                        Element strEle = (Element) j.next();
                        strEmoticonCodes[EMOTICON_TYPE_START_INDEX[1]+numIndex] = strEle.getText();
                        numIndex++;
                    }
                }
                else if(index == 2) //Nature
                {
                    int numIndex = 0;
                    for(Iterator<?> j = emoticonArrayEle.elementIterator("string"); j.hasNext();)
                    {
                        Element strEle = (Element) j.next();
                        strEmoticonCodes[EMOTICON_TYPE_START_INDEX[2]+numIndex] = strEle.getText();
                        numIndex++;
                    }
                }
                else if(index == 3) //Objects
                {
                    int numIndex = 0;
                    for(Iterator<?> j = emoticonArrayEle.elementIterator("string"); j.hasNext();)
                    {
                        Element strEle = (Element) j.next();
                        strEmoticonCodes[EMOTICON_TYPE_START_INDEX[0]+numIndex] = strEle.getText();
                        numIndex++;
                    }
                }
                index++;
            }

        }catch(Exception e)
        {
            e.printStackTrace();
            this.strEmoticonCodes = null;
        }
    }
    private void readEmoticons()
    {
        if (this.emoticons == null)
            this.emoticons = new Bitmap[TOTAL_EMOTIONC_COUNTS];
        for (int i = 0; i < TOTAL_EMOTIONC_COUNTS; i++)
        {
            this.emoticons[i] = getImage(this.strEmoticonUnicodes[i] + ".png");
        }
    }

    /**
     * For loading smileys from assets
     */
    private Bitmap getImage(String path) {
        AssetManager mngr = mContext.getAssets();
        InputStream in = null;
        try {
            in = mngr.open("emoticons/" + path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bitmap temp = BitmapFactory.decodeStream(in, null, null);
        return temp;
    }

    public Bitmap getEmoticon(int index)
    {
        if(emoticons == null)
        {
            emoticons = new Bitmap[TOTAL_EMOTIONC_COUNTS];
            readEmoticons();
        }

        if(emoticons[index] != null)
            return emoticons[index];
        else
        {
            emoticons[index] = getImage(strEmoticonCodes[index] + ".png");
        }

        return emoticons[index];
    }

    public String getEmoticonCode(int index)
    {
        return strEmoticonCodes[index];
    }

    /**
     * Builds the regular expression we use to find smileys in {@link #addSmileySpans}.
     */
    private Pattern buildPattern() {
        // Set the StringBuilder capacity with the assumption that the average
        // smiley is 3 characters long.
        StringBuilder patternString = new StringBuilder();
        // Build a regex that looks like (:-)|:-(|...), but escaping the smilies
        // properly so they will be interpreted literally by the regex matcher.
        patternString.append('(');
        for (String s : strEmoticonCodes) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        // Replace the extra '|' with a ')'
        patternString.replace(patternString.length() - 1, patternString.length(), ")");

        return Pattern.compile(patternString.toString());
    }

    /**
     * Adds ImageSpans to a CharSequence that replace textual emoticons such
     * as :-) with a graphical version.
     *
     * @param text A CharSequence possibly containing emoticons
     * @return A CharSequence annotated with ImageSpans covering any
     *         recognized emoticons.
     */
    public CharSequence addSmileySpans(CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int index = this.emoticonIndexMap.get(matcher.group());
            Drawable drawable = new BitmapDrawable(mContext.getResources(),emoticons[index]);
            int emoWidth = emoticonHeight*(drawable.getIntrinsicHeight()/drawable.getIntrinsicWidth());
            drawable.setBounds(0, 0, emoWidth, emoticonHeight);
            //drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
            builder.setSpan(span,
                    matcher.start(), matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }


    public void waitForLoad()
    {
        if (this.isLoading)
        {

            try
            {
                this.loadEmoticonThread.join();
                this.isLoading = false;
                return;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                this.isLoading = false;
                this.loadEmoticonThread = null;
            }
            finally
            {
                this.isLoading = false;
                this.loadEmoticonThread = null;
            }
        }

    }

    class LoadEmoticonThread extends Thread
    {
        public LoadEmoticonThread()
        {
        }

        public void run()
        {
            EmoticonUtility.this.readEmoticonUnicodeFileNames();
            EmoticonUtility.this.readUnicodeHexCodeStrings();
            if (EmoticonUtility.this.emoticonIndexMap == null)
            {
                EmoticonUtility.this.emoticonIndexMap = new HashMap<String, Integer>();
                for (int i = 0; i < TOTAL_EMOTIONC_COUNTS; i++)
                    EmoticonUtility.this.emoticonIndexMap.put(EmoticonUtility.this.strEmoticonCodes[i], Integer.valueOf(i));
            }
            EmoticonUtility.this.readEmoticons();
            mPattern = buildPattern();
            EmoticonUtility.this.isLoading = false;
        }
    }
}
