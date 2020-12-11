package eionet.xmlconv.jobExecutor.scriptExecution.services;

public interface SysCommandExecutorService {
    long getTimeout();
    void setEnvironmentVar(String name, String value);
    int runCommand(String commandLine) throws Exception;
    String getCommandOutput();
    String getCommandError();
}
