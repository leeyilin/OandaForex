package com.eastmoney.vss.oanda.output;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;

/**
 * User: xww
 * Date: 14-5-6
 * Time: 下午4:29
 */
public class RawdataWriter {
    private Logger logger = LoggerFactory.getLogger(RawdataWriter.class);
    //private String filename;
    private static    RawdataWriter rawdataWriter;

    public RawdataWriter(){
        //this.filename = filename;
    }


    public static  RawdataWriter getRawdataWriter(){
        if(rawdataWriter == null){
            rawdataWriter = new RawdataWriter();
        }
        return rawdataWriter;
    }

    public void write(String filename,byte[] bytes) throws Exception {

        try{
            File ret = new File(filename);
            FileOutputStream outputStream = new FileOutputStream(ret,true);

            outputStream.write(bytes);
            outputStream.close();
        }catch (Exception e){
            logger.warn(e.toString());
        }
    }

    public void write(String filename,String record) throws Exception {
        FileUtils.writeStringToFile(new File(filename),record,true);
    }
}
