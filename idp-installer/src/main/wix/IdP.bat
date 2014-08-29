@Echo off
REM Generate msi
setlocal

REM Preconditions

if "%1%" == "" (
  Echo Idp PATH_TO_IDP_DISTRIBUTION
  goto done
)

if not defined WIX (
   echo WIX should be installed
   goto done
)

if not exist jetty.msm (
   echo RUN JETTY.BAT FIRST
   goto done
)

REM Clear up detritus from last time

if exist idp_contents.wxs (
   del idp_contents.wxs
)

if exist idp-extract (
   rd /q /s idp-extract
)

REM Set up environment

if not defined JAVA_JDK  (
   set JAVA_JDK=%JAVA_HOME%
)

if not defined JAVA_JDK  (
  echo Error: Nether JAVA_JDK nor JAVA_HOME is defined.
  exit /b
)

if not defined JARCMD (
  set JARCMD=%JAVA_JDK%\bin\jar.exe
)

if not exist "%JARCMD%" (
  echo Error: JAVA_HOME is not defined correctly.
  echo Cannot execute %JARCMD%
  goto done
)

REM Test and extract

if not exist %1% (
   echo Error: Could not locate idp zip %1%
   goto done
)

if not exist %1.asc (
   echo Error: Could not locate signature for procrun zip %1%.asc
   goto done
)

gpg --verify %1.asc %1
if ERRORLEVEL 1 (
   echo Error: Signature check failed on %1%
   goto done
)

mkdir idp-extract
cd idp-extract
if exist "%1" (
  "%JARCMD%" xf %1 
) else (
  "%JARCMD%" xf ..\%1 
)

rem is this a real package?
dir /s idp.properties 1> nl:a 2> nl:b
if ERRORLEVEL 1 (
  cd ..
  echo Could not find idp.properties in IdP package
  goto done;
)

for /D %%X in (*) do set idpex=%%X

rem we do not want to populate conf/flows/view
rem we will populate them from the dist directory

rd/s/q %idpex%\conf
if ERRORLEVEL 1 (
  cd ..
  echo Conf directory not found?
  goto done;
)
rd/s/q %idpex%\flows
if ERRORLEVEL 1 (
  cd ..
  echo Flows directory not found?
  goto done;
)
rd/s/q %idpex%\views
if ERRORLEVEL 1 (
  cd ..
  echo Views directory not found?
  goto done;
)
cd ..

"%WIX%/BIN/HEAT" dir idp-extract\%idpex% -platform -gg -dr IdPFolder -var var.idpSrc -cg IdPGroup -out idp_contents.wxs -srd
if ERRORLEVEL 1 goto done

REM Build
"%WIX%/BIN/CANDLE" -nologo -arch x86 -didpSrc=idp-extract\%idpex% idp_contents.wxs
if ERRORLEVEL 1 goto done

"%WIX%/BIN/CANDLE" -nologo -arch x86 -dProjectDir=. ShibbolethIdP-main.wxs ShibbolethIdP-registry.wxs ShibbolethIdP-gui.wxs ShibbolethIdP-install-dlg.wxs ShibbolethIdP-update-dlg.wxs
if ERRORLEVEL 1 goto done

"%WIX%/BIN/LIGHT" -nologo -out idp.msi -ext WixUIExtension ShibbolethIdP-main.wixobj idp_contents.wixobj ShibbolethIdP-registry.wixobj ShibbolethIdP-gui.wixobj ShibbolethIdP-install-dlg.wixobj ShibbolethIdP-update-dlg.wixobj
if ERRORLEVEL 1 goto done

dir idp.msi

REM Tidy up in the Sucessful exit case
   del *.wixobj *.wixpdb
   rd /s /q idp-extract

:done
