# kotlin-voxel
A voxel engine (like Minecraft) written in Kotlin

------

This voxel engine was created as a programming exercise. It was created with the Kotlin programming language and libgdx, a Java game library.

![screenshot](/voxel1.png)

Youtube Video: https://t.co/77OObtUcxR

To run, you must use the gradle task

Linux/OSX:
```
./gradlew desktop:run
```
Windows:
```
gradlew.bat desktop:run
```

If you want to edit the code with IntelliJ IDEA indepedent from gradle, you can create idea project files with the task:
```
./gradlew idea (windows: gradlew.bat idea)
```
Then open the .ipr file with IntelliJ

Controls are explained in the in-game HUD

additionally, you may press 'G' to see OpenGL debugging info, or 'V' to reset the view.
