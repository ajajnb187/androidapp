pluginManagement {
    // 插件仓库配置
    repositories {
        // 优先使用国内镜像，提高下载速度
        // 阿里云镜像
        maven {
            setUrl("https://maven.aliyun.com/repository/public")
            name = "AliYun-Public"
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/google")
            name = "AliYun-Google"
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/gradle-plugin")
            name = "AliYun-Gradle"
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/jcenter")
            name = "AliYun-JCenter"
        }
        // 腾讯云镜像
        maven {
            setUrl("https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
            name = "Tencent"
        }
        // 华为云镜像
        maven {
            setUrl("https://repo.huaweicloud.com/repository/maven")
            name = "HuaweiCloud"
        }
        // 网易云镜像
        maven {
            setUrl("https://mirrors.163.com/maven/repository/maven-public")
            name = "NetEase"
        }

        // 官方仓库放在后面作为备用
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 优先使用国内镜像，提高下载速度
        // 阿里云镜像
        maven {
            setUrl("https://maven.aliyun.com/repository/public")
            name = "AliYun-Public"
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/google")
            name = "AliYun-Google"
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/jcenter")
            name = "AliYun-JCenter"
        }
        // 腾讯云镜像
        maven {
            setUrl("https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
            name = "Tencent"
        }
        // 华为云镜像
        maven {
            setUrl("https://repo.huaweicloud.com/repository/maven")
            name = "HuaweiCloud"
        }
        // 网易云镜像
        maven {
            setUrl("https://mirrors.163.com/maven/repository/maven-public")
            name = "NetEase"
        }

        // 官方仓库放在后面作为备用
        google()
        mavenCentral()
    }
}
rootProject.name = "xinwenApp"
include(":app")
 