package com.tencent.bk.devops.git.core.util

import com.tencent.bk.devops.git.core.constant.GitConstants.BK_CI_GIT_REPO_HEAD_COMMIT_SHORT_ID
import com.tencent.bk.devops.git.core.enums.ScmType
import com.tencent.bk.devops.git.core.pojo.api.CommitMaterial
import org.junit.Assert
import org.junit.Test

class EnvHelperTest {

    @Test
    fun addLogEnvWritesShortHeadCommitId() {
        EnvHelper.addLogEnv(
            projectName = "blueking/ci-checkout",
            commitMaterial = CommitMaterial(
                lastCommitId = null,
                newCommitId = "1234567890abcdef",
                newCommitComment = null,
                newCommitAuthor = null,
                newCommitCommitter = null,
                commitTimes = 1,
                commitIds = listOf("1234567890abcdef"),
                scmType = ScmType.GITHUB
            )
        )

        Assert.assertEquals(
            "12345678",
            EnvHelper.getEnvVariable(BK_CI_GIT_REPO_HEAD_COMMIT_SHORT_ID)
        )
    }

    @Test
    fun addLogEnvWritesBlankShortHeadCommitIdWhenHeadCommitIsMissing() {
        EnvHelper.addLogEnv(
            projectName = "blueking/ci-checkout",
            commitMaterial = CommitMaterial(
                lastCommitId = null,
                newCommitId = null,
                newCommitComment = null,
                newCommitAuthor = null,
                newCommitCommitter = null,
                commitTimes = 0,
                commitIds = emptyList(),
                scmType = ScmType.GITHUB
            )
        )

        Assert.assertEquals(
            "",
            EnvHelper.getEnvVariable(BK_CI_GIT_REPO_HEAD_COMMIT_SHORT_ID)
        )
    }
}
