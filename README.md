# Pi-Cropper 
Android library, which helps to pick images from a device and crops them

[![](https://jitpack.io/v/VladYatsenko/Pi-Cropper.svg)](https://jitpack.io/#VladYatsenko/Pi-Cropper)
# Preview 
<img src="media/Screenshot_20220706-154832.jpg" alt="" width="250"/> <img src="media/Screenshot_20220706-154855.jpg" alt="" width="250"/> <img src="media/Screenshot_20220706-154905.jpg" alt="" width="250"/>

# Installation

Include the library to your project

In `settings.gradle` file:
```
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```
In app `build.gradle` file:
``` 
dependencies {
    implementation 'com.github.VladYatsenko:pi-cropper:X.X' 
}	
```
# Usage
Define result callback in your fragment
```java
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    piCropperFragmentResultListener { list ->
        val result: List<Uri> = list
    }
}
```

Open PiCropperFragment
```java
val args = PiCropperFragment.prepareOptions(
    aspectRatio = AspectRatio.defaultList,
    allImagesFolder = "All Images",
    collectCount = 10,
    forceOpenEditor = false,
    circleCrop = false,
    quality = 90,
    compressFormat = CompressFormat.JPEG
)
findNavController().navigate(R.id.piCropperFragment, args)
```
    
# Customization
### Setup params
| Param | Description | Default value |
| --- | --- | --- |
| aspectRatio | List of Aspect rations for croping image. If it has less than 2 items - aspect ratio selector won't show. | AspectRatio.defaultList |
| allImagesFolder | Name of folder with all images | All images |
| collectCount | Max images count to return | 10 |
| shouldForceOpenEditor | Open editor from images grid. Only if collectCount == 1 | false |
| circleCrop | Editor's circle crop mode | false |
| quality | Quality of edited image | 80 |
| compressFormat | Compress format of edited image | CompressFormat.JPEG |

### Theming params

| Param | Description |
| --- | --- |
| accentColor | Accent color ("Provide result" floating action button, crop instruments) |
| accentDualColor | Color for icon of floating action button |
| statusBarColor | Color of android status bar |
| navigationBarColor | Color of android navigation bar |
| toolbarColor | Color of picker toolbar |
| toolbarContentColor | Color toolbar's content (icons, toolbar title) |
| gridBackgroundColor | Picker background color |
| imageBackgroundColor | Background color of selected image |
| toolsColor | Crop tools color (icons, horizontal wheel) |
| toolsResetRotationColor | Crop tools - reset rotation color |
| toolsBackgroundColor | Crop tools background color |
| checkBoxTextColor | Checkbox text color |
| checkBoxBackground | Checkbox background color |
| checkBoxCheckedBorder | Checkbox border color (checked, on grid) |
| checkBoxUncheckedBorder | Checkbox border color (unchecked) |
| checkBoxCheckedBorderOverlay | Checkbox border color (checked, in fullscreen viewer) |


