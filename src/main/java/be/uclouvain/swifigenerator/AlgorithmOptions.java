package be.uclouvain.swifigenerator;

public class AlgorithmOptions {
    private String pythonPath;
    private String swifiPath;
    private long nbMaxSimu;
    private int nop;
    private int z1b;
    private int z1w;
    private int flp;
    //private int jmp;
    //private int jbe;
    private String otherParams;

    public AlgorithmOptions(String pythonPath, String swifiPath, long nbMaxSimu, int nop, int z1b, int z1w, int flp, String otherParams) {
        this.pythonPath = pythonPath;
        this.swifiPath = swifiPath;
        this.nbMaxSimu = nbMaxSimu;
        this.nop = nop;
        this.z1b = z1b;
        this.z1w = z1w;
        this.flp = flp;
        //this.jmp = jmp;
        //this.jbe = jbe;
        this.otherParams = otherParams;
    }

    public String getPythonPath() {
        return pythonPath;
    }

    public String getSwifiPath() {
        return swifiPath;
    }

    public long getNbMaxSimu() {
        return nbMaxSimu;
    }

    public int getNop() {
        return nop;
    }

    public int getZ1b() {
        return z1b;
    }

    public int getZ1w() {
        return z1w;
    }

    public int getFlp() {
        return flp;
    }

    /*public int getJmp() {
        return jmp;
    }*/

    /*public int getJbe() {
        return jbe;
    }*/

    public String getOtherParams() {
        return otherParams;
    }
}
