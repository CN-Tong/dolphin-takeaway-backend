package com.tong.config;

import com.tong.properties.HuaweiObsProperties;
import com.tong.utils.HuaweiObsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类，用于创建HuaweiObsUtil对象
 */
@Configuration
@Slf4j
public class ObsConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HuaweiObsUtil huaweiObsUtil(HuaweiObsProperties huaweiObsProperties){
        log.info("开始创建华为云文件上传工具类对象：{}", huaweiObsProperties);
        return new HuaweiObsUtil(huaweiObsProperties.getEndpoint(), huaweiObsProperties.getAccessKeyId(),
                huaweiObsProperties.getAccessKeySecret(), huaweiObsProperties.getBucketName());
    }
}
