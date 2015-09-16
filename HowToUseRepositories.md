# Zero configuration (recommended) #

Our public repository is synchronized with the maven central repository (http://repo1.maven.org/maven2/com/agilejava/docbkx/) so you should have no extra configuration in your pom.xml (or settings.xml) to declare our repository.


# Stable releases #

Since 2.0.15 version, releases are published into oss sonatype repository. It is already synched with maven central repository.

However if you really need our public previous repository you can use the following statement:

```
<pluginRepositories>
  <pluginRepository>
    <id>docbkx.snapshots</id>
    <name>Maven Plugin Snapshots</name>
    <url>http://docbkx-tools.sourceforge.net/repository/</url>
    <releases>
      <enabled>true</enabled>
    </releases>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
  </pluginRepository>
</pluginRepositories>
```

# Snapshot releases #

Before making a public stable release with a number of new functionalities or bug corrections we often make intermediate unstable releases. In the maven way of life these unstable releases are called snapshots (you will notice a **-SNAPSHOT** appended after the version numbers).

Snapshot release artifacts are not accessible through the Maven Central Repository thus you will need to modify your maven configuration (pom.xml or settings.xml)

Since 2.0.15 snapshot releases are published into oss sonatype repository, you can use the following statement:

```
<pluginRepositories>
  <pluginRepository>
    <id>sonatype.snapshots</id>
    <name>Maven Plugin Snapshots</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </pluginRepository>
</pluginRepositories>
```

However if you need our previous repository, you can use the following statement:

```
<pluginRepositories>
  <pluginRepository>
    <id>docbkx.snapshots</id>
    <name>Maven Plugin Snapshots</name>
    <url>http://docbkx-tools.sourceforge.net/snapshots/</url>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </pluginRepository>
</pluginRepositories>
```

# Need more information? #

Just have a look at the [dedicated](http://maven.apache.org/guides/development/guide-testing-development-plugins.html) maven site page.