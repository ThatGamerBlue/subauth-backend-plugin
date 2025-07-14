# SubAuth Backend Server Plugin

Building
---
Requires Java 21, simply run `./gradlew build`  
Resulting plugin jar is in `build/libs/subauth-backend-plugin-version-all.jar`

Configuration
---
The config file is very simple, only needs the address of the [backend service](https://github.com/ThatGamerBlue/subauth-server), and the key used to authenticate communication between it and this plugin. Make sure the key is the same as the one in the `env.json` for the backend service.