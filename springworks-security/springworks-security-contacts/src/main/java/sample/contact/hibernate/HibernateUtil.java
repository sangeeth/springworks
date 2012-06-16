package sample.contact.hibernate;
import java.io.InputStream;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

public class HibernateUtil {
    private static SessionFactory sessionFactory = null;
    
    public static Session getSession() {
        return sessionFactory.getCurrentSession();
    }
    
    public static void configure(String propertyFile) throws Exception {
        AnnotationConfiguration configuration = new AnnotationConfiguration();
        
        Properties properties = new Properties();
        
        InputStream in = HibernateUtil.class.getResourceAsStream(propertyFile);
        properties.load(in);
        
        configuration.setProperties(properties);
        configuration.configure();
        
        sessionFactory = configuration.buildSessionFactory();
    }
}
