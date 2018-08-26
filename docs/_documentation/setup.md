---
position: 3
title: Maven Project Setup
parameters:
  - name:
    content:
content_markdown: |-

  To create a Triton project, you must add the standard library
  to your projects dependencies as well as the Triton compiler plugin.
  
left_code_blocks:
  - code_block: |-
      <dependency>
          <groupId>com.github.bradleywood</groupId>
          <artifactId>triton-stdlib</artifactId>
          <version>1.0-SNAPSHOT</version>
      </dependency>
    title: Stdlib dependency
    language: xml

right_code_blocks:
  - code_block: |-
      <plugin>
          <groupId>com.github.bradleywood</groupId>
          <artifactId>triton-maven-plugin</artifactId>
          <version>1.0-SNAPSHOT</version>
          <executions>
              <execution>
                  <goals>
                      <goal>compile</goal>
                  </goals>
              </execution>
          </executions>
      </plugin>
    title: Triton Compiler Plugin
    language: xml
---