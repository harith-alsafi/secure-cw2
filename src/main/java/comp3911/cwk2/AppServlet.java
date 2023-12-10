package comp3911.cwk2;

import java.io.File;
import java.io.IOException;
import java.security.DigestException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

@SuppressWarnings("serial")
public class AppServlet extends HttpServlet {
  private final BruteForceBlock bruteForceBlock = new BruteForceBlock();
  private ProtectedSqlDatabase protectedDatabase = new ProtectedSqlDatabase();

  private final Configuration fm = new Configuration(Configuration.VERSION_2_3_28);

  @Override
  public void init() throws ServletException {
    configureTemplateEngine();
    protectedDatabase.connectToDatabase();
  }

  private void configureTemplateEngine() throws ServletException {
    try {
      fm.setDirectoryForTemplateLoading(new File("./templates"));
      fm.setDefaultEncoding("UTF-8");
      fm.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
      fm.setLogTemplateExceptions(false);
      fm.setWrapUncheckedExceptions(true);
    } catch (IOException error) {
      throw new ServletException(error.getMessage());
    }
  }

  private boolean isAuthenticated(String username, String password) {
    String hashedPassword;
    try {
      hashedPassword = PasswordHashing.hash(password);
      return protectedDatabase.isAuthenticated(username, hashedPassword);
    } catch (DigestException e) {
      return false;
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      Template template = fm.getTemplate("login.html");
      template.process(null, response.getWriter());
      response.setContentType("text/html");
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (TemplateException error) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Get form parameters
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    String surname = request.getParameter("surname");

    try {
      if (bruteForceBlock.isAccountLocked(username)) {
        Template template = fm.getTemplate("locked.html");
        template.process(null, response.getWriter());
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        return;
      } else if (isAuthenticated(username, password)) {
        bruteForceBlock.handleSuccessfulLogin(username);
        // Get search results and merge with template
        Map<String, Object> model = new HashMap<>();
        model.put("records", protectedDatabase.searchResults(surname));
        Template template = fm.getTemplate("details.html");
        template.process(model, response.getWriter());
      } else {
        bruteForceBlock.handleFailedLogin(username);
        Template template = fm.getTemplate("invalid.html");
        template.process(null, response.getWriter());
      }
      response.setContentType("text/html");
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (Exception error) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
