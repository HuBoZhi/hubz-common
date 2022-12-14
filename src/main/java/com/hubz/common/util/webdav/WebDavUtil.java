package com.hubz.common.util.webdav;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.PathUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.log.StaticLog;
import com.hubz.common.constant.CommonConstant;
import com.hubz.common.util.TimeUtils;
import com.hubz.common.util.http.HttpRequestClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.FileEntity;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathConstants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author hubz
 * @date 2022/7/26 19:03
 **/
public final class WebDavUtil {
    private static String WEB_DAV_URL = null;
    private static HttpRequestClient HTTP_REQUEST_CLIENT = null;


    public static void createHttpRequestClient(String username, String password, String webDavUrl) {
        WebDavUtil.HTTP_REQUEST_CLIENT = new HttpRequestClient(username, password);
        WebDavUtil.WEB_DAV_URL = webDavUrl;
    }

    /**
     * 参数检查
     * @author hubz
     * @date 2022/12/9 14:22
     *
     * @param basePath 根目录
     **/
    private static void check(String basePath) {
        if (Objects.isNull(WebDavUtil.HTTP_REQUEST_CLIENT)) {
            throw new RuntimeException("HTTP_REQUEST_CLIENT is not init");
        }
        if (Objects.isNull(WebDavUtil.WEB_DAV_URL)) {
            throw new RuntimeException("WEB_DAV_URL is not init");
        }
        if (Objects.isNull(basePath) || CommonConstant.EMPTY_STRING.equals(basePath)) {
            throw new RuntimeException("basePath is not empty");
        }
    }

    /**
     * 创建根目录
     * @author hubz
     * @date 2022/12/9 14:22
     *
     * @param basePath 根目录
     * @return java.lang.Boolean
     **/
    public static Boolean init(String basePath) {
        check(basePath);
        try {
            String url = StrUtil.format("{}/{}", WEB_DAV_URL, basePath);
            HttpResponse response = HTTP_REQUEST_CLIENT.execute(WebDavConstant.HTTP_METHOD_MKCOL, dealUrl(url));
            ThreadUtil.sleep(500);
            return WebDavConstant.REQUEST_OK.equals(response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            StaticLog.error(e, "创建根目录失败");
        }
        return false;
    }

    /**
     * 创建新文件夹
     * @author hubz
     * @date 2022/7/27 22:22
     *
     * @param basePath 根目录
     * @param dir 新的目录名
     * @return java.lang.Boolean true 成功  false 失败
     **/
    public static Boolean createDir(String basePath, String dir) {
        check(basePath);
        try {
            StaticLog.info("创建目录【{}】", dir);
            String[] dirNames = dir.split("/");
            String baseUrl = StrUtil.format("{}/{}", WEB_DAV_URL, basePath);
            StringBuilder toCreatePath = new StringBuilder();
            for (String dirName : dirNames) {
                if (StrUtil.isNotBlank(dirName)) {
                    toCreatePath.append("/").append(dirName);
                    // 处理路径
                    String encodeToCreatePath = encodeFilePath(toCreatePath.toString());
                    StaticLog.debug("开始创建目录【{}】", encodeToCreatePath);
                    if (!checkPathExist(basePath, encodeToCreatePath)) {
                        String createDirUrl = dealUrl(StrUtil.format("{}/{}", baseUrl, encodeToCreatePath));
                        StaticLog.debug("创建目录URL：{}", createDirUrl);
                        HttpResponse httpResponse = null;
                        try {
                            httpResponse = HTTP_REQUEST_CLIENT.execute(WebDavConstant.HTTP_METHOD_MKCOL, createDirUrl);
                            if (!WebDavConstant.WEBDAV_SUCCESS.equals(httpResponse.getStatusLine().getStatusCode())) {
                                return false;
                            }
                        } finally {
                            if (null != httpResponse) {
                                EntityUtils.consumeQuietly(httpResponse.getEntity());
                            }
                        }
                        ThreadUtil.sleep(500);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            StaticLog.error(e, "创建文件夹失败【{}】", dir);
        }
        return false;
    }

    /**
     * 上传单文件
     * @author hubz
     * @date 2022/7/30 0:06
     *
     * @param basePath 根目录
     * @param sourceFilePath 源文件路径
     * @param targetPath 目标上传路径
     * @return java.lang.Boolean
     **/
    public static Boolean uploadFile(String basePath, String sourceFilePath, String targetPath) {
        check(basePath);
        HttpResponse httpResponse = null;
        try {
            Boolean createDirResult = createDir(basePath, targetPath);
            if (Boolean.FALSE.equals(createDirResult)) {
                return false;
            }
            Path targetFilePath = Path.of(targetPath, Path.of(sourceFilePath).getFileName().toString());
            StaticLog.info("开始上传指定文件【{}】到WebDav目录【{}】", sourceFilePath, targetFilePath);
            String encodeTargetFilePath = encodeFilePath(targetFilePath.toString());

            String url = StrUtil.format("{}/{}/{}", WEB_DAV_URL, basePath, encodeTargetFilePath);
            File file = new File(sourceFilePath);
            HttpEntity entity = new FileEntity(file);
            httpResponse = HTTP_REQUEST_CLIENT.doPut(dealUrl(url), entity);
            ThreadUtil.sleep(500);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            StaticLog.debug("上传指定文件【{}】到WebDav目录【{}】完成：请求响应状态码【{}】", sourceFilePath, targetFilePath, statusCode);
            return WebDavConstant.WEBDAV_SUCCESS.equals(statusCode) || WebDavConstant.WEBDAV_SUCCESS_2.equals(statusCode);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != httpResponse) {
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
        }
        return false;
    }

    /**
     * 批量上传文件
     * @author hubz
     * @date 2022/7/30 19:51
     *
     * @param basePath 根目录
     * @param sourcePath 待上传的文件/目录路径
     * @param targetPath WebDav目标路径
     * @return java.lang.Boolean
     **/
    public static Boolean uploadFilesFromPath(String basePath, String sourcePath, String targetPath) {
        check(basePath);
        return uploadFilesFromPath(basePath, "", sourcePath, targetPath);
    }

    /**
     * 批量上传文件
     * @author hubz
     * @date 2022/7/30 13:43
     *
     * @param basePath 根目录
     * @param fileBasePath 文件上级目录
     * @param sourcePath 待上传的文件/目录路径
     * @param targetPath WebDav目标路径
     * @return java.lang.Boolean
     **/
    private static Boolean uploadFilesFromPath(String basePath, String fileBasePath, String sourcePath, String targetPath) {
        check(basePath);
        Path path = Paths.get(sourcePath);
        if (StrUtil.isNotBlank(fileBasePath)) {
            fileBasePath = fileBasePath + "/" + path.getName(path.getNameCount() - 1);
        } else {
            fileBasePath = path.getName(path.getNameCount() - 1).toString();
        }
        String finalFileBasePath = fileBasePath;
        try {
            StaticLog.info("开始上传目录【{}】至【{}】", path, targetPath);
            Files.list(path).forEach(item -> {
                if (Files.isDirectory(item)) {
                    uploadFilesFromPath(basePath, finalFileBasePath, item.toString(), targetPath);
                } else if (Files.isRegularFile(item)) {
                    String filePath = item.toAbsolutePath().toString();
                    Boolean uploadResult = uploadFile(basePath, filePath, targetPath + "/" + finalFileBasePath);
                    if (uploadResult) {
                        StaticLog.debug("文件【{}】上传成功", filePath);
                    } else {
                        StaticLog.error("文件【{}】上传失败", filePath);
                    }
                }
            });
            return true;
        } catch (Exception e) {
            StaticLog.error(e, "遍历失败：{}", sourcePath);
        }
        return false;
    }

    /**
     * 删除文件
     * @author hubz
     * @date 2022/7/26 23:58
     *
     * @param basePath 根目录
     * @param sourceDir 文件所在目录
     * @param fileName 文件名称
     * @return java.lang.Boolean 删除结果
     **/
    public static Boolean deleteFile(String basePath, String sourceDir, String fileName) {
        check(basePath);
        HttpResponse httpResponse = null;
        try {
            StaticLog.info("删除WebDav目录【{}】中指定文件【{}】", sourceDir, fileName);
            String filePath = StrUtil.format("{}/{}", sourceDir, fileName);
            if (Boolean.FALSE.equals(checkPathExist(basePath, filePath))) {
                StaticLog.warn("文件【{}】不存在，不需要删除", filePath);
                return true;
            }
            filePath = Paths.get(basePath, filePath).toString();

            String encodeFilePath = encodeFilePath(filePath);
            String url = StrUtil.format("{}/{}", WEB_DAV_URL, encodeFilePath);
            httpResponse = HTTP_REQUEST_CLIENT.doDelete(dealUrl(url), null);
            ThreadUtil.sleep(500);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            StaticLog.debug("删除WebDav目录【{}】中指定文件【{}】完成：请求响应状态码【{}】", sourceDir, fileName, statusCode);
            return WebDavConstant.REQUEST_OK.equals(statusCode) || WebDavConstant.WEBDAV_SUCCESS.equals(statusCode) || WebDavConstant.WEBDAV_SUCCESS_2.equals(statusCode);
        } catch (Exception e) {
            StaticLog.error(e, StrUtil.format("删除WebDav目录【{}】中指定文件【{}】文件失败", sourceDir, fileName));
        } finally {
            if (null != httpResponse) {
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
        }
        return false;
    }

    /**
     * 删除指定目录
     * @author hubz
     * @date 2022/8/14 12:54
     *
     * @param basePath 根目录
     * @param deletePath 待删除的目录
     * @return java.lang.Boolean 删除结果
     **/
    public static Boolean deletePath(String basePath, String deletePath) {
        check(basePath);
        HttpResponse httpResponse = null;
        try {
            StaticLog.info("删除WebDav目录【{}】", deletePath);
            if (Boolean.FALSE.equals(checkPathExist(basePath, deletePath))) {
                StaticLog.warn("目录【{}】不存在，不需要删除", deletePath);
                return true;
            }
            String filePath = Paths.get(basePath, deletePath).toString();
            String encodeFilePath = encodeFilePath(filePath);
            String url = StrUtil.format("{}/{}", WEB_DAV_URL, encodeFilePath);
            httpResponse = HTTP_REQUEST_CLIENT.doDelete(dealUrl(url), null);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            StaticLog.debug("删除WebDav目录【{}】完成：请求响应状态码【{}】", deletePath, statusCode);
            return WebDavConstant.REQUEST_OK.equals(statusCode) || WebDavConstant.WEBDAV_SUCCESS.equals(statusCode) || WebDavConstant.WEBDAV_SUCCESS_2.equals(statusCode);
        } catch (Exception e) {
            StaticLog.error(e, StrUtil.format("删除WebDav目录【{}】失败", deletePath));
        } finally {
            if (null != httpResponse) {
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
        }
        return false;
    }


    /**
     * 获取文件内容
     * @author hubz
     * @date 2022/7/26 20:32
     *
     * @param basePath 根目录
     * @param sourceDir 文件WebDav的目录
     * @param fileName 文件名
     * @return java.lang.String 文件内容
     **/
    public static String getFileBody(String basePath, String sourceDir, String fileName) {
        check(basePath);
        HttpResponse httpResponse = null;
        try {
            sourceDir = Paths.get(basePath, sourceDir).toString();
            StaticLog.info("获取WebDav目录【{}】中文件【{}】的内容", sourceDir, fileName);
            String encodeFileName = encodeFilePath(fileName);
            String url = StrUtil.format("{}/{}/{}", WEB_DAV_URL, sourceDir, encodeFileName);
            httpResponse = HTTP_REQUEST_CLIENT.doGetHttpResponse(dealUrl(url));
            String fileBodyStr = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
            ThreadUtil.sleep(500);
            StaticLog.debug("获取WebDav目录【{}】中文件【{}】的内容完成：{}", sourceDir, fileName, fileBodyStr);
            return fileBodyStr;
        } catch (Exception e) {
            StaticLog.error(e, StrUtil.format("WebDav获取【{}/{}】文件内容失败", sourceDir, fileName));
        } finally {
            if (null != httpResponse) {
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
        }
        return "";
    }

    /**
     * 下载文件
     * @author hubz
     * @date 2022/7/26 22:23
     *
     * @param basePath 根目录
     * @param sourceDir 文件WebDav的目录
     * @param fileName 文件名
     * @param targetPath 本地目标文件所在的路径
     * @param needBak 是否需要备份已有数据，是的话进行备份，否的话检查是否存在数据，存在则删除
     * @param bakPath 备份已有文件的备份目录
     * @return java.lang.Boolean 下载结果
     **/
    public static Boolean downloadFile(String basePath, String sourceDir, String fileName, String targetPath, Boolean needBak, String bakPath) {
        check(basePath);
        HttpResponse httpResponse = null;
        try {
            sourceDir = Paths.get(basePath, sourceDir).toString();
            String targetFilePath = Paths.get(targetPath, fileName).toAbsolutePath().toString();
            StaticLog.info("下载WebDav文件【{}】至本地路径【{}】", sourceDir, targetFilePath);
            if (needBak) {
                if (FileUtil.exist(targetFilePath)) {
                    StaticLog.warn("目标文件已存在,备份中...");
                    if (StrUtil.isBlank(bakPath)) {
                        StaticLog.warn("备份目录为空...");
                        return false;
                    }
                    // 创建备份目录
                    Path fileBakPath = Paths.get(bakPath, fileName + ".bak." + TimeUtils.getCurrentLongTime());
                    PathUtil.mkParentDirs(fileBakPath);
                    // 将文件移动到备份目录中
                    FileUtil.move(Paths.get(targetFilePath), Paths.get(fileBakPath.toAbsolutePath().toString(), fileName), true);
                }
            } else {
                // 不需要备份就清空旧数据
                FileUtil.del(targetFilePath);
            }
            String encodeFileName = encodeFilePath(fileName);
            String url = StrUtil.format("{}/{}/{}", WEB_DAV_URL, sourceDir, encodeFileName);
            httpResponse = HTTP_REQUEST_CLIENT.doGetHttpResponse(dealUrl(url));
            ThreadUtil.sleep(500);
            OutputStream outputStream = new FileOutputStream(targetFilePath);
            httpResponse.getEntity().writeTo(outputStream);
            return FileUtil.exist(targetFilePath);
        } catch (Exception e) {
            StaticLog.error(e, StrUtil.format("从WebDav下载【{}/{}】文件失败", sourceDir, fileName));
        } finally {
            if (null != httpResponse) {
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
        }
        return false;
    }

    /**
     * 下载文件到指定目录
     * @author hubz
     * @date 2022/8/1 22:57
     *
     * @param basePath 根目录
     * @param webDavSourceFilePath 文件的WebDav路径
     * @param targetFilePathStr 本地目标文件所在的路径
     * @param needBak 是否需要备份已有数据，是的话进行备份，否的话检查是否存在数据，存在则删除
     * @param bakPath 备份已有文件的备份目录
     * @return java.lang.Boolean 下载结果
     **/
    public static Boolean downloadFile(String basePath, String webDavSourceFilePath, String targetFilePathStr, Boolean needBak, String bakPath) {
        check(basePath);
        HttpResponse httpResponse = null;
        try {
            webDavSourceFilePath = Paths.get(basePath, webDavSourceFilePath).toString();
            Path targetFileBasePath = Paths.get(targetFilePathStr);
            String targetFilePath = targetFileBasePath.toAbsolutePath().toString();
            StaticLog.info("下载WebDav文件【{}】至本地路径【{}】", webDavSourceFilePath, targetFilePath);
            if (needBak) {
                if (FileUtil.exist(targetFilePath)) {
                    StaticLog.warn("目标文件【{}】已存在,备份中...", targetFilePath);
                    if (StrUtil.isBlank(bakPath)) {
                        StaticLog.warn("备份目录为空...");
                        return false;
                    }
                    // 创建备份目录
                    String fileName = targetFileBasePath.getName(targetFileBasePath.getNameCount() - 1).toString();
                    Path fileBakPath = Paths.get(bakPath, fileName + ".bak." + TimeUtils.getCurrentLongTime());
                    PathUtil.mkParentDirs(fileBakPath);
                    // 将文件移动到备份目录中
                    FileUtil.move(Paths.get(targetFilePath), Paths.get(fileBakPath.toAbsolutePath().toString(), fileName), true);
                }
            } else {
                StaticLog.warn("目标文件【{}】已存在,已配置不需要备份，删除中...", targetFilePath);
                // 不需要备份就清空旧数据
                FileUtil.del(targetFilePath);
            }

            Path targetFileParentPath = Path.of(targetFilePath).getParent();
            if (!FileUtil.exist(targetFileParentPath.toFile())) {
                // 创建父级目录
                Files.createDirectories(targetFileParentPath);
            }

            String encodeFilePath = encodeFilePath(webDavSourceFilePath);
            String url = StrUtil.format("{}/{}", WEB_DAV_URL, encodeFilePath);
            StaticLog.debug("开始下载WebDav远端【{}】的文件", dealUrl(url));
            httpResponse = HTTP_REQUEST_CLIENT.doGetHttpResponse(dealUrl(url));
            try (OutputStream outputStream = new FileOutputStream(targetFilePath)) {
                httpResponse.getEntity().writeTo(outputStream);
            }
            return FileUtil.exist(targetFilePath);
        } catch (Exception e) {
            StaticLog.error(e, StrUtil.format("从WebDav下载【{}】文件失败", webDavSourceFilePath));
        } finally {
            if (null != httpResponse) {
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
        }
        return false;
    }


    /**
     * 判断路径是否存在
     * @author hubz
     * @date 2022/7/30 14:35
     *
     * @param basePath 根目录
     * @param path 文件/目录路径
     * @return java.lang.Boolean true 存在 false，不存在
     **/
    public static Boolean checkPathExist(String basePath, String path) {
        check(basePath);
        HttpResponse httpResponse = null;
        try {
            String encodePath = encodeFilePath(path);
            String url = StrUtil.format("{}/{}/{}", WEB_DAV_URL, basePath, encodePath);
            String hasDealUrl = dealUrl(url);
            StaticLog.info("检查路径【{}】是否存在", hasDealUrl);
            httpResponse = HTTP_REQUEST_CLIENT.execute(WebDavConstant.HTTP_METHOD_PROPFIND, hasDealUrl);
            ThreadUtil.sleep(500);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (String.valueOf(statusCode).startsWith(WebDavConstant.CODE_40)) {
                StaticLog.warn("路径【{}】不存在", hasDealUrl);
                return false;
            } else {
                StaticLog.debug("路径【{}】存在", hasDealUrl);
                return WebDavConstant.FOUND_STATUS_CODE.equals(statusCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (null != httpResponse) {
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
        }
    }

    /**
     * 获取指定路径下的文件/目录列表
     * @author hubz
     * @date 2022/8/1 22:54
     *
     * @param basePath 根目录
     * @param path 指定路径
     * @return java.util.List<com.hubz.minimdmanage.common.utils.webdav.WebDavPathResponse> 指定路径下的文件/目录列表
     **/
    public static List<WebDavPathResponse> getWebDavPathInfo(String basePath, String path) {
        check(basePath);
        HttpResponse httpResponse = null;
        try {
            StaticLog.info("获取WebDav路径【{}/{}】下的文件/目录列表", basePath, path);
            // 检查路径是否存在
            Boolean pathExist = checkPathExist(basePath, path);
            if (!pathExist) {
                StaticLog.warn("【{}/{}】路径不存在", basePath, path);
                return Collections.emptyList();
            } else {
                StaticLog.debug("【{}/{}】路径存在", basePath, path);
                String encodePath = encodeFilePath(path);
                String url = StrUtil.format("{}/{}/{}", WEB_DAV_URL, basePath, encodePath);
                StaticLog.debug("获取路径【{}】下的文件/目录信息", dealUrl(url));
                httpResponse = HTTP_REQUEST_CLIENT.execute(WebDavConstant.HTTP_METHOD_PROPFIND, dealUrl(url));
                String responseBodyStr = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                return parseWebDavResponseXML(path, responseBodyStr);
            }
        } catch (Exception e) {
            StaticLog.error(e, "获取路径{}目录下的路径列表错误");
        } finally {
            if (null != httpResponse) {
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
        }
        return Collections.emptyList();
    }

    /**
     * 解析请求结果
     * @author hubz
     * @date 2022/8/1 23:22
     *
     * @param basePath 根目录
     * @param dirInfo 请求返回的数据
     * @return java.util.List<com.hubz.minimdmanage.common.utils.webdav.WebDavPathResponse>
     **/
    private static List<WebDavPathResponse> parseWebDavResponseXML(String basePath, String dirInfo) {
        dirInfo = dirInfo.replace("d:", "");
        // 校验数据
        List<WebDavPathResponse> result = new ArrayList<>();
        // 解析数据
        Document document;
        try {
            document = XmlUtil.parseXml(dirInfo);
        } catch (Exception e) {
            StaticLog.error(e, "数据解析失败：【{}】", dirInfo);
            return Collections.emptyList();
        }

        if (null != document) {
            Element documentElement = document.getDocumentElement();
            List<Element> responseList = XmlUtil.getElements(documentElement, "response");
            // 从第二个信息开始是当前目录的子级目录/文件的信息，第一个是当前目录的信息
            for (int i = 2; i <= CollectionUtil.size(responseList); i++) {
                String href = XmlUtil.getByXPath(StrUtil.format("//response[{}]/href", i), document, XPathConstants.STRING).toString();
                String displayName = XmlUtil.getByXPath(StrUtil.format("//response[{}]/propstat/prop/displayname", i), document, XPathConstants.STRING).toString();
                String lastModifiedStr = XmlUtil.getByXPath(StrUtil.format("//response[{}]/propstat/prop/getlastmodified", i), document, XPathConstants.STRING).toString();
                String contentType = XmlUtil.getByXPath(StrUtil.format("//response[{}]/propstat/prop/getcontenttype", i), document, XPathConstants.STRING).toString();
                href = href.replace("/dav/minimd", "");
                // 判断当前路径信息是否为父级路径
                if (StrUtil.equals(basePath, href) || StrUtil.equals(basePath, displayName)) {
                    continue;
                }
                result.add(WebDavPathResponse.builder()
                        .href(href)
                        .displayName(displayName)
                        .lastModifiedStr(lastModifiedStr)
                        .lastModified(DateUtil.parse(lastModifiedStr, DatePattern.HTTP_DATETIME_FORMAT).getTime())
                        .contentType(contentType)
                        .build());
            }
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * 处理URL
     * @author hubz
     * @date 2022/7/30 19:51
     *
     * @param url url
     * @return java.lang.String 处理后的url
     **/
    private static String dealUrl(String url) {
        return "https://" + dealPath(url);
    }

    /**
     * 处理路径
     * @author hubz
     * @date 2022/12/10 15:22
     *
     * @param path 路径
     * @return java.lang.String
     **/
    private static String dealPath(String path) {
        return path.replace("%2F", "/")
                .replace("https://", "")
                .replaceAll("%5C", "/")
                .replaceAll("//", "/")
                .replace("\\", "/")
                .replaceAll("\\\\", "/");
    }

    /**
     * 编码文件路径
     * @author hubz
     * @date 2022/12/10 15:21
     *
     * @param path 文件路径
     * @return java.lang.String 处理后的文件路径
     **/
    private static String encodeFilePath(String path) {
        return URLEncoder.encode(dealPath(path), StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("%2F", "/");
    }

}
