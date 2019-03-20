package com.ginko.activity.contact;


import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListAdapter;

public  class IndexCursor implements Cursor {

    private ListAdapter adapter;
    private int position;
//    private Map<String, String> map;

    public IndexCursor(ListAdapter adapter){
        this.adapter = adapter;
    }

    @Override
    public int getCount() {return this.adapter.getCount();}

    /**
     * ȡ��������ĸ����������ǳ���Ҫ�����ʵ��������崦��
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getString(int columnIndex) {
        ContactItem item = (ContactItem)adapter.getItem(position);
        if (item.isSection()){
            return item.getSectionName();
        }
        if (item.getFullName().length() < 1) {
            return "";
        }
        return item.getFullName().substring(0,1);
    }

    @Override
    public boolean moveToPosition(int position) {
        if(position<-1||position>getCount()){
            return false;
        }

        this.position = position;
        //�������λ���е�����ƫ�Ļ��������⼸�д������޸���λ����ֵΪ������ֵ������
        //if(position+2>getCount()){
        //    this.position = position;
        //}else{
        //   this.position = position + 2;
        //}
        return true;
    }

    @Override
    public void close() {}
    @Override
    public void copyStringToBuffer(int arg0, CharArrayBuffer arg1) {}
    @Override
    public void deactivate() {}
    @Override
    public byte[] getBlob(int arg0) {return null;}
    @Override
    public int getColumnCount() {return 0;}
    @Override
    public int getColumnIndex(String columnName) {return 0;}
    @Override
    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {return 0;}
    @Override
    public String getColumnName(int columnIndex) {return null;}
    @Override
    public String[] getColumnNames() {return null;}
    @Override
    public double getDouble(int columnIndex) {return 0;}

    @Override
    public int getType(int columnIndex) {
        return 0;
    }

    @Override
    public Bundle getExtras() {return null;}
    @Override
    public float getFloat(int columnIndex) {return 0;}
    @Override
    public int getInt(int columnIndex) {return 0;}
    @Override
    public long getLong(int columnIndex) {return 0;}
    @Override
    public int getPosition() {return position;}
    @Override
    public short getShort(int columnIndex) {return 0;}
    @Override
    public boolean getWantsAllOnMoveCalls() {return false;}
    @Override
    public boolean isAfterLast() {return false;}
    @Override
    public boolean isBeforeFirst() {return false;}
    @Override
    public boolean isClosed() {return false;}
    @Override
    public boolean isFirst() {return false;}
    @Override
    public boolean isLast() {return false;}
    @Override
    public boolean isNull(int columnIndex) {return false;}
    @Override
    public boolean move(int offset) {return false;}
    @Override
    public boolean moveToFirst() {return false;}
    @Override
    public boolean moveToLast() {return false;}
    @Override
    public boolean moveToNext() {return false;}
    @Override
    public boolean moveToPrevious() {return false;}
    @Override
    public void registerContentObserver(ContentObserver observer) {}
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {}
    @Override
    public boolean requery() {return false;}
    @Override
    public Bundle respond(Bundle extras) {return null;}
    @Override
    public void setNotificationUri(ContentResolver cr, Uri uri) {}

    @Override
    public Uri getNotificationUri() {
        return null;
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {}
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {}

}