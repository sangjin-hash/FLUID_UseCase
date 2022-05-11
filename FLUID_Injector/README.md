# FLUIDInjector
## Index
  - [Setting](#Setting)
  - [Decompile](#Decompile)
  
  
  
## Setting
#### 1. sign.sh 설정

```
#!/bin/bash
BUILDTOOLS=[Android Sdk 경로]
...
```

#### 2. MPlusInjector.java 설정

```java
private static String androidJar = System.getProperty("user.home") + "[Android Sdk 경로]";
```

#### 3. build

``` vim
./gradlew build
```

#### 4. run

``` vim
./gradlew run --args="./apks/testapp.apk"
```

#### 5. sign

``` vim
./sign.sh output/testapp.apk ./hmsl-key.jks "hmsl1234"
```

## Decompile
#### 1. apk / dex -> jar
apk 파일이나 dex 파일을 jar로 변환하기 위해 dex2jar tool을 이용해야 한다.

```Linux
cd ~/dex-tools-2.2-SNAPSHOT-2021-10-31/dex-tools-2.2-SNAPSHOT/
sudo chmod +x d2j_invoke.sh             //d2j 권한 허가
sudo sh d2j-dex2jar.sh -f [apk / dex path]
```

위의 명령어를 수행하면, 해당 디렉토리에 [apk이름]-dex2jar.jar 파일이 생성된 것을 확인할 수 있다.
![image](https://user-images.githubusercontent.com/77181865/149071642-37f54ffc-6f20-4da2-bea6-949b7773ff6f.png)
</br>
</br>

***이전 버전을 사용한다면 ```com.google.code.d2j.DexException : not support version``` 에러가 발생할 수 있으므로, 최신의 버전을 이용해야 한다.***

dex2jar github에서 최신화된 버전을 이용할 수 있고 링크는 다음과 같다. 
https://github.com/pxb1988/dex2jar/releases
</br>
</br>
#### 2. jar 파일 확인 
1번에서 나온 output을 java로 decompile해주는 jd-gui/jd-cli 툴이 있다. jd-gui는 윈도우에서 실행하기 편하고, jd-cli는 Ubuntu의 터미널에서 작업할 경우 이용하기 편리하다.
</br>
- jd-gui</br>
```http://java-decompiler.github.io/ 설치 -> window OS 선택 -> jd.gui.exe 실행 ```

- jd-cli</br>
```
cd jd-cli-1.2.0-dist
java -jar jd-cli.jar [1번의 output jar 경로] -od [해당 statement 수행 후 발생하는 output을 저장할 경로]
```

ex) 
![image](https://user-images.githubusercontent.com/77181865/149143116-d6c72086-b164-4e2f-9971-17eea6ec245e.png)
</br>
</br>

***jd-gui를 이용할 때, input을 넣은 상태에서 다른 input을 넣으면 적용이 되지 않는다. 따라서 jd-gui를 다시 실행한 뒤 기존의 input을 지우고 새로운 input을 넣어야 적용이 된다!***


