package com.los.manager.ctrl.common;

import com.los.components.oss.config.OssYmlConfig;
import com.los.manager.ctrl.CommonCtrl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


/**
 * @author paul 2024/3/25
 */
/*
在Spring MVC框架中，Controller中的视图渲染是指Controller在接收到HTTP请求并完成业务逻辑处理之后，决定如何向客户端展示响应的过程。
具体来说，Controller负责准备模型数据（Model），并将这些数据与一个视图名关联起来，随后交给Spring MVC框架中的视图解析器（View Resolver）。

视图渲染步骤可以概括如下：

1. **处理请求**：Controller通过注解（如`@RequestMapping`）映射到特定的HTTP请求，当请求到达时，相关的方法会被调用执行业务逻辑。

2. **准备数据**：Controller在方法内部执行业务逻辑，从数据库或其他服务中获取所需数据，并将这些数据填充至一个或多个模型对象中。这些模型对象（通常是`Model`或`ModelAndView`）用于承载要在视图中展示的数据。

3. **选择视图**：Controller方法完成后，会选择一个视图来展示数据。这可以通过返回一个视图名（如“home”或“user/profile”），或者直接返回一个预填充了数据的`ModelAndView`对象。

4. **视图解析**：Spring MVC的视图解析器根据Controller返回的视图名查找对应的视图技术（如JSP、Thymeleaf、Velocity模板等）。视图解析器负责实例化视图对象，并将模型数据传入视图。

5. **渲染输出**：视图引擎使用模型数据渲染输出，生成HTML、JSON或其他格式的响应内容。最后，这个响应内容被发送回客户端，如Web浏览器。

因此，“试图渲染”是Controller职责的一部分，但实际的渲染工作是由视图技术完成的，Controller的角色主要是协调数据和视图之间的绑定。
 */
@Tag(name = "静态资源")
@Slf4j
@Controller
@RequestMapping("/api/anon")
public class StaticController extends CommonCtrl {

    @Autowired private OssYmlConfig ossYmlConfig;

    @Operation(summary = "图片预览")
    @GetMapping("/localOssFiles/**/*.*")
    public ResponseEntity<?> imgView() {
        try {
            // look for img source
            File imgFile = new File(ossYmlConfig.getOss().getFilePublicPath() + File.separator + request.getRequestURI().substring(24));
            if(!imgFile.isFile() || !imgFile.exists()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            //输出文件流（图片格式）
            HttpHeaders httpHeaders = new HttpHeaders();
//            httpHeaders.setContentType(MediaType.IMAGE_JPEG);  //图片格式
            InputStream inputStream = new FileInputStream(imgFile);
            return new ResponseEntity<>(new InputStreamResource(inputStream), httpHeaders, HttpStatus.OK);
        } catch (FileNotFoundException e) {
            log.error("[{}]getStaticFileFail",e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
