# android-calculator

## enviroment
- [macOS 10.14.6](https://www.apple.com/tw/macos/mojave/)
- [Android Studio 3.5](https://developer.android.com/studio)
- Java jdk 1.8.0_152, Kotlin 1.3.31
- emulator Pixel_2_API_29

## for Android Emulator to connect Internet
```shell
# emulator path
$ cd ~/Library/Android/sdk/emulator/

# show your emulator list
$ ./emulator -list-avds

# set dns and start your emulator
$ ./emulator -avd Pixel_2_API_29 -dns-server 8.8.8.8,8.8.8.4
```