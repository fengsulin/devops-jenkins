package com.aspire.devops.common.utils;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author: FSL
 * @date: 2023/2/20
 * @description: TODO
 */
@Component
public class IOUtils {
    @Resource
    ResourceLoader resourceLoader;

    public  String getResources(String classpath) throws IOException {
        StringBuilder builder = new StringBuilder();
        org.springframework.core.io.Resource resource = resourceLoader.getResource(classpath);
        InputStream is = resource.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);
        String data = null;
        while ((data = reader.readLine()) != null){
            builder.append(data);
        }
        if (is != null) is.close();
        if (reader != null) reader.close();
        if (isr != null) isr.close();
        return builder.toString();
    }
}
