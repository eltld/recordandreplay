recordandreplay
===============

A record and replay app for android 
Bugs that occur for a single time are hard to reproduce due to the change in the application environment and different use cases. We present a record and replay tool for
Android that records the events occurring in the Android application from the user space to a persistent storage (log
file). Using the replay system we are able to faithfully replay the saved execution of the recorded application execution. The log files contains data which represent the
timestamp and event related information .The replay can be done without requiring the specific APK, libraries or
support data. Using this tool we can find the exception paths in the framework and find the source of the exception
