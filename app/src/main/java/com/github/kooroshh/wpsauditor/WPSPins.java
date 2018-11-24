package com.github.kooroshh.wpsauditor;
import java.util.ArrayList;
import java.util.List;

public class WPSPins
{
    public String Generic(String mac){
        Generic g = new Generic();
        return g.Generate(mac);
    }
    public String Dlink(String mac){
        Dlink g = new Dlink();
        return g.Generate(mac);
    }
    public String Arris(String mac){
        ArrisDG860A g = new ArrisDG860A();
        return g.Generate(mac);
    }
    public String TrendNet(String mac){
        Trend g = new Trend();
        return g.Generate(mac);
    }
    public String Arcadyan(String mac){
        Arcadyan g = new Arcadyan();
        return g.Generate(mac);
    }

    interface IWPS{
        public String Generate(String mac);
    }

    private class Generic implements IWPS
    {
        public String Generate(String mac)
        {
            mac = mac.replace(":", "");
            mac = mac.substring(6);
            int pin = Integer.parseInt(mac, 16);
            pin = pin % 10000000;
            int p = pin;
            int accum = 0;
            while (pin > 0)
            {
                accum = (((accum + (3 * (pin % 10))) | 0) + (((pin / 10) | 0) % 10)) | 0;
                pin = ((pin / 100) | 0);

            }
            accum = ((10 - accum % 10) % 10);
            String answer = (ZeroPad(p, 7) + "" + accum);
            return answer;
        }
        private String ZeroPad(int number, int len)
        {
            String snumber = String.valueOf(number);
            if (snumber.length() < len)
            {
                while (snumber.length() < len)
                {
                    snumber = "0" + snumber;
                }
            }
            return snumber;
        }
    }
    private class Dlink implements IWPS
    {
        private int mac2nic(String mac)
        {
            mac = mac.replace(":", "").replace("-", "");
            mac = mac.substring(6);
            int nic = Integer.parseInt(mac, 16);
            return nic;
        }
        private long hex2long(String mac)
        {
            mac = mac.replace(":", "").replace("-", "");
            long nic = Long.parseLong(mac, 16);
            return nic;
        }

        private String long2hex(long i)
        {
            return Long.toHexString(i);
        }
        private int checksum(int pin)
        {
            int accum = 0;
            while (pin > 0)
            {
                accum += (3 * (pin % 10));
                pin = (pin / 10);
                accum += (pin % 10);
                pin = (pin / 10);
            }
            return ((10 - accum % 10) % 10);
        }

        public String Generate(String mac)
        {
            long imac = hex2long(mac) + 1;
            String nmac = long2hex(imac);
            int nic = mac2nic(nmac);
            int pin = nic ^ 0x55AA55;
            pin = pin ^ (((pin & 0x0F) << 4) +
                    ((pin & 0x0F) << 8) +
                    ((pin & 0x0F) << 12) +
                    ((pin & 0x0F) << 16) +
                    ((pin & 0x0F) << 20));
            int i10e5 = 1000000;
            int i10e6 = 10000000;
            pin = pin % i10e6;
            if (pin < i10e5)
                pin += ((pin % 9) * i10e5) + i10e5;
            return String.valueOf((pin * 10) + checksum(pin));
        }
    }
    private class ArrisDG860A implements IWPS
    {
        private int ComputeChecksum(int s)
        {
            int accum = 0;
            s *= 10;
            accum += 3 * ((s / 10000000) % 10);
            accum += 1 * (((s / 1000000)) % 10);
            accum += 3 * (((s / 100000)) % 10);
            accum += 1 * (((s / 10000)) % 10);
            accum += 3 * (((s / 1000)) % 10);
            accum += 1 * (((s / 100)) % 10);
            accum += 3 * (((s / 10)) % 10);
            int digit = (accum % 10);
            return (10 - digit) % 10;
        }

        private int F(int n)
        {
            if (n == 1 || n == 2 || n == 0)
            {
                return 1;
            }
            else
            {
                return F(n - 1) + F(n - 2);
            }
        }
        private int FibGen(int num)
        {
            return F(num);
        }
        public String Generate(String strMac)
        {

            int[] fibnum = new int[6];
            int fibsum = 0;
            int seed = 16;
            int counter = 0;

            int a = 0;
            String[] arrayMacsString;
            List<Integer> arrayMacs = new ArrayList<>();
            arrayMacsString = strMac.split(":");
            for (int i = 0; i < arrayMacsString.length; i++)
            {
                arrayMacs.add(Integer.parseInt(arrayMacsString[i], 16));
            }
            Object[] temp = arrayMacs.toArray();
            int[] tmp = new int[temp.length] ;
            for(int i = 0 ; i < temp.length ; i++){
                tmp[i] = (int) temp[i];
            }

            for (int i = 0; i < 6; i++)
            {
                if (tmp[i] > 30)
                {
                    while (tmp[i] > 31)
                    {
                        tmp[i] -= 16;
                        counter += 1;
                    }
                }
                if (counter == 0)
                {
                    if (tmp[i] < 3)
                    {
                        tmp[i] = tmp[0] + tmp[1] + tmp[2] + tmp[3] + tmp[4] + tmp[5] - tmp[i];
                        if (tmp[i] > 0xff)
                        {
                            tmp[i] = tmp[i] & 0xff;
                        }
                        tmp[i] = (tmp[i] % 28) + 3;
                    }

                    fibnum[i] = FibGen(tmp[i]);
                }
                else
                {
                    fibnum[i] = FibGen(tmp[i]) + FibGen(counter);
                }
                counter = 0;
            }

            for (int i = 0; i < 6; i++)
            {

                fibsum += (fibnum[i] * FibGen(i + seed)) + arrayMacs.get(i);
            }
            fibsum = fibsum % 10000000;
            int checksum = ComputeChecksum(fibsum);
            fibsum = (fibsum * 10) + checksum;
            return String.format("%08d", Math.abs(fibsum));
        }
    }
    private class Arcadyan implements IWPS
    {
        public String Generate(String mac)
        {
            String str11 = mac.substring(12, 13);
            String str21 = mac.substring(13, 14);
            String str31 = mac.substring(15, 16);
            String str41 = mac.substring(16, 17);
            int i = Integer.parseInt(str11 + str21 + str31 + str41, 16);
            int arrayOfObject1;
            arrayOfObject1 = i;
            String str1 = String.format("%04d",arrayOfObject1);
            if (str1.length() > 4)
                str1 = str1.substring(1);
            int[] arrayOfInt1 = new int[10];
            arrayOfInt1[6] = (0xF & Integer.parseInt(str1.substring(0, 1)));
            arrayOfInt1[7] = (0xF & Integer.parseInt(str1.substring(1, 2)));
            arrayOfInt1[8] = (0xF & Integer.parseInt(str1.substring(2, 3)));
            arrayOfInt1[9] = (0xF & Integer.parseInt(str1.substring(3, 4)));
            String str2 = mac.replace(":", "");
            int[] arrayOfInt2 = new int[str2.length()];
            for (int j = 0; ; j = (byte)(j + 1))
            {
                if (j >= arrayOfInt2.length)
                {
                    int[] arrayOfInt3 = new int[7];
                    int k = 0xF & arrayOfInt1[6] + arrayOfInt1[7] + arrayOfInt2[10] + arrayOfInt2[11];
                    int m = 0xF & arrayOfInt1[8] + arrayOfInt1[9] + arrayOfInt2[8] + arrayOfInt2[9];
                    arrayOfInt3[0] = (k ^ arrayOfInt1[9]);
                    arrayOfInt3[1] = (k ^ arrayOfInt1[8]);
                    arrayOfInt3[2] = (m ^ arrayOfInt2[9]);
                    arrayOfInt3[3] = (m ^ arrayOfInt2[10]);
                    arrayOfInt3[4] = (arrayOfInt2[10] ^ arrayOfInt1[9]);
                    arrayOfInt3[5] = (arrayOfInt2[11] ^ arrayOfInt1[8]);
                    arrayOfInt3[6] = (k ^ arrayOfInt1[7]);
                    String x = Long.toHexString(arrayOfInt3[0]);
                    x+= Long.toHexString(arrayOfInt3[1]);
                    x+= Long.toHexString(arrayOfInt3[2]);
                    x+= Long.toHexString(arrayOfInt3[3]);
                    x+= Long.toHexString(arrayOfInt3[4]);
                    x+= Long.toHexString(arrayOfInt3[5]);
                    x+= Long.toHexString(arrayOfInt3[6]);
                    int n = Integer.parseInt(x, 16) % 10000000;
                    n = n * 10 + wpsChecksum(n);
                    return String.format("%08d", n);
                }
                arrayOfInt2[j] = (0xF & Integer.parseInt(str2.substring(j, j+1), 16));
            }
        }
        private int wpsChecksum(int paramInt)
        {
            int i = 0;
            while (true)
            {
                if (paramInt <= 0)
                    return (10 - i % 10) % 10;
                int j = i + 3 * (paramInt % 10);
                int k = paramInt / 10;
                i = j + k % 10;
                paramInt = k / 10;
            }
        }
    }
    private class Trend implements IWPS{
        public String Generate(String paramString)
        {
            String str1 = LastThree(paramString);
            String str2 = str1.substring(0, 2);
            String str3 = str1.substring(2, 4);
            String str4 = str1.substring(4);
            int i = 10 * (Integer.parseInt(str4 + str3 + str2, 16) % 10000000);
            Object[] arrayOfObject = new Object[1];
            arrayOfObject[0] = Integer.valueOf(i + Checksum(i));
            return String.format("%08d", arrayOfObject);
        }
        private String LastThree(String paramString)
        {
            String str1 = paramString.substring(9, 11);
            String str2 = paramString.substring(12, 14);
            String str3 = paramString.substring(15, 17);
            return str1 + str2 + str3;
        }
        private int Checksum(int paramInt)
        {
            return (10 - (0 + 3 * (paramInt / 10000000 % 10) + 1 * (paramInt / 1000000 % 10) + 3 * (paramInt / 100000 % 10) + 1 * (paramInt / 10000 % 10) + 3 * (paramInt / 1000 % 10) + 1 * (paramInt / 100 % 10) + 3 * (paramInt / 10 % 10)) % 10) % 10;
        }
    }

}
