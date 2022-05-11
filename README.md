# FLUID_UseCase


## Module Description
</br>

### FLUID_Manager
#### aidl 지원을 위한 Android Service
#### aidl method의 구현
* aidl method의 parameter로 받은 정보를 Server Thread의 handler로 토스
#### Server Thread의 구현
* handler로 들어온 data를 guest device로 socket을 통해 전송

### FLUID_Lib
#### FLUID_Injector의 code 삽입을 간단화 하기 위한 Add On
#### Activity나 Service가 아닌 단순 method들의 집합
* aidl 기능 삽입을 위한 Field와 ServiceConnection 구현
* aidl method 호출을 위한 Method 구현

### FLUID_Injector
#### AndroidUtil
* android 지원을 위한 method 구현
#### InstrumentUtil
* soot의 setup과 삽입을 위한 method 구현
#### FluidInjector
* 삽입 프로그램의 main함수 구현
#### RPCInjector
* aidl method 삽입
* package analasys를 위한 method 구현

### Time_Comp
* adb 명령어로 각 device의 local time을 불러와 저장
* 저장된 time을 비교해 두 기기의 local time의 차이를 출력

### FLUID_Guest
#### Guest Device에서 분산된 UI를 구
