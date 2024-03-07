package spe.projectportfolio.backend.utils;

import java.security.SecureRandom;
import java.util.Random;


public class VerCodeGenerateUtil {

    private static final String SYMBOLS = "0123456789ABCDEFGHIGKLMNOPQRSTUVWXYZ";
    private static final Random RAND = new SecureRandom();


    //generate 6 bit number
    public static String generateVerCode(){
        char[] code = new char[6];
        for(int i = 0; i < code.length; i++) {
            code[i] = SYMBOLS.charAt(RAND.nextInt(SYMBOLS.length()));

        }
        return new String(code);
    }
}
