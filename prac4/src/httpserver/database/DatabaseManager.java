package httpserver.database;

import java.io.File;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

public class DatabaseManager {

  private EntityManager manager;

  private static final String DATABASE_LOCATION = new File(".\\db\\contacts.odb").getAbsolutePath();

  public DatabaseManager() {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory(DATABASE_LOCATION);
    manager = emf.createEntityManager();
  }

  public void persist(Object o) {
    manager.getTransaction().begin();
    manager.persist(o);
    manager.getTransaction().commit();
  }

  public List<Contact> get(String name) {
    String quary = "select c from Contact c where c.name like '" + name + "'";
    System.out.println(quary);
    TypedQuery<Contact> ret = manager.createQuery(quary, Contact.class);
    return ret.getResultList();
  }

}
