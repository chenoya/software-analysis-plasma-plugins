package be.uclouvain.gdbmiapi;

import be.uclouvain.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class GdbProcess {

    private PrintStream out;
    private Process pr;
    private String path;

    public GdbProcess(PrintStream out, String path) throws IOException, GdbException {
        this.out = out;
        this.path = path;
        createProcess();
        //disableAsyncExec();
    }

    private void createProcess() throws IOException {
        Runtime rt = Runtime.getRuntime();
        if (path == null) {
            this.pr = rt.exec("gdb --interpreter=mi");
        } else {
            this.pr = rt.exec(new String[]{path, "--interpreter=mi"});
        }
        String res = readAvailable();
        out.println(res);
    }

    public String readAvailable() throws IOException {
        Callable<String> task = () -> {
            InputStream is = pr.getInputStream();
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            StringBuilder builder = new StringBuilder();

            int result;
            while (true) {
                result = is.read();
                buf.write((byte) result);
                if ((char) result == '\n') {
                    String line = buf.toString(StandardCharsets.UTF_8.name());
                    buf.reset();
                    builder.append(line);
                    if (line.equals("(gdb) \n") || line.equals("(gdb) \r\n")) {
                        break;
                    }
                }
            }
            return builder.toString();
        };

        ExecutorService executor = Executors.newCachedThreadPool();
        Future<String> future = executor.submit(task);
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            this.pr.destroy();
            createProcess();
            throw new GdbException("Could not get a response from GDB on time.");
        } finally {
            future.cancel(true);
            executor.shutdown();
        }
    }

    public String executeGDBCommand(String command) throws IOException {
        pr.getOutputStream().write((command + "\n").getBytes());
        pr.getOutputStream().flush();
        this.out.print(command + " : ");
        String res = readAvailable();
        this.out.println(res);
        return res;
    }

    private void disableAsyncExec() throws IOException, GdbException {
        String res = executeGDBCommand("-gdb-set mi-async off");
        Utils.checkDone(res);
    }

    public void wash() throws IOException {
        pr.getOutputStream().flush();
        while (pr.getInputStream().available() > 0) {
            pr.getInputStream().read();
        }
        while (pr.getErrorStream().available() > 0) {
            pr.getErrorStream().read();
        }
    }
}
