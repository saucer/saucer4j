@echo OFF
cl launcher.c str_builder.c /link /SUBSYSTEM:CONSOLE /MACHINE:X64 /OUT:windows-launcher-x86_64.exe
cl launcher_w.c str_builder.c /link /SUBSYSTEM:WINDOWS /MACHINE:X64 /OUT:windows-launcherw-x86_64.exe
del *.obj