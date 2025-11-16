plugins {
    id("fabric-loom") version "1.12-SNAPSHOT"
}

base {
    archivesName = properties["archives_base_name"] as String
    version = properties["mod_version"] as String
    group = properties["maven_group"] as String
}

repositories {
    mavenCentral()
    maven {
        name = "meteor-maven"
        url = uri("https://maven.meteordev.org/releases")
    }
    maven {
        name = "meteor-maven-snapshots"
        url = uri("https://maven.meteordev.org/snapshots")
    }
    flatDir {
        dirs("libs")
    }
}

dependencies {
    // Fabric
    minecraft("com.mojang:minecraft:${properties["minecraft_version"] as String}")
    mappings("net.fabricmc:yarn:${properties["yarn_mappings"] as String}:v2")
    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"] as String}")

    // Meteor - using local JAR for consistent version
    modImplementation(files("libs/meteor-client-${properties["meteor_version"] as String}.jar"))

    // Compile-time access to Orbit event bus (provided by Meteor at runtime)
    compileOnly("meteordevelopment:orbit:0.2.4")

    // NanoHTTPD for HTTP server and WebSocket support
    modImplementation("org.nanohttpd:nanohttpd:2.3.1")!!.let { include(it) }
    modImplementation("org.nanohttpd:nanohttpd-websocket:2.3.1")!!.let { include(it) }

    // JSON serialization for WebSocket messages
    modImplementation("com.google.code.gson:gson:2.11.0")!!.let { include(it) }

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

tasks {
    processResources {
        val propertyMap = mapOf(
            "version" to project.version,
            "mc_version" to project.property("minecraft_version"),
        )

        inputs.properties(propertyMap)

        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(propertyMap)
        }
    }

    jar {
        inputs.property("archivesName", project.base.archivesName.get())

        from("LICENSE") {
            rename { "${it}_${inputs.properties["archivesName"]}" }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 21
        options.compilerArgs.add("-Xlint:deprecation")
        options.compilerArgs.add("-Xlint:unchecked")
    }

    test {
        useJUnitPlatform()
    }

    // Build WebUI with npm
    register<Exec>("buildWebUI") {
        group = "build"
        description = "Build the Vue.js WebUI"

        val webuiDir = file("webui")
        val npmCommand = if (System.getProperty("os.name").lowercase().contains("windows")) "npm.cmd" else "npm"

        workingDir = webuiDir
        commandLine(npmCommand, "run", "build")

        // Install dependencies first if node_modules doesn't exist
        doFirst {
            if (!file("webui/node_modules").exists()) {
                exec {
                    workingDir = webuiDir
                    commandLine(npmCommand, "install")
                }
            }
        }
    }

    // Copy built WebUI to resources
    register<Copy>("copyWebUI") {
        group = "build"
        description = "Copy built WebUI files to resources"
        dependsOn("buildWebUI")

        doFirst {
            project.delete("src/main/resources/webui")
        }

        from("webui/dist")
        into("src/main/resources/webui")
    }

    // Make processResources depend on copyWebUI
    processResources {
        dependsOn("copyWebUI")
    }

    // Clean generated WebUI resources
    clean {
        delete("src/main/resources/webui")
    }
}
