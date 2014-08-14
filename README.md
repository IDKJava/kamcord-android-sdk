Kamcord Android Framework
=========================

<h3>The documentation can be found on the <a href="http://www.kamcord.com/developers/docs/android/quickguide/">developer website</a>.</h3>

This repository contains a build of Kamcord for integrating into a Unity 4.2+ game or a generic Android project. The current version is 1.4.4. Please visit the <a href="https://github.com/kamcord/kamcord-android-sdk/wiki/Change-log">Change Log</a> to see a history of changes.

If you're using Unity, use the Unity package at `unity/Kamcord.unitypackage`.

If you'd like to integrate into an Android project not coming from Unity, the directory `kamcord` contains all the files you need.  You can find an example project with Kamcord integrated on the <a href="http://www.kamcord.com/developers/docs/android/examples/#ripples-integration-java">examples page</a> at the developer website.

Note that the `Kamcord.unitypackage` in this repository contains a complete copy of the Kamcord iOS Unity plugin, making it safe to import into existing Unity projects that use the Kamcord iOS Unity plugin. More information about the iOS plugin can be found at this repository: <a href="https://github.com/kamcord/Unity-Kamcord">https://github.com/kamcord/Unity-Kamcord</a>.

<b>Note to Unity developers upgrading from Kamcord versions before 1.4.3</b><br/>
The structure of the resources used in the Android library project changed slightly. Because of this, you may need to delete the `Assets/Plugins/Android/kamcord/res` directory before importing the new `Kamcord.unitypackage` into your Unity project.
