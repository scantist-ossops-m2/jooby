package examples;

import io.jooby.ExecutionMode;
import io.jooby.Jooby;

public class MvcApp extends Jooby {

  {
    mvc(new PlainText());
  }

  public static void main(String[] args) {
    runApp(MvcApp::new, ExecutionMode.EVENT_LOOP, args);
  }
}