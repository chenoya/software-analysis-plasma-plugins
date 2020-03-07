package be.uclouvain.gdbmiapi.commands;

import be.uclouvain.gdbmiapi.GDBException;
import be.uclouvain.gdbmiapi.GdbProcess;
import be.uclouvain.gdbmiapi.Utils;

import java.io.IOException;

public class ProgramExecution {
    public static void start(GdbProcess gdbProcess) throws IOException, GDBException {
        String[] res = gdbProcess.executeGDBCommand("-exec-run --start", 2);
        Utils.checkDone(res[0]);
    }

    public static void nexti(GdbProcess gdbProcess) throws IOException, GDBException {
        nexti(gdbProcess, false);
    }

    /**
     * Executes one machine instruction. If the instruction is a function call, continues until the function returns.
     * If the program stops at an instruction in the middle of a source line, the address will be printed as well.
     * If the ‘--reverse’ option is specified, resumes reverse execution of the inferior program, stopping at the
     * previous instruction. If the previously executed instruction was a return from another function,
     * it will continue to execute in reverse until the call to that function (from the current stack frame) is reached.
     * <br><br>
     * The corresponding GDB command is <b>‘nexti’</b>.
     *
     * @param gdbProcess
     * @param reverse
     * @throws IOException
     * @throws GDBException
     */
    public static void nexti(GdbProcess gdbProcess, boolean reverse) throws IOException, GDBException {
        String[] res = gdbProcess.executeGDBCommand(reverse ? "-exec-next-instruction --reverse" : "-exec-next-instruction", 2);
        Utils.checkDone(res[0]);
    }

    public static void stepi(GdbProcess gdbProcess) throws IOException, GDBException {
        stepi(gdbProcess, false);
    }

    /**
     * Resumes the inferior which executes one machine instruction. If the ‘--reverse’ option is specified,
     * resumes reverse execution of the inferior program, stopping at the previously executed instruction.
     * The output, once GDB has stopped, will vary depending on whether we have stopped in the middle of a
     * source line or not. In the former case, the address at which the program stopped will be printed as well.
     * <br><br>
     * The corresponding GDB command is <b>‘stepi’</b>.
     *
     * @param gdbProcess
     * @param reverse
     * @throws IOException
     * @throws GDBException
     */
    public static void stepi(GdbProcess gdbProcess, boolean reverse) throws IOException, GDBException {
        String[] res = gdbProcess.executeGDBCommand(reverse ? "-exec-step-instruction --reverse" : "-exec-step-instruction", 2);
        Utils.checkDone(res[0]);
    }


}
