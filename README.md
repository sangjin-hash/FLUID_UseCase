# FLUID_UseCase
</br>
</br>
</br>

## Module_Description
</br>

### FLUID_Manager
#### aidl 지원을 위한 Android Service
##### aidl method의 구현
* aidl method의 parameter로 받은 정보를 Server Thread의 handler로 토스
##### Server Thread의 구현
* handler로 들어온 data를 guest device로 socket을 통해 전송

</br>

### FLUID_Lib
#### FLUID_Injector의 code 삽입을 간단화 하기 위한 Add On
#### Activity나 Service가 아닌 단순 method들의 집합
* aidl 기능 삽입을 위한 Field와 ServiceConnection 구현
* aidl method 호출을 위한 Method 구현

</br>

### FLUID_Guest
#### Guest Device에서 분산된 UI를 받아 화면에 보여는 wrapper app
##### Client Thread
* Host Device에 socket으로 연결 & 들어오는 패킷을 Worker Thread에 전달
##### Worker Thread
* Client Thread로 부터 전달된 data를 기준으로 화면에 UI 그림

</br>

### FLUID_Injector
#### apk 파일에 code를 삽입하는 모듈
##### AndroidUtil
* android 지원을 위한 method 구현
##### InstrumentUtil
* soot의 setup과 삽입을 위한 method 구현
##### FluidInjector
* 삽입 프로그램의 main함수 구현
##### RPCInjector
* aidl method 삽입
* package analasys를 위한 method 구현

</br>

### Time_Comp
* adb 명령어로 각 device의 local time을 불러와 저장
* 저장된 time을 비교해 두 기기의 local time의 차이를 출력
</br>
</br>
</br>

## Demo

<img src="https://user-images.githubusercontent.com/17938197/167871370-87368ff7-57ba-454c-a9b3-b13aa7c4b703.png" width="400" height="300"/> <img src="https://user-images.githubusercontent.com/17938197/167872506-9eac2ef4-1118-4190-b235-9b80c1c48240.png" width="200" height="300"/> <img src="https://user-images.githubusercontent.com/17938197/167871485-77d6f0cc-0f39-45b7-b5d9-d6dd7ffd6571.png" width="400" height="300"/>

* 이미지의 왼쪽은 Host Device, 오른쪽은 Guest Device 이다.
* 왼쪽 이미지의 경우 모든 UI를 분산한 결과, 오른쪽 이미지는 이미지 UI만 분산한 결과이다.
</br>
</br>
</br>

## Evaluation
<img src="https://user-images.githubusercontent.com/17938197/167874526-11ea72f5-cc31-47c5-88f3-eece5e14c01b.PNG" width="400" height="200"/> <img src="https://user-images.githubusercontent.com/17938197/167872506-9eac2ef4-1118-4190-b235-9b80c1c48240.png" width="200" height="200"/> <img src="https://user-images.githubusercontent.com/17938197/167874530-cf3d0352-f6ad-42fa-bbe4-551520f77955.PNG" width="400" height="200"/> 
* UI 분산 소요 시간 (왼쪽) 과 UI 응답 시간 (오른쪽)
</br>
</br>
</br>

## Conclusion
### 현황
* Host to Guest 단방향 통신 완료
* TextView 기반 UI, ImageView 분산 완료


### 추후 방향
* Host-Guest 양방향 통신 구현
* Web UI 도입을 통한 Cross Platform 지원
