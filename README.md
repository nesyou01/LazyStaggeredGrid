# LazyStaggeredGrid

A Jetpack Compose library to achieve staggered grid view

## Getting Started

To start using this package, add the JitPack repository to your `settings.gradle` file 

```	
maven { url 'https://jitpack.io' }
```
Then add the dependency


```	
  implementation 'com.github.nesyou01:LazyStaggeredGrid:<latest-version>'
```

## Features

- Scroll animations
- Light weight
- Lazy implemtation

## Simple Example

```kotlin
LazyStaggeredGrid(cells = StaggeredCells.Adaptive(minSize = 180.dp)) {
     items(60) {
        val random: Double = 100 + Math.random() * (500 - 100)
          Image(
             painter = painterResource(id = R.drawable.image),
             contentDescription = null,
             modifier = Modifier.height(random.dp).padding(10.dp),
             contentScale = ContentScale.Crop
          )
      }
  }
```

## Result
The result of the code above:

<img src="https://github.com/nesyou01/LazyStaggeredGrid/blob/master/Screenshot_2022-01-24-23-21-38.jpg" width="250"/>

## Issues

Please file any issues, bugs or feature request as an [issue](https://github.com/nesyou01/LazyStaggeredGrid/issues) on our GitHub page.

## Buy me a coffe

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/nesyou)
