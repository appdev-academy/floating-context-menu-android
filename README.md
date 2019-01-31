# FloatingContextMenu

![Platform](http://img.shields.io/badge/platform-android-blue.svg?style=flat)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![API](https://img.shields.io/badge/API-14%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=14)
[![Download](https://api.bintray.com/packages/staspetrenko/maven/floating-context-menu/images/download.svg)](https://bintray.com/staspetrenko/maven/floating-context-menu/_latestVersion)

# Overview
<img src="https://github.com/appdev-academy/floating-context-menu-android/blob/master/images/overview.gif" alt="Overview" width="360"/>

# Installation

### gradle
```groovy
dependencies {
    implementation ‘academy.appdev:floating-context-menu:1.0.1’
}
```

### maven
``` xml
<dependency>
	<groupId>academy.appdev</groupId>
	<artifactId>floating-context-menu</artifactId>
	<version>1.0.1</version>
	<type>pom</type>
</dependency>
```

# Setup

## 1) Add FloatingMenu view to you layout and configure it

###  Anko

```kotlin
menu = floatingMenu(ArrayList<FloatingMenu.MenuItem>().apply {
                    (0..2).forEach { add(FloatingMenu.MenuItem(R.drawable.search_icon, "Option$it")) }
                }) {
                    // configuration
                    menuItemSize = 70
                    menuCorner = 130
                    radius = 130
                    animationDuration = 250L

                }.lparams {
                    width = matchParent
                    height = matchParent
                }
```

### XML

``` XML
    <appdev.academy.floatingcontextmenu.FloatingMenu
            android:id="@+id/floating_menu"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
```

Configure view in your presenter class :
```kotlin
 floating_menu.apply {
             // configuration
             menuItemSize = 70
             menuCorner = 130
             radius = 130
             animationDuration = 250L
         }
```

## 2) Let FloatingMenu handle touch events

Override `dispatchTouchEvent` method of your activity:
```kotlin
override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // Let Floating menu handle event if it can
        return if (FloatingMenu.interceptTouch(ev))
            true
        else
            super.dispatchTouchEvent(ev)
            }
```

## 3) Show menu

Detect long tap event for example:
```kotlin
setOnItemLongClickListener { _, _, position, _ ->
                        menu.apply {
                            onItemSelected = { toast("Item $position menu option $it") }
                            onLongTapDetected()
                        }
                        true
                    }
```
# Configuration

Floating menu has many public fields that can be modified

```
    // customization
    var menuItemSize = 70
    var menuCorner = 130
    var radius = 130
    var animationDuration = 250L

    var collapseScale = 0.3f
    var defaultScale = 1f
    var selectedScale = 1.13f
    var innerPadding: Int = 10

    var defaultIconTint = android.R.color.black
    var selectedIconTint = android.R.color.white

    var defaultBackgroundTint: Int = android.R.color.white
    var selectedBackgroundTint: Int = android.R.color.black

    var frameBgColor = R.color.shadow
    var textColor = android.R.color.white
```

# License

```
Copyright 2019 appdev.academy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```