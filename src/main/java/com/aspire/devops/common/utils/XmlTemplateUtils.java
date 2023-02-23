package com.aspire.devops.common.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

@Slf4j
public class XmlTemplateUtils {

    /**
     * 根据模板生成字符串
     * @param params
     * @return
     */
    public static String writeXmlStr(Map<String,String> params,String fileName){
        Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setDefaultEncoding("utf-8");
        try {
            configuration.setClassForTemplateLoading(configuration.getClass(), "/templates");
            StringWriter stringWriter = new StringWriter();
            Template template = configuration.getTemplate(fileName);
            template.process(params,stringWriter);
            return stringWriter.toString();
        } catch (IOException | TemplateException e) {
            log.error("xml模板动态读写报错：{}",e.getMessage());
            throw new RuntimeException(e.getMessage(),e);
        }
    }
}
