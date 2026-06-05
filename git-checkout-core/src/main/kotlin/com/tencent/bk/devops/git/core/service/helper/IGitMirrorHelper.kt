package com.tencent.bk.devops.git.core.service.helper

import com.tencent.bk.devops.git.core.pojo.GitSourceSettings

/**
 * 镜像助手
 * 支持自定义镜像地址
 * */
interface IGitMirrorHelper {
    /**
     * 根据当前配置，动态生成镜像地址
     * */
    fun getMirrorUrl(settings: GitSourceSettings): String?
}