package com.whj.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadConfig  implements WebMvcConfigurer {

//    addResourceHandlers 自定义资源映射
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //映射图片保存地址

        //当前项目路径
        String str = System.getProperty("user.dir");
        String[] s = str.split("\\\\");
        StringBuffer stringBuffer = new StringBuffer("");
        for(String ss : s){
            stringBuffer.append(ss+"\\\\");
        }
        registry.addResourceHandler("/images/**").addResourceLocations("file:"+stringBuffer.toString()+"src\\main\\resources\\static\\images\\");
    }
}