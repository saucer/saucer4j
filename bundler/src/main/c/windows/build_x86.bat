@echo OFF
cl launcher.c str_builder.c /link /SUBSYSTEM:CONSOLE /MACHINE:X86 /OUT:windows-launcher-x86.exe
cl launcher_w.c str_builder.c /link /SUBSYSTEM:WINDOWS /MACHINE:X86 /OUT:windows-launcherw-x86.exe
del *.obj