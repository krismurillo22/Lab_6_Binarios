package steam;

import java.util.Date;

public class Usuario {
    private int codigo;
    private String nombreUsuario;
    private String contrasena;
    private String nombre;
    private Date fechaNacimiento;
    private String tipoUsuario;

    public Usuario(int codigo, String nombreUsuario, String contrasena, String nombre, Date fechaNacimiento, String tipoUsuario) {
        this.codigo = codigo;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.tipoUsuario = tipoUsuario;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public String getNombre() {
        return nombre;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }
}

