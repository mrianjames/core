package com.oaktree.core.utils;

import com.oaktree.core.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.jvmstat.monitor.*;

import java.net.URISyntaxException;
import java.util.*;

/**
 * Accessor to other java processes running on this box.
 *
 * Created with IntelliJ IDEA.
 * User: IJ
 * Date: 06/03/13
 * Time: 19:25
 * To change this template use File | Settings | File Templates.
 */
public class JavaProcessManager {
    public static class Process {
        public int pid;
        public String main;
        public String cmd;
        public String mainArgs;
        public String jvmArgs;

        public Process(String main, String cmd, int pid, String jvmAgs, String mainArgs) {
            this.pid = pid;
            this.jvmArgs = jvmAgs;
            this.mainArgs = mainArgs;
            this.main = main;
            this.cmd = cmd;
        }

        @Override
        public String toString() {
            return pid + " main: " + main + " args: " + mainArgs + " cmd: " + cmd + " jvm: " + jvmArgs;
        }
    }

    private Logger logger = LoggerFactory.getLogger(JavaProcessManager.class);
    private MonitoredHost local;

    public JavaProcessManager() {
        try {
            local = MonitoredHost.getMonitoredHost("localhost");
        } catch (MonitorException e) {
            Log.exception(logger, e);
        } catch (URISyntaxException e) {
            Log.exception(logger, e);
        }
    }

    public Set<Integer> getActiveVms() {
        try {
            return new HashSet<Integer>(local.activeVms());
        } catch (MonitorException e) {
            Log.exception(logger, e);
        }
        return new HashSet<Integer>();
    }

    public Set<String> getMainClasses() {
        Set<String> classes = new HashSet<String>();
        for (Object id : getActiveVms()) {
            try {
                MonitoredVm vm = local.getMonitoredVm(new VmIdentifier("//" + id));
                String processname = MonitoredVmUtil.mainClass(vm, true);
                classes.add(processname);
            } catch (MonitorException e) {
//                    Log.exception(logger,e);
            } catch (URISyntaxException e) {
//                    Log.exception(logger,e);
            }
        }
        return classes;
    }

    public int getPidByMain(String main, boolean exact) {
        for (Object id : getActiveVms()) {
            try {
                MonitoredVm vm = local.getMonitoredVm(new VmIdentifier("//" + id));
                String mn = MonitoredVmUtil.mainClass(vm, true);
                if (exact) {
                    if (mn.equals(main)) {
                        return vm.getVmIdentifier().getLocalVmId();
                    }
                } else {
                    if (mn.contains(main)) {
                        return vm.getVmIdentifier().getLocalVmId();
                    }
                }
            } catch (MonitorException e) {
                Log.exception(logger, e);
            } catch (URISyntaxException e) {
                Log.exception(logger, e);
            }
        }
        return -1;
    }

    public int getPidByCmd(String search) {
        for (Object id : getActiveVms()) {
            try {

                MonitoredVm vm = local.getMonitoredVm(new VmIdentifier("//" + id));
                String mn = MonitoredVmUtil.commandLine(vm);
                if (mn.contains(search)) {
                    return vm.getVmIdentifier().getLocalVmId();
                }
            } catch (MonitorException e) {
                Log.exception(logger, e);
            } catch (URISyntaxException e) {
                Log.exception(logger, e);
            }

        }
        return -1;
    }

    /**
     * e.g. search by vm flag e.g. -Dapplication.name=xxx
     * //33ms via string split on power save.
     *
     * @param search
     * @return
     */
    public int getPidByVmFlag(String search) {
        for (Object id : getActiveVms()) {
            try {

                MonitoredVm vm = local.getMonitoredVm(new VmIdentifier("//" + id));
                String mn = MonitoredVmUtil.jvmArgs(vm);
                String[] vars = mn.split(" ");
                for (String var:vars) {
                    if (var.equals(search)) {
                        return vm.getVmIdentifier().getLocalVmId();
                    }
                }
            } catch (MonitorException e) {
                Log.exception(logger, e);
            } catch (URISyntaxException e) {
                Log.exception(logger, e);
            }

        }
        return -1;
    }

    public Process getProcessByCmd(String search) {
        for (Object id : getActiveVms()) {
            try {

                MonitoredVm vm = local.getMonitoredVm(new VmIdentifier("//" + id));
                String mn = MonitoredVmUtil.commandLine(vm);
                if (mn.contains(search)) {
                    return getProcess(id, vm);
                }
            } catch (MonitorException e) {
                Log.exception(logger, e);
            } catch (URISyntaxException e) {
                Log.exception(logger, e);
            }

        }
        return null;
    }

    public Collection<Process> getProcesses() {
        List<Process> processes = new ArrayList<Process>();
        for (Object id : getActiveVms()) {
            try {

                MonitoredVm vm = local.getMonitoredVm(new VmIdentifier("//" + id));

                processes.add(getProcess(id, vm));
            } catch (MonitorException e) {
                Log.exception(logger, e);
            } catch (URISyntaxException e) {
                Log.exception(logger, e);
            }

        }
        return processes;
    }

    private Process getProcess(Object id, MonitoredVm vm) throws MonitorException, URISyntaxException {
        String cmdLine = MonitoredVmUtil.commandLine(vm);
        String main = MonitoredVmUtil.mainClass(vm, true);
        String jvmArgs = MonitoredVmUtil.jvmArgs(vm);
        String mainArgs = MonitoredVmUtil.mainArgs(vm);
        Process process = new Process(main, cmdLine, (Integer) id, jvmArgs, mainArgs);
        return process;
    }

    public static void main(String args[]) {
        JavaProcessManager jp = new JavaProcessManager();
//        System.out.println("Processes:" + jp.getActiveVms());
//        System.out.println("Classes: " + jp.getMainClasses());
//        System.out.println("Main:" + jp.getPidByMain("Eclipse", false));
//        System.out.println("Cmd:" + jp.getPidByCmd("agent.properties"));
        System.out.println("JvmFlags:" + jp.getPidByVmFlag("-Dapplication.name=test.application"));
//        int TESTS = 100;
//
//        ResultTimer t = new ResultTimer();
//        for (int i = 0; i < TESTS; i++) {
//            t.startSample();
////            for (Process p : jp.getProcesses()) {
////                System.out.println(p);
////            }
//            int pid = jp.getPidByVmFlag("-Dapplication.name=test.application");
//            System.out.println(pid);
//            t.endSample();
//        }
//        System.out.println(t.toString(TimeUnit.MILLISECONDS));

//        ResultTimer t = new ResultTimer();
//        for (int i = 0; i < TESTS; i++) {
//            t.startSample();
//            System.out.println("Process:" + jp.getPidByCmd("agent.properties"));
//            t.endSample();
//        }
//        System.out.println(t.toString(TimeUnit.MILLISECONDS));
    }

}
