package banco;

import java.io.PrintStream;
import java.util.Scanner;

public class NumericOptionMenu extends Menu {

    private Opcion[] _opciones;

    public NumericOptionMenu(String c, Opcion[] os) {
        super(c);
        _opciones = os;
    }

    @Override
    public String[] cabecera(){
        return new String[0];
    }


    @Override 
    public Opcion[] opciones(){
        return _opciones.clone();
    }

}
