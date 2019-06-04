!include "go-base.nsi"

; Silent option `/START_AGENT=yes|no`
Var START_AGENT

Function "OnInitCallback"
  ; load this file to "data" memory
  File /oname=$PLUGINSDIR\ServerURL.ini ServerURL.ini
FunctionEnd

Function "un.PostUninstall"
  ; remove the entire install dir
  RMDir /r "$INSTDIR"
FunctionEnd

Function "BeforeUpgrade"
  Call RememberIfServiceWasRunning
  Call StopServiceIfRunning
  ; this will uninstall the old service, and install a new one, just to make sure that changes to tanuki, if any happen correctly
  Call UninstallService

  ; remove legacy files (from pre 19.5 installers), if present
  Delete "$INSTDIR\agent-bootstrapper.jar"
  Delete "$INSTDIR\agent-launcher.jar"
  Delete "$INSTDIR\*agent-launcher.jar"
  Delete "$INSTDIR\agent.jar"
  Delete "$INSTDIR\tfs-impl.jar"
  Delete "$INSTDIR\agent-plugins.jar"
  Delete "$INSTDIR\cruisewrapper.exe"
  Delete "$INSTDIR\LICENSE.dos"
  Delete "$INSTDIR\agent.cmd"
  Delete "$INSTDIR\start-agent.bat"
  Delete "$INSTDIR\stop-agent.bat"

  ; remove legacy environment variables (from pre 19.5 installers), if present
  DeleteRegValue HKLM "SYSTEM\CurrentControlSet\Control\Session Manager\Environment" "GO_AGENT_JAVA_HOME"
  DeleteRegValue HKLM "SYSTEM\CurrentControlSet\Control\Session Manager\Environment" "GO_SERVER_URL"
  DeleteRegValue HKLM "SYSTEM\CurrentControlSet\Control\Session Manager\Environment" "GO_AGENT_DIR"

  ; explicitly clear errors, if any
  ClearErrors
FunctionEnd

Function "AfterUpgrade"
  Call InstallService
  Call MaybeCreateDefaultWrapperProperties
  Call StartServiceIfRunningBeforeUpgrade
FunctionEnd

Function "PostInstall"
  ; Add to "Start menu"
  CreateDirectory "$SMPROGRAMS\Go ${COMPONENT_NAME}"
  CreateShortCut "$SMPROGRAMS\Go Agent\Run Go Agent.lnk" "$INSTDIR\bin\go-agent.bat"

  Call MaybeCreateDefaultWrapperProperties

  Call InstallService

  ${IfNot} $START_AGENT == "NO"
    Call StartService
  ${EndIf}
FunctionEnd

Function "DoParseCLI"
  ${GetOptions} $ARGV /START_AGENT= $START_AGENT
  ${GetOptions} $ARGV /SERVERURL= $GO_SERVER_URL
FunctionEnd

Function "MaybeCreateDefaultWrapperProperties"
  StrCmp $GO_SERVER_URL "" 0 +2
    StrCpy $GO_SERVER_URL "https://127.0.0.1:8154/go"

  ${LogText} "Checking if default wrapper-properties.conf exists"
  ${IfNot} ${FileExists} "$INSTDIR\config\wrapper-properties.conf"
    ${LogText} "Creating default wrapper properties file $INSTDIR\config\wrapper-properties.conf"

    ClearErrors
    ; open file in write mode, store file handle in $0
    FileOpen $0 $INSTDIR\config\wrapper-properties.conf w
    ; if there are no errors opening file, then write
    ${IfNot} ${Errors}
        FileWrite $0 "; This file was automatically generated by the gocd agent installer$\r$\n"
        FileWrite $0 "; See wrapper-properties.conf.example for passing options and environment variables to the gocd agent$\r$\n"
        FileWrite $0 "wrapper.app.parameter.100=-serverUrl$\r$\n"
        FileWrite $0 "wrapper.app.parameter.101=$GO_SERVER_URL$\r$\n"
        FileClose $0
    ${EndIf}
  ${EndIf}
FunctionEnd
