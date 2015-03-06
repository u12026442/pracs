package httpserver.server;

import httpserver.database.Contact;
import httpserver.database.DatabaseManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class HttpClientHandler extends Thread {

  private final Socket socket;

  private static final int REQUEST_TYPE = 0;

  private static final int REQUEST = 1;

  public HttpClientHandler(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      String request = reader.readLine();
      System.out.println(request);
      processRequest(request);
    }
    catch (IOException e) {
      System.err.println("Error in obtaining input stream.");
    }
  }

  private void processRequest(String request) {
    String[] parts = request.split(" ");
    if (parts[REQUEST_TYPE].equals("GET")) {
      try (PrintWriter writer = new PrintWriter(socket.getOutputStream())) {
        writer.print(processGetRequest(parts[REQUEST]));
      }
      catch (IOException e) {
        System.err.println("Error in obtaining output stream.");
      }
    }
  }

  private StringBuilder processGetRequest(String request) {
    String pageUri = request;
    List<Contact> contacts = null;
    if (request.contains(".ico")) return new StringBuilder();
    if (request.contains("?")) {
      String[] parts = request.split(new String("\\?"));
      pageUri = parts[0];
      if (request.contains("add&")) {
        newContact(parts[1]);
      }
      else {
        contacts = retrieveInfoFromDatabase(parts[1]);
      }
    }
    if (contacts == null) contacts = retrieveInfoFromDatabase("name=%");
    pageUri = pageUri.replace("/", "\\");
    if (pageUri.equals("\\")) pageUri = "\\index.html";

    return getPageString(".\\site" + pageUri, contacts);
  }

  private void newContact(String args) {
    String[] vars = args.split("&");
    Contact contact = new Contact();
    contact.setName(vars[1].split("=")[1]);
    contact.setSurname(vars[2].split("=")[1]);
    contact.setCell(vars[3].split("=")[1]);
    new DatabaseManager().persist(contact);
  }

  private StringBuilder getPageString(String fileName, List<Contact> contacts) {
    StringBuilder returnString = new StringBuilder();
    if (fileName.contains(".ico")) return returnString;
    try (BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.contains("%%contacts%%"))
          returnString.append(getContactCode(contacts));
        else
          returnString.append(line);
      }
    }
    catch (FileNotFoundException e) {
      processGetRequest("\\404.html");
    }
    catch (IOException e) {
      processGetRequest("\\404.html");
    }
    return returnString;
  }

  private StringBuilder getContactCode(List<Contact> contacts) {
    StringBuilder contactCode = new StringBuilder();
    if (contacts == null) return contactCode;
    for (Contact contact : contacts) {
      try (BufferedReader reader = new BufferedReader(new FileReader(new File(".\\site\\contact.html")))) {
        String line;
        while ((line = reader.readLine()) != null) {
          line = line.replace("%%name%%", contact.getName());
          line = line.replace("%%surname%%", contact.getSurname());
          line = line.replace("%%fullname%%", contact.getFullName());
          line = line.replace("%%cell%%", contact.getCell());
          contactCode.append(line);
        }
      }
      catch (FileNotFoundException e) {
        System.err.println("Contact.html not found");
      }
      catch (IOException e) {
        System.err.println("Contact.html IOException");
      }
    }
    return contactCode;
  }

  public List<Contact> retrieveInfoFromDatabase(String request) {

    String name = request.split("=")[1];

    DatabaseManager database = new DatabaseManager();
    return database.get(name);
  }
}
