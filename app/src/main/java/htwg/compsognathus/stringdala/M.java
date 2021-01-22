package htwg.compsognathus.stringdala;

public class M {


    public static int toUint(byte value)
    {
        return (value & 0x7F) + (value < 0 ? 128 : 0);
    }

    public static byte intMSB(int a)
    {
        return (byte) (a >> 8);
    }

    public static byte intLSB(int a)
    {
        return (byte) a;
    }

    public static byte intToByte(int a, int n)
    {
        return (byte) (a >> ((3-n) * 8));
    }

    public static int bbbbInt(byte b0, byte b1, byte b2, byte b3)
    {
        return ((int)((((int)(((int) toUint(b0) << 8) | toUint(b1)) << 8) | toUint(b2)) << 8)) | toUint(b3);
    }

    public static int bbInt(byte msb, byte lsb)
    {
        return ((int) toUint(msb) << 8) | toUint(lsb);
    }

}
