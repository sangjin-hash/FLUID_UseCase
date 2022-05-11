# FLUID_UseCase

## Index
  - [Abstract](#Abstract)
  - [System_Design](#System_Design)
  - [Module_Overview](#Module_Overview)
  - [Module_Description](#Module_Description)
  - [Demo](#Demo)
  - [Evaluation](#Evaluation)
  - [Conclusion](#Conclusion)
</br>
</br>

## Abstract
최근 디스플레이 기술의 비약적인 발전으로, 사용자가 여러 기기들의 서피스를 협력적으로 사용하여 하나의 앱을 사용하도록 하는 다중 서피스 상호작용에 많은 관심이 모이고 있다. 이러한 패러다임을 지원하기 위해 다중 서피스 환경에 특화된 모바일 운영체제를 제시하는 연구들이 많이 이루어졌으나, 대부분 그 기술 적용 가능성이 낮다는 단점을 지닌다. **이에 본 연구에서는 싱글 서피스를 위해 만들어진 기존 앱을 멀티 서피스 앱으로 자동 변환해 주는 프레임워크를 설계한다.** 제안한 프레임워크를 Google Pixel 5, Pixel 4a 기기에서 개발하였으며, 싱글 서피스를 위해 만들어진 갤러리 앱을 멀티 서피스 앱으로 효과적으로 변환할 수 있음을 보였다.
</br>
</br>

## System_Design
#### 제안된 프레임워크는 크게 UI 분산 인터페이스, 앱 변환 도구, UI 분산 서비스, Proxy 앱과 같은 네 가지 컴포넌트로 구성된다.
</br>

![image](https://user-images.githubusercontent.com/77181865/167851621-3cb893c1-e8d3-457c-9079-62ee7b0dddb5.png)
</br>

#### - 앱 변환 도구
- 기존 앱의 APK 파일을 입력으로 받게 되며, APK 파일에 UI 분산 인터페이스를 삽입하는 역할을 한다. 이후 최종적으로 UI 분산 인터페이스가 삽입된 새로운 APK 파일을 결과물로 생성한다. 인터페이스 삽입은 크게 **인터페이스 정의 코드**와 **호출 코드**로 나뉘어진다. 인터페이스 호출 코드는 사용자가 UI 요소를 선택하는 지점과 앱 로직이 UI 요소를 업데이트하기 위해 로컬함수를 호출하는 지점에 삽입이 된다. 이는 **Soot 라이브러리**를 이용하였는데, 코드 삽입의 과정은 다음과 같다. 

      (1) APK 파일은 Dalvik bytecode(.dex)로 컴파일 되어 있다.
      (2) Soot는 Dexpler를 이용하여 Dalvik bytecode를 Jimple bodies로 변환한다.
      (3) 전체 프로그램을 transform, optimize, annotate를 하는 Whole Packs를 실행한다.
      (4) 그 후 Jimple Transformation Pack이 각 Jimple body에서 실행된다.(이때 코드 수정이 일어난다)
      (5) Soot는 Jimple bodies를 Baf(a low-level intermediate representation)로 변환한다.
      (6) Dexpler를 이용해 모든 코드를 APK로 컴파일한다.


#### - UI 분산 인터페이스
- UI 분산 인터페이스는 앱 변환 도구를 통해 기존 앱에 삽입될 컴포넌트로, **타겟 UI 요소로부터 렌더링을 위한 그래픽 상태 정보를 추출하여 UI 분산 서비스에 전달하는 역할**을 한다. 이러한 동작은 사용자가 분산시킬 UI 요소를 최초로 선택하거나, 분산된 UI 요소에 대한 업데이트가 발생할 경우 수행된다. 이를 위해, UI 분산 인터페이스는 내부적으로 **사용자가 분산시킬 UI 요소를 선택할 수 있도록 하는 사용자 인터페이스**와 **UI 분산 서비스와 IPC 통신할 수 있도록 하는 통신 인터페이스를 제공**한다.

#### - UI 분산 서비스
- UI 분산 서비스는 별도의 서비스 프로세스로 동작하는 컴포넌트로, 앱으로부터 전달받은 그래픽 상태 정보를 **네트워크 통신을 통해 외부 기기로 전달함**으로써, UI 요소들을 실질적으로 분산시키는 역할을 한다. 해당 서비스는 앱 형태로 제공되기 때문에 사용자가 앱 마켓을 통해 쉽게 기기에 설치할 수 있다.

#### - Proxy
- 프록시 앱은 외부 기기에 설치되는 컴포넌트로, UI 분산 서비스로부터 전달받은 그래픽 상태 정보를 이용하여 **분산된 UI 요소를 재생성**하고, 이를 **외부 기기 화면에 렌더링**하는 역할을 한다. 또한, 앱 로직에 의해 일부 UI 요소가 업데이트되는 경우, UI 분산 서비스로부터 최신의 그래픽 상태 정보를 받아서 분산된 UI 요소도 마찬가지로 업데이트한다.
</br>
</br>

## Module_Overview
| Module | Details|
| ------ | ------ |
| FLUID_TargetApp | 기존 앱 |
| FLUID_INPUT | 기존 앱 apk |
| FLUID_Injector | Soot 라이브러리를 이용한 코드 삽입 모듈 |
| FLUID_Output | 코드 삽입이 된 apk |
| FLUID_Lib | IPC 통신을 위한 인터페이스 |
| FLUID_Manager | UI 분산 서비스 |
| FLUID_Guest | 프록시 앱|
| Time_Comp | 시간 측정을 위한 NTP 모듈 |
</br>
</br>

## Module_Description

### FLUID_Manager : aidl 지원을 위한 Android Service
##### - aidl method의 구현
* aidl method의 parameter로 받은 정보를 Server Thread의 handler로 토스
##### - Server Thread의 구현
* handler로 들어온 data를 guest device로 socket을 통해 전송

</br>

### FLUID_Lib : FLUID_Injector의 code 삽입을 간단화 하기 위한 Add On
#### - Activity나 Service가 아닌 단순 method들의 집합
* aidl 기능 삽입을 위한 Field와 ServiceConnection 구현
* aidl method 호출을 위한 Method 구현

</br>

### FLUID_Guest : Guest Device에서 분산된 UI를 받아 화면에 보여는 wrapper app
#### - Client Thread
* Host Device에 socket으로 연결 & 들어오는 패킷을 Worker Thread에 전달
#### - Worker Thread
* Client Thread로 부터 전달된 data를 기준으로 화면에 UI 그림

</br>

### FLUID_Injector : apk 파일에 code를 삽입하는 모듈
#### - AndroidUtil
* android 지원을 위한 method 구현
#### - InstrumentUtil
* soot의 setup과 삽입을 위한 method 구현
#### - FluidInjector
* 삽입 프로그램의 main함수 구현
#### - RPCInjector
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

## Evaluation
<img src="https://user-images.githubusercontent.com/17938197/167874526-11ea72f5-cc31-47c5-88f3-eece5e14c01b.PNG" width="400" height="200"/> <img src="https://user-images.githubusercontent.com/17938197/167872506-9eac2ef4-1118-4190-b235-9b80c1c48240.png" width="200" height="200"/> <img src="https://user-images.githubusercontent.com/17938197/167874530-cf3d0352-f6ad-42fa-bbe4-551520f77955.PNG" width="400" height="200"/> 
* UI 응답 시간(왼쪽)과 분산 소요 시간 (오른쪽)
</br>
</br>

## Conclusion
### 현황
* Host to Guest 단방향 통신 완료
* TextView 기반 UI, ImageView 분산 완료

### 추후 방향
* Host-Guest 양방향 통신 구현
* Web UI 도입을 통한 Cross Platform 지원
