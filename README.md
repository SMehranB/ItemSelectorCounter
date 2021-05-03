
# Item Selector/Counter View [![](https://jitpack.io/v/SMehranB/ItemSelectorCounter.svg)](https://jitpack.io/#SMehranB/ItemSelectorCounter)

### Customizable Item Selector and Item Counter
 
## Features!

 •	 Set textSize, textStyle, textColor, textFont, vertical and horizontal padding
 
 •	 Set backgroundColor, cornerRadius
 
 •	 Set drawableSize, drawableHorizontalPadding, drawableTint, dividerColor
 
 •	 Set items array in XML file
 
 •   Set animation duration
 
 •   Button click listeners are built-in so you don't have to worry about anything

 •   Set shadow for the view

## Screen recording
 <img src="./screen_recording1.gif" height="300"> <img src="./screen_recording2.gif" height="300">
 <img src="./screen_recording.gif" height="600">

# Install
```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
## Gradle

```groovy
dependencies {
	 implementation 'com.github.SMehranB:ItemSelectorCounter:2.1.0'
}
```

## Maven
```xml
<dependency>
	<groupId>com.github.SMehranB</groupId>
	<artifactId>ItemSelectorCounter</artifactId>
	<version>2.1.0</version>
</dependency>
 ```
# Use
 
## XML

### Item Selector
```xml
<com.smb.itemselectorview.ItemSelector
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginTop="16dp"
    app:is_textStyle="italic|bold"
    app:is_drawableHorizontalPadding="8dp"
    app:is_verticalPadding="24dp"
    app:is_horizontalPadding="32dp"
    app:is_animationDuration="300"
    app:is_shadowColor="#4A4A4A"
    app:is_shadowDx="10"
    app:is_shadowDy="10"
    app:is_shadowRadius="15"
    app:is_items="@array/sample_items"
    app:is_textColor="@color/white"
    app:is_buttonSize="30dp"
    app:is_drawableTint="@color/white"
    app:is_cornerRadius="25dp"
    app:is_backgroundColor="#2B7FFF"/>
 ```

### Item Counter
```xml
<com.smb.itemselectorview.ItemCounter
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="16dp"
    android:layout_marginTop="16dp"
    app:ic_DecButtonColor="#00B6B1"
    app:ic_IncButtonColor="#00B6B1"
    app:ic_backgroundColor="#00FFF7"
    app:ic_cornerRadius="10dp"
    app:ic_shadowColor="#4A4A4A"
    app:ic_shadowDx="10"
    app:ic_shadowDy="10"
    app:ic_shadowRadius="15"
    app:ic_drawableHorizontalPadding="8dp"
    app:ic_drawableTint="@color/black"
    app:ic_horizontalPadding="48dp"
    app:ic_textColor="@color/black"
    app:ic_textSize="18dp"
    app:ic_textStyle="bold"
    app:ic_animationDuration="200"
    app:ic_dividerColor="@color/black"
    app:ic_drawableSize="40dp"
    app:ic_verticalPadding="20dp"/>
```

## Kotlin

### Item Selector
```kotlin
val itemSelector = ItemSelector(this)
val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
params.setMargins(16, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt(), 16 ,0)

itemSelector.apply {
    layoutParams = params
    setTextParams(24, Color.YELLOW, Typeface.BOLD)
    setBackgroundParams(Color.RED, 25)
    setDrawableParams(50, 32)
    setTextPadding(32, 32)
    setShadowParams(Color.GRAY, 10f, 10f, 10f)
    items = arrayListOf("Short", "Tall", "Grande", "Venti")
    dividerColor = Color.GRAY
    animationDuration = 300L
    drawableTint = Color.YELLOW
}

viewHolder.addView(itemSelector)
```
```kotlin
itemSelector.getCurrentItem()
itemSelector.getCurrentItemIndex()
```

### Item Counter
```kotlin
val itemCounter = ItemCounter(this)

itemCounter.apply {
    layoutParams = params
    setTextParams(24, Color.BLACK, Typeface.BOLD)
    setBackgroundParams(Color.WHITE, 15)
    setDrawableParams(32, 24)
    setTextPadding(24, 32)
    setButtonsColor(Color.GREEN, Color.RED)
    setShadowParams(Color.GRAY, 10f, 10f, 10f)
    dividerColor = Color.GRAY
    animationDuration = 300L
    drawableTint = Color.BLACK
}

viewHolder.addView(itemCounter)
```
```kotlin
itemCounter.getCurrentNumber()
```

## 📄 License
```text
MIT License

Copyright (c) 2021 Seyed Mehran Behbahani

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
