package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){

        //判断basePath是否存在
        File dir = new File(basePath);
        if (!dir.exists()){
            //不存在则创建目录
            dir.mkdirs();
        }

        //获取原始文件名
        String originalFilename = file.getOriginalFilename();

        //获取文件名后缀
        log.info(originalFilename);
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID生成新的文件名
        String newName = UUID.randomUUID().toString()+suffix;
        try {
            file.transferTo(new File(basePath+newName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(newName);
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response, String name){
        try {
            //构建输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(basePath + name);

            //获取输出流，用于向浏览器写数据
            ServletOutputStream outputStream = response.getOutputStream();

            //设置返回类型
            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            fileInputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
