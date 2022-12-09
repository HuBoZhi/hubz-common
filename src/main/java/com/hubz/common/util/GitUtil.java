package com.hubz.common.util;

import cn.hutool.log.StaticLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;

/**
 * @author hubz
 * @date 2021/9/14 23:32
 **/
public final class GitUtil {
    /**
     * git分支
     */
    private static final String BASE_BRANCH = "master";
    /**
     * git用户名
     */
    private static final String USERNAME = "log_message@163.com";
    /**
     * git密码
     */
    private static final String PASSWORD = "yD3n88zFA2P6+*/-";
    /**
     * TAB的空格显示
     */
    private static final String TAB = "    ";
    /**
     * 认证
     */
    private static final CredentialsProvider CREDENTIAL =
            new UsernamePasswordCredentialsProvider(USERNAME, PASSWORD);
    /**
     * 远端目标
     */
    private static final String REMOTE = "origin";

    private GitUtil() {
    }

    /**
     * 克隆远程仓库到本地仓库
     * @param remoteRepoPath 远端仓库地址
     * @param localRepoPath 本地仓库路径
     *
     * @author hubz
     * @date 2021/9/15 22:35
     */
    private static void gitClone(String remoteRepoPath, String localRepoPath) {
        StaticLog.info("克隆远程仓库到本地仓库: 远端仓库地址【{}】 本地仓库路径【{}】", remoteRepoPath, localRepoPath);
        try {
            // 克隆代码库命令
            Git.cloneRepository()
                    //设置远程URI
                    .setURI(remoteRepoPath)
                    //设置clone下来的分支
                    .setBranch(BASE_BRANCH)
                    //设置本地仓库路径
                    .setDirectory(Paths.get(localRepoPath).toFile())
                    //设置权限验证
                    .setCredentialsProvider(CREDENTIAL)
                    .call();
            StaticLog.info(localRepoPath + " 仓库克隆成功!");
        } catch (Exception e) {
            StaticLog.error(String.format("仓库【%s】克隆失败", remoteRepoPath), e);
        }
    }

    /**
     * 读取已有仓库
     * @param remoteRepoPath 远端仓库地址
     * @param localRepoPath 本地仓库所在路径
     * @return org.eclipse.jgit.lib.Repository 仓库信息
     *
     * @author hubz
     * @date 2021/9/15 21:51
     */
    public synchronized static Repository getRepositoryFromDir(String remoteRepoPath, String localRepoPath) throws IOException {
        StaticLog.info("检查本地仓库：远端仓库地址【{}】 本地仓库路径【{}】", remoteRepoPath, localRepoPath);
        Path locationPath = Paths.get(localRepoPath);
        Path dotGitPath = Paths.get(localRepoPath, ".git");
        // 如果这两个路径任意不存在，则说明仓库没有拉取到本地
        if (Files.notExists(locationPath) || Files.notExists(dotGitPath)) {
            StaticLog.error("本地仓库【{}】不存在,开始从远端仓库克隆数据...", localRepoPath);
            gitClone(remoteRepoPath, localRepoPath);
        }
        File gitFile = dotGitPath.toFile();
        return new FileRepositoryBuilder().setGitDir(gitFile).build();
    }

    /**
     * 检查远端是否存在更新
     * @param remoteRepoPath 远端仓库地址
     * @param localRepoPath 本地仓库路径
     * @return java.lang.Boolean true 有更新，false 没有更新
     *
     * @author hubz
     * @date 2021/9/15 22:14
     */
    public static Boolean checkRemoteUpdate(String remoteRepoPath, String localRepoPath) {
        StaticLog.info("检查远端是否存在更新：远端仓库地址【{}】 本地仓库路径【{}】", remoteRepoPath, localRepoPath);
        try (Git git = new Git(getRepositoryFromDir(remoteRepoPath, localRepoPath))) {
            // fetch 不执行只查看,可以根据其中的updates列表的数量来确定是否发生了变化
            FetchResult fetchResult = git.fetch().setCredentialsProvider(CREDENTIAL)
                    .setDryRun(true).call();
            Collection<TrackingRefUpdate> trackingRefUpdates = fetchResult.getTrackingRefUpdates();
            // 大于0代表远端有更新
            boolean res = trackingRefUpdates.size() > 0;
            StaticLog.info("检查远端是否存在更新完成：【{}】", res ? "有更新" : "没有更新");
            return res;
        } catch (IOException | GitAPIException e) {
            StaticLog.error(String.format("文档仓库【%s】", remoteRepoPath), e);
            return false;
        }
    }

    /**
     * 拉取最新的文件到本地
     * @param remoteRepoPath 远端仓库地址
     * @param localRepoPath 本地仓库路径
     *
     * @author hubz
     * @date 2021/9/15 22:36
     */
    public static Boolean pullAll(String remoteRepoPath, String localRepoPath, Boolean deleteAble) {
        StaticLog.info("拉取最新的文件到本地：远端仓库地址【{}】 本地仓库路径【{}】", remoteRepoPath, localRepoPath);
        try (Git git = new Git(getRepositoryFromDir(remoteRepoPath, localRepoPath))) {
            git.pull().setCredentialsProvider(CREDENTIAL).call();
            StaticLog.info("拉取【{}】最新的文件到本地成功", remoteRepoPath);
            return true;
        } catch (CheckoutConflictException checkoutConflictException) {
            StaticLog.error("本地有未提交的更新，执行提交的操作", checkoutConflictException);
            // 本地有更新，提交更新
            commitAndPush(remoteRepoPath, localRepoPath, deleteAble);
            // 提交更修后需要重启系统
            return false;
        } catch (IOException | GitAPIException e) {

            StaticLog.error("拉取最新的文件到本地失败", e);
        }
        return false;
    }


    /**
     * 提交并推送本地发生改变的文件
     * @param remoteRepoPath 远端仓库地址
     * @param localRepoPath 本地仓库路径
     *
     * @author hubz
     * @date 2021/9/15 23:16
     */
    private static void commitAndPush(String remoteRepoPath, String localRepoPath, Boolean deleteAble) {
        StaticLog.info("提交并推送本地发生改变的文件：远端仓库地址【{}】 本地仓库路径【{}】", remoteRepoPath, localRepoPath);
        try (Git git = new Git(getRepositoryFromDir(remoteRepoPath, localRepoPath))) {
            Status status = git.status().call();
            // 将工作目录下的所有文件都添加进去
            git.add().setUpdate(true).addFilepattern(".").call();
            String commitMsg = getCommitMessage(status);
            if (StringUtils.isNotBlank(commitMsg)) {
                StaticLog.info("\n" + commitMsg);
            }
            boolean checkDelete = !deleteAble && (CollectionUtils.isNotEmpty(status.getRemoved()) || CollectionUtils.isNotEmpty(status.getMissing()));
            if (checkDelete) {
                StaticLog.error("未授权的删除操作,禁止执行");
            } else {
                // 提交信息
                git.commit().setMessage(commitMsg).call();
                // 推送数据
                git.push().setCredentialsProvider(CREDENTIAL).setRemote(REMOTE).call();
                StaticLog.info("提交并推送本地发生改变的文件完成");
            }
        } catch (JGitInternalException jGitInternalException) {
            StaticLog.error("index.lock已锁定，尝试进行删除该文件解锁", jGitInternalException);
            try {
                Files.deleteIfExists(Paths.get(localRepoPath, ".git", "index.lock"));
            } catch (IOException e) {
                StaticLog.error("index.lock已锁定，尝试删除解锁失败", e);
            }
        } catch (Exception e) {

            StaticLog.error("数据提交失败：", e);
        }
    }

    /**
     * 提交并推送本地发生改变的文件(允许删除操作)
     * @param remoteRepoPath 远端仓库地址
     * @param localRepoPath 本地仓库路径
     *
     * @author hubz
     * @date 2021/9/15 23:16
     */
    private static void commitAndPushWithDeleteAble(String remoteRepoPath, String localRepoPath) {
        StaticLog.info("提交并推送本地发生改变的文件：远端仓库地址【{}】 本地仓库路径【{}】", remoteRepoPath, localRepoPath);
        try (Git git = new Git(getRepositoryFromDir(remoteRepoPath, localRepoPath))) {
            Status status = git.status().call();
            // 将工作目录下的所有文件都添加进去
            git.add().setUpdate(true).addFilepattern(".").call();
            String commitMsg = getCommitMessage(status);
            if (StringUtils.isNotBlank(commitMsg)) {
                StaticLog.info("\n" + commitMsg);
            }
            // 提交信息
            git.commit().setMessage(commitMsg).call();
            // 推送数据
            git.push().setCredentialsProvider(CREDENTIAL).setRemote(REMOTE).call();
            StaticLog.info("提交并推送本地发生改变的文件（允许删除操作）完成");
        } catch (JGitInternalException jGitInternalException) {
            StaticLog.error("index.lock已锁定，尝试进行删除该文件解锁", jGitInternalException);
            try {
                Files.deleteIfExists(Paths.get(localRepoPath, ".git", "index.lock"));
            } catch (IOException e) {

                StaticLog.error("index.lock删除失败", e);
            }
        } catch (Exception e) {

            StaticLog.error("数据提交失败：", e);
        }
    }

    /**
     * 提交本地未被版本管理的文件
     * @param remoteRepoPath 远端仓库地址
     * @param localRepoPath 本地仓库路径
     *
     * @author hubz
     * @date 2021/9/22 22:16
     */
    private static void commitAndPushUntrackedFiles(String remoteRepoPath, String localRepoPath, Boolean deleteAble) {
        StaticLog.info("提交本地未被版本管理的文件：远端仓库地址【{}】 本地仓库路径【{}】", remoteRepoPath, localRepoPath);
        try (Git git = new Git(getRepositoryFromDir(remoteRepoPath, localRepoPath))) {
            Status status = git.status().call();
            AddCommand addCommand = git.add();
            // 获取未被版本管理的文件
            Set<String> untrackedFiles = status.getUntracked();
            if (CollectionUtils.isNotEmpty(untrackedFiles)) {
                StaticLog.info("未被版本管理的文件：" + JsonUtil.toString(untrackedFiles));
                // 将工作目录下未被版本管理的文件添加进去
                for (String untrackedFile : untrackedFiles) {
                    addCommand.addFilepattern(untrackedFile);
                }
                addCommand.call();
                String commitMsg = getCommitMessage(status);
                if (StringUtils.isNotBlank(commitMsg)) {
                    StaticLog.info("\n" + commitMsg);
                }
                // 禁止删除并且存在要删除的文件
                boolean checkDelete = !deleteAble && (CollectionUtils.isNotEmpty(status.getRemoved()) || CollectionUtils.isNotEmpty(status.getMissing()));
                if (checkDelete) {
                    StaticLog.error("未授权的删除操作,禁止执行");
                } else {
                    // 提交信息
                    git.commit().setMessage(commitMsg).call();
                    // 推送数据
                    git.push().setCredentialsProvider(CREDENTIAL).setRemote(REMOTE).call();
                    StaticLog.info("提交本地未被版本管理的文件完成");
                }
            }
        } catch (JGitInternalException jGitInternalException) {
            StaticLog.error("index.lock已锁定，尝试进行删除该文件解锁", jGitInternalException);
            try {
                Files.deleteIfExists(Paths.get(localRepoPath, ".git", "index.lock"));
            } catch (IOException e) {
                StaticLog.error("index.lock删除失败", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("未被版本管理的数据提交失败【%s】【%s】：", remoteRepoPath, localRepoPath), e);
        }
    }

    /**
     * 提交本地仓库中的所有文件
     * @param remoteRepoPath 远端仓库地址
     * @param localRepoPath 本地仓库路径
     *
     * @author hubz
     * @date 2021/9/22 22:17
     */
    public static void commitAndPushAll(String remoteRepoPath, String localRepoPath) {
        StaticLog.info("提交本地仓库中的所有文件：远端仓库地址【{}】 本地仓库路径【{}】", remoteRepoPath, localRepoPath);
        commitAndPush(remoteRepoPath, localRepoPath, false);
        commitAndPushUntrackedFiles(remoteRepoPath, localRepoPath, false);
        StaticLog.info("提交本地仓库中的所有文件完成");
    }

    /**
     * 提交本地仓库中的所有文件
     * @param remoteRepoPath 远端仓库地址
     * @param localRepoPath 本地仓库路径
     *
     * @author hubz
     * @date 2021/9/22 22:17
     */
    public static void commitAndPushAllWithDeleteAble(String remoteRepoPath, String localRepoPath) {
        StaticLog.info("提交本地仓库中的所有文件：远端仓库地址【{}】 本地仓库路径【{}】", remoteRepoPath, localRepoPath);
        commitAndPush(remoteRepoPath, localRepoPath, true);
        commitAndPushUntrackedFiles(remoteRepoPath, localRepoPath, true);
        StaticLog.info("提交本地仓库中的所有文件完成");
    }

    /**
     * 获取提交信息
     * @param status 状态
     * @return java.lang.String
     *
     * @author hubz
     * @date 2021/9/22 22:10
     */
    private static String getCommitMessage(Status status) {
        StringBuilder message = new StringBuilder();
        if (CollectionUtils.isNotEmpty(status.getRemoved()) || CollectionUtils.isNotEmpty(status.getMissing())) {
            message.append("======>删除的文件\n");
            status.getRemoved().forEach(removedFile -> message.append(TAB)
                    .append(String.format("删除【%s】文件", removedFile))
                    .append("\n"));
            status.getMissing().forEach(removedFile -> message.append(TAB)
                    .append(String.format("删除【%s】文件", removedFile))
                    .append("\n"));
        }
        // 因为不会有未被版本管理的文件，如果有一定是此次新增
        if (CollectionUtils.isNotEmpty(status.getAdded()) || CollectionUtils.isNotEmpty(status.getUntracked())) {
            message.append("======>新增的文件\n");
            status.getAdded().forEach(addedFile -> message.append(TAB)
                    .append(String.format("新增【%s】文件", addedFile))
                    .append("\n"));
            status.getUntracked().forEach(addedFile -> message.append(TAB)
                    .append(String.format("新增【%s】文件", addedFile))
                    .append("\n"));
        }
        if (CollectionUtils.isNotEmpty(status.getChanged()) || CollectionUtils.isNotEmpty(status.getModified())) {
            message.append("======>更新的文件\n");
            status.getChanged().forEach(changedFile -> message.append(TAB)
                    .append(String.format("更新【%s】文件", changedFile))
                    .append("\n"));
            status.getModified().forEach(modifiedFile -> message.append(TAB)
                    .append(String.format("更新【%s】文件", modifiedFile))
                    .append("\n"));
        }
        return message.toString();
    }

    /**
     * 使用打TAG的方式每日备份数据
     * @param remoteRepoPath 远端仓库地址
     * @param localRepoPath 本地仓库路径
     *
     * @author hubz
     * @date 2021/10/6 10:05
     */
    public static void createTag(String remoteRepoPath, String localRepoPath) {
        StaticLog.info("使用打TAG的方式每日备份数据：远端仓库地址【{}】 本地仓库路径【{}】", remoteRepoPath, localRepoPath);
        try (Git git = new Git(getRepositoryFromDir(remoteRepoPath, localRepoPath))) {
            git.tag()
                    .setName(TimeUtils.getCurrentFormatTimeString().replaceAll("[-,:,\\s]", ""))
                    .setMessage(TimeUtils.getCurrentFormatTimeString() + "备份")
                    .call();
            git.push().setCredentialsProvider(CREDENTIAL).setPushTags().call();
            StaticLog.info("使用打TAG的方式每日备份数据完成");
        } catch (JGitInternalException jGitInternalException) {
            StaticLog.error("index.lock已锁定，尝试进行删除该文件解锁", jGitInternalException);
            try {
                Files.deleteIfExists(Paths.get(localRepoPath, ".git", "index.lock"));
            } catch (IOException e) {
                StaticLog.error("index.lock删除失败", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("创建TAG失败【%s】【%s】：", remoteRepoPath, localRepoPath), e);
        }
    }
}
