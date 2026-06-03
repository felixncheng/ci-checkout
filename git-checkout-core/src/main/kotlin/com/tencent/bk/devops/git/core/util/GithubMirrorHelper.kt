package com.tencent.bk.devops.git.core.util

import com.tencent.bk.devops.git.core.constant.GitConstants
import com.tencent.bk.devops.git.core.exception.ParamInvalidException
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
     */
    fun shouldMirror(repositoryUrl: String, whiteProject: String?): Boolean {
        val whiteProjectList = whiteProject?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
        if (whiteProjectList.isNullOrEmpty()) {
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
    fun getMirrorUrl(repositoryUrl: String): String {
        val serverInfo = GitUtil.getServerInfo(repositoryUrl)
        val repositoryName = serverInfo.repositoryName
        val mirrorUrl = "https://${GitConstants.GITHUB_MIRROR_HOST}/$repositoryName.git"
        logger.info("rewrite github url to mirror url [$mirrorUrl]")
        return mirrorUrl
    }
}
