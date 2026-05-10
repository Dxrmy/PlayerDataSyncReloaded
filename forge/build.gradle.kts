plugins {
    id("net.minecraftforge.gradle") version "6.0.+"
}

version = "26.5.5"
group = "de.playerdatasync"

minecraft {
    mappings channel: 'official', version: '1.20.1'
}

dependencies {
    minecraft 'net.minecraftforge:forge:1.20.1-47.2.0'
    
    implementation project(':api')
    implementation project(':common')
}
