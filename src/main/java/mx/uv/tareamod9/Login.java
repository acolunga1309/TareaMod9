package mx.uv.tareamod9;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import mx.uv.tareamod9.models.Usuario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * Clase para registrar usuario y sesion.
 *
 * @author A. Colunga
 */
public class Login {

    private static final Logger LOGGER = LogManager.getLogger("Login");
    
    public static void main(String[] args) {
       
        /**
         * Ruta inicial.
         */
        get("/", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            String validador = "";
            String nombre = "";
            String mensaje = "";
            
            attributes.put("validador", validador);
            attributes.put("nombre", nombre);
            attributes.put("mensaje", mensaje);
            
            return new ModelAndView(attributes, "registrar.ftl");
        }, new FreeMarkerEngine());

        /**
         * Ruta para el registro.
         */
        post("/registrar", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            
            String nombre = req.queryParams("nombre");
            String email = req.queryParams("email");
            String password = req.queryParams("password");
            String apellidos = req.queryParams("apellidos");
            String validador = "";
            String mensaje = "";
            
            if(nombre.equals("")){
                //Falta el usuario
                validador = "Complete el nombre";
                attributes.put("nombre", nombre);
            }else if (email.equals("")){
                validador = "Complete el  email";
            }else if (password.equals("")){
                validador = "Complete el password";
            }
            
            try{
              EntityManagerFactory emf= Persistence.createEntityManagerFactory("TareaMod9PU");
              EntityManager em= emf.createEntityManager();
              Usuario usuario= new Usuario(email,nombre,apellidos,password);
              
              //guarda objeto
              em.getTransaction().begin();
              em.persist(usuario);
              em.getTransaction().commit();
              em.close();
              
            }catch(Exception ex)
            {
             //Con esto se cumple con el registro de actividad 
             LOGGER.error(String.format("El usuario %s no se registró.%s", email,ex.getMessage()));
              validador ="No se registró el usuario";
            }
            attributes.put("validador", validador);
            attributes.put("nombre", "");
            attributes.put("mensaje", mensaje);
  
            return new ModelAndView(attributes, "registrar.ftl");
        }, new FreeMarkerEngine());
        
        /**
         * Ruta para inicio de sesion.
         */
        post("/login", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            String email = req.queryParams("email");
            String password = req.queryParams("password");
            
            String validador = "";
            String nombre = "";
            String mensaje = "";
            
            String usuario = "n/a";
            
            attributes.put("validador", validador);
            attributes.put("nombre", nombre);
            
            //Con esto se cumple con el control de acceso y autenticacion
          
            try{
            
              EntityManagerFactory emf= Persistence.createEntityManagerFactory("TareaMod9PU");
              EntityManager em= emf.createEntityManager();
              Query q =em.createQuery("SELECT u from Usuario u WHERE u.email=:arg1");
              q.setParameter("arg1", email);
              Usuario usuarioToValidate =(Usuario) q.getSingleResult();
             
              
              //if(email.equals("e@mail.com") && password.equals("12345")){
              if(usuarioToValidate.getPassword().equals(usuarioToValidate.hashPassword(password))){
               mensaje = "entro al if";
                LOGGER.trace(String.format("El usuario %s inicio sesión.", email));
                usuario = email;
                attributes.put("mensaje", mensaje);
                attributes.put("usuario", usuario);
                return new ModelAndView(attributes, "home.ftl");
            }else{
               
                mensaje = "Usuario y/o Password  incorrectos.";
            }
            em.close();
            }catch(Exception ex)
            {
              mensaje = "Usuario y/o Password  incorrectos.";
             
            }
            
            attributes.put("mensaje", mensaje);
            
            return new ModelAndView(attributes, "registrar.ftl");
        }, new FreeMarkerEngine());
    }

}
