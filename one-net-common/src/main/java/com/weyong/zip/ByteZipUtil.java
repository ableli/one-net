package com.weyong.zip;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

/**
 * Created by hao.li on 9/23/2017.
 */
@Slf4j
public class ByteZipUtil {
    public static byte[] gzip(byte[] bytes)
    {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream os = new GZIPOutputStream(baos);
            os.write(bytes, 0, bytes.length);
            os.close();
            byte[] result = baos.toByteArray();
            return result;
        } catch (IOException e) {
            log.error(e.getMessage());
            return bytes;
        }
    }

    public static byte[] unGzip(byte[] data)
    {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            GZIPInputStream is = new GZIPInputStream(bais);
            byte[] tmp = new byte[256];
            while (true)
            {
                int r = is.read(tmp);
                if (r < 0)
                {
                    break;
                }
                buffer.write(tmp, 0, r);
            }
            is.close();
            byte[] result = buffer.toByteArray();
           return buffer.toByteArray();
        } catch (IOException e) {
            log.error(e.getMessage());
            return data;
        }
    }

    public static byte[] zipBytes(byte[] input)  {
        Deflater compresser = new Deflater();
        compresser.setLevel(Deflater.BEST_COMPRESSION);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compresser.setInput(input);
        byte[] output = new byte[4096];
        compresser.finish();
        int compressedDataLength;
        do{
            compressedDataLength = compresser.deflate(output);
            baos.write(output,0,compressedDataLength);
        }while(!compresser.finished());
        compresser.end();
        return baos.toByteArray();
    }

    public static byte[] unZipBytes(byte[] input)  {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Inflater decompresser = new Inflater();
            decompresser.setInput(input);
            byte[] result = new byte[4096];
            int resultLength = 0;
             do {
                 resultLength = decompresser.inflate(result);
                 baos.write(result,0,resultLength);
                }while (!decompresser.finished());
            decompresser.end();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage());
            return input;
        }
    }
}
