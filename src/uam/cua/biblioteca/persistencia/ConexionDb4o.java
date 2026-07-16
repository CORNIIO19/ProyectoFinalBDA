package uam.cua.biblioteca.persistencia;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;

public class ConexionDb4o {
    private static final String ARCHIVO = "biblioteca.db4o";

    public static ObjectContainer abrir() {
        return Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), ARCHIVO);
    }
}