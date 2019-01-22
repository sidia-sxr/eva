# Eva
A pet experience in Augmented Reality (AR). It was built using [SXR SDK](http://www.samsungxr.com/) for Android platform.

## Main features
* Plane detection and point cloud to show the AR detected points to compose the plane.
* Share AR experience using Cloud Anchors and connect several devices using Bluetooth.
* Physics simulation working with AR.
* 3D models, animations and Android views in AR environment.
* Capture and share screen images.

## Build
After downloading its source code, you can use the commands below to build the application and generate a APK file.

The APK file will be put inside the `build` directory according to the build's mode (eg. `eva/app/build/outputs/apk/monoscopic`).

### Debug mode
```bash
./gradlew assembleMonoscopicDebug
```

### Release mode
```bash
./gradlew assembleMonoscopicRelease
```