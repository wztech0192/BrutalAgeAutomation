@ECHO OFF
set /P key="What is the key: "
set /P value="What is the value: "
echo Hello %key% %value%!
for /r %i in (*) do echo %i
pause