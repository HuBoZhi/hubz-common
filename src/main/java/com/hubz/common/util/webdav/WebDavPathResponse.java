package com.hubz.common.util.webdav;

import lombok.Builder;
import lombok.Data;

/**
 * @author hubz
 * @date 2022/8/1 22:30
 **/
@Data
@Builder
public class WebDavPathResponse {
    /**
     * 路径
     */
    private String href;

    /**
     * 文件或目录的名称
     */
    private String displayName;

    /**
     * 最后修改时间字符串
     */
    private String lastModifiedStr;

    /**
     * 时间字符串转换成的绝对毫秒
     */
    private Long lastModified;

    /**
     * 类型：目录/文件
     */
    private String contentType;


}
