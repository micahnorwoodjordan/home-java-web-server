# context

- 2023-08-22
- this is a small-scale Java web server built for the fun of seeing if I could do it.
- currently, this minimal web server lacks many common web server features, such as caching, security, SSL, etc. These are all todos.

## spin up web server

~~~bash
# attempting to compile this application without the proper JVM/JRE parameters will yield compilation errors
# this snippet is the underlying command sent to the terminal by building the application via VSCode
# until this application is dockerized, running this command will break on any other filesystem

cd /Users/micahnorwoodjordan/code/studying/java/web-server;
/usr/bin/env /opt/homebrew/Cellar/openjdk/20.0.1/libexec/openjdk.jdk/Contents/Home/bin/java --enable-preview -XX:+ShowCodeDetailsInExceptionMessages -cp /Users/micahnorwoodjordan/Library/Application\ Support/Code/User/workspaceStorage/2dcab1d7a6bc2def3c6ad2c21b5db376/redhat.java/jdt_ws/web-server_5e795fd0/bin src.App
~~~

## hit the web server

~~~bash
curl http://localhost:8080/testOne
~~~

## TODO

- dockerize
- cache responses
- use SSL
- optimize multithreading implementation
- selecetively rate limit clients
- add simple security features***

## visual

![Alt text](image.png)

## quick refs and resources

- initial implementation credit to Robert Finn: <https://rjlfinn.medium.com/creating-a-http-server-in-java-9b6af7f9b3cd>
- <https://www.freecodecamp.org/news/basic-html5-template-boilerplate-code-example/>
- <https://www.digitalocean.com/community/tutorials/java-read-file-line-by-line>
