## 支持Java版本

* java-11



## 该项目引入的依赖

```xml
<properties>
    <!-- 依赖版本 -->
    <lombok.version>1.18.24</lombok.version>
    <guava.version>31.1-jre</guava.version>
    <hutool.version>5.7.21</hutool.version>
    <httpclient.version>4.5.13</httpclient.version>
    <commons-lang3.version>3.12.0</commons-lang3.version>
    <commons-collections4.version>4.4</commons-collections4.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
        <optional>true</optional>
    </dependency>

    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>${guava.version}</version>
    </dependency>

    <!-- URL 请求工具 -->
    <dependency>
        <groupId>org.apache.httpcomponents</groupId>
        <artifactId>httpclient</artifactId>
        <version>${httpclient.version}</version>
    </dependency>

    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
        <version>${commons-lang3.version}</version>
    </dependency>

    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-collections4</artifactId>
        <version>${commons-collections4.version}</version>
    </dependency>

    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>${hutool.version}</version>
    </dependency>
</dependencies>
```



## 使用方式

**Step 1.** Add the JitPack repository to your build file

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://www.jitpack.io</url>
    </repository>
</repositories>
```

**Step 2.** Add the dependency

```xml
<dependency>
    <groupId>com.github.HuBoZhi</groupId>
    <artifactId>hubz-common</artifactId>
    <version>${hubz.common.version}</version>
</dependency>
```

