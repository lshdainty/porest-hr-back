[![](https://github.com/lshdainty/myHR/actions/workflows/ci-main.yml/badge.svg)](https://github.com/lshdainty/myHR/actions/workflows/ci-main.yml)

# myHR

사업장에 속한 근로자의 일정관리 및 휴가관리를 쉽게 관리하고자 만든 서비스입니다.   
Golang 사용자가 Java Spring Boot를 공부하고자 시작했으며   
기존 사용하던 Legacy 프로그램을 JPA, QueryDSL 등 실무에서 많이 사용중인 lib를 사용하여 개선하고자 합니다.

## 기능
### 📅 일정관리

구성원들의 근무 및 휴가 일정을 캘린더 상에서 확인할 수 있습니다.
 * 사용자가 등록한 일정을 캘린더 상에서 쉽게 확인할 수 있습니다.
 * 휴가뿐만 아니라 공통으로 사용하는 일정 (예비군, 교육) 등 빠르고 간편하게 등록할 수 있습니다.

### 🏖️ 휴가관리

직원들의 휴가 발생과 잔여 일수 관리 등을 체계적으로 관리할 수 있습니다.
 * 직원들은 캘린더 상에서 쉽게 휴가를 등록할 수 있습니다.
 * 전체 휴가 부여뿐만 아니라 직원별 수동적으로 휴가를 등록할 수 있습니다.
 * 직원들의 휴가 발생 내역 및 사용 내역을 손쉽게 관리할 수 있습니다.

### 📝 전자결재

보고가 필요한 근무의 경우 전자결재를 통해 관리가 가능합니다.
 * 야근, 휴일근무 등 연장 근무의 경우 사전 결재를 통해 관리가 가능합니다.
 * 사전 보고된 결재를 통해 추가 휴가를 쉽게 부여할 수 있습니다.

## Install
__Requirements__
 * Spring Boot v3.4.2
 * Open JDK 17
 * nodeJS ?
 * React

## Stacks
### Frontend
![JavaScript](https://img.shields.io/badge/JavaScript-000000?style=for-the-badge&logo=Javascript&logoColor=F7DF1E&logoSize=auto)
![React](https://img.shields.io/badge/React-000000?style=for-the-badge&logo=react&logoColor=61DAFB&logoSize=auto)
![npm](https://img.shields.io/badge/npm-CB3837?style=for-the-badge&logo=npm&logoColor=white&logoSize=auto)
![Node.js](https://img.shields.io/badge/Node.js-5FA04E?style=for-the-badge&logo=Node.js&logoColor=white&logoSize=auto)

### Backend
![OpenJDK](https://img.shields.io/badge/OpenJDK-000000?style=for-the-badge&logo=openJdk&logoColor=white&logoSize=auto)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white&logoSize=auto)
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white&logoSize=auto)

### Environment
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white&logoSize=auto)
![macOS](https://img.shields.io/badge/macOS-000000?style=for-the-badge&logo=macOS&logoColor=white&logoSize=auto)

### Development
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=Git&logoColor=white&logoSize=auto)
![Github](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=GitHub&logoColor=white&logoSize=auto)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ_IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=withe&logoSize=auto)
![vscode](https://img.shields.io/badge/vscode-000000.svg?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI4MDAiIGhlaWdodD0iODAwIiB2aWV3Qm94PSItMC41NSAwIDIzNS4xIDIzNS4xIj48cGF0aCBkPSJtODMuMyAxNDMuOS01OCA0NS4yTDAgMTc2LjVWNTguN0wyNS4yIDQ2bDU3LjYgNDUuM0wxNzQgMGw2MCAyMy45djE4Ni45bC01OS43IDI0LjMtOTEtOTEuMnptODguOSAxNS45Vjc1LjNsLTU0LjYgNDIuMyA1NC42IDQyLjJ6TTI3LjMgMTQ0LjYgNTYgMTE4LjUgMjcuMyA4OS45djU0Ljd6IiBzdHlsZT0iZmlsbDojMDE3OWNiIi8+PC9zdmc+&style=for-the-badge)
![Perplexity](https://img.shields.io/badge/Perplexity-1FB8CD?style=for-the-badge&logo=Perplexity&logoColor=white&logoSize=auto)
