package banco;

import java.io.PrintStream;
import java.util.Scanner;

public abstract class Menu extends Pantalla{
    
    public static class Opcion{
        private Pantalla _accion;
        private String _msg;

        public Opcion( String nombre, Pantalla accion){
            _msg = nombre;
            _accion = accion;
        }

        public Opcion( Pantalla accion){
            this( accion.nombre(), accion );
        }

        public String nombre(){
            return _msg;
        }

        
        public Pantalla menu(){
            return _accion;
        }
    }

    private Opcion[] _opciones;
    
    public Menu( String nombre){
        super(nombre);
    }

    protected int readOption(Scanner in, PrintStream out, int min, int max) {
        int ret;
        do {
            render(out);
            out.printf("Introduzca una opción (%d-%d): ", min, max);
            String s = in.nextLine();
            ret = toInt(s);

        } while (ret < min || ret > max);
        
        return ret;
    }

    private int toInt(String s) {
        try {
            return Integer.parseInt(s);
        }
        catch (Exception e) {
            return -1;
        }
    }

    abstract public String[] cabecera();
    
    public void render( PrintStream out ){
        out.println( "Menu de " + nombre().toUpperCase() );

        String[] cs = cabecera();
        for( int i = 0 ; i < cs.length ; i++ ){
            out.println( cs[i] );
        }


        Opcion[] ops = opciones();
        for( int i = 0 ; i < ops.length ; i++ ){
            out.printf( "%d .- %s\n", i, ops[i].nombre() );
        }
    }
    


    @Override
    public Pantalla execute(Scanner in, PrintStream out) {
        Opcion[] opciones = opciones();
        int option = readOption(in, out, 0, opciones.length-1);
        return opciones[option].menu();
    }

    
    abstract public Opcion[] opciones();
}
