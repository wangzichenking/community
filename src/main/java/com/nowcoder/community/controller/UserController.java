package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;


    @RequestMapping(value = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage == null){
            model.addAttribute("error","您还没选择图片");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件的格式不正确");
            return "/site/setting";
        }

        //生成随机文件名
        filename = CommunityUtil.generateUUID()+suffix;
        //确定文件存放路径
        File dest = new File(uploadPath+"/"+filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            LOGGER.error("上传文件失败："+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常！"+e);
        }

        //更新当前用户头像路径(web)
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(value = "/header/{filename}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response){
        //服务器存放路径
        filename = uploadPath + "/" + filename;
        //文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fileInputStream = new FileInputStream(filename);
                ){
            //缓冲区
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            LOGGER.error("读取头像失败："+e.getMessage());
        }
    }
}
