My Contacts
=============
Simple project consisting in 2 activites that will list your contacts on Google+ after you authorize it on the first screen.

Assemble APK
-----------

    export ANDROID_HOME=<path_to_your_android_sdk>
    ./gradlew assembleRelease

The signed release apk will be created at app/build/outputs/apk/app-release.apk. Install it on the device using adb (adb install app/build/outputs/apk/app-release.apk) and check it out.

Signature
-----------

It is required from Google+ to register the signature SHA1 on Google Developers Console, so use the keystore found on this repository to test this application. The alias and password configuration can be found on build.gradle file
