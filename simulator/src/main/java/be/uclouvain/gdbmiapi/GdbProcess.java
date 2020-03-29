package be.uclouvain.gdbmiapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class GdbProcess {

    private PrintStream out;

    private Process pr;

    public GdbProcess(PrintStream out) throws IOException, GdbException {
        this.out = out;
        Runtime rt = Runtime.getRuntime();
        this.pr = rt.exec("gdb --interpreter=mi");
        String res = readAvailable();
        out.println(res);
        disableAsyncExec();
    }

    /*public static void main(String[] args) throws IOException {
        try {
            GdbProcess gdbProcess = new GdbProcess(System.out);

            File.file(gdbProcess, Paths.get("/home/antoine/Bureau/tt/test\""));
            File.info_source(gdbProcess);
            ProgramExecution.start(gdbProcess);
            //ProgramExecution.stepi(gdbProcess);
            //ProgramExecution.nexti(gdbProcess);
            System.out.println(DataManipulation.data_eval_expr(gdbProcess, "x"));
            System.out.println("2");
        } catch (GdbException e) {
            e.printStackTrace();
        }
    }*/

    /*public String readAvailable() throws IOException {
        BufferedInputStream bis = new BufferedInputStream(pr.getInputStream());
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        do {
            buf.write((byte) bis.read());
        } while (bis.available() > 0);
        System.out.println(bis.available());
        return buf.toString(StandardCharsets.UTF_8);
    }*/

    /*public String readAvailable() throws IOException {
        out.println("enter");
        InputStreamReader inputStreamReader = new InputStreamReader(pr.getInputStream());
        Scanner sc = new Scanner(inputStreamReader);
        StringBuffer stringBuffer = new StringBuffer();
        for(String str = null; ; str = sc.nextLine()) {
            if (str != null) {
                stringBuffer.append(str);
                stringBuffer.append("\n");
                if (str.equals("(gdb) ")) {
                    break;
                }
            }
        }
        out.println("leave");
        return stringBuffer.toString();
    }*/

    /*public String readAvailable() throws IOException {
        BufferedInputStream bis = new BufferedInputStream(pr.getInputStream());
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        StringBuilder builder = new StringBuilder();

        int result;
        while(true) {
            result = bis.read();
            buf.write((byte) result);
            if ((char) result == '\n') {
                String line = buf.toString(StandardCharsets.UTF_8.name());
                buf.reset();
                builder.append(line);
                if (line.equals("(gdb) \n")) {
                    break;
                }
            }
        }
        return builder.toString();
    }*/

    public String readAvailable() throws IOException {
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
                if (line.equals("(gdb) \n")) {
                    break;
                }
            }
        }
        return builder.toString();
    }

    public String executeGDBCommand(String command) throws IOException {
        pr.getOutputStream().write((command + "\n").getBytes());
        pr.getOutputStream().flush();
        this.out.print(command + " : ");
        String res = readAvailable();
        this.out.println(res);
        return res;
    }

    public String[] executeGDBCommand(String command, int nbResponse) throws IOException {
        pr.getOutputStream().write((command + "\n").getBytes());
        pr.getOutputStream().flush();
        this.out.print(command + " : ");
        String[] res = new String[nbResponse];
        for (int i = 0; i < nbResponse; i++) {
            res[i] = readAvailable();
        }
        this.out.println(res[0]);
        return res;
    }

    private void disableAsyncExec() throws IOException, GdbException {
        String res = executeGDBCommand("-gdb-set mi-async off");
        Utils.checkDone(res);
    }
}
