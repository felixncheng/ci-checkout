package com.tencent.bk.devops.git.core.util

import org.junit.Assert
import org.junit.Test

class GithubMirrorHelperTest {

    private val mirrorHost = "mirror.github.com"

    @Test
    fun shouldNotMirrorWhenMirrorHostIsNullOrEmpty() {
        Assert.assertFalse(
            GithubMirrorHelper.shouldMirror(
                repositoryUrl = "https://github.com/my-proj/my-repo.git",
                whiteProject = "my-proj/my-repo",
                githubMirrorHost = null
            )
        )
        Assert.assertFalse(
            GithubMirrorHelper.shouldMirror(
                repositoryUrl = "https://github.com/my-proj/my-repo.git",
                whiteProject = "my-proj/my-repo",
                githubMirrorHost = ""
            )
        )
    }

    @Test
    fun shouldNotMirrorWhenWhiteListIsNullOrBlank() {
        Assert.assertFalse(
            GithubMirrorHelper.shouldMirror(
                repositoryUrl = "https://github.com/my-proj/my-repo.git",
                whiteProject = null,
                githubMirrorHost = mirrorHost
            )
        )
        Assert.assertFalse(
            GithubMirrorHelper.shouldMirror(
                repositoryUrl = "https://github.com/my-proj/my-repo.git",
                whiteProject = "",
                githubMirrorHost = mirrorHost
            )
        )
        Assert.assertFalse(
            GithubMirrorHelper.shouldMirror(
                repositoryUrl = "https://github.com/my-proj/my-repo.git",
                whiteProject = "  ,  ",
                githubMirrorHost = mirrorHost
            )
        )
    }

    @Test
    fun shouldMirrorWhenGithubHttpRepoHitWhiteList() {
        Assert.assertTrue(
            GithubMirrorHelper.shouldMirror(
                repositoryUrl = "https://github.com/my-proj/my-repo.git",
                whiteProject = "my-proj/my-repo",
                githubMirrorHost = mirrorHost
            )
        )
        // 不带.git后缀
        Assert.assertTrue(
            GithubMirrorHelper.shouldMirror(
                repositoryUrl = "https://github.com/my-proj/my-repo",
                whiteProject = "my-proj/my-repo",
                githubMirrorHost = mirrorHost
            )
        )
        // 带认证信息
        Assert.assertTrue(
            GithubMirrorHelper.shouldMirror(
                repositoryUrl = "https://oauth2:xxx@github.com/my-proj/my-repo.git",
                whiteProject = "my-proj/my-repo",
                githubMirrorHost = mirrorHost
            )
        )
    }

    @Test
    fun shouldMirrorWhenGithubSshRepoHitWhiteList() {
        Assert.assertTrue(
            GithubMirrorHelper.shouldMirror(
                repositoryUrl = "git@github.com:my-proj/my-repo.git",
                whiteProject = "my-proj/my-repo",
                githubMirrorHost = mirrorHost
            )
        )
    }

    @Test
    fun shouldMirrorWhiteListSupportMultipleAndTrim() {
        Assert.assertTrue(
            GithubMirrorHelper.shouldMirror(
                repositoryUrl = "https://github.com/my-proj/my-repo.git",
                whiteProject = " other/repo , my-proj/my-repo , foo/bar ",
                githubMirrorHost = mirrorHost
            )
        )
    }

    @Test
    fun shouldNotMirrorWhenRepoNotInWhiteList() {
        Assert.assertFalse(
            GithubMirrorHelper.shouldMirror(
                repositoryUrl = "https://github.com/my-proj/my-repo.git",
                whiteProject = "other/repo",
                githubMirrorHost = mirrorHost
            )
        )
    }

    @Test
    fun shouldNotMirrorWhenHostIsNotGithub() {
        Assert.assertFalse(
            GithubMirrorHelper.shouldMirror(
                repositoryUrl = "https://git.example.com/my-proj/my-repo.git",
                whiteProject = "my-proj/my-repo",
                githubMirrorHost = mirrorHost
            )
        )
    }

    @Test
    fun shouldNotMirrorWhenRepositoryUrlIsInvalid() {
        Assert.assertFalse(
            GithubMirrorHelper.shouldMirror(
                repositoryUrl = "not-a-valid-url",
                whiteProject = "my-proj/my-repo",
                githubMirrorHost = mirrorHost
            )
        )
    }

    @Test
    fun getMirrorUrlFromHttpUrl() {
        Assert.assertEquals(
            "https://$mirrorHost/my-proj/my-repo.git",
            GithubMirrorHelper.getMirrorUrl("https://github.com/my-proj/my-repo.git", mirrorHost)
        )
    }

    @Test
    fun getMirrorUrlFromHttpUrlWithoutGitSuffix() {
        Assert.assertEquals(
            "https://$mirrorHost/my-proj/my-repo.git",
            GithubMirrorHelper.getMirrorUrl("https://github.com/my-proj/my-repo", mirrorHost)
        )
    }

    @Test
    fun getMirrorUrlFromHttpUrlWithAuth() {
        Assert.assertEquals(
            "https://$mirrorHost/my-proj/my-repo.git",
            GithubMirrorHelper.getMirrorUrl("https://oauth2:xxx@github.com/my-proj/my-repo.git", mirrorHost)
        )
    }

    @Test
    fun getMirrorUrlFromSshUrl() {
        Assert.assertEquals(
            "https://$mirrorHost/my-proj/my-repo.git",
            GithubMirrorHelper.getMirrorUrl("git@github.com:my-proj/my-repo.git", mirrorHost)
        )
    }
}
