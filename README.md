# BrutalAgeAutomation
A Android Game Automation Tool

** Due to recent the game update, this tool can no longer generate new game account automatically. **

# Dependencies
- Window OS
- Visual C++ 2013 Redistributable
- NoxPlayer Emulator
- Brutal Age.apk (Preferred v8.1.1)

# Knowledge Required
- Java Programming Language
- Basic linux CMD
- ABD - Android Development Bridge
- Burtal Age game experience...

# Development Setup
1. Import the project in Intellij or any Java IDE
2. Restroe Dependencies
3. Run the app

## Note
When the app run first time, it will generate a ba_config file in your user folder. Make sure to config setting is correct based on your device. The setting can also modify under the application setting dialog.

Example:
```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Config>
    <eventFolder>baevents</eventFolder>
    <eventName>event4</eventName>
    <instanceNumber>2</instanceNumber>
    <maxStorePos>0</maxStorePos>
    <noxPath>C:\Program Files (x86)/Nox/bin</noxPath>
    <ownerName>wz</ownerName>
    <saveErrorScreenshot>false</saveErrorScreenshot>
</Config>
```
## UI
1. Active game instance
2. Select NoxPlayer instance and specify a directory to store accounts
3. Connecting to the NoxPlayer via adb - If connection failed, the instance will be close
4. Connected to controller ui

## Game Instance Lifecycle - after click start
1. initiate - load account then stop and rerun brutal age game.
2. starting - listen to logcat and wait until the game fully started
3. when_start - perform checks to clear all popup modals
4. city_work - perform tasks when inside the city
5. world_map - perform tasks when outsite of the city in the world map.
6. return to step 1

## Development Tips
- Register click/scroll events use Event.Builder and place them under the src/events/register directory
- Consume the events in the src/events/handler directory. The handler is named based on the status of the game instance.
- All events are dispached through src/dispatcher/EventDispatcher class.


# Technical Features
- Emulator screenshot
- OpenCV template matching
- Image to Text
- Access adb shell input api (click, scroll, etc)
- Access adb logcat and translate the info into actions

# Game Features
- Auto gather resources
- Auto transport resrouces
- Auto leveling
- Auto make troops
- Auto feed troops to temple



