package com.tencent.bk.devops.git.core.util

import com.tencent.bk.devops.git.core.constant.GitConstants
import com.tencent.bk.devops.git.core.exception.ParamInvalidException
import com.tencent.bk.devops.git.core.service.GitCommandManager
import org.slf4j.LoggerFactory

/**
 * github镜像白名单工具类
 *
 */
object GithubMirrorHelper {

    private val logger = LoggerFactory.getLogger(GithubMirrorHelper::class.java)

    /**
     * 是否需要走github镜像
     * 1. 仓库host为github.com
     * 2. 仓库名(owner/repo)在白名单内
     * 3. 设置了镜像源
     */
    fun shouldMirror(repositoryUrl: String, whiteProject: String?, githubMirrorHost: String?): Boolean {
        logger.info(
            "check if should mirror for repositoryUrl[$repositoryUrl], " +
                    "whiteProject[$whiteProject] , githubMirrorHost[$githubMirrorHost]"
        )
        val whiteProjectList = whiteProject?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
        if (githubMirrorHost.isNullOrEmpty() || whiteProjectList.isNullOrEmpty()) {
            return false
        }
        val repositoryName = resolveGithubRepositoryName(repositoryUrl)
        val mirror = repositoryName != null && whiteProjectList.contains(repositoryName)
        if (mirror) {
            logger.info("github project [$repositoryName] hit mirror white list")
        }
        return mirror
    }

    /**
     * 解析github仓库名(owner/repo),非github域名或解析失败返回null
     */
    private fun resolveGithubRepositoryName(repositoryUrl: String): String? {
        val serverInfo = try {
            GitUtil.getServerInfo(repositoryUrl)
        } catch (e: ParamInvalidException) {
            logger.debug("fail to parse repo url for github mirror, repositoryUrl[$repositoryUrl]")
            return null
        }
        logger.info("project [${serverInfo.repositoryName}] host name [${serverInfo.hostName}]")
        // 支持https/ssh,非github域名不镜像
        return if (serverInfo.hostName.contains(GitConstants.GITHUB_HOST)) {
            serverInfo.repositoryName
        } else {
            null
        }
    }

    /**
     * 将github.com仓库地址替换为镜像地址,统一产出https形态
     */
    fun getMirrorUrl(repositoryUrl: String, mirrorHost: String): String {
        val serverInfo = GitUtil.getServerInfo(repositoryUrl)
        val repositoryName = serverInfo.repositoryName
        val mirrorUrl = "https://$mirrorHost/$repositoryName.git"
        logger.info("rewrite github url to mirror url [$mirrorUrl]")
        return mirrorUrl
    }

    /**
     * 镜像优先执行,失败回源(github)
     * 适用于git fetch/git lfs pull等依赖origin地址的操作
     * 1. 未配置镜像源时,直接执行action
     * 2. 配置了镜像源时,临时将origin指向镜像源后执行action,执行完成后还原origin
     * 3. 镜像源执行失败时,还原origin并回源(github)重新执行action
     */
    fun runWithMirror(git: GitCommandManager, mirrorFetchUrl: String?, action: () -> Unit) {
        if (mirrorFetchUrl.isNullOrBlank()) {
            action()
            return
        }
        val originUrl = git.tryGetFetchUrl()
        var mirrorSuccess = false
        try {
            logger.info("execute from mirror $mirrorFetchUrl")
            git.remoteSetUrl(remoteName = GitConstants.ORIGIN_REMOTE_NAME, remoteUrl = mirrorFetchUrl)
            action()
            mirrorSuccess = true
        } catch (ignore: Exception) {
            logger.warn(
                "failed to execute from mirror, fallback to github directly: " +
                        SensitiveLineParser.onParseLine(ignore.message ?: "")
            )
        } finally {
            // 还原origin为github,保证镜像执行成功后续阶段以及降级均走github
            git.remoteSetUrl(remoteName = GitConstants.ORIGIN_REMOTE_NAME, remoteUrl = originUrl)
        }
        // 镜像执行失败,回源重新执行
        if (!mirrorSuccess) {
            action()
        }
    }
}
