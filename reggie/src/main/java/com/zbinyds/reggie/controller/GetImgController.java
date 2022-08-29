package com.zbinyds.reggie.controller;

import com.zbinyds.reggie.commen.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.UUID;

/**
 * @author zbinyds
 * @time 2022/08/18 11:17
 *
 * 全局文件上传、下载请求-控制层
 */

@RestController
@Slf4j
@RequestMapping("/common")
public class GetImgController {

    // 将配置文件中的文件下载路径进行属性注入
    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传功能
     *
     * @param imgFile：接收前端传递的图片，封装为MultipartFile类型
     * @return：返回上传的文件名，用于图片回显
     */
    @PostMapping("/upload")
    public R<String> upload(@RequestPart("file") MultipartFile imgFile) {
        // 此时的file文件是一个临时文件，我们需要进行转存transferTo
        log.info("{}", imgFile);

        //创建一个目录对象，用于存放图片资源
        File dir = new File(basePath);
        //判断这个目录是否存在
        if (!dir.exists()) {
            //目录不存在，需要创建
            dir.mkdirs();
        }
        // 获取原始文件名（不建议使用，因为如果文件重名了，它会进行自动替换）
        String originalFilename = imgFile.getOriginalFilename();
        // 这里我们使用uuid生成一个文件名，这样就不会重复了。（UUID+文件后缀）==> 新文件名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + suffix;

        // 将文件进行转存
        try {
            imgFile.transferTo(new File(basePath + filename));
        } catch (IOException e) {
            throw new RuntimeException("存放图片文件的路径未找到！");
        }
        return R.success(filename);
    }

    /**
     * 文件下载功能
     * @param fileName：文件名
     * @return：返回ResponseEntity对象
     */
    @GetMapping("/download")
    public ResponseEntity download(@RequestParam("name") String fileName) {
        //创建输入流
        InputStream is = null;
        byte[] bytes = null;
        try {
            is = new FileInputStream(basePath + fileName);
            //创建字节数组
            bytes = new byte[is.available()];
            //将流读到字节数组中
            is.read(bytes);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("文件路径不存在！");
        }catch (IOException e) {
            throw new RuntimeException("文件流读取发生了异常..");
        } finally {
            try {
                if (is != null){
                    is.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("文件关闭发生了异常..");
            }
        }
        //创建HttpHeaders对象设置响应头信息
        MultiValueMap<String, String> headers = new HttpHeaders();
        //设置要下载方式以及下载文件的名字
        headers.add("Content-Disposition", "attachment;filename="+fileName);
        //设置响应状态码
        HttpStatus statusCode = HttpStatus.OK;
        //创建ResponseEntity对象
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(bytes, headers,
                statusCode);
        //关闭输入流
        return responseEntity;
    }
}
